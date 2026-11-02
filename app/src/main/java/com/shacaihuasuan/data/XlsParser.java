package com.shacaihuasuan.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;

public class XlsParser {
    public static class Parsed {
        public String date;
        public Map<String, Item> items = new HashMap<>();
    }
    public static class Item {
        public String name, spec, category, sub, unit;
        public double avg;
        public List<Model.Shop> outlets = new ArrayList<>();
        public double min, p20, p50, p80, max;
    }

    public static Parsed parse(File file) throws Exception {
        Workbook wb = Workbook.getWorkbook(file);
        Sheet sh = wb.getSheet(0);
        Parsed out = new Parsed();
        String title = cell(sh, 0, 0);
        out.date = dateOf(title);
        if (out.date == null) out.date = dateOf(cell(sh, 2, 0));
        if (out.date == null) out.date = "未知日期";

        int cols = sh.getColumns(), rows = sh.getRows();
        if (rows < 9) { wb.close(); return out; }
        String[] names = row(sh, 4), specs = row(sh, 5), units = row(sh, 6), cats = row(sh, 3);
        String[] avgs = row(sh, 7);

        for (int c = 2; c < cols; c++) {
            String name = safe(names, c);
            if (name.isEmpty()) continue;
            String spec = safe(specs, c);
            String rawCat = safe(cats, c);
            Item it = new Item();
            it.name = name; it.spec = spec;
            it.unit = safe(units, c);
            it.category = category(rawCat, name);
            it.sub = sub(name, it.category);
            it.avg = num(safe(avgs, c), Double.NaN);
            for (int r = 8; r < rows; r++) {
                String dist = cell(sh, 0, r);
                String shop = cell(sh, 1, r);
                double v = num(cell(sh, c, r), Double.NaN);
                if (Double.isNaN(v) || v <= 0) continue;
                Model.Shop s = new Model.Shop();
                s.price = v; s.district = dist; s.shop = shop;
                it.outlets.add(s);
            }
            if (it.outlets.isEmpty() && Double.isNaN(it.avg)) continue;
            it.outlets.sort((a, b) -> Double.compare(a.price, b.price));
            if (Double.isNaN(it.avg)) {
                double sum = 0; for (Model.Shop s : it.outlets) sum += s.price;
                it.avg = sum / it.outlets.size();
            }
            List<Double> vs = new ArrayList<>();
            for (Model.Shop s : it.outlets) vs.add(s.price);
            Collections.sort(vs);
            if (!vs.isEmpty()) {
                it.min = vs.get(0); it.max = vs.get(vs.size() - 1);
                it.p20 = q(vs, .20); it.p50 = q(vs, .50); it.p80 = q(vs, .80);
            }
            out.items.put(key(it.name, it.spec), it);
        }
        wb.close();
        return out;
    }

    static double q(List<Double> a, double p) {
        if (a.isEmpty()) return 0;
        double k = (a.size() - 1) * p;
        int f = (int) Math.floor(k), c = Math.min(f + 1, a.size() - 1);
        return a.get(f) + (a.get(c) - a.get(f)) * (k - f);
    }
    static String key(String name, String spec) {
        return spec == null || spec.isEmpty() || spec.equals("新鲜一级") ? name : name + "·" + spec;
    }
    static String category(String raw, String name) {
        if ("蔬菜".equals(raw)) return "veg";
        if ("水果".equals(raw)) return "fruit";
        if ("鱼虾".equals(raw)) return "aqua";
        if ("粮食".equals(raw) || "食用油".equals(raw)) return "grain";
        if ("肉禽蛋".equals(raw)) return "鸡蛋".equals(name) ? "dairy" : "meat";
        return "veg";
    }
    static String sub(String name, String cat) {
        if ("dairy".equals(cat)) return "鸡蛋".equals(name) ? "egg" : "milk";
        if ("meat".equals(cat)) {
            if (name.contains("猪")) return "pork";
            if (name.contains("牛")) return "beef";
            if (name.contains("羊")) return "lamb";
            return "chicken";
        }
        if ("aqua".equals(cat)) return "fish";
        return "";
    }
    static String dateOf(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日").matcher(s);
        if (m.find()) return String.format("%s-%02d-%02d", m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        return null;
    }
    static String[] row(Sheet sh, int r) {
        String[] a = new String[sh.getColumns()];
        for (int c = 0; c < a.length; c++) a[c] = cell(sh, c, r);
        return a;
    }
    static String cell(Sheet sh, int c, int r) {
        try { return sh.getCell(c, r).getContents().trim(); }
        catch (Exception e) { return ""; }
    }
    static String safe(String[] a, int i) { return i < a.length && a[i] != null ? a[i].trim() : ""; }
    static double num(String s, double d) {
        if (s == null) return d;
        s = s.replace(",", "").replace("元", "").trim();
        try { return Double.parseDouble(s); } catch (Exception e) { return d; }
    }
}
