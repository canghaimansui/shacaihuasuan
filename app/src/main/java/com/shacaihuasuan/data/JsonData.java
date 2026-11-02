package com.shacaihuasuan.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class JsonData {
    public static String readAsset(android.content.Context c, String name) throws Exception {
        InputStream in = c.getAssets().open(name);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) out.write(b, 0, n);
        in.close();
        return out.toString("UTF-8");
    }

    public static Model.CityData parse(String json, String cityId) throws Exception {
        JSONObject root = new JSONObject(json);
        Model.CityData d = new Model.CityData();
        JSONObject m = root.optJSONObject("meta");
        if (m != null) {
            d.meta.city = m.optString("city", "");
            d.meta.latestDate = m.optString("latestDate", "");
            d.meta.source = m.optString("source", "");
            d.meta.sourceUrl = m.optString("sourceUrl", "");
            d.meta.unit = m.optString("unit", "元/500g");
        }
        JSONArray arr = root.getJSONArray("products");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Model.Product p = new Model.Product();
            p.city = cityId;
            p.id = o.getString("id");
            p.name = o.getString("name");
            p.displayName = o.optString("display", p.name);
            p.spec = o.optString("spec", "");
            p.category = o.getString("category");
            p.sub = o.optString("sub", "");
            p.unit = o.optString("unit", "元/500g");
            p.current = o.optDouble("current", 0);
            p.prev = o.optDouble("prev", p.current);
            p.tag = o.optString("tag", "参考");
            p.isMilk = o.optBoolean("isMilk", false);
            p.belowP20 = o.optInt("belowP20", 0);
            p.outletCount = o.optInt("outletCount", 0);
            p.seriesType = o.optString("seriesType", "retail");
            if (!o.isNull("farm")) p.farm = o.optDouble("farm");
            if (!o.isNull("supermarket")) p.supermarket = o.optDouble("supermarket");
            if (!o.isNull("wholesale")) p.wholesale = o.optDouble("wholesale");
            JSONObject os = o.optJSONObject("outlet30");
            if (os != null) {
                p.outlet30 = new Model.OutletStats();
                p.outlet30.min = os.optDouble("min");
                p.outlet30.p20 = os.optDouble("p20");
                p.outlet30.p50 = os.optDouble("p50");
                p.outlet30.p80 = os.optDouble("p80");
                p.outlet30.max = os.optDouble("max");
                p.outlet30.n = os.optInt("n");
            }
            JSONArray ss = o.optJSONArray("series");
            if (ss != null) for (int j = 0; j < ss.length(); j++) {
                JSONObject so = ss.getJSONObject(j);
                Model.Day day = new Model.Day();
                day.date = so.optString("date");
                day.avg = so.optDouble("avg");
                day.min = so.optDouble("min");
                day.max = so.optDouble("max");
                day.p20 = so.optDouble("p20");
                day.p80 = so.optDouble("p80");
                p.series.add(day);
            }
            JSONArray sh = o.optJSONArray("cheapest3");
            if (sh != null) for (int j = 0; j < sh.length(); j++) {
                JSONObject so = sh.getJSONObject(j);
                Model.Shop s = new Model.Shop();
                s.price = so.optDouble("price");
                s.district = so.optString("district");
                s.shop = so.optString("shop");
                p.cheapest3.add(s);
            }
            d.products.add(p);
        }
        return d;
    }
}
