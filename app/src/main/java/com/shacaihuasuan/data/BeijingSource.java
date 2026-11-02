package com.shacaihuasuan.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BeijingSource {
    public static final String MEAT = "https://www.beijingprice.cn/data/drjg/drjg.json";
    public static final String VEG = "https://www.beijingprice.cn/data/template/93ee358f-fac8-11ee-acc7-00ffefc89a05.json";

    public static class Update {
        public String name, spec, date;
        public double farm, supermarket, wholesale;
    }

    public static class Result {
        public String date;
        public List<Update> updates = new ArrayList<>();
    }

    public static Result fetch(Context c) throws Exception {
        String meat = Network.get(MEAT);
        String veg = Network.get(VEG);
        save(c, "bj_meat.json", meat);
        save(c, "bj_veg.json", veg);
        return parse(meat, veg);
    }

    public static Result parse(String meat, String veg) throws Exception {
        Result r = new Result();
        JSONObject v = new JSONObject(veg).getJSONObject("data");
        r.date = v.optString("dateStr", "").replace("年", "-").replace("月", "-").replace("日", "");
        JSONArray data = v.getJSONArray("data");
        JSONArray names = data.getJSONArray(0);
        for (int i = 0; i < names.length(); i++) {
            Update u = new Update();
            u.name = names.getString(i);
            u.spec = "蔬菜";
            u.date = r.date;
            u.wholesale = n(data.getJSONArray(2).optString(i));
            u.farm = n(data.getJSONArray(3).optString(i));
            u.supermarket = n(data.getJSONArray(4).optString(i));
            r.updates.add(u);
        }
        JSONObject m = new JSONObject(meat);
        JSONArray list = m.optJSONArray("meatList");
        if (list != null) for (int i = 0; i < list.length(); i++) {
            JSONObject o = list.getJSONObject(i);
            Update u = new Update();
            u.name = o.optString("ItemName");
            u.spec = o.optString("ItemLevel");
            u.date = o.optString("PriceDate");
            u.farm = o.optDouble("price03", 0);
            u.supermarket = o.optDouble("price04", 0);
            u.wholesale = o.optDouble("price02", 0);
            if ("鲜牛肉".equals(u.name) && u.spec.contains("牛腩")) {
                u.name = "牛腩"; u.spec = "鲜牛肉 · 牛腩";
            } else if ("鲜猪肉".equals(u.name) && u.spec.contains("精瘦肉")) {
                u.name = "猪精瘦肉"; u.spec = "鲜猪肉 · 精瘦肉";
            }
            r.updates.add(u);
        }
        JSONArray oils = m.optJSONArray("oilList");
        if (oils != null) for (int i = 0; i < oils.length(); i++) {
            JSONObject o = oils.getJSONObject(i);
            Update u = new Update();
            u.name = o.optString("ItemName");
            u.spec = o.optString("ItemLevel");
            u.date = o.optString("PriceDate");
            u.farm = o.optDouble("price03", 0);
            u.supermarket = o.optDouble("price04", 0);
            u.wholesale = o.optDouble("price02", 0);
            r.updates.add(u);
        }
        if (r.date.isEmpty() && !r.updates.isEmpty()) r.date = r.updates.get(0).date;
        return r;
    }

    static double n(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0; } }
    static void save(Context c, String name, String s) throws Exception {
        FileOutputStream fo = new FileOutputStream(new File(c.getFilesDir(), name));
        fo.write(s.getBytes("UTF-8")); fo.close();
    }
}
