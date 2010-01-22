<%-- @annotation@
			Tile which displays debug information if dbug mode is on
--%><%--
  @author Gary Brown.
  @version $Id: debug.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>


<c:if test="${debugPrefs.isActive}">
<div id="debug">
<script type="text/javascript">

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
<a href="populateDebugPrefs.do" ><grouper:message key="debug.prefs.edit.link"/></a>
<c:if test="${debugPrefs.doShowResources}">
	<span id="resourceButton"><a href="#" onclick="changeDebug('resource');return false;">Show resources</a></span>
	<script type="text/javascript">debugStuff['resource']=1;</script>
</c:if>

<c:if test="${debugPrefs.doShowTilesHistory}">
	<span id="tileHistoryButton"><a href="#" onclick="changeDebug('tileHistory');return false;">Show tile history</a></span>
	<script type="text/javascript">debugStuff['tileHistory']=1;</script>
</c:if>

<span id="requestParametersButton"><a href="#" onclick="changeDebug('requestParameters');return false;">Show request parameters</a></span>
	<script type="text/javascript">debugStuff['requestParameters']=1;</script>
<span id="requestAttributesButton"><a href="#" onclick="changeDebug('requestAttributes');return false;">Show request attributes</a></span>
	<script type="text/javascript">debugStuff['requestAttributes']=1;</script>
<span id="requestHeadersButton"><a href="#" onclick="changeDebug('requestHeaders');return false;">Show request headers</a></span>
	<script type="text/javascript">debugStuff['requestHeaders']=1;</script>
	
<span id="sessionAttributesButton"><a href="#" onclick="changeDebug('sessionAttributes');return false;">Show session attributes</a></span>
	<script type="text/javascript">debugStuff['sessionAttributes']=1;</script>
	
<span id="applicationAttributesButton"><a href="#" onclick="changeDebug('applicationAttributes');return false;">Show application attributes</a></span>
	<script type="text/javascript">debugStuff['applicationAttributes']=1;</script>

<br/>
<c:if test="${debugPrefs.doShowResources}">
<%
	Collection navList = (Collection)UIThreadLocal.get("navResource");
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
<div id="tileHistoryPanel" style="top:20;">
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
<script type="text/javascript">
	
	if(debugStuff['resource']) changeDebug('resource');
</script>
</c:if>
</div>

<div id="requestParametersPanel" style="top:20;display:none">
<%
	pageContext.setAttribute("reqParams",request.getParameterMap());
%>
<table><tr><th align="right">Parameter</th><th align="left">Value</th></tr>
<c:forEach var="entry" items="${reqParams}">
<tr><td align="right"><c:out value="${entry.key}"/></td>
<td>
	<c:forEach var="val" items="${entry.value}">
		<c:out value="${val}"/><br/>
	</c:forEach>
</td></tr>
</c:forEach>
</table>
</div>

<div id="requestAttributesPanel" style="top:20;display:none">

<table><tr><th align="right">Attribute</th><th align="left">Value</th></tr>
<c:forEach var="entry" items="${requestScope}">
<tr><td align="right"><c:out value="${entry.key}"/></td>
<td>
		<c:out value="${entry.value}"/><br/>
</td></tr>
</c:forEach>
</table>
</div>

<div id="requestHeadersPanel" style="top:20;display:none">

<table><tr><th align="right">Header</th><th align="left">Value</th></tr>
<c:forEach var="entry" items="${header}">
<tr><td align="right"><c:out value="${entry.key}"/></td>
<td>
		<c:out value="${entry.value}"/><br/>
</td></tr>
</c:forEach>
</table>
</div>

<div id="sessionAttributesPanel" style="top:20;display:none">

<table width="100%"><tr><th align="right">Attribute</th><th align="left">Value</th></tr>
<c:forEach var="entry" items="${sessionScope}">
<tr><td align="right"><c:out value="${entry.key}"/></td>
<td>
		<c:out value="${entry.value}"/><br/>
</td></tr>
</c:forEach>
</table>
</div>

<div id="applicationAttributesPanel" style="top:20;display:none">

<table width="100%"><tr><th align="right">Attribute</th><th align="left">Value</th></tr>
<c:forEach var="entry" items="${applicationScope}">
<tr><td align="right"><c:out value="${entry.key}"/></td>
<td>
		<c:out value="${entry.value}"/><br/>
</td></tr>
</c:forEach>
</table>
</div>

</c:if>


