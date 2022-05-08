package cn.zhaobin.jerrymouse.servlet;

import cn.hutool.core.util.ReflectUtil;
import cn.zhaobin.jerrymouse.catalina.Context;
import cn.zhaobin.jerrymouse.http.Request;
import cn.zhaobin.jerrymouse.http.Response;
import cn.zhaobin.jerrymouse.util.Constant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InvokerServlet extends HttpServlet {

    private static final InvokerServlet instance = new InvokerServlet();  // 单例
    private InvokerServlet() { }

    public static InvokerServlet getInstance() { return instance; }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        Request request = (Request) req;
        Response response = (Response) resp;

        Context context = request.getContext();
        String uri = request.getUri();

        try {
            Class<?> servletClass = context.getWebappClassLoader().loadClass(context.getServletClassName(uri));
            System.out.println("servletClass:" + servletClass);
            System.out.println("servletClass' classLoader:" + servletClass.getClassLoader());
            ReflectUtil.invoke(context.getSingletonServlet(servletClass), "service", request, response);
            response.setStatus(Constant.CODE_200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
