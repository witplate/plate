package android.app.printerapp.viewer;

import android.app.printerapp.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;


public class DeltaPlate {
	private final String vertexShaderCode =
			    "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
			  + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.

			  + "uniform vec4 a_Color;          \n"		// Color information we will pass in.
			  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
			  + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
			  + "attribute vec2 a_TexCoordinate;\n"

			  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
			  + "varying vec3 v_Position;		\n"
			  + "varying vec3 v_Normal;			\n"
			  + "varying vec2 v_TexCoordinate;	\n"

			  + "void main()                    \n" 	// The entry point for our vertex shader.
			  + "{                              \n"

			  + "	v_Position = vec3(u_MVMatrix*a_Position);							\n"

			  + "   v_Color = a_Color;  												\n"

			  + "	v_TexCoordinate = a_TexCoordinate;    								\n"

			  + "	v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));					\n"
			  + "   gl_Position = u_MVPMatrix * a_Position;                            \n"
			  + "}                                                                     \n";


    private final String fragmentShaderCode =
			"precision mediump float;       \n"

    		+ "uniform vec3 u_LightPos;		\n"
  		  	+ "uniform sampler2D u_Texture; \n"

  		  	+ "varying vec3 v_Position;		\n"
  		  	+ "varying vec4 v_Color;        \n"
  		  	+ "varying vec3 v_Normal;       \n"
  		  	+ "varying vec2 v_TexCoordinate;\n"
  		  	+ "void main()					\n"
  		  	+ "{                            \n"

  		  	+ "	float distance = length(u_LightPos - v_Position); 							\n"

  		  	+ "	vec3 lightVector = normalize(u_LightPos - v_Position);						\n"
  		  	+ " 	float diffuse = max(dot(v_Normal, lightVector), 0.0);						\n"
  		  	+ "	diffuse = 0.8;													\n"
  		  	+ "	gl_FragColor = (v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate));	\n"
  		  	+ "}               																\n";

    private final Context mContext;
	
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mOrderListBuffer;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mNormalBuffer;


    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mNormalHandle;
    private int mTextureDataHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordHandle;
    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;


    static final int COORDS_PER_VERTEX = 3;
    static final int COORDS_PER_NORMAL = 3;
    static final int COORDS_PER_TEXTURE = 2;


    float mColor[] = {1.0f, 1.0f, 1.0f, 0.5f };
    
    private final short mDrawOrder[] = {0, 1, 3, 1, 2, 3};
    
    
    private static final int INFINITE = 800;
    
    private float mCoordsArray[];
    
    private static final float mCoordsInfiniteArray[] = {	
        	-INFINITE, INFINITE, 0, 		
    		-INFINITE,-INFINITE, 0, 				
    		 INFINITE,-INFINITE, 0, 
    		 INFINITE, INFINITE, 0, 
    };

 	static final float mTextureCoordinateData[] = {
 		0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
 	};					
 	
 	static final float mNormalData[] = {
 		0, 0, 1,
 		0, 0, 1,
 		0, 0, 1,
 		0, 0, 1,
 		0, 0, 1
 	};
    

    public DeltaPlate(Context context, boolean infinite, int[] type) {
    	this.mContext = context;

    	generatePlaneCoords(type,infinite);

        mTextureDataHandle = loadTexture (mContext, R.drawable.witbox_plate);
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

    public void generatePlaneCoords(int[] type, boolean infinite){



        float[] auxPlane;

        if (infinite ) auxPlane = mCoordsInfiniteArray;
        else {

                    auxPlane = new float[] {
                            -type[0],  type[1], 0,
                            -type[0], -type[1], 0,
                            type[0], -type[1], 0,
                            type[0],  type[1], 0
                    };

            }



        this.mCoordsArray = auxPlane;


        ByteBuffer bb = ByteBuffer.allocateDirect(

                mCoordsArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mCoordsArray);
        mVertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(

                mDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mOrderListBuffer = dlb.asShortBuffer();
        mOrderListBuffer.put(mDrawOrder);
        mOrderListBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(

                mTextureCoordinateData.length * 4);
        tb.order(ByteOrder.nativeOrder());
        mTextureBuffer = tb.asFloatBuffer();
        mTextureBuffer.put(mTextureCoordinateData);
        mTextureBuffer.position(0);
ByteBuffer nbb = ByteBuffer.allocateDirect(mNormalData.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        mNormalBuffer = nbb.asFloatBuffer();
        mNormalBuffer.put(mNormalData);
        mNormalBuffer.position(0);

    }
    
    public static int loadTexture(final Context context, final int resourceId)  {
        final int[] textureHandle = new int[1];
     
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
     

            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
     

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        }
     
        if (textureHandle[0] == 0)  {

        }
     
        return textureHandle[0];
    }

    public void draw(float[] mvpMatrix, float[] mvMatrix) {

	    GLES20.glUseProgram(mProgram);

		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


	    mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        ViewerRenderer.checkGlError("glGetAttribLocation Texture Coord Handle");

	    GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_TEXTURE, 
	    							GLES20.GL_FLOAT, false, 
	    							0, mTextureBuffer);
        
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        ViewerRenderer.checkGlError("glGetAttribLocation Texture Coord Handle");

		
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
		
        ViewerRenderer.checkGlError("glGetUniformLocation Texture Uniform Handle");
		

	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	 

	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
	 

	    GLES20.glUniform1i(mTextureUniformHandle, 0);


	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        ViewerRenderer.checkGlError("glGetAttribLocation Position Handle");



	    GLES20.glEnableVertexAttribArray(mPositionHandle);


	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	            					 GLES20.GL_FLOAT, false, 
	            					 0, mVertexBuffer);


	    mColorHandle = GLES20.glGetUniformLocation(mProgram, "a_Color");


	    GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);
	    
	    mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");
        ViewerRenderer.checkGlError("glGetAttribLocation Normal Handle");


        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_NORMAL, 
        							 GLES20.GL_FLOAT, false, 
        							 0, mNormalBuffer);
        
        GLES20.glEnableVertexAttribArray(mNormalHandle);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");

        try {
            ViewerRenderer.checkGlError("glGetUniformLocation"); //TODO error
        } catch (RuntimeException e){


        }


        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        ViewerRenderer.checkGlError("glUniformMatrix4fv");
        
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix"); 
        ViewerRenderer.checkGlError("glGetUniformLocation");


        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0); 
        ViewerRenderer.checkGlError("glUniformMatrix4fv");
       


        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, mDrawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, mOrderListBuffer);


	    GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
