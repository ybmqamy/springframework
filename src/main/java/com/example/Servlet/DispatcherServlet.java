package com.example.Servlet;

import com.example.Annotation.Controller;
import com.example.Annotation.GetMapping;
import com.example.Annotation.PostMapping;
import com.example.Annotation.RestController;
import com.example.Bean.BeanDefinition;
import com.example.Dispatcher.Dispatcher;
import com.example.Dispatcher.Result;
import com.example.Marker.FreeMarkerViewResolver;
import com.example.Resolver.PropertyResolver;
import com.example.Utils.JsonUtils;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.context.ApplicationContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/// 所有的mvc转发规则都在这里定义，哪个url交给哪个controller，都是在这里定义的
/// 它需要从ioc中找到对应的controller和restcontroller完成消息的转发
/// 为了完成这一步，我们需要定义一个转发类，用来装载所有的转发方法
///
/// 由于在处理的每一步都可以向HttpServletResponse写入响应，
/// 因此，后续步骤写入时，应判断前面的步骤是否已经写入并发送了HTTP Header。isCommitted()方法就是干这个用的：
public class DispatcherServlet extends HttpServlet {

    final Logger logger = LoggerFactory.getLogger(getClass());
    List<Dispatcher> getDispatcher = new ArrayList<>();
    List<Dispatcher> postDispatcher = new ArrayList<>();

    ApplicationContext ApplicationContext;
    PropertyResolver propertyResolver;
    /// 图标和static静态页面资源
    String faviconPath;
    String resourcePath;
    FreeMarkerViewResolver viewResolver;

    public DispatcherServlet(AnnotationConfigApplicationContext applicationContext, PropertyResolver propertyResolver) {
        this.ApplicationContext = applicationContext;
        this.propertyResolver = propertyResolver;
    }

    /// 找到所有的controller
    @Override
    public void init() throws ServletException {
        for (BeanDefinition beanDefinition : this.ApplicationContext.findBeanDefinitions(Object.class)) {
            Class<?> aClass = beanDefinition.getAClass();
            Object instance = beanDefinition.getInstance();
            Controller controller = aClass.getAnnotation(Controller.class);
            RestController restController = aClass.getAnnotation(RestController.class);
            if (controller != null && restController != null) {
                throw new ServletException("Found @Controller and @RestController ,a class cant has dual controller ");
            }
            if (controller != null) {
                addcontroller(false, beanDefinition.getName(), instance);
            }
            if (restController != null) {
                addcontroller(true, beanDefinition.getName(), instance);
            }
        }

    }

    @Override
    public void destroy() {
        this.ApplicationContext.close();
    }

    private void addcontroller(boolean ifrest, String beanname, Object bean) throws ServletException {
        logger.info("已找到" + beanname + bean);
        addmethod(ifrest, bean, bean.getClass());
    }

    /// 把对应的control加入到dispatcher
    private void addmethod(boolean ifrest, Object bean, Class aClass) throws ServletException {
        for (Method declaredMethod : aClass.getDeclaredMethods()) {
            GetMapping GET = declaredMethod.getAnnotation(GetMapping.class);
            /// get不为空，说明是一个需要接受请求的方法
            if (GET != null) {
                checkMethod(declaredMethod);
                this.getDispatcher.add(new Dispatcher("GET", ifrest, bean, declaredMethod, GET.value()));
            }
            PostMapping POST = declaredMethod.getAnnotation(PostMapping.class);
            if (POST != null) {
                checkMethod(declaredMethod);
                this.postDispatcher.add((new Dispatcher("POST", ifrest, bean, declaredMethod, POST.value())));
            }
        }
        /// todo子类实例本来就是父类实例（is-a），反射调用父类方法完全没问题
        ///子类实例就是一个（更具体的）父类实例，他们都是同一个实例
        /// 如果父类还有get和post方法要给它加进去
        Class superclass = aClass.getSuperclass();
        if (superclass != null) {
            addmethod(ifrest, bean, superclass);
        }
    }

