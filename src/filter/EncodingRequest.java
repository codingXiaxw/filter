package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Created by codingBoy on 16/11/16.
 * 装饰request
 */


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
