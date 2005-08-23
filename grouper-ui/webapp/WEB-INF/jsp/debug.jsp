<%-- @annotation@
			Tile which displays debug information if dbug mode is on
--%><%--
  @author Gary Brown.
  @version $Id: debug.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>


<c:if test="${debugPrefs.isActive}">
<div id="debug">
<script language="Javascript">

var debugStuff = new Object();


function changeDebug(mode) {

	for(var i in debugStuff) {
		if(i==mode) {
			hide(i + 'Button');
			show(i + 'Panel');	
		}else{
			hide(i + 'Panel');
			show(i + 'Button');
		}
	}	
	
}

function hide(id) {

	var toHide = document.getElementById(id);
	toHide.style.display='none';
}

function show(id) {
	var toShow = document.getElementById(id);
	if(toShow.nodeName.toLowerCase()=='div') {
		toShow.style.display='block';

	}else{
		toShow.style.display='inline';
	
	}
}


</script>
<a href="populateDebugPrefs.do" border="0"><fmt:message bundle="${nav}" key="debug.prefs.edit.link"/></a>
<c:if test="${debugPrefs.doShowResources}">
	<span id="resourceButton"><a href="#" onclick="changeDebug('resource');return false;">Show resources</a></span>
	<script language="javascript">debugStuff['resource']=1;</script>
</c:if>

<c:if test="${debugPrefs.doShowTilesHistory}">
	<span id="tileHistoryButton"><a href="#" onclick="changeDebug('tileHistory');return false;">Show tile history</a></span>
	<script language="javascript">debugStuff['tileHistory']=1;</script>
</c:if>

<br/>
<c:if test="${debugPrefs.doShowResources}">
<%
	List navList = (List)UIThreadLocal.get("navResource");
	Map navMap = (Map)UIThreadLocal.get("navResourceMap");
	pageContext.setAttribute("navList",navList);
	pageContext.setAttribute("navMap",navMap);
%>
<div id="resourcePanel" style="top:20">
<table><tr><th align="right">Key</th><th align="left">Value</th></tr>
<c:forEach var="navKey" items="${navList}">
<tr><td align="right"><c:out value="${navKey}"/></td><td><c:out value="${navMap[navKey]}"/></td></tr>
</c:forEach>
</table>
</div>
</c:if>
<c:if test="${debugPrefs.doShowTilesHistory}">
<div id="tileHistoryPanel" class="top:20;">
<%
	List dynamicTiles = (List)UIThreadLocal.get("dynamicTiles");
	request.setAttribute("dynamicTiles",dynamicTiles);
	ResourceBundle defaultInit = ResourceBundle.getBundle("/resources/init");
	String defaultModule=defaultInit.getString("default.module");	
	if("grouper".equals(defaultModule)) defaultModule="";
	request.setAttribute("defaultModule",defaultModule);
%>

<tiles:insert page="/WEB-INF/jsp/dynamicTileHistory.jsp"/>

</div>
<script language="Javascript">
	
	if(debugStuff['resource']) changeDebug('resource');
</script>
</c:if>
</div>
</c:if>


