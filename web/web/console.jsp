<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.lang.management.ManagementFactory" %>
<%@ page import="java.lang.management.ThreadMXBean" %>
<%@ page import="java.lang.management.ThreadInfo" %>
<!doctype html>
<html>
<head>
<meta charset="utf-8" >
<meta name="keywords" content="监控服务器,禁止外网访问" >
<style type="text/css">
body {
  font-family: Verdana, sans-serif;
  font-size: 12px;
  color: #484848;
  margin: 0;
  padding: 0;
  min-width: 1024px;
}
.table {
  border: 1px solid #e4e4e4;
  border-collapse: collapse;
  width: 800px;
  margin: 4px auto;
}
.table caption {
  font-size: 1.2em;
  font-weight: bold;
  padding: 10px;
}
.table th {
  border-width: 1px;
  border-style: solid;
  border-top-color: #d7d7d7;
  border-right-color: #d7d7d7;
  border-left-color: #d7d7d7;
  border-bottom-color: #999999;
}
.table td {
  text-align: center;
  vertical-align: top;
  padding-right: 10px;
  border: solid 1px #d7d7d7;
}
.list {
  text-align: left;
  font-family: Consolas;
  font-size: 10.5px;
}
.code {
  text-align: left;
  font-family: Consolas;
  font-size: 10.5px;
}
.foot {
  padding: 10px 0;
  width: 800px;
  margin: 4px auto;
}
</style>
</head>
<body>
<%!
    final Map cpuTimes = new HashMap();
    final Map cpuTimeFetch = new HashMap();
%><%
final long cpus = Runtime.getRuntime().availableProcessors();
final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
final long now = System.currentTimeMillis();
final ThreadInfo[] t = threads.dumpAllThreads(false, false);
%>
<table class="table">
<caption>Dumped threads</caption>
<tr>
    <th>Id</th>
    <th>Id(HEX)</th>
    <th>Name</th>
    <th>CPU Percent</th>
    <th>Total time</th>
    <th>Details</th>
</tr>
<%
String oStTId = request.getParameter("st_t_id");
long stTId = -1L;
if (oStTId != null) {
    try {
        stTId = Long.parseLong(oStTId);
    } catch (Exception e) {
    }
}
for (int i = 0; i < t.length; ++i) {
    final long id = Long.valueOf(t[i].getThreadId());
    long current = 0;
    if (cpuTimes.get(id) != null) {
        long prev = ((Number) cpuTimes.get(id)).longValue();
        current = threads.getThreadCpuTime(t[i].getThreadId());
        long catchTime = ((Long) cpuTimeFetch.get(id)).longValue();
        double percent = (current - prev) / ((now - catchTime) * cpus * 10000);
        percent = percent > 100 ? 100 : percent < 0 ? 0 : percent;
        final int clPercent = (int) (percent * 5);
        final int percentGreen = clPercent < 50 ? 255 : (255 * (100 - clPercent) / 50);
        final int percentRed = clPercent > 50 ? 255 : (255 * clPercent / 50);
        final String percentColor = "rgb(" + percentRed + ", " + percentGreen + ", " + 0 + ")";

        final long NONOS_PER_SECOND = 1000000000L;
        final int totalSecond = (int) (current / NONOS_PER_SECOND);
        final int hours = totalSecond / 3600;
        final int minitues = (totalSecond % 3600) / 60;
        final int seconds = (totalSecond % 60) / 60;
        // if (percent > 0 && prev > 0) {
%>
    <tr>
        <td><%=id%></td>
        <td><%=Long.toHexString(id).toUpperCase()%></td>
        <td><%=t[i].getThreadName()%></td>
        <td style="color: <%=percentColor%>; background-color: #000"><%=percent%>%</td>
        <td>
            <%if (hours > 24) {%>&gt; 1d<%} else {%>
                <%=hours%>h<%=minitues%>m<%=seconds%>s
            <%}%>
        </td>
        <td>
            <a href="console.html?st_t_id=<%=id%>">[ + ]</a>
        </td>
    </tr>
    <% if (stTId == id) {
        %>
    <tr>
        <td colspan="6" >
        <ul class="list">
            <li>State: <%=t[i].getThreadState()%></li>
            <li>Lock info: <%=String.valueOf(t[i].getLockInfo())%></li>
        </ul>
        <pre class="code">
            <% for (final StackTraceElement ele : t[i].getStackTrace()) { %>
            <%=ele.toString()%>
            <% } %>
        </pre>
        </td>
    </tr>
        <%
    }
%>
<%
        // }
    }
    cpuTimes.put(id, new Long(current));
    cpuTimeFetch.put(id, new Long(now));
}
%>
</tbody>
</table>
<p class="foot"><%=t.length%> threads in total.</p>
</body>