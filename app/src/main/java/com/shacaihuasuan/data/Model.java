package com.shacaihuasuan.data;

import java.util.ArrayList;
import java.util.List;

public class Model {
    public static class Day {
        public String date;
        public double avg, min, max, p20, p80;
    }
    public static class OutletStats {
        public double min, p20, p50, p80, max;
        public int n;
    }
    public static class Shop {
        public double price;
        public String district, shop;
    }
    public static class Product {
        public String id, name, displayName, spec, category, sub, unit, tag;
        public double current, prev;
        public boolean isMilk;
        public List<Day> series = new ArrayList<>();
        public OutletStats outlet30;
        public List<Shop> cheapest3 = new ArrayList<>();
        public int belowP20, outletCount;
        public Double farm, supermarket, wholesale, todayMin, todayMax;
        public String seriesType = "retail";
        public String city;

        public boolean isBeijing() { return "bj".equals(city); }
        public boolean isChengdu() { return "cd".equals(city); }
        public String priceUnit() { return unit == null || unit.isEmpty() ? "元/500g" : unit; }
    }
    public static class Meta {
        public String city, latestDate, source, sourceUrl, unit;
    }
    public static class CityData {
        public Meta meta = new Meta();
        public List<Product> products = new ArrayList<>();
    }
}
