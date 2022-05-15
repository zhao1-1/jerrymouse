package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.http.ApplicationFilterChain;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.servlet.DefaultServlet;
import cn.zhaobin.jerrymouse.servlet.InvokeServlet;
import cn.zhaobin.jerrymouse.util.SessionManager;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.List;

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
//            executeServlet();
            executeServletChain();
            if (request.isForwarded()) return;
            response.handle();
        } catch (Exception e) {
            LogFactory.get().error(e);
            response.handle500(e);
        }
    }

    private void executeServlet() throws Exception{
        if (request.getContext().servletClassValid(request.getRequestURI()))
            InvokeServlet.getInstance().service(this.request, this.response);
        else
            DefaultServlet.getInstance().service(this.request, this.response);
    }

    private void executeServletChain() throws Exception {
        HttpServlet workingServlet;
        if (this.request.getContext().servletClassValid(this.request.getRequestURI()))
            workingServlet = InvokeServlet.getInstance();
        else
            workingServlet = DefaultServlet.getInstance();
        List<Filter> filters = this.request.getContext().getMatchedFilters(this.request.getRequestURI());
        new ApplicationFilterChain(filters, workingServlet).doFilter(this.request, this.response);
    }

    private void prepareSession(Request request, Response response) {
        request.setSession(SessionManager.getSession(request.getJSessionIdFromCookie(), request, response));
    }
}
