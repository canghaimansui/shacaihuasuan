package com.shacaihuasuan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shacaihuasuan.data.Model;
import com.shacaihuasuan.data.Repo;
import com.shacaihuasuan.widget.BandView;
import com.shacaihuasuan.widget.LineView;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends Activity {
    private Model.Product p;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Repo.init(this);
        p = Repo.get().byId(getIntent().getStringExtra("id"));
        if (p == null) { finish(); return; }
        build();
    }
    private void build() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(Ui.dp(this,18), Ui.dp(this,10), Ui.dp(this,18), Ui.dp(this,48));
        sv.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
        // header
        LinearLayout head = Ui.row(this);
        TextView back = Ui.tv(this, "‹", 30, Ui.INK, false);
        back.setPadding(Ui.dp(this,4), 0, Ui.dp(this,12), 0);
        back.setOnClickListener(v -> finish());
        head.addView(back);
        head.addView(Ui.tv(this, p.displayName, 20, Ui.INK, true));
        View sp = new View(this); head.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        head.addView(Ui.tv(this, p.isBeijing() ? "北京" : "上海", 11, Ui.MUTED, false));
        root.addView(head);
        View gap = new View(this); root.addView(gap, new LinearLayout.LayoutParams(1, Ui.dp(this,8)));

        if (p.isMilk) { milk(root); }
        else if (p.isBeijing()) { beijing(root); }
        else { shanghai(root); }

        // buttons
        LinearLayout btns = Ui.row(this);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.topMargin = Ui.dp(this,18);
        btns.setLayoutParams(blp);
        TextView cmp = Ui.button(this, "去 6 个平台比价", true);
        cmp.setOnClickListener(v -> PlatformDialog.show(this, p.displayName));
        btns.addView(cmp, new LinearLayout.LayoutParams(0, Ui.dp(this,54), 1.5f));
        TextView xhs = Ui.button(this, "小红书搜", false);
        xhs.setOnClickListener(v -> Jump.openXhs(this, p.displayName));
        Ui.margin(xhs, Ui.dp(this,10), 0, 0, 0);
        btns.addView(xhs, new LinearLayout.LayoutParams(0, Ui.dp(this,54), 1f));
        root.addView(btns, btns.getLayoutParams());
    }

    private void milk(LinearLayout root) {
        LinearLayout card = Ui.col(this);
        card.setPadding(Ui.dp(this,18), Ui.dp(this,18), Ui.dp(this,18), Ui.dp(this,18));
        card.setBackground(Ui.bg(Ui.CARD, 16));
        card.addView(Ui.tv(this, "牛奶相关信息", 15, Ui.INK, true));
        TextView big = Ui.tv(this, "暂无数据源。", 22, Ui.ORANGE, true);
        Ui.margin(big, 0, Ui.dp(this,10), 0, 0); card.addView(big);
        card.addView(Ui.tv(this, "上海和北京的官方监测数据中，暂时没有乳制品价格。", 9, Ui.MUTED, false));
        root.addView(card);
    }

    private void shanghai(LinearLayout root) {
        // hero
        LinearLayout hero = Ui.col(this);
        hero.addView(Ui.tv(this, p.spec, 9, Ui.MUTED, false));
        LinearLayout priceRow = Ui.row(this);
        priceRow.setGravity(Gravity.BOTTOM);
        priceRow.addView(Ui.tv(this, Ui.money(p.current), 40, Ui.INK, true));
        TextView unit = Ui.tv(this, " 元/500g", 10, Ui.MUTED, false);
        Ui.margin(unit, Ui.dp(this,6), 0, 0, Ui.dp(this,7));
        priceRow.addView(unit);
        View sp = new View(this); priceRow.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        priceRow.addView(Ui.tag(this, p.tag));
        hero.addView(priceRow);
        String concl = "便宜".equals(p.tag) ? "均价是近一个月低位，可以买。" : ("偏贵".equals(p.tag) ? "最近偏贵，不急可以再等等。" : "价格正常，可以买。");
        hero.addView(Ui.tv(this, concl, 12, Ui.GREEN, true));
        TextView delta = Ui.tv(this, deltaText(p), 9, p.current > p.prev ? Ui.RED : Ui.GREEN, true);
        Ui.margin(delta, 0, Ui.dp(this,5), 0, 0);
        hero.addView(delta);
        root.addView(hero);

        // range card
        if (p.outlet30 != null) {
            LinearLayout card = card("最近 30 天，大家都买在什么价", p.series.size() > 0 ? "今天 " + Ui.money(p.current) : "");
            BandView band = new BandView(this);
            band.set(p.outlet30.min, p.outlet30.p20, p.outlet30.p50, p.outlet30.p80, p.outlet30.max, p.current);
            card.addView(band, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this,46)));
            LinearLayout labels = Ui.row(this);
            labels.addView(label("最低", p.outlet30.min));
            labels.addView(label("便宜", p.outlet30.p20));
            labels.addView(label("常见", p.outlet30.p50));
            labels.addView(label("偏贵", p.outlet30.p80));
            labels.addView(label("最高", p.outlet30.max));
            card.addView(labels);
            root.addView(card);
        }

        // trend
        if (p.series.size() > 1) {
            LinearLayout card = card("每日均价", "近 " + p.series.size() + " 个工作日");
            LineView line = new LineView(this);
            List<Double> vals = new ArrayList<>();
            for (Model.Day d : p.series) vals.add(d.avg);
            line.set(vals, Ui.GREEN);
            card.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this,100)));
            LinearLayout dates = Ui.row(this);
            dates.addView(Ui.tv(this, p.series.get(0).date, 8, Ui.LIGHT, false));
            View spd = new View(this); dates.addView(spd, new LinearLayout.LayoutParams(0,1,1));
            dates.addView(Ui.tv(this, p.series.get(p.series.size()-1).date, 8, Ui.LIGHT, false));
            card.addView(dates);
            root.addView(card);
        }

        // stats
        if (p.outlet30 != null) {
            LinearLayout stats = Ui.row(this);
            stats.addView(stat("最低", p.outlet30.min, "30 天最便宜的店"));
            stats.addView(stat("常见", p.outlet30.p50, "一半的监测点低于此"));
            stats.addView(stat("最高", p.outlet30.max, "精品超市/核心菜场"));
            LinearLayout.LayoutParams stlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stlp.topMargin = Ui.dp(this,10);
            root.addView(stats, stlp);
        }

        // cheap
        if (p.outlet30 != null) {
            LinearLayout cheap = Ui.col(this);
            cheap.setPadding(Ui.dp(this,16), Ui.dp(this,12), Ui.dp(this,16), Ui.dp(this,12));
            cheap.setBackground(Ui.bg(Ui.OSOFT, 12));
            LinearLayout top = Ui.row(this);
            top.addView(Ui.tv(this, "想捡漏：低于 " + Ui.money(p.outlet30.p20) + " 元", 12, Ui.ORANGE, true));
            View spt = new View(this); top.addView(spt, new LinearLayout.LayoutParams(0,1,1));
            top.addView(Ui.tv(this, "今天 " + p.belowP20 + " 家", 9, Ui.ORANGE, true));
            cheap.addView(top);
            cheap.addView(Ui.tv(this, "近 30 天最便宜的 20% 监测点，都在这个价以下。", 9, 0xFF8A5A1E, false));
            Ui.margin(cheap, 0, Ui.dp(this,10), 0, 0);
            root.addView(cheap);
        }

        // cheapest shops
        if (!p.cheapest3.isEmpty()) {
            LinearLayout card = card("今天最便宜的 3 家店", "上海逐店监测");
            for (int i=0;i<p.cheapest3.size();i++) {
                Model.Shop s = p.cheapest3.get(i);
                LinearLayout row = Ui.row(this);
                row.setPadding(0, Ui.dp(this,7), 0, Ui.dp(this,7));
                TextView idx = Ui.tv(this, String.valueOf(i+1), 9, Ui.GREEN, true);
                idx.setGravity(Gravity.CENTER);
                idx.setBackground(Ui.bg(Ui.SOFT, 20));
                row.addView(idx, new LinearLayout.LayoutParams(Ui.dp(this,22), Ui.dp(this,22)));
                LinearLayout names = Ui.col(this);
                names.addView(Ui.tv(this, s.shop, 10, Ui.INK, true));
                names.addView(Ui.tv(this, s.district, 8, Ui.MUTED, false));
                Ui.margin(names, Ui.dp(this,8), 0, 0, 0);
                row.addView(names);
                View spr = new View(this); row.addView(spr, new LinearLayout.LayoutParams(0,1,1));
                row.addView(Ui.tv(this, Ui.money(s.price), 13, Ui.ORANGE, true));
                card.addView(row);
                if (i < p.cheapest3.size()-1) card.addView(Ui.divider(this));
            }
            root.addView(card);
        }

        // note
        TextView note = Ui.tv(this, "数据来源：上海发改委 · " + p.series.get(p.series.size()-1).date + " 逐店监测；价格仅供参考，以实际结算为准。", 8, Ui.LIGHT, false);
        Ui.margin(note, 0, Ui.dp(this,10), 0, 0);
        root.addView(note);
    }

    private void beijing(LinearLayout root) {
        LinearLayout refs = Ui.row(this);
        refs.addView(ref("农贸均价", p.farm, "今日零售"));
        refs.addView(ref("超市均价", p.supermarket, "今日零售"));
        refs.addView(ref("新发地批发", p.wholesale, "30 天参考"));
        root.addView(refs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (p.series.size() > 1) {
            LinearLayout card = card("新发地批发参考 · 近 30 天", "元/500g");
            LineView line = new LineView(this);
            List<Double> vals = new ArrayList<>();
            for (Model.Day d : p.series) vals.add(d.avg);
            line.set(vals, Ui.ORANGE);
            card.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this,100)));
            LinearLayout dates = Ui.row(this);
            dates.addView(Ui.tv(this, p.series.get(0).date, 8, Ui.LIGHT, false));
            View spd = new View(this); dates.addView(spd, new LinearLayout.LayoutParams(0,1,1));
            dates.addView(Ui.tv(this, p.series.get(p.series.size()-1).date, 8, Ui.LIGHT, false));
            card.addView(dates);
            card.addView(Ui.tv(this, "北京零售仅有当日均价，无 30 天门店历史。", 8, Ui.MUTED, false));
            root.addView(card);
        } else {
            LinearLayout card = card("新发地批发参考 · 近 30 天", "");
            card.addView(Ui.tv(this, "暂无数据源", 10, Ui.MUTED, false));
            root.addView(card);
        }

        LinearLayout why = card("价格说明", "");
        why.addView(noteLine("北京仅有全市均价，无单店价格，故不列店名。"));
        why.addView(noteLine("30 天曲线来自新发地批发，非零售价。"));
        why.addView(noteLine("上海存在逐店监测数据，具体门店见上海详情页。"));
        root.addView(why);

        LinearLayout src = card("数据来源", "查看原始数据  ›");
        src.addView(Ui.tv(this, "北京价格 · " + (p.series.isEmpty() ? "11-01" : p.series.get(p.series.size()-1).date) + " 农贸/超市均价", 9, Ui.MUTED, false));
        src.addView(Ui.tv(this, "新发地 · " + (p.series.isEmpty() ? "11-01" : p.series.get(p.series.size()-1).date) + " 批发参考", 9, Ui.MUTED, false));
        src.addView(Ui.tv(this, "价格仅供参考，以实际结算为准。", 8, Ui.LIGHT, false));
        root.addView(src);
    }

    private LinearLayout card(String title, String right) {
        LinearLayout c = Ui.col(this);
        c.setPadding(Ui.dp(this,14), Ui.dp(this,12), Ui.dp(this,14), Ui.dp(this,12));
        c.setBackground(Ui.bg(Ui.CARD, 14));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = Ui.dp(this,10);
        c.setLayoutParams(clp);
        LinearLayout top = Ui.row(this);
        top.addView(Ui.tv(this, title, 11, Ui.INK, true));
        View sp = new View(this); top.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        if (right != null && !right.isEmpty()) top.addView(Ui.tv(this, right, 8, Ui.MUTED, false));
        c.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return c;
    }
    private LinearLayout label(String label, double v) {
        LinearLayout l = Ui.col(this);
        l.setGravity(Gravity.CENTER);
        l.addView(Ui.tv(this, label, 8, Ui.MUTED, false));
        l.addView(Ui.tv(this, Ui.money(v), 9, Ui.INK, true));
        l.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return l;
    }
    private LinearLayout stat(String title, double v, String sub) {
        LinearLayout c = Ui.col(this);
        c.setPadding(Ui.dp(this,10), Ui.dp(this,10), Ui.dp(this,10), Ui.dp(this,10));
        c.setBackground(Ui.bg(Ui.CARD, 10));
        c.addView(Ui.tv(this, Ui.money(v), 17, Ui.INK, true));
        c.addView(Ui.tv(this, title, 9, Ui.GREEN, true));
        c.addView(Ui.tv(this, sub, 7, Ui.MUTED, false));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.rightMargin = Ui.dp(this,8);
        c.setLayoutParams(lp);
        return c;
    }
    private LinearLayout ref(String title, Double v, String note) {
        LinearLayout c = Ui.col(this);
        c.setPadding(Ui.dp(this,10), Ui.dp(this,10), Ui.dp(this,10), Ui.dp(this,10));
        c.setBackground(Ui.bg(Ui.CARD, 12));
        c.addView(Ui.tv(this, title, 8, Ui.MUTED, false));
        c.addView(Ui.tv(this, v == null ? "—" : Ui.money(v), 19, "新发地批发".equals(title) ? Ui.ORANGE : Ui.INK, true));
        c.addView(Ui.tv(this, note, 7, "新发地批发".equals(title) ? Ui.ORANGE : Ui.MUTED, false));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.rightMargin = Ui.dp(this,8);
        c.setLayoutParams(lp);
        return c;
    }
    private View noteLine(String s) {
        LinearLayout row = Ui.row(this);
        row.setPadding(0, Ui.dp(this,5), 0, Ui.dp(this,5));
        TextView dot = Ui.tv(this, "•", 11, Ui.GREEN, true);
        row.addView(dot);
        TextView t = Ui.tv(this, s, 9, Ui.MUTED, false);
        Ui.margin(t, Ui.dp(this,4), 0, 0, 0);
        row.addView(t);
        return row;
    }
    private String deltaText(Model.Product p) {
        double d = p.current - p.prev;
        if (Math.abs(d) < 0.005) return "— 持平";
        return (d > 0 ? "↑ " : "↓ ") + Ui.money(Math.abs(d));
    }
}
