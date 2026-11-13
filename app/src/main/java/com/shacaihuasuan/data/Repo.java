package com.shacaihuasuan.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repo {
    public static class RefreshResult {
        public boolean shanghai, beijing, chengdu;
        public String message = "";
    }
    public interface Callback { void done(RefreshResult r); }

    private static Repo I;
    private final Context app;
    private final List<Model.Product> sh = new ArrayList<>();
    private final List<Model.Product> bj = new ArrayList<>();
    private final List<Model.Product> cd = new ArrayList<>();
    private Model.Meta shMeta = new Model.Meta(), bjMeta = new Model.Meta(), cdMeta = new Model.Meta();
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private String city;

    public static synchronized Repo init(Context c) {
        if (I == null) I = new Repo(c.getApplicationContext());
        return I;
    }
    public static Repo get() { return I; }

    private Repo(Context c) {
        app = c;
        SharedPreferences sp = c.getSharedPreferences("app", Context.MODE_PRIVATE);
        // 默认城市：成都。老版本升级上来的也切一次，之后尊重用户选择。
        if (!sp.getBoolean("defaultCityV3", false)) {
            city = "cd";
            sp.edit().putString("city", "cd").putBoolean("defaultCityV3", true).apply();
        } else {
            city = sp.getString("city", "cd");
        }
        try {
            Model.CityData a = JsonData.parse(JsonData.readAsset(c, "shanghai.json"), "sh");
            sh.addAll(a.products); shMeta = a.meta;
            Model.CityData b = JsonData.parse(JsonData.readAsset(c, "beijing.json"), "bj");
            bj.addAll(b.products); bjMeta = b.meta;
            Model.CityData d = JsonData.parse(JsonData.readAsset(c, "chengdu.json"), "cd");
            cd.addAll(d.products); cdMeta = d.meta;
        } catch (Exception e) { e.printStackTrace(); }
        // cached latest data
        try {
            File xls = new File(c.getFilesDir(), "sh_latest.xls");
            if (xls.exists()) mergeShanghai(XlsParser.parse(xls));
        } catch (Exception ignored) {}
        try {
            File m = new File(c.getFilesDir(), "bj_meat.json");
            File v = new File(c.getFilesDir(), "bj_veg.json");
            if (m.exists() && v.exists()) {
                String ms = read(m), vs = read(v);
                mergeBeijing(BeijingSource.parse(ms, vs));
            }
        } catch (Exception ignored) {}
        try {
            File f = new File(c.getFilesDir(), "cd_latest.json");
            if (f.exists()) mergeChengdu(ChengduSource.parseCache(read(f)));
        } catch (Exception ignored) {}
    }

    private static String read(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        byte[] b = new byte[(int) f.length()];
        int off = 0, n;
        while ((n = in.read(b, off, b.length - off)) > 0) off += n;
        in.close();
        return new String(b, 0, off, "UTF-8");
    }

    public String city() { return city; }
    public void setCity(String c) {
        city = c;
        app.getSharedPreferences("app", Context.MODE_PRIVATE).edit().putString("city", c).apply();
    }
    public boolean isShanghai() { return "sh".equals(city); }
    public boolean isBeijing() { return "bj".equals(city); }
    public boolean isChengdu() { return "cd".equals(city); }
    public String cityName() {
        if (isShanghai()) return "上海";
        if (isBeijing()) return "北京";
        return "成都";
    }
    public Model.Meta meta() {
        if (isShanghai()) return shMeta;
        if (isBeijing()) return bjMeta;
        return cdMeta;
    }
    public List<Model.Product> all() {
        if (isShanghai()) return sh;
        if (isBeijing()) return bj;
        return cd;
    }

    public Model.Product byId(String id) {
        for (Model.Product p : sh) if (p.id.equals(id)) return p;
        for (Model.Product p : bj) if (p.id.equals(id)) return p;
        for (Model.Product p : cd) if (p.id.equals(id)) return p;
        return null;
    }

    public List<Model.Product> home(String cat) {
        List<String> order = HOME.get(cat);
        List<Model.Product> src = new ArrayList<>();
        for (Model.Product p : all()) if (p.category.equals(cat)) src.add(p);
        List<Model.Product> out = new ArrayList<>();
        if (order != null) for (String n : order) for (Model.Product p : src)
            if (p.displayName.equals(n) && !out.contains(p)) out.add(p);
        for (Model.Product p : src) if (!out.contains(p)) out.add(p);
        if (out.size() > 8) out = new ArrayList<>(out.subList(0, 8));
        return out;
    }

    public List<Model.Product> cat(String cat, String sub) {
        List<Model.Product> out = new ArrayList<>();
        for (Model.Product p : all()) if (p.category.equals(cat) && (sub == null || sub.isEmpty() || sub.equals(p.sub))) out.add(p);
        return out;
    }

    public int count(String cat) {
        int n = 0; for (Model.Product p : all()) if (p.category.equals(cat)) n++;
        return n;
    }

    public String categoryName(String cat) {
        switch (cat) {
            case "veg": return "蔬菜"; case "fruit": return "水果"; case "meat": return "肉禽";
            case "dairy": return "蛋奶"; case "aqua": return "水产"; case "grain": return "粮油";
        }
        return cat;
    }

    public String date() { return meta().latestDate == null ? "" : meta().latestDate; }
    public String source() { return meta().source == null ? "" : meta().source; }

    public String overviewTop() {
        if (isShanghai()) {
            int down = 0;
            for (Model.Product p : sh) {
                if (p.series.size() < 2) continue;
                double a = p.series.get(0).avg, b = p.series.get(p.series.size() - 1).avg;
                if (b < a - 1e-6) down++;
            }
            return down + " 个降价";
        }
        if (isBeijing()) return "今日参考";
        return "今日参考";
    }
    public String overviewUp() {
        List<Model.Product> list = all();
        int up = 0;
        for (Model.Product p : list) {
            if (p.series.size() < 2) continue;
            double a = p.series.get(0).avg, b = p.series.get(p.series.size() - 1).avg;
            if (b > a + 1e-6) up++;
        }
        return up + " 个涨价";
    }
    public String overviewSub() {
        if (isShanghai()) return "近 30 天 · 上海 " + countAll() + " 个品种";
        if (isBeijing()) return "北京 · 农贸 / 超市 / 批发";
        return "成都 · 全市均价";
    }
    public String overviewNote() {
        if (isShanghai()) return "价格为监测点均价 · 仅供参考";
        if (isBeijing()) return "农贸/超市为全市均价，批发为新发地参考";
        return "成都市价格服务公益平台 · 全市均价";
    }
    public int countAll() { return all().size(); }

    public void refresh(Callback cb) {
        pool.execute(() -> {
            RefreshResult r = new RefreshResult();
            XlsParser.Parsed shParsed = null;
            BeijingSource.Result bjParsed = null;
            Model.CityData cdParsed = null;
            try { shParsed = ShanghaiSource.fetch(app); r.shanghai = true; }
            catch (Exception e) { r.message += "上海: " + e.getMessage() + " "; }
            try { bjParsed = BeijingSource.fetch(app); r.beijing = true; }
            catch (Exception e) { r.message += "北京: " + e.getMessage() + " "; }
            try { cdParsed = ChengduSource.fetch(app); r.chengdu = true; }
            catch (Exception e) { r.message += "成都: " + e.getMessage() + " "; }
            final XlsParser.Parsed sp = shParsed;
            final BeijingSource.Result bp = bjParsed;
            final Model.CityData cp = cdParsed;
            main.post(() -> {
                if (sp != null) mergeShanghai(sp);
                if (bp != null) mergeBeijing(bp);
                if (cp != null) mergeChengdu(cp);
                cb.done(r);
            });
        });
    }

    private void mergeShanghai(XlsParser.Parsed parsed) {
        Map<String, XlsParser.Item> byName = new HashMap<>();
        for (XlsParser.Item it : parsed.items.values()) byName.put(it.name, it);
        for (Model.Product p : sh) {
            if (p.isMilk) continue;
            XlsParser.Item it = parsed.items.get(key(p.name, p.spec));
            if (it == null) it = byName.get(p.name);
            if (it == null) continue;
            double old = p.current;
            boolean newer = true;
            if (!p.series.isEmpty()) newer = parsed.date.compareTo(p.series.get(p.series.size() - 1).date) > 0;
            if (newer) p.prev = old;
            p.current = round(it.avg);
            p.outletCount = it.outlets.size();
            Model.Day d = new Model.Day();
            d.date = parsed.date; d.avg = round(it.avg); d.min = it.min; d.max = it.max; d.p20 = it.p20; d.p80 = it.p80;
            boolean replaced = false;
            for (int i = 0; i < p.series.size(); i++) if (p.series.get(i).date.equals(d.date)) { p.series.set(i, d); replaced = true; break; }
            if (!replaced) p.series.add(d);
            Collections.sort(p.series, (a, b) -> a.date.compareTo(b.date));
            while (p.series.size() > 30) p.series.remove(0);
            if (p.outlet30 == null) {
                p.outlet30 = new Model.OutletStats();
                p.outlet30.min = it.min; p.outlet30.p20 = it.p20; p.outlet30.p50 = it.p50; p.outlet30.p80 = it.p80; p.outlet30.max = it.max; p.outlet30.n = it.outlets.size();
            }
            p.cheapest3.clear();
            for (int i = 0; i < Math.min(3, it.outlets.size()); i++) p.cheapest3.add(it.outlets.get(i));
            p.belowP20 = 0;
            if (p.outlet30 != null) for (Model.Shop s : it.outlets) if (s.price <= p.outlet30.p20 + 1e-6) p.belowP20++;
            p.tag = tagOf(p);
        }
        shMeta.latestDate = parsed.date;
    }

    private void mergeBeijing(BeijingSource.Result parsed) {
        Map<String, BeijingSource.Update> map = new HashMap<>();
        for (BeijingSource.Update u : parsed.updates) map.put(u.name, u);
        for (Model.Product p : bj) {
            if (p.isMilk) continue;
            BeijingSource.Update u = map.get(p.name);
            if (u == null) continue;
            if (u.farm > 0) p.farm = round(u.farm);
            if (u.supermarket > 0) p.supermarket = round(u.supermarket);
            if (u.wholesale > 0) p.wholesale = round(u.wholesale);
            if (p.farm != null && p.farm > 0) p.current = p.farm;
            else if (p.supermarket != null && p.supermarket > 0) p.current = p.supermarket;
            else if (p.wholesale != null) p.current = p.wholesale;
        }
        if (parsed.date != null && !parsed.date.isEmpty()) bjMeta.latestDate = parsed.date;
    }

    private void mergeChengdu(Model.CityData parsed) {
        Map<String, Model.Product> byName = new HashMap<>();
        for (Model.Product p : parsed.products) byName.put(p.name, p);
        for (Model.Product p : cd) {
            Model.Product src = byName.get(p.name);
            if (src == null) continue;
            p.current = src.current;
            if (src.series.size() > 1) p.prev = src.series.get(src.series.size() - 2).avg;
            p.series.clear();
            p.series.addAll(src.series);
            p.unit = src.unit;
            p.tag = tagOf(p);
        }
        if (parsed.meta != null && parsed.meta.latestDate != null && !parsed.meta.latestDate.isEmpty())
            cdMeta.latestDate = parsed.meta.latestDate;
    }

    public static String tagOf(Model.Product p) {
        if (p.series.size() < 5) return "正常";
        List<Double> avgs = new ArrayList<>();
        for (Model.Day d : p.series) avgs.add(d.avg);
        double p30 = quant(avgs, .30), p70 = quant(avgs, .70);
        if (p.current <= p30) return "便宜";
        if (p.current >= p70) return "偏贵";
        return "正常";
    }
    static double quant(List<Double> a, double p) {
        if (a.isEmpty()) return 0;
        List<Double> b = new ArrayList<>(a); Collections.sort(b);
        double k = (b.size() - 1) * p; int f = (int) Math.floor(k), c = Math.min(f + 1, b.size() - 1);
        return b.get(f) + (b.get(c) - b.get(f)) * (k - f);
    }
    static double round(double v) { return Math.round(v * 100.0) / 100.0; }
    static String key(String name, String spec) { return spec == null || spec.isEmpty() || spec.equals("新鲜一级") ? name : name + "·" + spec; }

    private static final Map<String, List<String>> HOME = new HashMap<>();
    static {
        HOME.put("veg", java.util.Arrays.asList("青菜","花菜","西红柿","土豆","大白菜","黄瓜","菠菜","芹菜","青椒","白萝卜","莲花白"));
        HOME.put("fruit", java.util.Arrays.asList("苹果","富士苹果","苹果（红富士）","香蕉","国产香蕉","橙子","脐橙","梨","鸭梨","西瓜","葡萄"));
        HOME.put("meat", java.util.Arrays.asList("猪精瘦肉","猪肋条肉","猪带皮后腿肉","猪肋排","牛腩","牛腱子","白条鸡","鲜羊肉","白条猪","鸭子","鲜牛肉","猪肉（精瘦）","猪肉（去骨一级）","五花肉","排骨","牛肉","羊肉","鸡肉"));
        HOME.put("dairy", java.util.Arrays.asList("鸡蛋","洋鸡蛋","牛奶"));
        HOME.put("aqua", java.util.Arrays.asList("基围虾","鲫鱼","带鱼","草鱼","花鲢","黄鱼","鲢鱼"));
        HOME.put("grain", java.util.Arrays.asList("粳米","面粉","切面","大豆油","富强粉","标准粉","鲁花花生油","金龙鱼调和油","粳米（珍珠米）","大米（普通散装）","调和油（5L桶装）"));
    }
}
