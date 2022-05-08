package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.classloader.WebappClassLoader;
import cn.zhaobin.jerrymouse.exception.WebConfigDuplicatedException;
import cn.zhaobin.jerrymouse.http.ApplicationContext;
import cn.zhaobin.jerrymouse.http.StandardServletConfig;
import cn.zhaobin.jerrymouse.util.parsexml.ContextXMLUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

public class Context {

    private final Host host;
    private String path;    // 访问的路径
    private String docBase; // 对应在文件系统中的绝对位置
    private ServletContext servletContext;

    private File contextWebXmlFile; // 为空，表示不存在，或无效的web.xml文件
    private Document contextWebXmlDocument;

    // 懒加载, 如果该项目（context）不含WEB-INF/web.xml文件，则不创建以下数据结构
    private Map<String, String> url_servletName;
    private Map<String, String> servletName_className;
    private Map<String, String> className_servletName;
    private Map<String, String> url_servletClassName;
    private Map<String, Map<String, String>> servletClassName_initParams;
    private List<String> loadOnStartupServletClassNames;

    private WebappClassLoader webappClassLoader;

    private Map<Class<?>, HttpServlet> servletPool;

    public Context(Host host, String path, String docBase) {
        this.host = host;
        this.path = path;
        this.docBase = docBase;
        this.servletContext = new ApplicationContext(this);
        deploy();
    }

    private void deploy() {
        TimeInterval timeInterval = DateUtil.timer();
        initServlet();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms",this.docBase,timeInterval.intervalMs());
    }

    private void initServlet() {
        this.contextWebXmlFile = new File(this.docBase, ContextXMLUtils.parseWatchedResource());
        if (!contextWebXmlFile.exists()) return;
        this.contextWebXmlDocument = Jsoup.parse(FileUtil.readUtf8String(this.contextWebXmlFile));
        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            this.contextWebXmlFile = null;
            return;
        }
        loadServletSource();
        parseWebServletXml();
        handleLoadOnStartup();
    }

    private void checkDuplicated() throws WebConfigDuplicatedException {
        checkDuplicated("servlet-mapping url-pattern", "url-pattern 重复,请保持其唯一性:{} ");
        checkDuplicated("servlet servlet-name", "servlet-name 重复,请保持其唯一性:{} ");
        checkDuplicated("servlet servlet-class", "servlet-class 重复,请保持其唯一性:{} ");
    }
    private void checkDuplicated(String mapping, String desc) throws WebConfigDuplicatedException {
        // 判断逻辑是放入一个集合，然后把集合排序之后看两临两个元素是否相同
        List<String> contents = new ArrayList<>();
        this.contextWebXmlDocument.select(mapping).forEach(ele -> contents.add(ele.text()));
        Collections.sort(contents);
        for (int i = 0; i < contents.size() - 1; i++) {
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);
            if (contentPre.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }
    }

    private void loadServletSource() {
        this.webappClassLoader = new WebappClassLoader(docBase, Thread.currentThread().getContextClassLoader());
        this.servletPool = new HashMap<>();

        this.url_servletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();
        this.url_servletClassName = new HashMap<>();

        this.servletClassName_initParams = new HashMap<>();

        this.loadOnStartupServletClassNames = new ArrayList<>();
    }

    private void parseWebServletXml() {
        parseServletMapping();
        parseServletInitParams();
        parseLoadOnStartup();
    }

    private void parseServletMapping() {
        buildUrl_servletName();
        buildServletName_className();
        buildClassName_servletName();
        buildUrl_servletClassName();
    }
    private void buildUrl_servletName() {
        this.contextWebXmlDocument.select("servlet-mapping").forEach(ele -> this.url_servletName.put(
                ele.select("url-pattern").text(),
                ele.select("servlet-name").first().text()));
    }
    private void buildServletName_className() {
        this.contextWebXmlDocument.select("servlet").forEach(ele -> this.servletName_className.put(
                ele.select("servlet-name").first().text(),
                ele.select("servlet-class").first().text()));
    }
    private void buildClassName_servletName() {
        this.contextWebXmlDocument.select("servlet").forEach(ele -> this.className_servletName.put(
                ele.select("servlet-class").first().text(),
                ele.select("servlet-name").first().text()));
    }
    private void buildUrl_servletClassName() {
        this.url_servletName.keySet().forEach(ele -> this.url_servletClassName.put(
                ele,
                this.servletName_className.get(this.url_servletName.get(ele))));
    }

    private void parseServletInitParams() {
        Elements servletClassNameElements = this.contextWebXmlDocument.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initElements = servletClassNameElement.parent().select("init-param");
            if (initElements.isEmpty())
                continue;
            Map<String, String> initParams = new HashMap<>();
            for (Element element : initElements) {
                initParams.put(
                        element.select("param-name").get(0).text(),
                        element.select("param-value").get(0).text());
            }
            servletClassName_initParams.put(servletClassName, initParams);
        }
    }

    public void parseLoadOnStartup() {
        this.contextWebXmlDocument.select("load-on-startup").forEach(ele ->
                this.loadOnStartupServletClassNames.add(ele.parent().select("servlet-class").text()));
    }

    public void handleLoadOnStartup() {
        if (!this.contextWebXmlFile.exists()) return;
        this.loadOnStartupServletClassNames.forEach(ele -> {
            try {
                getSingletonServlet(this.webappClassLoader.loadClass(ele));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
                e.printStackTrace();
            }
        });
    }

    public String getPath() { return path; }

    public String getDocBase() { return docBase; }

    public String getServletClassName(String uri) {
        if (null != this.url_servletClassName)
            return url_servletClassName.get(uri);
        return null;
    }

    public boolean servletClassValid(String uri) {
        if (!this.contextWebXmlFile.exists()) return false;
        if (null == this.url_servletClassName.get(uri)) return false;
        return true;
    }

    public WebappClassLoader getWebappClassLoader() { return webappClassLoader; }

    public ServletContext getServletContext() { return servletContext; }

    public synchronized  HttpServlet getSingletonServlet(Class<?> clazz)
            throws InstantiationException, IllegalAccessException, ServletException {
        if (!this.contextWebXmlFile.exists()) throw new IllegalAccessException("can not call this method");
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();
            servlet.init(new StandardServletConfig(
                    this.getServletContext(),
                    className_servletName.get(clazz.getName()),
                    servletClassName_initParams.get(clazz.getName())));
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    public void stop() {
        webappClassLoader.stop();
        destroyServlets();
    }

    private void destroyServlets() { servletPool.values().forEach(Servlet::destroy); }
}
