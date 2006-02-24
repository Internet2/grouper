<html>
<head></head>
<%
String location=null;
if(request.getRemoteUser()==null) {
	location="populateIndex.do";
}else{
	location="home.do";
}%>
<body onload="document.location.href='<%=location%>'">
</body>
</html>
