package cn.zhaobin.jerrymouse.servlet;

import cn.hutool.core.util.ReflectUtil;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InvokeServlet extends HttpServlet {

    private static final InvokeServlet instance = new InvokeServlet();  // 单例
    private InvokeServlet() { }

    public static InvokeServlet getInstance() { return instance; }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        Request request = (Request) req;
        Response response = (Response) resp;

        Context context = request.getContext();
        String uri = request.getRequestURI();

        try {
            Class<?> servletClass = context.getWebAppClassLoader().loadClass(context.getServletClassName(uri));
            System.out.println("servletClass:" + servletClass);
            System.out.println("servletClass' classLoader:" + servletClass.getClassLoader());
            ReflectUtil.invoke(context.getSingletonServlet(servletClass), "service", request, response);
            response.servletHandle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
