package com.shacaihuasuan;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(Ui.dp(this,18), Ui.dp(this,10), Ui.dp(this,18), Ui.dp(this,24));
        sv.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
        LinearLayout head = Ui.row(this);
        TextView back = Ui.tv(this, "‹", 30, Ui.INK, false);
        back.setOnClickListener(v -> finish());
        head.addView(back);
        head.addView(Ui.tv(this, "数据说明", 20, Ui.INK, true));
        root.addView(head);
        block(root, "价格口径", "上海：上海市发改委每日监测，逐店零售价。\n北京：北京价格每日农贸/超市均价；30 天曲线来自新发地批发，非零售价。\n单位：元/500g；食用油按原包装单位标注。");
        block(root, "更新方式", "每天首次启动后下拉刷新。无服务器、无后台任务；失败时保留上一次成功数据。");
        block(root, "平台跳转", "只拉起对应 App 首页并复制食材名，不抓取平台价格；未安装时提示“暂未安装”。");
        block(root, "免责声明", "价格受产地、季节、渠道、促销影响，仅供参考，以实际结算为准。");
        block(root, "版本", "啥菜划算 2.0.2");
    }
    private void block(LinearLayout root, String title, String body) {
        LinearLayout c = Ui.col(this);
        c.setPadding(Ui.dp(this,14), Ui.dp(this,12), Ui.dp(this,14), Ui.dp(this,12));
        c.setBackground(Ui.bg(Ui.CARD, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this,12);
        c.setLayoutParams(lp);
        c.addView(Ui.tv(this, title, 13, Ui.INK, true));
        TextView t = Ui.tv(this, body, 10, Ui.MUTED, false);
        Ui.margin(t, 0, Ui.dp(this,6), 0, 0);
        c.addView(t);
        root.addView(c);
    }
}
