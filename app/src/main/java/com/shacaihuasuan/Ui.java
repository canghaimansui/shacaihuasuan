package com.shacaihuasuan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class Ui {
    public static final int BG = 0xFFF6F4EE;
    public static final int CARD = 0xFFFFFFFF;
    public static final int INK = 0xFF18251E;
    public static final int MUTED = 0xFF7A827C;
    public static final int LIGHT = 0xFF9AA19B;
    public static final int LINE = 0xFFE9E5DA;
    public static final int GREEN = 0xFF1E8A4D;
    public static final int DARK = 0xFF143E2B;
    public static final int SOFT = 0xFFE7F3EA;
    public static final int ORANGE = 0xFFE8891C;
    public static final int OSOFT = 0xFFFFF3E2;
    public static final int RED = 0xFFD9534F;
    public static final int RSOFT = 0xFFFBEDEC;

    public static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }
    public static GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(radius);
        return g;
    }
    public static GradientDrawable stroke(int fill, int stroke, float radius, int w) {
        GradientDrawable g = bg(fill, radius); g.setStroke(w, stroke); return g;
    }
    public static TextView tv(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        t.setIncludeFontPadding(true);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    public static TextView tag(Context c, String text) {
        int bg, fg;
        switch (text == null ? "" : text) {
            case "便宜": bg = SOFT; fg = GREEN; break;
            case "偏贵": bg = RSOFT; fg = RED; break;
            case "参考": bg = 0xFFF1F1EC; fg = MUTED; break;
            case "暂无数据源": bg = 0xFFF1F1EC; fg = LIGHT; break;
            default: bg = 0xFFF0F1ED; fg = MUTED; break;
        }
        TextView t = tv(c, text == null ? "" : text, 10, fg, true);
        t.setPadding(dp(c, 7), dp(c, 2), dp(c, 7), dp(c, 2));
        t.setBackground(bg(bg, 20));
        return t;
    }
    public static TextView button(Context c, String text, boolean primary) {
        TextView t = tv(c, text, 13, primary ? 0xFFFFFFFF : INK, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
        t.setBackground(primary ? bg(GREEN, 14) : stroke(CARD, LINE, 14, 1));
        return t;
    }
    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    public static void margin(View v, int l, int t, int r, int b) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) {
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ViewGroup.MarginLayoutParams m = (ViewGroup.MarginLayoutParams) lp;
        m.setMargins(l, t, r, b); v.setLayoutParams(m);
    }
    public static void weight(View v, float w) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) ((LinearLayout.LayoutParams) lp).weight = w;
    }
    public static String money(double v) {
        return String.format(Locale.CHINA, "%.2f", v);
    }
    public static String tagOf(String t) { return t == null || t.isEmpty() ? "正常" : t; }
    public static View divider(Context c) {
        View v = new View(c); v.setBackgroundColor(LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 1)));
        return v;
    }
}
