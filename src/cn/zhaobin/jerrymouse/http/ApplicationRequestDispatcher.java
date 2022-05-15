package cn.zhaobin.jerrymouse.http;

import cn.zhaobin.jerrymouse.catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ApplicationRequestDispatcher implements RequestDispatcher {

    private final String uri;

    public ApplicationRequestDispatcher(String uri) {
        if(!uri.startsWith("/"))
            uri = "/" + uri;
        this.uri = uri;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;

        request.setUri(uri);

        new HttpProcessor(request, response).execute();
        request.setForwarded(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) {

    }
}
