package com.shacaihuasuan.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class LineView extends View {
    private double[] vals; private int color = 0xFF1E8A4D;
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    public LineView(Context c) { super(c); init(); }
    public LineView(Context c, AttributeSet a) { super(c, a); init(); }
    private void init() {
        line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(dp(2.4f)); line.setStrokeJoin(Paint.Join.ROUND); line.setStrokeCap(Paint.Cap.ROUND);
        grid.setColor(0xFFF0EDE4); grid.setStrokeWidth(dp(1));
        dot.setStyle(Paint.Style.FILL);
    }
    public void set(List<Double> v, int color) {
        this.color = color; if (v == null) vals = null; else { vals = new double[v.size()]; for (int i=0;i<v.size();i++) vals[i]=v.get(i); }
        invalidate();
    }
    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(), h=getHeight(); if(w<=0||h<=0) return;
        float l=dp(4), r=w-dp(4), top=dp(10), bot=h-dp(10);
        for(int i=0;i<3;i++){ float y=top+(bot-top)*i/2f; canvas.drawLine(l,y,r,y,grid); }
        if(vals==null||vals.length<2) return;
        double mn=Double.MAX_VALUE,mx=-Double.MAX_VALUE;
        for(double v:vals){ if(v<mn)mn=v; if(v>mx)mx=v; }
        double span=Math.max(0.001,mx-mn); mn-=span*.15; mx+=span*.15; span=mx-mn;
        Path p=new Path();
        for(int i=0;i<vals.length;i++){
            float x=l+(r-l)*i/(vals.length-1);
            float y=(float)(bot-(vals[i]-mn)/span*(bot-top));
            if(i==0)p.moveTo(x,y); else p.lineTo(x,y);
        }
        line.setColor(color); canvas.drawPath(p,line);
        dot.setColor(color);
        for(int i=0;i<vals.length;i++){
            if(i!=vals.length-1) continue;
            float x=l+(r-l)*i/(vals.length-1);
            float y=(float)(bot-(vals[i]-mn)/span*(bot-top));
            canvas.drawCircle(x,y,dp(4),dot);
        }
    }
}
