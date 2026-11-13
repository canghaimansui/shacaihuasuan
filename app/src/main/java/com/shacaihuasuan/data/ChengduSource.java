package com.shacaihuasuan.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;

/** 成都市城市居民生活价格服务公益平台（www.cdprice.cn）的品种价格。 */
public class ChengduSource {
    public static final String HOST = "https://www.cdprice.cn";
    public static final String LIST = HOST + "/price/newHomePageTowAction!getProductList.do";
    public static final String DETAIL = HOST + "/price/homePageAction!getProductDetail.do";

    public static class Result {
        public String date = "";
        public JSONObject raw = new JSONObject();
    }

    /** 拉取全市 9 个品类 + 每个品种的 7 天序列，返回可直接合并的 JSON。 */
    public static Model.CityData fetch(Context c) throws Exception {
        int[] typeIds = {1, 7, 8, 9, 10, 11, 5, 4, 2};
        JSONArray products = new JSONArray();
        String latest = "";
        int seq = 0;
        for (int typeId : typeIds) {
            String body = "random=" + Math.random() + "&areaid=510100&marketid=&typeid=" + typeId
                    + "&timeorder=&priceorder=&minprice=&maxprice=&productname=&page=1&pageSize=500&scType=";
            String res = post(LIST, body);
            JSONObject root = new JSONObject(res);
            JSONObject data = root.optJSONObject("data");
            if (data == null) continue;
            JSONArray list = data.optJSONArray("productList");
            if (list == null) continue;
            for (int i = 0; i < list.length(); i++) {
                JSONObject o = list.getJSONObject(i);
                String pid = o.optString("cltpro01id");
                String name = o.optString("cltpro01001");
                String date = o.optString("cltprc05001");
                if (pid.isEmpty() || name.isEmpty()) continue;
                JSONObject p = new JSONObject();
                p.put("pid", pid);
                p.put("name", name);
                p.put("typeid", typeId);
                p.put("date", date);
                p.put("latest", o.optDouble("cltprc05007", 0));
                // detail：均价 / 最低 / 最高 / 单位 / 7 天序列
                try {
                    JSONObject det = new JSONObject(post(DETAIL, "random=" + Math.random() + "&productid=" + URLEncoder.encode(pid, "UTF-8")));
                    JSONObject d = det.optJSONObject("detail");
                    if (d != null) {
                        p.put("avg", d.optDouble("avgprice", 0));
                        p.put("min", d.optDouble("minprice", 0));
                        p.put("max", d.optDouble("maxprice", 0));
                        p.put("unit", d.optString("cltpro01004", d.optString("cltpro01005", "元/500g")));
                        String dd = d.optString("cltprc05001", "");
                        if (dd.compareTo(date) > 0) { date = dd; p.put("date", dd); }
                    }
                    JSONArray pl = det.optJSONArray("priceList");
                    JSONArray ser = new JSONArray();
                    if (pl != null) for (int k = 0; k < pl.length(); k++) {
                        JSONObject x = pl.getJSONObject(k);
                        JSONObject day = new JSONObject();
                        day.put("date", x.optString("cltprc05001"));
                        day.put("avg", x.optDouble("cltprc05007", 0));
                        ser.put(day);
                        String d2 = x.optString("cltprc05001");
                        if (d2.compareTo(latest) > 0) latest = d2;
                    }
                    p.put("series", ser);
                } catch (Exception ignored) {
                }
                products.put(p);
                seq++;
            }
        }
        // 转成 Model.CityData
        Model.CityData cd = new Model.CityData();
        cd.meta.city = "成都";
        cd.meta.latestDate = latest;
        cd.meta.source = "成都市城市居民生活价格服务公益平台";
        cd.meta.sourceUrl = HOST;
        cd.meta.unit = "元/500g";
        for (int i = 0; i < products.length(); i++) {
            JSONObject o = products.getJSONObject(i);
            Model.Product p = new Model.Product();
            p.city = "cd";
            p.id = "cd_" + o.optString("pid");
            p.name = o.optString("name");
            p.displayName = p.name;
            p.spec = "成都市";
            p.category = category(o.optInt("typeid", 1));
            p.sub = sub(p.category, p.name);
            p.unit = o.optString("unit", "元/500g").replace("／", "/");
            p.current = round(o.optDouble("avg", o.optDouble("latest", 0)));
            p.tag = "参考";
            p.seriesType = "city";
            JSONArray ser = o.optJSONArray("series");
            if (ser != null) for (int k = 0; k < ser.length(); k++) {
                JSONObject x = ser.getJSONObject(k);
                Model.Day d = new Model.Day();
                d.date = x.optString("date");
                d.avg = round(x.optDouble("avg", 0));
                p.series.add(d);
            }
            if (p.series.size() > 1) p.prev = p.series.get(p.series.size() - 2).avg;
            p.isMilk = false; // 成都有牛奶真实价格
            double tmn = o.optDouble("min", 0), tmx = o.optDouble("max", 0);
            if (tmn > 0) p.todayMin = tmn;
            if (tmx > 0) p.todayMax = tmx;
            cd.products.add(p);
        }
        // 缓存 raw
        FileOutputStream fo = new FileOutputStream(new File(c.getFilesDir(), "cd_latest.json"));
        fo.write(products.toString().getBytes("UTF-8"));
        fo.close();
        return cd;
    }

