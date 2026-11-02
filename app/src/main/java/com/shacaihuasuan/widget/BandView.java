package com.shacaihuasuan.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BandView extends View {
    private double min, p20, p50, p80, max, today;
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint band = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    public BandView(Context c) { super(c); init(); }
    public BandView(Context c, AttributeSet a) { super(c, a); init(); }
    private void init() {
        track.setColor(0xFFEEEBE1);
        band.setColor(0xFFBFDEC8);
        tick.setColor(0xFF8DB99A); tick.setStrokeWidth(dp(1));
        dot.setColor(0xFFFFFFFF); dot.setStyle(Paint.Style.FILL);
    }
    public void set(double min, double p20, double p50, double p80, double max, double today) {
        this.min=min; this.p20=p20; this.p50=p50; this.p80=p80; this.max=max; this.today=today; invalidate();
    }
    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(), h=getHeight(); if(w<=0||h<=0||max<=min) return;
        float inset=dp(8), y=h*0.46f, th=dp(6);
        float l=inset, r=w-inset;
        RectF all=new RectF(l,y-th/2,r,y+th/2);
        canvas.drawRoundRect(all,th/2,th/2,track);
        float a=l+(float)((p20-min)/(max-min))*(r-l);
        float b=l+(float)((p80-min)/(max-min))*(r-l);
        canvas.drawRoundRect(new RectF(a,y-th/2,b,y+th/2),th/2,th/2,band);
        float m=l+(float)((p50-min)/(max-min))*(r-l);
        canvas.drawLine(m,y-dp(5),m,y+dp(5),tick);
        float t=l+(float)((today-min)/(max-min))*(r-l);
        dot.setColor(0xFFFFFFFF); canvas.drawCircle(t,y,dp(6),dot);
        dot.setStyle(Paint.Style.STROKE); dot.setStrokeWidth(dp(2)); dot.setColor(0xFF1E8A4D);
        canvas.drawCircle(t,y,dp(6),dot);
    }
}
