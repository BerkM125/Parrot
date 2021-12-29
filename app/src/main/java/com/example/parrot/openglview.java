package com.example.parrot;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class openglview extends GLSurfaceView {
    public openglview(Context context) {
        super(context);
        init();
    }

    public openglview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        setEGLContextClientVersion(2);
        //setPreserveEGLContextOnPause(true);
        setRenderer(new parrotGLRenderer(this.getContext()));
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
