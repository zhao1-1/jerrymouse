
> 虽然tomcat自身就是一款轻量级的web容器，但是随着当下微服务的普及，原有tomcat的一些模块就没有存在的必要了。
> 因此打算改造一款尽量轻量化的tomcat，适应当前微服务的当下。


### 初步涉及的改动点如下：

+ 取消JSP模块；
+ 不再支持热加载（热部署）；
+ 只专注处理Servlet；
+ 不再支持静态资源；
+ 取消原来的Server -> Service -> Engine -> Host -> Context结构，缩减为：Server -> Context；
+ 不再支持多路径部署，强制配置，不过有默认配置（借鉴SpringBoot的思想，约定大于配置）；
+ 只支持POST请求，不再支持其他HTTP请求；