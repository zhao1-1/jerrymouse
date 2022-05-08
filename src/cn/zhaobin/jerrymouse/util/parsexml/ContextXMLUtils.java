package cn.zhaobin.jerrymouse.util.parsexml;

import cn.hutool.core.io.FileUtil;
import cn.zhaobin.jerrymouse.util.Constant;
import org.jsoup.Jsoup;

public class ContextXMLUtils {

    public static String parseWatchedResource() {
        try {
            return Jsoup.parse(FileUtil.readUtf8String(Constant.CONTEXT_XML_FILE))
                    .select("WatchedResource")
                    .first()
                    .text();
        } catch (Exception e) {
            e.printStackTrace();
            return Constant.DEFAULT_WATCHED_RESOURCE;
        }
    }
}
