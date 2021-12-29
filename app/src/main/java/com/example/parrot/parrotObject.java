package com.example.parrot;

import android.content.Context;
import android.opengl.GLES20;

public class parrotObject {
    public float color[] = { 0.21f, 0.633f, 0.7805f, 1.0f };
    public float whitecolor[] = { 1f, 1f, 1f, 1.0f };
    public int renderMode = GLES20.GL_TRIANGLES;
    private int positionHandle;
    private int colorHandle;

    parrotResourceLayer presourceloader;
    public parrotObject(Context mcontext) {
        presourceloader = new parrotResourceLayer(mcontext);
    }

    public void draw(float[] mvpMatrix) {
        //Currently programmed to conform to the basic shaders provided in raw resources
        GLES20.glUseProgram(presourceloader.mProgram);
        positionHandle = GLES20.glGetAttribLocation(presourceloader.mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, presourceloader.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                presourceloader.vertexStride, presourceloader.vertexBuffer);
        colorHandle = GLES20.glGetUniformLocation(presourceloader.mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glDrawArrays(renderMode, 0, presourceloader.vertexCount);
        GLES20.glUniform4fv(colorHandle, 1, whitecolor, 0);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, presourceloader.vertexCount);
        GLES20.glDisableVertexAttribArray(positionHandle);
        presourceloader.vPMatrixHandle = GLES20.glGetUniformLocation(presourceloader.mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(presourceloader.vPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glDrawArrays(renderMode, 0, presourceloader.vertexCount);
        GLES20.glUniform4fv(colorHandle, 1, whitecolor, 0);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, presourceloader.vertexCount);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

}

