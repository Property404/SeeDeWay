package com.example.tareg.seedeway;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;



// The Beacon of light
class BeaconView extends View {
    private Paint paint = new Paint();
    private float mHorizontal;
    private final int BARS = 15;
    private final float WIDTH = 9;
    private float HEIGHT = 500;
    public BeaconView(Context context){
        this(context, 0);
    }
    public BeaconView(Context context, float horizontal) {
        super(context);
        mHorizontal = horizontal;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        HEIGHT = displayMetrics.heightPixels;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);

        paint.setColor(Color.WHITE);
        for(int i=0;i<BARS;i++){

            paint.setAlpha(200-12*Math.abs(BARS/2-i));

            canvas.drawRect(mHorizontal+(i-1)*(WIDTH), 0, WIDTH*i+mHorizontal, HEIGHT, paint );


        }

    }

}