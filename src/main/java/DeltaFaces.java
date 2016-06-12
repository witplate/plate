package android.app.printerapp.viewer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class DeltaFaces {
	private final String vertexShaderCode =

            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +

            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";



    public static final int TYPE_WITBOX = 0;


    public static int DELTA_WITDH = 105;
	public static int DELTA_HEIGHT = 200;
	public static int DELTA_LONG = 148;
    public int [] mSizeArray;
    public int mType;
	
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;

    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    static final int COORDS_PER_VERTEX = 3;

    private float mCoordsArray [];
    int vertexCount;
    final int vertexStride = COORDS_PER_VERTEX * 4;


    float color[] = {1.0f, 1.0f, 1.0f, 0.6f };


    
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    public DeltaFaces(int face, int[] type) {

        mType = face;

        generatePlaneCoords(face,type);
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

    public void generatePlaneCoords(int face, int[] type){


        mSizeArray = type;


        switch (face) {
            case ViewerRenderer.DOWN:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  mSizeArray[1], 0,
                        -mSizeArray[0], -mSizeArray[1], 0,
                        mSizeArray[0], -mSizeArray[1], 0,
                        mSizeArray[0],  mSizeArray[1], 0 };
                break;
            case ViewerRenderer.BACK:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  mSizeArray[1], mSizeArray[2],
                        -mSizeArray[0],  mSizeArray[1], 0,
                        mSizeArray[0],  mSizeArray[1], 0,
                        mSizeArray[0],  mSizeArray[1], mSizeArray[2] };
                color[3] = 0.3f;
                break;
            case ViewerRenderer.RIGHT:
                mCoordsArray  = new float[]{
                        mSizeArray[0], -mSizeArray[1], mSizeArray[2],
                        mSizeArray[0], -mSizeArray[1], 0,
                        mSizeArray[0],  mSizeArray[1], 0,
                        mSizeArray[0],  mSizeArray[1], mSizeArray[2] };
                color[3] = 0.35f;
                break;
            case ViewerRenderer.LEFT:
                mCoordsArray = new float[]{
                        -mSizeArray[0], -mSizeArray[1], mSizeArray[2],
                        -mSizeArray[0], -mSizeArray[1], 0,
                        -mSizeArray[0],  mSizeArray[1], 0,
                        -mSizeArray[0],  mSizeArray[1], mSizeArray[2] };

                color[3] = 0.35f;
                break;
            case ViewerRenderer.FRONT:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  -mSizeArray[1], mSizeArray[2],
                        -mSizeArray[0],  -mSizeArray[1], 0,
                         mSizeArray[0],  -mSizeArray[1], 0,
                         mSizeArray[0],  -mSizeArray[1], mSizeArray[2] };

                color[3] = 0.3f;
                break;

            case ViewerRenderer.TOP:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  mSizeArray[1], mSizeArray[2],
                        -mSizeArray[0],  -mSizeArray[1], mSizeArray[2],
                        mSizeArray[0],  -mSizeArray[1], mSizeArray[2],
                        mSizeArray[0],  mSizeArray[1], mSizeArray[2] };
                color[3] = 0.4f;
                break;
        }

        vertexCount = mCoordsArray.length / COORDS_PER_VERTEX;

        ByteBuffer bb = ByteBuffer.allocateDirect(

                mCoordsArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mCoordsArray);
        mVertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);


    }

    public void draw(float[] mvpMatrix) {

	    GLES20.glUseProgram(mProgram);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glEnable(GLES20.GL_CULL_FACE);

        switch(mType){

            case ViewerRenderer.RIGHT:
            case ViewerRenderer.FRONT:
            case ViewerRenderer.TOP:

                GLES20.glCullFace(GLES20.GL_FRONT);

                break;

            case ViewerRenderer.BACK:
            case ViewerRenderer.LEFT:


                GLES20.glCullFace(GLES20.GL_BACK);

                break;
        }


	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");


	    GLES20.glEnableVertexAttribArray(mPositionHandle);


	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	            GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);


	    mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");


	    GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        ViewerRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        ViewerRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        float color2[] = {1.0f, 1.0f, 1.0f, 0.01f };
        GLES20.glUniform4fv(mColorHandle, 1, color2, 0);

        GLES20.glLineWidth(3f);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertexCount);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

	    GLES20.glDisableVertexAttribArray(mPositionHandle);


    }
}

