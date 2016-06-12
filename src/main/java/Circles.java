package android.app.printerapp.viewer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


public class Circles {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    " gl_Position = uMVPMatrix * vPosition;" +
                    " gl_PointSize = 5.0;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;
    public static final float LINE_WIDTH = 2f;
    private static final float TRANSPARENCY = 0.5f;
    private static final float[] X_COLOR = { 0.0f, 0.9f, 0.0f, TRANSPARENCY };
    private static final float[] Y_COLOR = { 1.0f, 0.0f, 0.0f, TRANSPARENCY };
    private static final float[] Z_COLOR = { 0.0f, 0.0f, 1.0f, TRANSPARENCY };
    private float vertices[] = new float[364 * 3];
    private float maxRadius = 0;
    final FloatBuffer mVertexBuffer;
    private final ShortBuffer mDrawListBuffer;
    final int mProgram;
    int mPositionHandle;
    int mColorHandle;
    float mCoordsArray [];
    float mCurrentColor [];
    private int mMVPMatrixHandle;
    final int COORDS_PER_VERTEX = 3;
    int vertexCount;
    int vertexStride = COORDS_PER_VERTEX * 4;
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 };


    public Circles() {

        mCoordsArray = vertices;

        vertexCount = mCoordsArray.length / COORDS_PER_VERTEX;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                mCoordsArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);
        int vertexShader = ViewerRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = ViewerRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    private float[] drawXAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[364 * 3];


        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z;

        for(int i =1; i <364; i++){
            tempAxis[(i * 3)+ 0] = tempAxis[0];
            tempAxis[(i * 3)+ 1] = (float) (maxRadius * Math.cos((3.14/180) * (float)i ) + tempAxis[1]);
            tempAxis[(i * 3)+ 2] = (float) (maxRadius * Math.sin((3.14/180) * (float)i ) + tempAxis[2]);
        }

        return tempAxis;

    } ;
 private float[] drawYAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[364 * 3];


        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z;

        for(int i =1; i <364; i++){
            tempAxis[(i * 3)+ 0] = (float) (maxRadius * Math.cos((3.14/180) * (float)i ) + tempAxis[0]);
            tempAxis[(i * 3)+ 1] = tempAxis[1];
            tempAxis[(i * 3)+ 2] = (float) (maxRadius * Math.sin((3.14/180) * (float)i ) + tempAxis[2]);
        }

        return tempAxis;

    } ;
    private float[] drawZAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[364 * 3];


        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z;

        for(int i =1; i <364; i++){
            tempAxis[(i * 3)+ 0] = (float) (maxRadius * Math.cos((3.14/180) * (float)i ) + tempAxis[0]);
            tempAxis[(i * 3)+ 1] = (float) (maxRadius * Math.sin((3.14/180) * (float)i ) + tempAxis[1]);
            tempAxis[(i * 3)+ 2] = tempAxis[2];
        }

        return tempAxis;

    } ;

    public float getRadius(DataStorage data){

        float values[] = new float[3];
        float value = 0;

        values[0] = data.getMaxX() - data.getMinX();
        values[1] = data.getMaxY() - data.getMinY();
        values[2] = data.getMaxZ() - data.getMinZ() - data.getAdjustZ();

        for (int i = 0; i < values.length; i++){

            if (values[i]> value) value = values[i];

        }

        return (value / 2) + 20f;
    }

    public void draw(DataStorage data, float[] mvpMatrix, int currentAxis) {
    GLES20.glUseProgram(mProgram);

        GLES20.glEnable (GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        maxRadius = getRadius(data);
        switch (currentAxis){
            case X_AXIS:
                mCoordsArray = drawXAxis(data.getLastCenter(), data.getTrueCenter().z);
                mCurrentColor = X_COLOR;
                break;
            case Y_AXIS:
                mCoordsArray = drawYAxis(data.getLastCenter(), data.getTrueCenter().z);
                mCurrentColor = Y_COLOR;
                break;
            case Z_AXIS:
                mCoordsArray = drawZAxis(data.getLastCenter(), data.getTrueCenter().z);
                mCurrentColor = Z_COLOR;
                break;
            default:
                mCoordsArray = null;
                break;

        }


        if (mCoordsArray!=null) {

            mVertexBuffer.put(mCoordsArray);
            mVertexBuffer.position(0);


            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);


            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");


            GLES20.glUniform4fv(mColorHandle, 1, mCurrentColor, 0);


            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            ViewerRenderer.checkGlError("glGetUniformLocation");


            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
            ViewerRenderer.checkGlError("glUniformMatrix4fv");

            GLES20.glLineWidth(LINE_WIDTH);
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);


            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }


    }
}
