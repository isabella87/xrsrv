<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Enumeration" %>
<!doctype html>
<html>
<head>
<title>DEBUG</title>
<style type="text/css">
pre {
	font-family: "Consolas";
	font-size: 12pt;
	border: none;
	margin: 10px auto;
	padding: 20px;
	color: #333;
	background-color: #FFFFCC;
}
</style>
</head>
<body>
	<table>
		<caption>Request</caption>
		<tr>
			<th>Key</th>
			<th>Value</th>
		</tr>
		<tr>
			<td>Context path:</td>
			<td><%=request.getContextPath()%></td>
		</tr>
		<tr>
			<td>Auth type:</td>
			<td><%=request.getAuthType()%></td>
		</tr>
		<tr>
			<td>Request URI:</td>
			<td><%=request.getRequestURI()%></td>
		</tr>
		<tr>
			<td>Servlet path:</td>
			<td><%=request.getServletPath()%></td>
		</tr>
		<tr>
			<td>Path info:</td>
			<td><%=request.getPathInfo()%></td>
		</tr>
		<tr>
			<td>Path translated:</td>
			<td><%=request.getPathTranslated()%></td>
		</tr>
		<tr>
			<td>Remote host:</td>
			<td><%=request.getRemoteHost()%></td>
		</tr>
		<tr>
			<td>Remote addr:</td>
			<td><%=request.getRemoteAddr()%></td>
		</tr>
		<% final Enumeration<String> headerNames = request.getHeaderNames(); %>
		<% while (headerNames.hasMoreElements()) {
			final String headerName = headerNames.nextElement();
		%>
		<tr>
			<td>HTTP-HEADER[<%=headerName%>]</td>
			<td><%=request.getHeader(headerName)%></td>
		</tr>
		<%}%>
	</table>
</body>
</html>