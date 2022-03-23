package com.example.funnybirdssurface;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class GameView extends SurfaceView implements SurfaceHolder.Callback  {
    private DrawThread drawThread;
    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        int h=getHeight();
        int w=getWidth();
        drawThread = new DrawThread(getContext(),getHolder());
        drawThread.setView(w,h);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        drawThread.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
        }

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        drawThread.setXY((int)event.getX(),(int)event.getY(),true);
        return false;
    }

}
