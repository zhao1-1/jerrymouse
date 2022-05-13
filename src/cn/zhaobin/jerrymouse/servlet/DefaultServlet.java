package cn.zhaobin.jerrymouse.servlet;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.util.Constant;
import cn.zhaobin.jerrymouse.util.parsexml.WebXMLUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.zhaobin.jerrymouse.util.StatusCodeEnum.STATUS_CODE_200;

public class DefaultServlet extends HttpServlet {

    private static final DefaultServlet instance = new DefaultServlet();
    private DefaultServlet() { }

    public static DefaultServlet getInstance() { return instance; }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;

        String requestURI = request.getUri();
        Context context = request.getContext();

        if("/500.html".equals(requestURI)){
            throw new RuntimeException("this is a deliberately created exception");
        }

        if ("/".equals(requestURI)) {
            requestURI = WebXMLUtils.getWelcomeFileName(context);
            if (requestURI.isEmpty()) {
                response.getWriter().println(Constant.NO_INDEX_WELCOME_CONTENT);
                response.setStatus(STATUS_CODE_200.getCode());
                return;
            }
        }

        String fileName = StrUtil.removePrefix(requestURI, "/");
        if (fileName.equals("timeConsume.html")) ThreadUtil.sleep(1000);
        response.handleResourceFile(request.getRealPath(fileName));
    }
}
