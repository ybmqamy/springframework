package com.example.Marker;

import com.example.Resolver.ViewResolver;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.Map;
import java.util.Objects;

///freemarker是spring原生的html渲染引擎,我们需要它解析html
/// 把文档的不变部分做成模板，把可变部分用占位符代替，然后free讲两者结合，生成最终的模板
/// 解析html的页面对象，最终返回一个html的网页
public class FreeMarkerViewResolver implements ViewResolver {

    final String templatePath;   ///指定的资源根路径
    final String templateEncoding; ///指定编码格式
    ///它代表整个当前 Web 应用。每个 Web 应用在启动时，Servlet 容器（如 Tomcat）都会为它创建一个唯一的 ServletContext 对象，应用中的所有 Servlet、JSP、过滤器、监听器都共享这一个对象
    ServletContext servletContext;
    Configuration configuration;
    /// 使用spring自带的freemarker的html渲染器
    /// 这里需要注入servletcontext，需要把servelt注册
    public FreeMarkerViewResolver(ServletContext servletContext, String templatePath, String templateEncoding) {
        this.servletContext = servletContext;
        this.templatePath = templatePath;
        this.templateEncoding = templateEncoding;
    }


    @Override
    public void init() {
        Configuration cfg=new Configuration(Configuration.VERSION_2_3_0);
        cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);  ///设置输出对象
        cfg.setDefaultEncoding(templateEncoding); ///设置编码格式
        cfg.setTemplateLoader(new ServletTemplateLoader(this.servletContext, this.templatePath));
        ///装配servletloader读取资源，free的配置类就会在指定的目录读取所有需要的资源
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setAutoEscapingPolicy(Configuration.ENABLE_IF_SUPPORTED_AUTO_ESCAPING_POLICY);
        cfg.setLocalizedLookup(false);
        var ow = new DefaultObjectWrapper(Configuration.VERSION_2_3_32);
        ow.setExposeFields(true);
        cfg.setObjectWrapper(ow);
        this.configuration = cfg;
    }

    @Override
    public void render(String viewName, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Template template=null;
        Template template1 = configuration.getTemplate(viewName);
        PrintWriter writer = resp.getWriter();
        /// 写回一个html的页面
        try {
            template1.process(model,writer);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
        writer.flush();
    }
}

class ServletTemplateLoader implements TemplateLoader {

    private final ServletContext servletContext;
    private final String subdirPath;
    /// 传入资源所在根路径
    ServletTemplateLoader(ServletContext servletContext, String subdirPath) {
        Objects.requireNonNull(servletContext);
        Objects.requireNonNull(subdirPath);
        /// 把根路径设置为合法的路径
        subdirPath = subdirPath.replace('\\', '/');
        if (!subdirPath.endsWith("/")) {
            subdirPath += "/";
        }
        if (!subdirPath.startsWith("/")) {
            subdirPath = "/" + subdirPath;
        }
        this.subdirPath = subdirPath;
        this.servletContext = servletContext;
    }


    /// 根据完整的路径找到对应的资源找到对应的资源
    @Override
    public Object findTemplateSource(String name) throws IOException {
        /// 拼接名字得到完整的路径，读取对应的文件
        String fullname = subdirPath + name;
        String realPath = servletContext.getRealPath(fullname);
        try {
            if (realPath!=null){
                File file = new File(realPath);
                if (file.canRead() && file.isFile()) {
                    return file;
                }
            }
        }catch (Exception e){
            throw new IOException("读取静态资源文件异常");
        }
        return null;
    }
    /// 这tm是获取最后修改时间
    @Override
    public long getLastModified(Object o) {
        if(o instanceof File){
            return ((File)o).lastModified();
        }
        return 0;
    }

    @Override
    public Reader getReader(Object template, String encoding) throws IOException {
        if(template instanceof File){
            return new InputStreamReader(new FileInputStream((File) template), encoding);
        }
        return null;
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {

    }
}