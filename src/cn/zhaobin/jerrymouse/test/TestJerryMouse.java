package cn.zhaobin.jerrymouse.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.zhaobin.jerrymouse.util.Constant;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestJerryMouse {

    private static final int port = Constant.CONNECTOR_1_PORT;
    private static final String ip = Constant.LOCAL_IP;

    @BeforeClass
    public static void beforeClass() {
        //所有测试开始前看diy jerryMouse 是否已经启动了
        if(NetUtil.isUsableLocalPort(port)) {
            System.err.println("请先启动 位于端口: " +port+ " 的diy tomcat，否则无法进行单元测试");
            System.exit(1);
        }
        else {
            System.out.println("检测到 diy tomcat已经启动，开始进行单元测试");
            System.out.println("====================================");
        }
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("====================================");
        System.out.println("全部单元测试执行完毕");
    }

    @Test
    public void testMiniBrowser() {
        String url = "http://static.how2j.cn/diytomcat.html";
        String htmlContent = MiniBrowser.getHttpString(url);
        containAssert(htmlContent, "hello diytomcat");
    }

    @Test
    public void testAHtml() {
        String htmlContent = getContentStringViaMiniBrowser("/a.html");
        Assert.assertEquals(htmlContent, "hi, welcome jerryMouse by zhaoBin from a.html");
        System.out.println("testAHtml SUCCESS");
    }

    @Test
    public void testTimeConsumeHtml() throws InterruptedException {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                20, 20, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10));

        TimeInterval timeInterval = DateUtil.timer();

        for (int i = 0; i < 3; i++) {
            threadPool.execute(() -> getContentStringViaMiniBrowser("/timeConsume.html"));
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        long duration = timeInterval.intervalMs();
        System.out.println("duration: " + duration);
        Assert.assertTrue(duration < 3000);
        System.out.println("testTimeConsumeHtml SUCCESS");
    }

    @Test
    public void testAContext() {
        String htmlContent = getContentStringViaMiniBrowser("/a/index.html?name=wxx");
        Assert.assertEquals(htmlContent, "hello jerryMouse from index.html@a");
    }

    @Test
    public void testBContext() {
        String htmlContent = getContentStringViaMiniBrowser("/b2b/index.html");
        Assert.assertEquals(htmlContent, "hello jerryMouse from index.html@b");
    }

    @Test
    public void testDContext() {
        String htmlContent = getContentStringViaMiniBrowser("/d/d1/hello.html");
        Assert.assertEquals(htmlContent, "hello jerryMouse from hello.html@d/d1");
    }

    @Test
    public void testAIndex() {
        String htmlContent = getContentStringViaMiniBrowser("/a");
        Assert.assertEquals(htmlContent, "hello jerryMouse from index.html@a");
    }

    @Test
    public void testBIndex() {
        String htmlContent = getContentStringViaMiniBrowser("/b2b/");
        Assert.assertEquals(htmlContent, "hello jerryMouse from index.html@b");
    }

    @Test
    public void testRootIndex() {
        String htmlContent = getContentStringViaMiniBrowser("/");
        Assert.assertEquals(htmlContent, "hello jerryMouse by zhaoBin from ROOT");
    }

    @Test
    public void testEmptyURI() {
        String htmlContent = getContentStringViaMiniBrowser("");
        Assert.assertEquals(htmlContent, "hello jerryMouse by zhaoBin from ROOT");
    }

    @Test
    public void testCNoIndexHtml() {
        String content = getContentStringViaMiniBrowser("/c");
        Assert.assertEquals(content, Constant.NO_INDEX_WELCOME_CONTENT);
    }

    @Test
    public void test404_1() {
        String response = getHttpStringViaMiniBrowser("/not_exist.html");
        containAssert(response, "HTTP/1.1 404 Not Found");
    }

    @Test
    public void test404_2() {
        String response = getHttpStringViaMiniBrowser("/c/index.html");
        containAssert(response, "HTTP/1.1 404 Not Found");
    }

    @Test
    public void test500() {
        String response = getHttpStringViaMiniBrowser("/500.html");
        containAssert(response, "HTTP/1.1 500 Internal Server Error");
        System.out.println("test500 SUCCESS");
    }

    @Test
    public void testMineType() {
        String response = getHttpStringViaMiniBrowser("/a/hello.txt");
        containAssert(response, "Content-Type: text/plain");
    }

    @Test
    public void testPNG() {
        byte[] bytes = getContentBytesViaMiniBrowser("/logo.png");
        int pngFileLength = 1672;
        Assert.assertEquals(pngFileLength, bytes.length);
    }

    @Test
    public void testPDF() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        HttpUtil.download(getTestURL("/etf.pdf"), stream, true);
        int pdfFileLength = 3590775;
        Assert.assertEquals(pdfFileLength, stream.toByteArray().length);
    }

    @Test
    public void helloServlet() {
        String html = getContentStringViaMiniBrowser("/pdd/hello");
        Assert.assertEquals(html,"Hello JerryMouse from HelloServlet");
    }

    @Test
    public void testDemoHelloServlet() {
        String html = getContentStringViaMiniBrowser("/demo/hello");
        containAssert(html,"Hello JerryMouse from HelloServlet@demoWeb");
    }

    @Test
    public void testSingletonHelloServlet() {
        String html1 = getContentStringViaMiniBrowser("/demo/hello");
        String html2 = getContentStringViaMiniBrowser("/demo/hello");
        Assert.assertEquals(html1, html2);
    }

    @Test
    public void testGetParam() {
        Map<String,Object> params = new HashMap<>();
        params.put("name","LuBan");
        String html = getContentStringViaMiniBrowser("/demo/param", params, true);
        Assert.assertEquals(html,"get name:LuBan");
    }

    @Test
    public void testPostParam() {
        Map<String,Object> params = new HashMap<>();
        params.put("name","YaSe");
        String html = getContentStringViaMiniBrowser("/demo/param", params, false);
        Assert.assertEquals(html,"post name:YaSe");
    }

    @Test
    public void testHeader() {
        String html = getContentStringViaMiniBrowser("/demo/header");
        Assert.assertEquals(html, "ZhaoBin mini Browser / java1.8");
    }

    @Test
    public void testRequestInfo() {
        String html = getContentStringViaMiniBrowser("/demo/request-info");
        containAssert(html, "/request-info");
    }

    @Test
    public void testSetCookie() {
        String html = getHttpStringViaMiniBrowser("/demo/setCookie");
        containAssert(html,"Set-Cookie: sessionId=pddXxx123(cookie); Expires=");
    }

    @Test
    public void testGetCookie() throws IOException {
        URL u = new URL(getTestURL("/demo/getCookie"));
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie","name=Gareen(cookie)");
        conn.connect();
        String html = IoUtil.read(conn.getInputStream(), "utf-8");
        containAssert(html,"name:Gareen(cookie)");
    }

    @Test
    public void testSession() throws IOException {
        String jsessionid = getContentStringViaMiniBrowser("/demo/setSession");
        if(null != jsessionid)
            jsessionid = jsessionid.trim();
        HttpURLConnection conn = (HttpURLConnection) new URL(getTestURL("/demo/getSession")).openConnection();
        conn.setRequestProperty("Cookie", "JSESSIONID=" + jsessionid);
        conn.connect();
        String html = IoUtil.read(conn.getInputStream(), "utf-8");
        containAssert(html,"ChoPaoPao(session)");
    }

    @Test
    public void testClientJump() {
        String http_servlet = getHttpStringViaMiniBrowser("/demo/clientJump");
        containAssert(http_servlet, "HTTP/1.1 302 Found\nLocation: /hello");
    }

    @Test
    public void testServerJump() {
        String http_servlet = getContentStringViaMiniBrowser("/demo/serverJump");
        containAssert(http_servlet, "Hello JerryMouse from HelloServlet@demoWeb");
    }

    @Test
    public void testJumpWithAttribute() {
        String html = getContentStringViaMiniBrowser("/demo/serverJump");
        containAssert(html, "name=cpp-pdd");
    }

    @Test
    public void testWarDeployment() {
        String html = getContentStringViaMiniBrowser("/demoweb0/hello");
        containAssert(html,"Hello JerryMouse from HelloServlet@demoWeb");
    }


    private String getContentStringViaMiniBrowser(String uri) { return MiniBrowser.getContentString(getTestURL(uri)); }

    private String getContentStringViaMiniBrowser(String uri, Map params, boolean isGet) { return MiniBrowser.getContentString(getTestURL(uri), params, isGet); }

    private String getHttpStringViaMiniBrowser(String uri) { return MiniBrowser.getHttpString(getTestURL(uri)); }

    private byte[] getContentBytesViaMiniBrowser(String uri) { return MiniBrowser.getContentBytes(getTestURL(uri)); }

    private String getTestURL(String uri) { return StrUtil.format("http://{}:{}{}", ip,port,uri); }

    private void containAssert(String html, String keyword) { Assert.assertTrue(StrUtil.containsAny(html, keyword)); }

}