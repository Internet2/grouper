<html>
<%
String location=null;
if(request.getRemoteUser()==null || "y".equals(request.getParameter("badRole"))) {
	location="populateIndex.do";
}else{
	location="home.do";
}%>
<head><meta http-equiv="refresh" content="0;<%=location%>"/></head>

<body onload="document.location.href='<%=location%>'">
</body>
</html>
