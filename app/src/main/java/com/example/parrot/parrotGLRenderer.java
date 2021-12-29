package com.example.parrot;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.graphics.Color.*;

public class parrotGLRenderer implements GLSurfaceView.Renderer {
    //Sample objects
    private parrotObject mParrotObject;
    private parrotMatrixLayer mParrotMatrixLayer = new parrotMatrixLayer();
    private Context mcontext;
    float scale = 1f;
    float viewangle = 0.0f;
    float angle = 0.0f;
    public parrotGLRenderer(Context context) {
        mcontext = context;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 1f, 1f, 0);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        mParrotObject = new parrotObject(mcontext);
        try {
            mParrotObject.presourceloader.importMeshFromFile(R.raw.object, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mParrotMatrixLayer.projectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float[] scratch = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        viewangle += 0.01f;
        angle += 1f;
        scale = 0.5f;
        //Sample matrix layer code; adjusting the viewangle, angle, and scale values
        //will deliver different camera angles (viewangle) as well as size and directional matrices
        //(angle, scale) that can be applied to a parrotObject
        Matrix.setLookAtM(mParrotMatrixLayer.viewMatrix, 0, (float) Math.sin(viewangle)*3, 0, (float) Math.cos(viewangle)*3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mParrotMatrixLayer.vPMatrix, 0, mParrotMatrixLayer.projectionMatrix, 0, mParrotMatrixLayer.viewMatrix, 0);
        Matrix.setRotateM(mParrotMatrixLayer.rotationMatrix, 0, angle, 0, 0, -1.0f);
        Matrix.multiplyMM(scratch, 0, mParrotMatrixLayer.vPMatrix, 0, mParrotMatrixLayer.rotationMatrix, 0);
        Matrix.scaleM(scratch, 0, scale, scale, scale);
        mParrotObject.draw(scratch);

    }
}
