package com.shacaihuasuan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shacaihuasuan.data.Model;
import com.shacaihuasuan.data.Repo;

public class SearchActivity extends Activity {
    private LinearLayout list;
    private EditText input;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Repo.init(this);
        ScrollView sv = new ScrollView(this);
        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(Ui.dp(this,18), Ui.dp(this,10), Ui.dp(this,18), Ui.dp(this,24));
        sv.addView(root);
        setContentView(sv);
        LinearLayout head = Ui.row(this);
        TextView back = Ui.tv(this, "‹", 30, Ui.INK, false);
        back.setOnClickListener(v -> finish());
        head.addView(back);
        input = new EditText(this);
        input.setHint("搜牛腩、菜花、鸡蛋…");
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setBackground(Ui.stroke(Ui.CARD, Ui.LINE, 12, 1));
        input.setPadding(Ui.dp(this,12), 0, Ui.dp(this,12), 0);
        head.addView(input, new LinearLayout.LayoutParams(0, Ui.dp(this,44), 1));
        root.addView(head);
        list = Ui.col(this);
        Ui.margin(list, 0, Ui.dp(this,14), 0, 0);
        root.addView(list);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { render(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });
        render("");
        input.requestFocus();
    }
    private void render(String q) {
        list.removeAllViews();
        String query = q == null ? "" : q.trim();
        int n = 0;
        for (Model.Product p : Repo.get().all()) {
            if (!query.isEmpty() && !p.displayName.contains(query) && !p.spec.contains(query)) continue;
            n++;
            LinearLayout row = Ui.row(this);
            row.setPadding(Ui.dp(this,14), Ui.dp(this,12), Ui.dp(this,14), Ui.dp(this,12));
            row.setBackground(Ui.bg(Ui.CARD, 12));
            Ui.margin(row, 0, 0, 0, Ui.dp(this,8));
            LinearLayout names = Ui.col(this);
            names.addView(Ui.tv(this, p.displayName, 14, Ui.INK, true));
            names.addView(Ui.tv(this, p.spec, 9, Ui.MUTED, false));
            row.addView(names);
            android.view.View sp = new android.view.View(this);
            row.addView(sp, new LinearLayout.LayoutParams(0,1,1));
            if (!p.isMilk) row.addView(Ui.tv(this, Ui.money(p.current), 16, Ui.INK, true));
            else row.addView(Ui.tag(this, "暂无数据源"));
            row.setOnClickListener(v -> {
                Intent i = new Intent(this, DetailActivity.class);
                i.putExtra("id", p.id);
                startActivity(i);
            });
            list.addView(row);
        }
        if (n == 0) list.addView(Ui.tv(this, "没有找到，换个说法试试。", 12, Ui.MUTED, false));
        else list.addView(Ui.tv(this, "共 " + n + " 个结果 · " + Repo.get().cityName(), 8.5f, Ui.LIGHT, false));
    }
}
