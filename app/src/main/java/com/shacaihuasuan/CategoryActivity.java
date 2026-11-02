package com.shacaihuasuan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shacaihuasuan.data.Model;
import com.shacaihuasuan.data.Repo;
import com.shacaihuasuan.widget.BandView;

import java.util.List;

public class CategoryActivity extends Activity {
    private String cat, sub;
    private LinearLayout list, tabs;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Repo.init(this);
        cat = getIntent().getStringExtra("cat");
        sub = getIntent().getStringExtra("sub");
        if (sub == null) sub = "";
        build();
    }
    private void build() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(Ui.dp(this,18), Ui.dp(this,10), Ui.dp(this,18), Ui.dp(this,24));
        sv.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        LinearLayout head = Ui.row(this);
        TextView back = Ui.tv(this, "‹", 30, Ui.INK, false);
        back.setPadding(Ui.dp(this,4), 0, Ui.dp(this,12), 0);
        back.setOnClickListener(v -> finish());
        head.addView(back);
        head.addView(Ui.tv(this, Repo.get().categoryName(cat), 20, Ui.INK, true));
        View sp = new View(this); head.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        head.addView(Ui.tv(this, Repo.get().isShanghai() ? "上海" : "北京", 11, Ui.MUTED, false));
        root.addView(head);

        if ("meat".equals(cat) || "dairy".equals(cat)) {
            HorizontalScrollView hs = new HorizontalScrollView(this);
            hs.setHorizontalScrollBarEnabled(false);
            tabs = Ui.row(this);
            String[] keys, names;
            if ("meat".equals(cat)) {
                keys = new String[]{"","pork","beef","chicken","lamb"};
                names = new String[]{"全部","猪肉","牛肉","鸡肉","羊肉"};
            } else {
                keys = new String[]{"","egg","milk"};
                names = new String[]{"全部","鸡蛋","牛奶"};
            }
            for (int i=0;i<keys.length;i++) {
                TextView t = chip(names[i], keys[i].equals(sub));
                final String k = keys[i];
                t.setOnClickListener(v -> { sub = k; rebuildList(); });
                Ui.margin(t, 0, 0, Ui.dp(this,8), 0);
                tabs.addView(t);
            }
            hs.addView(tabs);
            Ui.margin(hs, 0, Ui.dp(this,14), 0, 0);
            root.addView(hs);
        }

        Ui.margin(root, 0, 0, 0, 0);
        list = Ui.col(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.topMargin = Ui.dp(this,14);
        root.addView(list, llp);
        rebuildList();
    }
    private TextView chip(String s, boolean on) {
        TextView t = Ui.tv(this, s, 12, on ? 0xFFFFFFFF : Ui.MUTED, on);
        t.setPadding(Ui.dp(this,14), Ui.dp(this,7), Ui.dp(this,14), Ui.dp(this,7));
        t.setBackground(on ? Ui.bg(Ui.GREEN, 18) : Ui.stroke(Ui.CARD, Ui.LINE, 18, 1));
        return t;
    }
    private void rebuildList() {
        if (list == null) return;
        if (tabs != null) {
            for (int i=0;i<tabs.getChildCount();i++) {
                TextView t = (TextView) tabs.getChildAt(i);
                String n = t.getText().toString();
                boolean on = (sub.isEmpty() && n.equals("全部")) || (!sub.isEmpty() && ((sub.equals("pork")&&n.equals("猪肉"))||(sub.equals("beef")&&n.equals("牛肉"))||(sub.equals("chicken")&&n.equals("鸡肉"))||(sub.equals("lamb")&&n.equals("羊肉"))||(sub.equals("egg")&&n.equals("鸡蛋"))||(sub.equals("milk")&&n.equals("牛奶"))));
                t.setTextColor(on ? 0xFFFFFFFF : Ui.MUTED);
                t.setBackground(on ? Ui.bg(Ui.GREEN, 18) : Ui.stroke(Ui.CARD, Ui.LINE, 18, 1));
            }
        }
        list.removeAllViews();
        List<Model.Product> ps = Repo.get().cat(cat, sub);
        for (Model.Product p : ps) list.addView(row(p));
        if (ps.isEmpty()) list.addView(Ui.tv(this, "暂无数据源", 12, Ui.MUTED, false));
    }
    private View row(Model.Product p) {
        LinearLayout c = Ui.col(this);
        c.setPadding(Ui.dp(this,14), Ui.dp(this,12), Ui.dp(this,14), Ui.dp(this,12));
        c.setBackground(Ui.bg(Ui.CARD, 14));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.bottomMargin = Ui.dp(this,10);
        c.setLayoutParams(clp);
        LinearLayout top = Ui.row(this);
        LinearLayout names = Ui.col(this);
        names.addView(Ui.tv(this, p.displayName, 15, Ui.INK, true));
        names.addView(Ui.tv(this, p.spec, 9, Ui.MUTED, false));
        top.addView(names);
        View sp = new View(this); top.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        if (p.isMilk) {
            top.addView(Ui.tag(this, "暂无数据源"));
        } else {
            LinearLayout right = Ui.col(this); right.setGravity(Gravity.END);
            right.addView(Ui.tv(this, Ui.money(p.current), 19, Ui.INK, true));
            right.addView(Ui.tv(this, p.priceUnit(), 8, Ui.MUTED, false));
            top.addView(right);
            TextView tg = Ui.tag(this, p.tag);
            Ui.margin(tg, Ui.dp(this,8), 0, 0, 0);
            top.addView(tg);
        }
        c.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (p.isMilk) {
            TextView note = Ui.tv(this, "牛奶相关信息暂无数据源。", 10, Ui.MUTED, false);
            Ui.margin(note, 0, Ui.dp(this,8), 0, 0); c.addView(note);
        } else if (p.isBeijing()) {
            TextView line = Ui.tv(this, three(p), 9, Ui.MUTED, false);
            Ui.margin(line, 0, Ui.dp(this,7), 0, 0); c.addView(line);
        } else if (p.outlet30 != null) {
            BandView band = new BandView(this);
            band.set(p.outlet30.min, p.outlet30.p20, p.outlet30.p50, p.outlet30.p80, p.outlet30.max, p.current);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this,34));
            Ui.margin(band, 0, Ui.dp(this,7), 0, 0);
            c.addView(band, lp);
            LinearLayout lab = Ui.row(this);
            lab.addView(Ui.tv(this, "低 " + Ui.money(p.outlet30.min), 8, Ui.MUTED, false));
            View s2 = new View(this); lab.addView(s2, new LinearLayout.LayoutParams(0,1,1));
            lab.addView(Ui.tv(this, "常见 " + Ui.money(p.outlet30.p50), 8, Ui.MUTED, false));
            View s3 = new View(this); lab.addView(s3, new LinearLayout.LayoutParams(0,1,1));
            lab.addView(Ui.tv(this, "高 " + Ui.money(p.outlet30.max), 8, Ui.MUTED, false));
            c.addView(lab);
        }
        c.setOnClickListener(v -> {
            Intent i = new Intent(this, DetailActivity.class); i.putExtra("id", p.id); startActivity(i);
        });
        return c;
    }
    private String three(Model.Product p) {
        return "农贸 " + (p.farm==null?"—":Ui.money(p.farm)) + "  ·  超市 " + (p.supermarket==null?"—":Ui.money(p.supermarket)) + "  ·  批发 " + (p.wholesale==null?"—":Ui.money(p.wholesale));
    }
}
