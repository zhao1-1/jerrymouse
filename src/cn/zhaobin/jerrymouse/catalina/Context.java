package cn.zhaobin.jerrymouse.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.zhaobin.jerrymouse.classloader.WebAppClassLoader;
import cn.zhaobin.jerrymouse.exception.WebConfigDuplicatedException;
import cn.zhaobin.jerrymouse.http.ApplicationContext;
import cn.zhaobin.jerrymouse.http.StandardFilterConfig;
import cn.zhaobin.jerrymouse.http.StandardServletConfig;
import cn.zhaobin.jerrymouse.util.parsexml.ContextXMLUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

public class Context {

    private final Host host;
    private String path;    // 访问的路径
    private String docBase; // 对应在文件系统中的绝对位置
    private ServletContext servletContext;

    private File contextWebXmlFile; // 为空，表示不存在，或无效的web.xml文件，也用于判断该Context是否仅仅包含静态文件
    private Document contextWebXmlDocument;

    /*
    ================================================================
    懒加载, 如果该项目（context）不含WEB-INF/web.xml文件，则不创建以下数据结构
    ================================================================
    */
    private WebAppClassLoader webAppClassLoader;    // 类加载器

    // servlet配置
    private Map<String, String> url_servletName;
    private Map<String, String> servletName_className;
    private Map<String, String> className_servletName;
    private Map<String, String> url_servletClassName;
    private Map<String, Map<String, String>> servletClassName_initParams;
    private List<String> loadOnStartupServletClassNames;

    // Filter配置
    private Map<String, List<String>> url_filterNames;
    private Map<String, String> filterName_className;
    private Map<String, String> className_filterName;
    private Map<String, List<String>> url_filterClassNames;
    private Map<String, Map<String, String>> filterClassName_initParams;

    // 单例（Servlet、Filter）
    private Map<Class<?>, HttpServlet> servletPool;
    private Map<String, Filter> filterPool;

    // Listener配置
    private List<ServletContextListener> listenerPool;

    public Context(Host host, String path, String docBase) {
        this.host = host;
        this.path = path;
        this.docBase = docBase;
        this.servletContext = new ApplicationContext(this);
        deploy();
    }

    private void deploy() {
        TimeInterval timeInterval = DateUtil.timer();
        this.contextWebXmlFile = new File(this.docBase, ContextXMLUtils.parseWatchedResource());
        if (!contextWebXmlFile.exists())
            return;
        this.contextWebXmlDocument = Jsoup.parse(FileUtil.readUtf8String(this.contextWebXmlFile));
        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            this.contextWebXmlFile = null;
            return;
        }
        init();
        fireEvent("init");
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms",this.docBase,timeInterval.intervalMs());
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

    private void init() {
        // create classLoader
        this.webAppClassLoader = new WebAppClassLoader(docBase, Thread.currentThread().getContextClassLoader());

        // init servlet
        loadServletSource();
        parseWebXmlServlet();
        handleLoadOnServlet();

        // init filter
        loadFilterSource();
        parseWebXmlFilter();
        handleLoadOnFilter();

        // init listener
        loadListenerSource();
        parseListenerMappingAndLoadOn();
    }




    private void loadServletSource() {
        this.servletPool = new HashMap<>();

        this.url_servletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();
        this.url_servletClassName = new HashMap<>();

        this.servletClassName_initParams = new HashMap<>();

        this.loadOnStartupServletClassNames = new ArrayList<>();
    }
    private void loadFilterSource() {
        this.filterPool = new HashMap<>();

        this.url_filterNames = new HashMap<>();
        this.filterName_className = new HashMap<>();
        this.className_filterName = new HashMap<>();
        this.url_filterClassNames = new HashMap<>();

        this.filterClassName_initParams = new HashMap<>();
    }
    private void loadListenerSource() {
        this.listenerPool = new ArrayList<>();
    }

