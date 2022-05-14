package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.servlet.DefaultServlet;
import cn.zhaobin.jerrymouse.servlet.InvokeServlet;
import cn.zhaobin.jerrymouse.util.SessionManager;

public class HttpProcessor {

    private final Request request;
    private final Response response;

    public HttpProcessor(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public void execute() {
        try {
            prepareSession(this.request, this.response);
            executeServlet();
            if (request.isForwarded()) return;
            response.handle();
        } catch (Exception e) {
            LogFactory.get().error(e);
            response.handle500(e);
        }
    }

    private void executeServlet() throws Exception{
        if (request.getContext().servletClassValid(request.getUri()))
            InvokeServlet.getInstance().service(this.request, this.response);
        else
            DefaultServlet.getInstance().service(this.request, this.response);
    }

    private void prepareSession(Request request, Response response) {
        request.setSession(SessionManager.getSession(request.getJSessionIdFromCookie(), request, response));
    }
}
