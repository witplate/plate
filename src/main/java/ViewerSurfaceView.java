package android.app.printerapp.viewer;

import android.app.printerapp.viewer.Geometry.Vector;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class ViewerSurfaceView extends GLSurfaceView{

	public static final int NORMAL = 0;
	public static final int XRAY = 4;
	public static final int TRANSPARENT = 2;
	public static final int LAYERS = 3;
	public static final int OVERHANG = 1;

    public static final int MIN_ZOOM = -500;
    public static final int MAX_ZOOM = -30;


	ViewerRenderer mRenderer;
	private List<DataStorage> mDataList = new ArrayList<DataStorage>();
	private int mMode;
	private final float TOUCH_SCALE_FACTOR_ROTATION = 90.0f / 320;  //180.0f / 320;
	private float mPreviousX;
	private float mPreviousY;
    private float mPreviousDragX;
    private float mPreviousDragY;
	private float pinchScale = 1.0f;

	private PointF pinchStartPoint = new PointF();
	private float pinchStartY = 0.0f;
	private float pinchStartZ = 0.0f;
	private float pinchStartDistance = 0.0f;
	private float pinchStartFactorX = 0.0f;
	private float pinchStartFactorY = 0.0f;
	private float pinchStartFactorZ = 0.0f;
	private static final int TOUCH_NONE = 0;
	private static final int TOUCH_DRAG = 1;
	private static final int TOUCH_ZOOM = 2;
	private int touchMode = TOUCH_NONE;
	public static final int ROTATION_MODE =0;
	public static final int TRANSLATION_MODE = 1;


	private int mMovementMode;
	private boolean mEdition = false;
	private int mEditionMode;
	private int mRotateMode;

	public static final int NONE_EDITION_MODE = 0;
	public static final int MOVE_EDITION_MODE = 1;
	public static final int ROTATION_EDITION_MODE =2;
	public static final int SCALED_EDITION_MODE = 3;


	public static final int ROTATE_X = 0;
	public static final int ROTATE_Y = 1;
	public static final int ROTATE_Z = 2;

	private int mObjectPressed = -1;

    private float[] mCurrentAngle = {0,0,0};

	public ViewerSurfaceView(Context context) {
	    super(context);
	}
	public ViewerSurfaceView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

    boolean mDoubleTapFirstTouch = false;
    long mDoubleTapCurrentTime = 0;

    public static final int DOUBLE_TAP_MAX_TIME = 300;
	public ViewerSurfaceView(Context context, List<DataStorage> data, int state, int mode, Slicer handler) {
		super(context);

        setEGLContextClientVersion(2);

        this.mMode = mode;
        this.mDataList = data;
		this.mRenderer = new ViewerRenderer (data, context, state, mode);
		setRenderer(mRenderer);


        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	private boolean isStl() {
		if (mDataList.size()>0)
			if (mDataList.get(0).getPathFile().endsWith(".stl") || mDataList.get(0).getPathFile().endsWith(".STL")) return true;

		return false;
	}


	public void configViewMode (int state) {
		switch (state) {
		case (ViewerSurfaceView.NORMAL):
			setOverhang(false);
			setXray(false);
			setTransparent(false);
			break;
		case (ViewerSurfaceView.XRAY):
			setOverhang(false);
			setXray(true);
			setTransparent(false);
			break;
		case (ViewerSurfaceView.TRANSPARENT):
			setOverhang(false);
			setXray(false);
			setTransparent(true);
			break;
		case (ViewerSurfaceView.OVERHANG):
			setOverhang(true);
			setXray(false);
			setTransparent(false);
			break;
		}

		requestRender();
	}

	public void setOverhang (boolean overhang) {
		mRenderer.setOverhang(overhang);
	}


	public void setTransparent (boolean trans) {
		mRenderer.setTransparent(trans);
	}

	public void setXray (boolean xray) {
		mRenderer.setXray(xray);
	}


	public void setEditionMode (int mode) {
		mEditionMode = mode;
	}
    public int getEditionMode(){ return mEditionMode; }

	public void deleteObject() {
		mRenderer.deleteObject(mObjectPressed);
	}

	public int getObjectPresed () {
		return mObjectPressed;
	}


	public void setRotationVector (int mode) {
		switch (mode) {

		case ROTATE_X:
			mRotateMode = ROTATE_X;
			mRenderer.setRotationVector(new Vector (1,0,0));
			break;
		case ROTATE_Y:
			mRotateMode = ROTATE_Y;
			mRenderer.setRotationVector(new Vector (0,1,0));
			break;
		case ROTATE_Z:
			mRotateMode = ROTATE_Z;
			mRenderer.setRotationVector(new Vector (0,0,1));
			break;


		}
        mCurrentAngle = new float[]{0, 0, 0};
	}

	public void rotateAngleAxisX (float angle) {
		if (mRotateMode!=ROTATE_X)	setRotationVector(ROTATE_X);

        float rotation = angle - mCurrentAngle[0];
        mCurrentAngle[0] = mCurrentAngle[0] + (angle - mCurrentAngle[0]);

		mRenderer.setRotationObject (rotation);
	    //mRenderer.refreshRotatedObjectCoordinates();

	}
public void rotateAngleAxisY (float angle) {
		if (mRotateMode!=ROTATE_Y) setRotationVector(ROTATE_Y);

        float rotation = angle - mCurrentAngle[1];
        mCurrentAngle[1] = mCurrentAngle[1] + (angle - mCurrentAngle[1]);

		mRenderer.setRotationObject (rotation);

	}

	public void rotateAngleAxisZ (float angle) {
		if (mRotateMode!=ROTATE_Z) setRotationVector(ROTATE_Z);

        float rotation = angle - mCurrentAngle[2];
        mCurrentAngle[2] = mCurrentAngle[2] + (angle - mCurrentAngle[2]);

        mRenderer.setRotationObject (rotation);

	}

    public void refreshRotatedObject(){

        mRenderer.refreshRotatedObjectCoordinates();

    }

    public void setRendererAxis(int axis){
        mRenderer.setCurrentaxis(axis);
        requestRender();

    }

    public void changePlate(int[] type){

        mRenderer.generatePlate(type);

    }

    public void doPress(int i){



        mRenderer.setObjectPressed(i);
        mRenderer.changeTouchedState();
        mEdition = true;
        mObjectPressed=i;
        ViewerMainFragment.showActionModePopUpWindow();
        ViewerMainFragment.displayModelSize(mObjectPressed);

        touchMode = TOUCH_DRAG;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {


		float x = event.getX();
        float y = event.getY();

        float normalizedX = (event.getX() / (float) mRenderer.getWidthScreen()) * 2 - 1;
		float normalizedY = -((event.getY() / (float) mRenderer.getHeightScreen()) * 2 - 1);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_POINTER_DOWN:

                if (mMovementMode!=TRANSLATION_MODE)
				if (event.getPointerCount() >= 2) {

                    mMovementMode = TRANSLATION_MODE;

					pinchStartDistance = getPinchDistance(event);
					pinchStartY = mRenderer.getCameraPosY();
					pinchStartZ = mRenderer.getCameraPosZ();

					if (mObjectPressed!=-1) {
						pinchStartFactorX = mDataList.get(mObjectPressed).getLastScaleFactorX();
						pinchStartFactorY = mDataList.get(mObjectPressed).getLastScaleFactorY();
						pinchStartFactorZ = mDataList.get(mObjectPressed).getLastScaleFactorZ();
					}

					if (pinchStartDistance > 0f) {
						getPinchCenterPoint(event, pinchStartPoint);
						mPreviousX = pinchStartPoint.x;
						mPreviousY = pinchStartPoint.y;
						touchMode = TOUCH_ZOOM;

					}

				}
				break;
			case MotionEvent.ACTION_DOWN:

                mPreviousX = event.getX();
                mPreviousY = event.getY();
                mPreviousDragX = mPreviousX;
                mPreviousDragY = mPreviousY;

                if (mMode!= ViewerMainFragment.PRINT_PREVIEW){

                    if (touchMode == TOUCH_NONE && event.getPointerCount() == 1) {
                        int objPressed = mRenderer.objectPressed(normalizedX, normalizedY);
                        if (objPressed!=-1 && isStl()) {
                            mEdition = true;
                            mObjectPressed=objPressed;
                            ViewerMainFragment.showActionModePopUpWindow();
                            ViewerMainFragment.displayModelSize(mObjectPressed);

                            Geometry.Point p = mDataList.get(mObjectPressed).getLastCenter();


                            while (!mRenderer.restoreInitialCameraPosition(p.x,p.y, true, false)){
                                requestRender();
                            };


                        }
                        else {

                            ViewerMainFragment.hideActionModePopUpWindow();
                            ViewerMainFragment.hideCurrentActionPopUpWindow();
                        }

                    }



                    if(mDoubleTapFirstTouch && (System.currentTimeMillis() - mDoubleTapCurrentTime) <= DOUBLE_TAP_MAX_TIME) { //Second touch
             mDoubleTapFirstTouch = false;

                        while (!mRenderer.restoreInitialCameraPosition(0,0, false, true)){
                            requestRender();
                        };

                    } else {

                        mDoubleTapFirstTouch = true;
                        mDoubleTapCurrentTime = System.currentTimeMillis();
                    }

                }

                touchMode = TOUCH_DRAG;


				break;
			case MotionEvent.ACTION_MOVE:

					if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0f) {

                        pinchScale = getPinchDistance(event) / pinchStartDistance;

                        PointF pt = new PointF();
                        getPinchCenterPoint(event, pt);

                        mPreviousX = pt.x;
                        mPreviousY = pt.y;

                        if (mEdition && mEditionMode == SCALED_EDITION_MODE) {
                            float fx = pinchStartFactorX*pinchScale;
                            float fy = pinchStartFactorY*pinchScale;
                            float fz = pinchStartFactorZ*pinchScale;

                            mRenderer.scaleObject(fx,fy,fz, false);
                            ViewerMainFragment.displayModelSize(mObjectPressed);

                        } else {

                            if ((mRenderer.getCameraPosY() < MIN_ZOOM) && (pinchScale < 1.0)) {


                            } else if ((mRenderer.getCameraPosY() > MAX_ZOOM) && (pinchScale > 1.0)){


                            } else{
                                mRenderer.setCameraPosY(pinchStartY / pinchScale);
                                mRenderer.setCameraPosZ(pinchStartZ / pinchScale);
                            }

                            requestRender();

                        }


					}


                if (touchMode != TOUCH_NONE)
                if (pinchScale<1.5f) {

                    float dx = x - mPreviousDragX;
                    float dy = y - mPreviousDragY;

                        mPreviousDragX = x;
                        mPreviousDragY = y;


                        if (mEdition && mEditionMode == MOVE_EDITION_MODE) {
                            mRenderer.dragObject(normalizedX, normalizedY);
                        } else 	if (!mEdition) dragAccordingToMode (dx,dy);


                }


					requestRender();
	                break;


			case MotionEvent.ACTION_UP:

                mMovementMode = ROTATION_MODE;

			case MotionEvent.ACTION_POINTER_UP:

				if (touchMode == TOUCH_ZOOM) {
					pinchScale = 1.0f;
					pinchStartPoint.x = 0.0f;
					pinchStartPoint.y = 0.0f;
				}

				if(mEdition) {

                    mRenderer.changeTouchedState();


                    ViewerMainFragment.slicingCallback();
                }

				touchMode = TOUCH_NONE;

			    requestRender();
				break;
		}
		return true;
	}


	public void exitEditionMode () {


        mEdition = false;
        mEditionMode = NONE_EDITION_MODE;


        Handler handler = new Handler();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mObjectPressed = -1;
                mRenderer.setObjectPressed(mObjectPressed);


                mRenderer.changeTouchedState();


                requestRender();

            }
        }, 100);


	}

	private void dragAccordingToMode (float dx, float dy) {
		switch (mMovementMode) {
		case ROTATION_MODE:
			doRotation (dx,dy);
			break;
		case TRANSLATION_MODE:
            float scale = -mRenderer.getCameraPosY() / 500f;
			doTranslation ((dx * scale) , (dy * scale));
			break;
		}
	}

    public void doScale(float x, float y, float z, boolean uniform){

        if (mEdition && mEditionMode == SCALED_EDITION_MODE) {

            float factorX = mDataList.get(mObjectPressed).getLastScaleFactorX();
            float factorY = mDataList.get(mObjectPressed).getLastScaleFactorY();
            float factorZ = mDataList.get(mObjectPressed).getLastScaleFactorZ();

            DataStorage data = mDataList.get(mObjectPressed);

            float scaleX = x / (data.getMaxX() - data.getMinX());
            float scaleY = y / (data.getMaxY() - data.getMinY());
            float scaleZ = z / (data.getMaxZ() - data.getMinZ());

            float fx = factorX;
            float fy = factorY;
            float fz = factorZ;


            if (!uniform){
                if (x > 0) fx = factorX*scaleX;
                if (y > 0) fy = factorY*scaleY;
                if (z > 0) fz = factorZ*scaleZ;
            } else {

                if (x > 0) {
                    fx = factorX*scaleX;  fy = fx; fz = fx;
                }
                if (y > 0) {
                    fy = factorY*scaleY;  fx = fy; fz = fy;
                }
                if (z > 0) {
                    fz = factorZ*scaleZ;  fx = fz; fy = fz;
                }
            }

            mRenderer.scaleObject(fx,fy,fz, true);
            ViewerMainFragment.displayModelSize(mObjectPressed);
            requestRender();

        }
    }

	private void doRotation (float dx, float dy) {              
        mRenderer.setSceneAngleX(dx*TOUCH_SCALE_FACTOR_ROTATION);
        mRenderer.setSceneAngleY(dy*TOUCH_SCALE_FACTOR_ROTATION);		
	} 

	private void doTranslation(float dx, float dy) {

        mRenderer.matrixTranslate(dx,-dy,0);
	}
	private float getPinchDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void getPinchCenterPoint(MotionEvent event, PointF pt) {
		pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
		pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
	}
}


