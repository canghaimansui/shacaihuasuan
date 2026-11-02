package com.shacaihuasuan.data;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShanghaiSource {
    public static final String BASE = "https://fgw.sh.gov.cn";
    public static final String LIST = BASE + "/fgw_jgjgdt/";

    public static XlsParser.Parsed fetch(Context c) throws Exception {
        String html = Network.get(LIST);
        Pattern p = Pattern.compile("href=\"(/fgw_jgjgdt/\\d{8}/[^\"]+\\.html)\"[^>]*title=\"([^\"]+)\"");
        Matcher m = p.matcher(html);
        String article = null;
        while (m.find()) {
            String title = m.group(2);
            if (title.contains("主要主副食品品种价格信息表")) { article = m.group(1); break; }
        }
        if (article == null) {
            m = p.matcher(html);
            if (m.find()) article = m.group(1);
        }
        if (article == null) throw new Exception("没有找到价格文章");
        String ahtml = Network.get(BASE + article);
        Matcher x = Pattern.compile("href=\"([^\"]+\\.xls)\"").matcher(ahtml);
        if (!x.find()) throw new Exception("没有找到 XLS");
        String xurl = x.group(1);
        if (xurl.startsWith("/")) xurl = BASE + xurl;
        byte[] bytes = Network.getBytes(xurl);
        File f = new File(c.getFilesDir(), "sh_latest.xls");
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes); fo.close();
        return XlsParser.parse(f);
    }
}
