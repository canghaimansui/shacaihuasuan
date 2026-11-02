package com.shacaihuasuan;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class PlatformDialog {
    private static final String[][] PLATFORMS = {
            {"永辉生活", "线下门店 · 需定位", "cn.yonghui.hyd"},
            {"叮咚买菜", "最快 30 分钟达", "com.yaya.zone"},
            {"七鲜", "京东旗下 · 前置仓", "com.xstore.sevenfresh"},
            {"盒马", "门店 3 公里内", "com.wudaokou.hippo"},
            {"小象超市", "美团旗下 · 30 分钟", "com.meituan.retail.v.android"},
            {"多多买菜", "次日自提 · 更便宜", "com.xunmeng.pinduoduo"}
    };
    public static void show(final Activity a, final String keyword) {
        final Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = Ui.col(a);
        root.setPadding(Ui.dp(a,20), Ui.dp(a,16), Ui.dp(a,20), Ui.dp(a,20));
        root.setBackground(Ui.bg(0xFFFFFFFF, 24));
        TextView title = Ui.tv(a, "去哪儿买更便宜？", 21, Ui.INK, true);
        root.addView(title);
        root.addView(Ui.tv(a, keyword + " · 打开 App 后粘贴搜索", 10, Ui.MUTED, false));
        View sp = new View(a); root.addView(sp, new LinearLayout.LayoutParams(1, Ui.dp(a,10)));
        for (int i=0;i<PLATFORMS.length;i++) {
            final String pkg = PLATFORMS[i][2], name = PLATFORMS[i][0];
            LinearLayout row = Ui.row(a);
            row.setPadding(0, Ui.dp(a,12), 0, Ui.dp(a,12));
            TextView logo = Ui.tv(a, name.substring(0,1), 13, 0xFFFFFFFF, true);
            logo.setGravity(Gravity.CENTER);
            logo.setBackground(Ui.bg(colorFor(i), 20));
            row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(a,36), Ui.dp(a,36)));
            LinearLayout texts = Ui.col(a);
            texts.addView(Ui.tv(a, name, 14, Ui.INK, true));
            texts.addView(Ui.tv(a, PLATFORMS[i][1], 9, Ui.MUTED, false));
            Ui.margin(texts, Ui.dp(a,10), 0, 0, 0);
            row.addView(texts);
            View s = new View(a); row.addView(s, new LinearLayout.LayoutParams(0,1,1));
            row.addView(Ui.tv(a, "打开  ›", 11, Ui.GREEN, true));
            row.setOnClickListener(v -> { d.dismiss(); Jump.open(a, pkg, keyword); });
            root.addView(row);
            if (i < PLATFORMS.length-1) root.addView(Ui.divider(a));
        }
        TextView xhs = Ui.button(a, "在小红书搜「" + keyword + " 价格」", true);
        xhs.setBackground(Ui.bg(0xFFFF2442, 14));
        xhs.setOnClickListener(v -> { d.dismiss(); Jump.openXhs(a, keyword); });
        Ui.margin(xhs, 0, Ui.dp(a,16), 0, 0);
        root.addView(xhs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(a,48)));
        root.addView(Ui.tv(a, "平台价需登录/定位，以结算页为准。", 8, Ui.LIGHT, false));
        d.setContentView(root);
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
        }
        d.show();
    }
    static int colorFor(int i) {
        switch (i) { case 0: return 0xFF3C8D5A; case 1: return 0xFF00A870; case 2: return 0xFFD6453D;
            case 3: return 0xFF1B9AD6; case 4: return 0xFFE8B21A; default: return 0xFFD94B3F; }
    }
}
