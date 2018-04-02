<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<!doctype html>
<html>
<head>
<title>跳转至银联</title>
<style type="text/css">
.jumping {
	font-family: "新宋体,Consolas";
	font-size: 12pt;
	text-align: center;
	background-image: url(../web/images/pre-loading.gif);
	background-repeat: no-repeat;
	background-position: 50% 48px;
	border: none;
	margin: 20px auto;
	padding: 20px 20px 120px 20px;
	color: #333;
	background-color: transparent;
	width: 320px;
}

.error {
	font-family: "新宋体,Consolas";
	font-size: 12pt;
	border: none;
	text-align: center;
	margin: 20px auto;
	padding: 20px 20px 20px 20px;
	color: #f00;
	background-color: transparent;
	width: 320px;
}
</style>
</head>
<body>
	<%
		final Log logger = LogFactory.getLog("chinapay-jump-jsp");
		final Object viewData = request.getAttribute("__view_data__");
			final Map<?, ?> formData = viewData instanceof Map ? (Map<?, ?>) viewData : null;
			final Object formAction = formData == null ? null : formData.get("FormAction");
	%>
	<%
		if (formData != null && formAction != null) {
			StringBuilder sb = new StringBuilder("======= CHINAPAY FORM =======\n");
			sb.append("FormAction=" + formAction);
			sb.append("\n");
	%>
	<div class="jumping">&nbsp;正在跳转...</div>
	<form name="form1" id="form1" action="<%=formAction%>"  method="POST">
		<%
			for (Map.Entry<?, ?> entry : formData.entrySet()) {
				// 跳过FormAction域
				if ("FormAction".equals(entry.getKey())) {
					continue;
				}
				sb.append(entry.getKey() + "=" + entry.getValue());
				sb.append("\n");
		%>
		<input type="hidden" name="<%=entry.getKey()%>"
			value="<%=entry.getValue()%>" />
		<%
			} // end of for
			sb.append("============ END ============");
			logger.debug(sb);
		%>
		<%
			} else {
			final Object error = formData == null ? null : formData.get("error");
			String errorStr = error == null ? "" : error.toString();
			final int spos = errorStr.indexOf('*');
			if (spos != -1) {
				errorStr = errorStr.substring(spos + 1);
			}
		%>
		
		<script type="text/javascript">
			if(typeof myObject !='undefined'){
				myObject.jumpError('<%=errorStr.isEmpty() ? "数据错误" : errorStr%>');
			}
		</script>
		
		<%@ include file="ios-webview.jsp"%>

		<div class="error"><%=errorStr.isEmpty() ? "数据错误" : errorStr%></div>
		<%
			} // end of else
		%>
	</form>
	<script type="text/javascript">
		"use strict";
		window.onload = function() {
			var form1 = document.getElementById("form1");
			if (form1) {
				form1.submit();
			}
		}; 
	</script>
</body>
</html>