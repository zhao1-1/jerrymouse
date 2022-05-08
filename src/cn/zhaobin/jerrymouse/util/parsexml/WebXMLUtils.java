package cn.zhaobin.jerrymouse.util.parsexml;

import cn.hutool.core.io.FileUtil;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.util.Cache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

import static cn.zhaobin.jerrymouse.util.Constant.WEB_XML_FILE;

public class WebXMLUtils {

    public static synchronized String getMimeType(String extName) {
        if (Cache.mimeTypeMapping.isEmpty())
            initMimeTypeCache();
        String mimeType = Cache.mimeTypeMapping.get(extName);
        if (null == mimeType)
            return "text/html";
        return mimeType;
    }

    private static void initMimeTypeCache() {
        Elements elements = Jsoup.parse(FileUtil.readUtf8String(WEB_XML_FILE)).select("mime-mapping");
        for (Element ele : elements) {
            String extName = ele.select("extension").first().text();
            String mimeType = ele.select("mime-type").first().text();
            Cache.mimeTypeMapping.put(extName, mimeType);
        }
    }

    public static String getWelcomeFileName(Context context) {
        Elements elements = Jsoup.parse(FileUtil.readUtf8String(WEB_XML_FILE)).select("welcome-file");
        for (Element ele : elements) {
            String welcomeFileName = ele.text();
            File f = new File(context.getDocBase(), welcomeFileName);
            if (f.exists())
                return welcomeFileName;
        }
        return "";  // 表示不存在WelcomeFile，没有index.html等欢迎文件
    }

}
