<html>
<%
String location=null;

	location="error.do?" + request.getQueryString();

%>
<head><meta http-equiv="refresh" content="0;<%=location%>"/></head>

<body onload="document.location.href='<%=location%>'">
</body>
</html>