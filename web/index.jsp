<%--
  Created by IntelliJ IDEA.
  User: codingBoy
  Date: 16/11/16
  Time: 下午4:44
  To change this template use File | Settings | File Templates.
--%>
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
