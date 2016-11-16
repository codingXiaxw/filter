package filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by codingBoy on 16/11/16.
 */
public class Filter implements javax.servlet.Filter {
    public void destroy() {
    }

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

    public void init(FilterConfig config) throws ServletException {

    }

}
