<%@ page isErrorPage="true" %>
<%@ page import = "java.util.Date" %>
<%
final String requestURL = request.getRequestURL().toString();
final String requestURI = request.getRequestURI();
final int index = requestURL.indexOf(requestURI);
final String retryURL = requestURL.substring(0, index) +
                     request.getAttribute("javax.servlet.error.request_uri");
%>
<HTML>
    <BODY>
        <H1>Error Page</H1>
        A program error occurred on <%= new Date() %> at <%= request.getServerName() %>.<p />
        <a href="<%=retryURL%>">Click here to retry this operation.</a><br><br>
        <strong>javax.servlet.error.request_uri: </strong>
        <%= request.getAttribute("javax.servlet.error.request_uri") %>
        <br>
        <strong>javax.servlet.error.exception: </strong>
          <%= request.getAttribute("javax.servlet.error.exception")%>
        <br>
        <strong>javax.servlet.error.status_code: </strong>
          <%= request.getAttribute("javax.servlet.error.status_code")%>
        <br>
        <strong>javax.servlet.error.servlet_name: </strong>
          <%= request.getAttribute("javax.servlet.error.servlet_name")%>
        <br>
        <br>
        <!--
        <STRONG> Dettagli: </STRONG>
        <br></br>
        <c:set var="exception" value="${requestScope['javax.servlet.error.exception']}"/>
        <!-- Stack trace -->
        <jsp:scriptlet>
          exception.printStackTrace(new java.io.PrintWriter(out));
        </jsp:scriptlet>
    </BODY>
</HTML>