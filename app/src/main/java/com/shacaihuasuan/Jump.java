package com.shacaihuasuan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class Jump {
    public static void openXhs(Activity a, String keyword) {
        String q = Uri.encode(keyword + " 价格");
        String[] uris = {
                "xhsdiscover://search/result?keyword=" + q,
                "xhsdiscover://search?keyword=" + q
        };
        for (String u : uris) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
                i.setPackage("com.xingin.xhs");
                a.startActivity(i);
                copy(a, keyword + " 价格");
                return;
            } catch (Exception ignored) {}
        }
        open(a, "com.xingin.xhs", keyword + " 价格");
    }
    static void copy(Activity a, String keyword) {
        ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("菜价", keyword));
        Toast.makeText(a, "已复制「" + keyword + "」，粘贴搜索", Toast.LENGTH_SHORT).show();
    }
    public static void open(Activity a, String pkg, String keyword) {
        Intent i = a.getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) {
            new AlertDialog.Builder(a).setTitle("暂未安装").setMessage("未检测到该 App。").setPositiveButton("知道了", null).show();
            return;
        }
        try {
            a.startActivity(i);
            copy(a, keyword);
        } catch (Exception e) {
            new AlertDialog.Builder(a).setTitle("暂未安装").setMessage("未检测到该 App。").setPositiveButton("知道了", null).show();
        }
    }
}