    public static Model.CityData parseCache(String json) throws Exception {
        JSONArray products = new JSONArray(json);
        Model.CityData cd = new Model.CityData();
        cd.meta.city = "成都";
        cd.meta.source = "成都市城市居民生活价格服务公益平台";
        cd.meta.sourceUrl = HOST;
        cd.meta.unit = "元/500g";
        String latest = "";
        for (int i = 0; i < products.length(); i++) {
            JSONObject o = products.getJSONObject(i);
            Model.Product p = new Model.Product();
            p.city = "cd";
            p.id = "cd_" + o.optString("pid");
            p.name = o.optString("name");
            p.displayName = p.name;
            p.spec = "成都市";
            p.category = category(o.optInt("typeid", 1));
            p.sub = sub(p.category, p.name);
            p.unit = o.optString("unit", "元/500g").replace("／", "/");
            p.current = round(o.optDouble("avg", o.optDouble("latest", 0)));
            p.tag = "参考";
            p.isMilk = false; // 成都有牛奶真实价格
            double tmn = o.optDouble("min", 0), tmx = o.optDouble("max", 0);
            if (tmn > 0) p.todayMin = tmn;
            if (tmx > 0) p.todayMax = tmx;
            JSONArray ser = o.optJSONArray("series");
            if (ser != null) for (int k = 0; k < ser.length(); k++) {
                JSONObject x = ser.getJSONObject(k);
                Model.Day d = new Model.Day();
                d.date = x.optString("date");
                d.avg = round(x.optDouble("avg", 0));
                p.series.add(d);
                if (d.date.compareTo(latest) > 0) latest = d.date;
            }
            if (p.series.size() > 1) p.prev = p.series.get(p.series.size() - 2).avg;
            cd.products.add(p);
        }
        cd.meta.latestDate = latest.isEmpty() ? "" : latest;
        return cd;
    }

    static String category(int typeId) {
        switch (typeId) {
            case 1: return "veg";
            case 4: return "fruit";
            case 7: case 8: case 9: return "meat";
            case 10: case 11: return "dairy";
            case 5: return "aqua";
            case 2: return "grain";
            default: return "veg";
        }
    }
    static String sub(String cat, String name) {
        if ("meat".equals(cat)) {
            if (name.contains("猪") || name.contains("排骨")) return "pork";
            if (name.contains("牛")) return "beef";
            if (name.contains("羊")) return "lamb";
            return "chicken";
        }
        if ("dairy".equals(cat)) return name.contains("蛋") ? "egg" : "milk";
        if ("aqua".equals(cat)) return "fish";
        return "";
    }
    static double round(double v) { return Math.round(v * 100.0) / 100.0; }

    static String post(String url, String body) throws Exception {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(25000);
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36");
        c.setRequestProperty("Referer", HOST + "/");
        c.getOutputStream().write(body.getBytes("UTF-8"));
        java.io.InputStream in = c.getInputStream();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) out.write(b, 0, n);
        in.close();
        c.disconnect();
        return out.toString("UTF-8");
    }
}
