<%@include file="/WEB-INF/jsp/include.jsp"%>
<html>
<head>
<style type="text/css">
body {
	font-family:arial;
	width:100%;
	height:100%;
}

span.message {
	font-size:8pt;
}

ul {
	list-style: none;
	margin-left: 0;
	padding-left: 1em;
	text-indent: -1em;
}

a.nolink, a.nolink:visited, a.nolink:hover {
	color:000000;
	text-decoration:none;
}


a.set,a.set:visited	 {
	background-color:000;
	color:fff;
}
a, a:visited {
	color:006666;
}
#top {
	clear:both;
}
#wrapper {
	width:100%;
	height:100%;
	background-color:ccccff;
}

#links {
	position:absolute;
	width:40px;
	height:100%;
	float:left;
	background-color:ffcccc;	
}

#messages {
	position:absolute;
	width:220px;
	left:54px;
	float:left;
	clear:right;
	height:100%;
	background-color:ffffff;
}

.messages {
	position:absolute;
	width:100%;
	
	top:0px;
	visibility:hidden;
	
	
	background-color:ffffff;	
}

.errorMessage {
	color:#ff0000;
}

.messageHeader {
	font-weight:bold;
}

li.test {
	font-weight:bold;
}


</style>

<script type="text/javascript">
var noPages = <c:out value="${_testPagesCount}"/>;
var curPage=1;
function setCurPage(p) {
	var curPageEl = document.getElementById("link" + curPage);
	curPageEl.className="";
	
	var messagesEl = document.getElementById("messages" + curPage);
	messagesEl.style.visibility="hidden";
	var controlForm = document.getElementById('controlForm');
	controlForm.count.value=p;
	curPage=p;
	curPageEl = document.getElementById("link" + curPage);
	curPageEl.className="set";
	
	messagesEl = document.getElementById("messages" + curPage);
	messagesEl.style.visibility="visible";
	var leftFrame = parent.frames[0];
	leftFrame.document.location.href="_test/page" + p + ".html";	
}

function nextPage() {
	if(curPage<noPages) {
		setCurPage(curPage+1);
		return;
	}
	alert("No more slides");
}

function prevPage() {
	if(curPage>1) {
		setCurPage(curPage-1);
		return;
	}
	alert("No more slides");
}
</script>

</head>
<body bgcolor="cccccc" onload="setCurPage(1)">
<div id="top">
<table><tr><td><a href="populateUploadTest.do" target="_top">Load...</a></td><td>
<form id="controlForm" onsubmit="setCurPage(this.count.value);return false;">
<input type="button" value="&lt;" onclick="prevPage()"/><input type="text" name="count" size="3"/><input type="button" value="&gt;" onclick="nextPage()"/>
</form></td></tr></table>
</div>
<table border="1px" cellpadding="0" cellspacing="0">
<c:forEach var="attr" items="${uploadUser}">
<tr><td><c:out value="${attr.key}"/></td><td><c:out value="${attr.value}"/></td></tr>
</c:forEach>
</table>
<div id="wrapper">
<div id="links">
<ul>
<c:forEach var="test" items="${_tests}">
<li class="test"><a href="#" title="<c:out value="${test.id}"/>" class="nolink">Test</a></li>
<c:forEach var="page" items="${test.pages}">
<li><a id="link<c:out value="${page.no}"/>" onclick="setCurPage(<c:out value="${page.no}"/>);return false;" href="#" target="leftFrame" title="Show <c:out value="${page.url}"/>">P<c:out value="${page.no}"/></a></li>
</c:forEach>
</c:forEach>
</ul>
</div><div id="messages">
<c:forEach var="test" items="${_tests}">
<c:forEach var="page" items="${test.pages}">
<div id="messages<c:out value="${page.no}"/>" class="messages">
<div class="messageHeader"><c:out value="${test.id}"/><hr/></div>
<c:forEach var="msg" items="${page.messages}">
<c:choose>
	<c:when test="${msg.isError}">
		<span class="errorMessage"><c:out value="${msg.message}"/></span>	
	</c:when>
	<c:otherwise>
		<span class="message"><c:out value="${msg.message}"/></span>
	</c:otherwise>
</c:choose>


<hr />
</c:forEach>
</c:forEach></div>
</c:forEach>
</div>
</div>
</body>
</html>
