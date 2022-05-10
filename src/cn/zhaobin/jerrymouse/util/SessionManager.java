package cn.zhaobin.jerrymouse.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class SessionManager {
    private static final Map<String, StandardSession> sessionMap = new HashMap<>();
    private static final int defaultTimeout;

    static {
        startSessionExpiredCheckThread();
        defaultTimeout = getTimeout();
    }

    private static void startSessionExpiredCheckThread() {
        new Thread(() -> {
            while (true) {
                checkExpiredSession();
                ThreadUtil.sleep(1000 * 30);
            }
        }).start();
    }

    private static void checkExpiredSession() {
        List<String> expiredJSessionIds = new ArrayList<>();
        for (String jsessionid : sessionMap.keySet()) {
            StandardSession session = sessionMap.get(jsessionid);
            long interval = System.currentTimeMillis() -  session.getLastAccessedTime() - session.getMaxInactiveInterval() * 1000L;
            if (interval > 0)
                expiredJSessionIds.add(jsessionid);
        }
        for (String jsessionid : expiredJSessionIds) {
            sessionMap.remove(jsessionid);
        }
    }

    private static int getTimeout() {
        int defaultResult = 30;
        try {
            Elements es = Jsoup.parse(Constant.WEB_XML_FILE, "utf-8").select("session-config session-timeout");
            if (es.isEmpty())
                return defaultResult;
            return Convert.toInt(es.get(0).text());
        } catch (IOException e) {
            return defaultResult;
        }
    }

    private static HttpSession newSession(Request request, Response response) {
        String sid = generateSessionId();
        StandardSession session = new StandardSession(sid, request.getServletContext());
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sid, session);
        createCookieBySession(session, request, response);
        return session;
    }

    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

    public static synchronized String generateSessionId() {
        return SecureUtil.md5(new String(RandomUtil.randomBytes(16))).toUpperCase();
    }

    public static HttpSession getSession(String jsessionid, Request request, Response response) {
        if (null == jsessionid) {
            return newSession(request, response);
        } else {
            StandardSession currentSession = sessionMap.get(jsessionid);
            if (null == currentSession) {
                return newSession(request, response);
            } else {
                currentSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(currentSession, request, response);
                return currentSession;
            }
        }
    }

}
