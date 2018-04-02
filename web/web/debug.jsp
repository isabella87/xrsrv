<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="org.json.JSONArray"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>
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
	<%
		final Object obj = JSONObject.wrap(request.getAttribute("__view_data__"));
		final String s;
		if (obj instanceof JSONObject) {
			s = ((JSONObject) obj).toString(2);
		} else if (obj instanceof JSONArray) {
			s = ((JSONArray) obj).toString(2);
		} else {
			s = String.valueOf(obj);
		}
	%>
	<pre>
<%=StringEscapeUtils.escapeHtml4(s)%>
	</pre>
</body>
</html>