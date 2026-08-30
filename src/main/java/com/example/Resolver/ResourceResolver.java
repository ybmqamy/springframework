package com.example.Resolver;

import com.example.Record.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

///仿写扫描包，根据class过滤出文件
/// 扫描包目前是硬编码拆解的字符串，效率一般

public class ResourceResolver {
    /// 指定的扫描包，指示我要对哪个包展开扫描
    String basePackage;
    /// 获取日志对象，用于记录日志
    Logger logger= LoggerFactory.getLogger(getClass());

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }
    /// 扫描包的核心方法,传入了一个映射函数，扫描出class文件
    public <R> List<R> scan(Function<Resource,R> mapper){
        //切分出包的路径,根据spring的接口，它传入一个com.xxx.xx格式的包，就可以扫描包下所有文件，为了扫描包下的文件，这里替换.为路径分隔符
        String basePath = this.basePackage.replace(".", "/");
        String path=basePath;
        List<R> C=new ArrayList<>();
        try{
            //扫描的逻辑
            List<R> Collector=new ArrayList<>();
            scan0(basePath,path,Collector,mapper);
            return Collector;
        }catch (Exception e){
            throw new RuntimeException("运行时出现了异常");
        }
    }
    /// 扫描的逻辑
    <R> void scan0(String basePackagePath, String path, List<R> collector, Function<Resource, R> mapper) throws IOException, URISyntaxException, IOException {
        logger.atDebug().log("scan path: {}", path);
        /// 构造器去整个磁盘目录寻找所有路径符合的并把它们打成一个枚举类返回
        Enumeration<URL> en = getloader().getResources(path);
        while (en.hasMoreElements()) {
            //不断的取到所有搜索到的路径
            URL url = en.nextElement();
            URI uri = url.toURI();
            //移除尾部的不合法字符
            String uriStr = removeTrailingSlash(uriToString(uri));
            /// 截取掉我传进来的包名，保证扫描到整个根目录，但靠长度硬性切割，比较脆弱
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());
            /// 扫描到的资源，既可以是file，也可以是jar包，两者的扫描方式不一样
            /// 但文件的开头都会标明是jar包还是file
            /// file: 是磁盘上真实存在的目录，可以直接遍历；jar: 是一个压缩归档，必须先"挂载"成虚拟文件系统才能遍历。
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5);
            }
            if (uriStr.startsWith("jar:")) {
                /// jar包是一个压缩的文件，需要先挂载到文件系统上，才可以进一步扫描
                scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), collector, mapper);
            } else {
                /// 如果是文件，截取到文件的根目录，直接通过一个path来指向该目录
                scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper);
            }
        }
    }
    /// 把jar包挂载到文件系统上
    Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
        return FileSystems.newFileSystem(jarUri, Map.of()).getPath(basePackagePath);
    }

    /// 拿到了根目录，直接遍历文件内部
    <R> void scanFile(boolean isjar,String uri,Path root,List<R> collector,Function<Resource,R> mapper) throws IOException {
        String baseDir=removeTrailingSlash(uri); //根目录
        /// 遍历path指向目录下的所有文件，为每个文件创建一个resource
        Files.walk(root).filter(Files::isRegularFile).forEach(file->{
            Resource res=null;
            if(isjar){
                res=new Resource(baseDir,removeLandingSlash(file.toString()));
            }else{
                String path = file.toString();
                /// 截取掉根目录的名字，得到干净的相对名
                String name = removeLandingSlash(path.substring(baseDir.length()));
                res = new Resource("file:" + path, name);
            }
            logger.info("正在创建资源"+res);
            R apply = mapper.apply(res);
            if(apply!=null){
                collector.add(apply);
            }
        });
    }


    /// 如果路径的尾部多余的文件分隔符，那么需要截取掉
    String removeTrailingSlash(String s){
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    String removeLandingSlash(String s){
        if (s.startsWith("/") || s.startsWith("\\")){
            s = s.substring(1);
        }
        return s;
    }



    /// 获取对应的classloader对象，class首先从当前线程的context中取得
    /// 其次从当下的包中 的class取得
    /// 有了classloader我们就可以指定classpath来获取对应的文件
    ClassLoader getloader(){
        logger.info("正在执行classloader");
        ClassLoader c1=null;
        c1=Thread.currentThread().getContextClassLoader();
        if(c1==null){
            c1=getClass().getClassLoader();
        }
        return c1;
    }

    String uriToString(URI uri){
        return URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
    }

    /// 总体实现的思路
    /// 1.获取到classloader类加载器，classloader的类加载器查找读取对应的字节码文件，读取对应的字节码文件，去除class，完成包的扫描
    /// 2.首先传入要扫描的包的路径，把.换成/
    /// 3.然后执行扫描，用loader中的get方法获得所有包含该路径得文件，打包成枚举类
    ///  4.遍历该枚举类，拿到每个待扫描文件得url，转换为便于操作得uri
    ///  5.截取资源得根目录，根据文件类型得不同传入不同得方法，文件直接遍历即可，jar包需要先创建文件系统再遍历
    /// 6.遍历对应得文件，为每个资源创建一个resource


}
