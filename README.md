## 案例:解决全站字符乱码问题

**写在前面:**更多关于过滤器的介绍以及案例请[点击这里前往我的博客](http://codingxiaxw.cn/2016/10/27/27-JavaWeb%E4%B8%89%E5%A4%A7%E7%BB%84%E4%BB%B6%E4%B9%8B%E8%BF%87%E6%BB%A4%E5%99%A8/)

一般我们通过jsp页面请求转发到servlet时，若请求方式为POST且请求参数包含中文参数时，我们需要在servlet的doPost()方法中设置POST请求编码问题:` request.setCharacterEncoding("utf-8");`、设置响应编码问题:`response.setContentType("text/html;charset=utf-8");`,这样便可以解决post请求即响应编码问题；而对于GET请求，若传递的请求参数包含中文参数时设置请求编码就比较麻烦，需要在servlet的doGet()方法中设置响应编码:`response.setContentType("text/html;charset=utf-8");`以及请求编码:`首先获得传递给servlet的请求参数:String username=request.getParameter("username")`假设传递的请求参数为`username`,然后再输入代码`username=new String(username.getBytes("ISO8859-1"),"utf-8");`，这样通过jsp页面转发到servlet的参数便解决了编码问题。即可以通过`response.getWrite().prinltn(username)`正常显示在网页上。  

试想:以后的开发中往往会用到很多的servlet，那我们岂不是要在每一个servlet的doPost()和doGet方法中都写上上述的解决编码代码？这时候我们就可以通过过滤器来解决了。  

首先附上页面:
```xml
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
  <head>
    <title>$Title$</title>
  </head>
  <body>
  <a href="<c:url value="/AServlet?username=张三"/> ">点击这里</a>

  <form action="<c:url value="/AServlet"/> " method="post">
    用户名:<input type="text" name="username" value="李四">
    <input type="submit" value="提交">
  </form>
  </body>
</html>
```

显示在网页上的界面为:![](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-16%20%E4%B8%8B%E5%8D%885.35.05.png)  

通过"点击这里"的链接我们便完成了通过jsp页面向servlet发送GET请求参数，通过"提交"按钮我们便完成了通过jsp页面向servlet发送POST请求参数。创建一个servlet,我们在servlet中完成响应参数编码的问题:
```java
public class AServlet extends HttpServlet {



    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");

    }
}
```
接下来在过滤器中完成请求参数编码的问题，创建一个过滤器Filter，在web.xml中注册:
```xml
 <filter>
        <filter-name>Filter</filter-name>
        <filter-class>filter.Filter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>Filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

Filter中编码为:
```java
public class Filter implements javax.servlet.Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

           }

    public void init(FilterConfig config) throws ServletException {

    }
}
```
对于POST请求参数的编码设置我们直接在doFilter()方法体中添加`  request.setCharacterEncoding("utf-8");`代码即可(此时运行程序，POST请求参数编码的问题成功解决)，对于GET请求参数的编码，有些同学会觉得直接在doFilter()方法体中添加
```java
String  username=request.getParameter("username");username=new String(username.getBytes("ISO-8859-1"),"utf-8");
```
即可。这样的参数是不太靠谱的，因为这里我们知道要传递的请求参数为username所以这里可以明了的指出，以后我们不知道请求参数为什么或者请求参数有很多时那就需要更多的上诉代码，所以这里我们采用装饰者模式对request进行装饰(即将本来的request换成我们自己写的request)，创建一个EncodingRequest.java继承HttpServletRequestWrapper,代码如下:
```java
public class EncodingRequest extends HttpServletRequestWrapper
{
    private HttpServletRequest req;

    public EncodingRequest(HttpServletRequest request)
    {
        super(request);
        this.req=request;
    }

    @Override
    public String getParameter(String name) {
        String value=req.getParameter(name);


        //处理编码问题
        try {
            value=new String(value.getBytes("ISO-8859-1"),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }
}
```
在构造方法中，我们传入系统的request,然后将这个request赋值给我们自己编写的req,然后在重写的getParameter()方法中通过我们自己写的req获取请求参数并解决编码问题，然后返回解决完编码后的参数value(此时这个中文参数已解决编码),然后在Filer中对我们自己编写的request(即Encodingquest对象)放行即可。现在doFilter()方法的方法体为:
```java
 public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        //处理post请求编码问题
        request.setCharacterEncoding("utf-8");

        HttpServletRequest req= (HttpServletRequest) request;
        /**
         * 处理get请求的编码问题
         */
//        String username=request.getParameter("username");
//        username=new String(username.getBytes("ISO-8859-1"),"utf-8");
        /**
         * 调包request
         * 1.写一个request的装饰类
         * 2.在放行时，使用我们自己的request
         */

            EncodingRequest er = new EncodingRequest(req);
            chain.doFilter(er, response);
}
```
运行程序，成功解决GET请求方式的编码问题，但是POST请求方式的编码又出现了问题，这是为什么呢？因为我们在doFilter方法中已经通过代码` request.setCharacterEncoding("utf-8");
`处理了POST请求方式的编码问题，但是此时的请求是系统的request对象而不是我们自己写的req，我们对req进行了放行而没有对request进行方式，所以方法体中应该增加if判断语句，改正后的doFilter()方法体内容为:
```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        //处理post请求编码问题
        request.setCharacterEncoding("utf-8");

        HttpServletRequest req= (HttpServletRequest) request;
        /**
         * 处理get请求的编码问题
         */
//        String username=request.getParameter("username");
//        username=new String(username.getBytes("ISO-8859-1"),"utf-8");

        /**
         * 调包request
         * 1.写一个request的装饰类
         * 2.在放行时，使用我们自己的request
         */
        if (req.getMethod().equals("GET")) {
            EncodingRequest er = new EncodingRequest(req);

            chain.doFilter(er, response);
        }else if (req.getMethod().equals("POST")){
            chain.doFilter(request, response);

        }
    }
```
此时运行程序，成功解决POST请求方式和GET请求方式的编码问题。在学习框架之前我们都这样通过Filter解决编码问题，而当我们学习了Spring MVC框架后我们处理POST请求参数的编码问题时直接在web.xml中添加如下配置而不用再写一个过滤器:
```xml
<filter>
<filter-name>CharacterEncodingFilter</filter-name>
<filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
<init-param>
<param-name>encoding</param-name>
<param-value>utf-8</param-value>
</init-param>
</filter>
<filter-mapping>
<filter-name>CharacterEncodingFilter</filter-name>
<url-pattern>/*</url-pattern>
</filter-mapping>
```

解决GET请求方式的编码问题时有两种解决方法:1.修改tomcat配置文件添加编码与工程编码一致，如下:
```xml
<Connector URIEncoding="utf-8" connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"/>
```
2.对参数进行重新编码:
```java
String userName new 
String(request.getParamter("userName").getBytes("ISO8859-1"),"utf-8")
```
第二种方法需要对每个参数都进行重新编码，比较麻烦。  

回归我们的过滤器讲解，通过如上包装request的方式便可以通过过滤器解决全站编码问题。

## 联系  

If you have some questions after you see this article,you can tell your doubts in the comments area or you can find some info by  clicking these links.

 
- [Blog@codingXiaxw's blog](codingxiaxw.cn)

- [Weibo@codingXiaxw](http://weibo.com/u/5023661572?from=hissimilar_home&refer_flag=1005050003_)

- [Zhihu@codingXiaxw](http://www.zhihu.com/people/e9f78fa34b8002652811ac348da3f671)  
- [Github@codingXiaxw](https://github.com/codingXiaxw)