    private void parseWebXmlServlet() {
        parseServletMapping();
        parseServletInitParams();
        parseLoadOnStartup();
    }
    private void parseWebXmlFilter() {
        parseFilterMapping();
        parseFilterInitParams();
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

    private void parseFilterMapping() {
        buildUrl_filterNames();
        buildFilterName_ClassName();
        buildClassName_filterName();
        buildUrl_FilterClassNames();
    }
    private void buildUrl_filterNames() {
        this.contextWebXmlDocument.select("filter-mapping").forEach(ele -> {
            String urlPattern = ele.select("url-pattern").text();
            String filterName = ele.select("filter-name").first().text();

            List<String> filterNames = this.url_filterNames.get(urlPattern);
            if(null == filterNames) {
                filterNames = new ArrayList<>();
                this.url_filterNames.put(urlPattern, filterNames);
            }
            filterNames.add(filterName);
        });
    }
    private void buildFilterName_ClassName() {
        this.contextWebXmlDocument.select("filter").forEach(ele -> this.filterName_className.put(
                ele.select("filter-name").text(),
                ele.select("filter-class").first().text()));
    }
    private void buildClassName_filterName() {
        this.contextWebXmlDocument.select("filter").forEach(ele -> this.className_filterName.put(
                ele.select("filter-class").first().text(),
                ele.select("filter-name").text()));
    }
    private void buildUrl_FilterClassNames() {
        Set<String> urls = this.url_filterNames.keySet();
        for (String url : urls) {
            List<String> filterNames = this.url_filterNames.get(url);
            if(null == filterNames) {
                filterNames = new ArrayList<>();
                this.url_filterNames.put(url, filterNames);
            }
            for (String filterName : filterNames) {
                String filterClassName = this.filterName_className.get(filterName);
                List<String> filterClassNames = this.url_filterClassNames.get(url);
                if(null == filterClassNames) {
                    filterClassNames = new ArrayList<>();
                    url_filterClassNames.put(url, filterClassNames);
                }
                filterClassNames.add(filterClassName);
            }
        }
    }

    private void parseFilterInitParams() {
        this.contextWebXmlDocument.select("filter-class").forEach(ele -> {
            Elements initElements = ele.parent().select("init-param");
            if (!initElements.isEmpty()) {
                Map<String, String> initParams = new HashMap<>();
                for (Element element : initElements) {
                    initParams.put(
                            element.select("param-name").get(0).text(),
                            element.select("param-value").get(0).text());
                }
                filterClassName_initParams.put(ele.text(), initParams);
            }
        });
    }

    private void parseLoadOnStartup() {
        this.contextWebXmlDocument.select("load-on-startup").forEach(ele ->
                this.loadOnStartupServletClassNames.add(ele.parent().select("servlet-class").text()));
    }

    private void handleLoadOnServlet() {
        this.loadOnStartupServletClassNames.forEach(ele -> {
            try {
                getSingletonServlet(this.webAppClassLoader.loadClass(ele));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
                e.printStackTrace();
            }
        });
    }
    private void handleLoadOnFilter() {
        className_filterName.keySet().forEach(ele -> {
            try {
                getSingletonFilter(this.webAppClassLoader.loadClass(ele));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public synchronized  HttpServlet getSingletonServlet(Class<?> clazz)
            throws InstantiationException, IllegalAccessException, ServletException {
        if (!this.isValidServletContext()) throw new IllegalAccessException("can not call this method");
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();
            servlet.init(new StandardServletConfig(
                    this.servletContext,
                    className_servletName.get(clazz.getName()),
                    servletClassName_initParams.get(clazz.getName())));
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }
    private synchronized Filter getSingletonFilter(Class<?> clazz)
            throws Exception {
        Filter filter = filterPool.get(clazz);
        if(null == filter) {
            filter = (Filter) ReflectUtil.newInstance(clazz);
            filter.init(new StandardFilterConfig(
                    this.servletContext,
                    className_filterName.get(clazz.getName()),
                    filterClassName_initParams.get(clazz.getName())));
            filterPool.put(clazz.getName(), filter);
        }
        return filter;
    }

    private void parseListenerMappingAndLoadOn() {
        this.contextWebXmlDocument.select("listener").forEach(ele -> {
            try {
                this.listenerPool.add((ServletContextListener) this.getWebAppClassLoader().loadClass(ele.text()).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isValidServletContext() { return this.contextWebXmlFile.exists(); }




    public String getPath() { return path; }

    public String getDocBase() { return docBase; }

    public String getServletClassName(String uri) {
        if (!this.isValidServletContext())
            return null;
        return url_servletClassName.get(uri);
    }

    public boolean servletClassValid(String uri) {
        if (!this.isValidServletContext()) return false;
        if (null == this.url_servletClassName.get(uri)) return false;
        return true;
    }

    public WebAppClassLoader getWebAppClassLoader() { return this.webAppClassLoader; }

    public ServletContext getServletContext() { return servletContext; }

    public void stop() {
        webAppClassLoader.stop();
        destroyServlets();
        fireEvent("destroy");
    }
    private void destroyServlets() { servletPool.values().forEach(Servlet::destroy); }

    public List<Filter> getMatchedFilters(String uri) {
        if (!this.isValidServletContext())
            return Collections.emptyList();
        Set<String> matchedUrl = new HashSet<>();
        url_filterNames.keySet().forEach(ele -> {
            if(match(ele, uri)) {
                matchedUrl.add(ele);
            }
        });

        Set<String> matchedFilterClassNames = new HashSet<>();
        matchedUrl.forEach(ele -> matchedFilterClassNames.addAll(this.url_filterClassNames.get(ele)));

        List<Filter> matchedFilters = new ArrayList<>();
        matchedFilterClassNames.forEach(ele -> matchedFilters.add(this.filterPool.get(ele)));

        return matchedFilters;
    }
    private boolean match(String pattern, String uri) {
        // 完全匹配
        if(StrUtil.equals(pattern, uri))
            return true;
        // /* 模式
        if(StrUtil.equals(pattern, "/*"))
            return true;
        // 后缀名 /*.jsp
        if(StrUtil.startWith(pattern, "/*.")) {
            String patternExtName = StrUtil.subAfter(pattern, '.', false);
            String uriExtName = StrUtil.subAfter(uri, '.', false);
            if(StrUtil.equals(patternExtName, uriExtName))
                return true;
        }
        // 其他模式就懒得管了
        return false;
    }

    private void fireEvent(String type) {
        ServletContextEvent event = new ServletContextEvent(this.servletContext);
        for (ServletContextListener servletContextListener : this.listenerPool) {
            if("init".equals(type))
                servletContextListener.contextInitialized(event);
            if("destroy".equals(type))
                servletContextListener.contextDestroyed(event);
        }
    }
}