    void checkMethod(Method m) throws ServletException {
        int mod = m.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new ServletException("Cannot do URL mapping to static method: " + m);
        }
        m.setAccessible(true);
    }

    /// 处理get的请求
    /// 需要根据请求选择返回一个页面静态资源还是一组json的数据
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /// process处理直接得到结果集，
        String url = req.getRequestURI();
        if ((this.faviconPath != null && url.equals(this.faviconPath))
                || (this.resourcePath != null && url.startsWith(this.resourcePath))) {
            doResource(url, req, resp);
        } else {
            try {
                doService(req, resp, this.getDispatcher);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// 资源请求解析
    ///
    /// @return
    private Result doResource(String url, HttpServletRequest req, HttpServletResponse resp) {
        ServletContext servletContext = req.getServletContext();
        try (InputStream ipm = servletContext.getResourceAsStream(url)) {
            if (ipm == null) {
                return new Result(false, 404, "你是傻逼吗");
            } else {
                String file = url;
                int n = url.lastIndexOf('/');
                /// 截取斜杠的后面一个纯文件名
                if (n >= 0) {
                    file = url.substring(n + 1);
                }
                /// 取得文件的mime，提取消息描述符，告诉浏览器怎么解析
                /// mime这个东西计算机网络学过，它最开始是属于stmp做电子邮件解析的，负责把非ASCII码的转换为ASCII
                /// http沿用了这一标准，指定文件解析和读取的格式
                String mime = servletContext.getMimeType(file);
                if (mime == null) {
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime); ///设置文件解析的类型
                ServletOutputStream output = resp.getOutputStream();
                ipm.transferTo(output);  ///转换为输出流，把整合的信息写进输出流
                output.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /// 处理post的请求
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            doService(request, response, this.postDispatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// 统一处理转发请求
    /// REST类型写入JSON序列化结果.isrest判断是否是rest风格的接口
    protected void doService(HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws Exception {
        for (Dispatcher dispatcher : dispatchers) {
            String url = req.getRequestURI();
            Result result = dispatcher.process(url, req, resp);
            if (result != null && result.success) {   ///如果成功返回
                Object data = result.data;
                if (dispatcher.isRest) { /// rest风格就是json么
                    /// 如果是rest风格，需要写入json
                    /// 防止覆盖已经发出的响应头
                    if (!resp.isCommitted()) {
                        resp.setContentType("application/json");
                    }
                } ///如果需要请求体
                if (dispatcher.isResponseBody) {
                    if (data instanceof String s) { /// 类似条件转移语句，如果是这个类型同时就完成转换
                        PrintWriter writer = resp.getWriter();
                        writer.write(s);
                        writer.flush();
                    } else if (data instanceof byte[] bytes) {
                        ServletOutputStream outputStream = resp.getOutputStream();
                        outputStream.write(bytes);
                        outputStream.flush();
                    } else {
                        throw new RuntimeException("请求体异常");
                    }       ///printerwriter字符输出流
                }else if(dispatcher.isVoid){ /// 是否返回空
                    PrintWriter writer = resp.getWriter();
                    JsonUtils.writeJson(writer, data);
                    writer.flush();
                }
                else {
                    // process MVC:
                    if (!resp.isCommitted()) {
                        resp.setContentType("text/html");
                    }  ///string和byte直接返回的就是响应体的，不需要渲染什么的
                    if (data instanceof String s) {
                        if (dispatcher.isResponseBody) {
                            // send as response body:
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (s.startsWith("redirect:")) {
                            /// 如果需要重定向，那就重定向到新的url
                            resp.sendRedirect(s.substring(9));
                        } else {
                            // error:
                            throw new ServletException("Unable to process String result when handle url: " + url);
                        }
                    } else if (data instanceof byte[] bytes) {
                        if (dispatcher.isResponseBody) {
                            // send as response body:
                            ServletOutputStream output = resp.getOutputStream();
                            output.write(bytes);
                            output.flush();
                        } else {
                            // error:
                            throw new ServletException("Unable to process byte[] result when handle url: " + url);
                        }               ///model和view原本是spring原生的类，用于返回视图和模型，便于页面的渲染
                    } else if (data instanceof ModelAndView mv) {
                        ///只给模板和数据，html是现场渲染的
                        String view = mv.getViewName();
                        if (view.startsWith("redirect:")) {
                            /// 如果是重定向的，就转发到重定向的地址
                            resp.sendRedirect(view.substring(9));
                        } else {  /// 否则渲染html页面
                            this.viewResolver.render(view, mv.getModel(), req, resp);
                        }
                    } else if (!dispatcher.isVoid && data != null) {
                        // error:
                        throw new ServletException("Unable to process " + data.getClass().getName() + " result when handle url: " + url);
                    }
                }
                return;
            }
        }
        resp.sendError(404, "Not Found");
    }
    /// 这里有一个很有意思的问题，明明可以直接现在渲染好要啥返回啥，那为啥spring还要搞视图这一出
    /// 把数据和展示分开，不必每次改样式就重写java，同一个数据不同的展示格式
    /// 这在resst风格之前是很常见的，但现在有了rest，很多时候直接返回json即可，而不必再使用视图对象
    /// 只有在需要模板渲染 HTML 页面时它才有价值。你代码里保留 ModelAndView 分支，就是为了兼容「返回视图渲染
    ///   HTML」这种场景，跟 REST 返回 JSON 是两条并行的路。


}
