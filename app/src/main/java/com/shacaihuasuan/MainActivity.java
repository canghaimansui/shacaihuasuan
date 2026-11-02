package com.shacaihuasuan;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shacaihuasuan.data.Model;
import com.shacaihuasuan.data.Repo;

public class MainActivity extends Activity {
    private SwipeRefreshLayout refresh;
    private LinearLayout content, sections;
    private TextView cityBtn, dateTv;

    private static final String[][] CATS = {
            {"veg","蔬菜","蔬"}, {"fruit","水果","果"}, {"meat","肉禽","肉"},
            {"dairy","蛋奶","蛋"}, {"aqua","水产","鱼"}, {"grain","粮油","粮"}
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Repo.init(this);
        build();
    }
    @Override protected void onResume() { super.onResume(); if (sections != null) render(); }

    private void build() {
        refresh = new SwipeRefreshLayout(this);
        refresh.setColorSchemeColors(Ui.GREEN);
        refresh.setProgressBackgroundColorSchemeColor(Ui.CARD);
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        content = Ui.col(this);
        content.setBackgroundColor(Ui.BG);
        content.setPadding(Ui.dp(this,18), Ui.dp(this,10), Ui.dp(this,18), Ui.dp(this,24));
        sv.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        refresh.addView(sv, new SwipeRefreshLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(refresh);
        refresh.setOnRefreshListener(() -> Repo.get().refresh(r -> {
            refresh.setRefreshing(false);
            render();
            String msg = (r.shanghai ? "上海已更新" : "上海更新失败") + " · " + (r.beijing ? "北京已更新" : "北京更新失败");
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
        render();
    }

    private void render() {
        content.removeAllViews();
        addHeader();
        addSearch();
        addHero();
        sections = Ui.col(this);
        for (String[] c : CATS) sections.addView(section(c[0], c[1], c[2]));
        content.addView(sections, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addFooter();
    }

    private void addHeader() {
        LinearLayout top = Ui.row(this);
        TextView logo = Ui.tv(this, "菜", 18, 0xFFFFFFFF, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(Ui.bg(Ui.DARK, 10));
        top.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this,38), Ui.dp(this,38)));
        LinearLayout names = Ui.col(this);
        TextView appTitle = Ui.tv(this, "啥菜划算", 21, Ui.INK, true);
        appTitle.setSingleLine(true);
        names.addView(appTitle);
        TextView sub = Ui.tv(this, "买菜前，先看一眼", 10, Ui.MUTED, false);
        Ui.margin(sub, 0, Ui.dp(this,2), 0, 0);
        names.addView(sub);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        nlp.leftMargin = Ui.dp(this,10);
        top.addView(names, nlp);
        LinearLayout right = Ui.col(this);
        right.setGravity(Gravity.END);
        cityBtn = Ui.tv(this, ("sh".equals(Repo.get().city()) ? "上海" : "北京") + "  ▾", 12, Ui.INK, true);
        cityBtn.setOnClickListener(v -> cityDialog());
        right.addView(cityBtn);
        dateTv = Ui.tv(this, Repo.get().date() + " 更新", 9, Ui.MUTED, false);
        Ui.margin(dateTv, 0, Ui.dp(this,3), 0, 0);
        right.addView(dateTv);
        top.addView(right);
        content.addView(top);
    }

    private void addSearch() {
        TextView s = Ui.tv(this, "⌕   搜牛腩、菜花、鸡蛋…", 12, Ui.MUTED, false);
        s.setGravity(Gravity.CENTER_VERTICAL);
        s.setPadding(Ui.dp(this,14), 0, Ui.dp(this,14), 0);
        s.setBackground(Ui.stroke(Ui.CARD, Ui.LINE, 14, 1));
        s.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this,42));
        slp.topMargin = Ui.dp(this,12);
        content.addView(s, slp);
    }

    private void addHero() {
        LinearLayout hero = Ui.col(this);
        hero.setMinimumHeight(Ui.dp(this,92));
        hero.setPadding(Ui.dp(this,16), Ui.dp(this,12), Ui.dp(this,16), Ui.dp(this,12));
        hero.setBackground(Ui.bg(Ui.DARK, 16));
        LinearLayout line = Ui.row(this);
        TextView l = Ui.tv(this, Repo.get().overviewSub(), 10, 0xFFBCD6C4, false);
        line.addView(l);
        View sp = new View(this); line.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        line.addView(Ui.tv(this, Repo.get().date(), 9, 0xFFBCD6C4, false));
        hero.addView(line);
        LinearLayout big = Ui.row(this);
        big.setMinimumHeight(Ui.dp(this,40));
        big.setGravity(Gravity.BOTTOM);
        big.addView(Ui.tv(this, Repo.get().overviewTop(), 25, 0xFFF4EFE2, true));
        if (Repo.get().isShanghai()) {
            String up = Repo.get().overviewUp();
            if (up != null && !up.isEmpty()) big.addView(Ui.tv(this, "  · " + up, 10, 0xFFBCD6C4, false));
        }
        hero.addView(big);
        TextView note = Ui.tv(this, Repo.get().overviewNote(), 9, 0xFFBCD6C4, false);
        Ui.margin(note, 0, Ui.dp(this,5), 0, 0);
        hero.addView(note);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = Ui.dp(this,12);
        content.addView(hero, hlp);
    }

    private View section(String cat, String name, String icon) {
        LinearLayout sec = Ui.col(this);
        LinearLayout.LayoutParams secLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secLp.topMargin = Ui.dp(this,14);
        sec.setLayoutParams(secLp);
        LinearLayout head = Ui.row(this);
        head.setPadding(Ui.dp(this,8), Ui.dp(this,0), Ui.dp(this,8), Ui.dp(this,0));
        head.setBackground(Ui.bg(Ui.SOFT, 10));
        TextView ic = Ui.tv(this, icon, 10, 0xFFFFFFFF, true);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(Ui.bg(Ui.GREEN, 20));
        head.addView(ic, new LinearLayout.LayoutParams(Ui.dp(this,22), Ui.dp(this,22)));
        TextView nm = Ui.tv(this, name, 15, Ui.INK, true);
        Ui.margin(nm, Ui.dp(this,8), 0, 0, 0);
        head.addView(nm);
        TextView tg = Ui.tag(this, catTag(cat));
        Ui.margin(tg, Ui.dp(this,8), 0, 0, 0);
        head.addView(tg);
        View sp = new View(this); head.addView(sp, new LinearLayout.LayoutParams(0,1,1));
        head.addView(Ui.tv(this, Repo.get().count(cat) + " 个 · 横滑  ›", 9, Ui.MUTED, false));
        head.setOnClickListener(v -> openCategory(cat, null));
        sec.addView(head, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this,36)));

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = Ui.row(this);
        row.setPadding(0, Ui.dp(this,8), Ui.dp(this,4), 0);
        java.util.List<Model.Product> ps = Repo.get().home(cat);
        for (Model.Product p : ps) row.addView(card(p));
        row.addView(allCard(cat));
        hs.addView(row);
        sec.addView(hs);
        return sec;
    }

    private String catTag(String cat) {
        // category-level majority tag
        java.util.Map<String,Integer> m = new java.util.HashMap<>();
        for (Model.Product p : Repo.get().cat(cat, null)) m.put(p.tag, m.getOrDefault(p.tag,0)+1);
        String best = "正常"; int n = -1;
        for (java.util.Map.Entry<String,Integer> e : m.entrySet()) if (e.getValue()>n) { n=e.getValue(); best=e.getKey(); }
        return best;
    }

    private View card(Model.Product p) {
        LinearLayout c = Ui.col(this);
        c.setPadding(Ui.dp(this,9), Ui.dp(this,9), Ui.dp(this,9), Ui.dp(this,9));
        c.setBackground(Ui.bg(Ui.CARD, 10));
        c.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this,100), Ui.dp(this,126)));
        Ui.margin(c, 0, 0, Ui.dp(this,8), 0);
        c.addView(Ui.tv(this, p.displayName, 11, Ui.INK, true));
        if (p.isMilk) {
            TextView a = Ui.tv(this, "暂无", 10, Ui.LIGHT, false); Ui.margin(a,0,Ui.dp(this,14),0,0); c.addView(a);
            c.addView(Ui.tv(this, "数据源", 10, Ui.LIGHT, false));
            c.addView(Ui.tag(this, "暂无数据源"));
        } else {
            TextView price = Ui.tv(this, Ui.money(p.current), 18, Ui.INK, true);
            Ui.margin(price, 0, Ui.dp(this,12), 0, 0); c.addView(price);
            String u = p.isBeijing() && "fruit".equals(p.category) ? "元/500g · 批发" : p.priceUnit();
            c.addView(Ui.tv(this, u, 7.5f, Ui.MUTED, false));
            TextView tg = Ui.tag(this, p.tag);
            Ui.margin(tg, 0, Ui.dp(this,6), 0, 0); c.addView(tg);
        }
        c.setOnClickListener(v -> openDetail(p));
        return c;
    }

    private View allCard(String cat) {
        LinearLayout c = Ui.col(this);
        c.setGravity(Gravity.CENTER);
        c.setBackground(Ui.stroke(Ui.SOFT, Ui.GREEN, 10, 1));
        c.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this,100), Ui.dp(this,126)));
        TextView t = Ui.tv(this, "查看全部\n" + Repo.get().count(cat) + " 种", 10, Ui.GREEN, true);
        t.setGravity(Gravity.CENTER);
        t.setLineSpacing(Ui.dp(this,2), 1f);
        c.addView(t, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        c.setOnClickListener(v -> openCategory(cat, null));
        return c;
    }

    private void addFooter() {
        TextView t = Ui.tv(this, "数据：上海发改委 / 北京价格 / 新发地  ·  仅供参考", 8.5f, Ui.LIGHT, false);
        Ui.margin(t, 0, Ui.dp(this,18), 0, 0);
        content.addView(t);
        TextView about = Ui.tv(this, "数据说明与免责声明  ›", 9, Ui.GREEN, true);
        Ui.margin(about, 0, Ui.dp(this,6), 0, 0);
        about.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        content.addView(about);
    }

    private void cityDialog() {
        final String[] items = {"上海（逐店零售）", "北京（农贸/超市/批发）"};
        new AlertDialog.Builder(this)
                .setTitle("选择城市")
                .setItems(items, (d, w) -> {
                    Repo.get().setCity(w == 0 ? "sh" : "bj");
                    render();
                }).show();
    }

    private void openCategory(String cat, String sub) {
        Intent i = new Intent(this, CategoryActivity.class);
        i.putExtra("cat", cat);
        if (sub != null) i.putExtra("sub", sub);
        startActivity(i);
    }
    private void openDetail(Model.Product p) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("id", p.id);
        startActivity(i);
    }
}
