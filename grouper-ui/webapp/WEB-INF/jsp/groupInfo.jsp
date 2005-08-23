<%-- @annotation@
		Tile which displays a summary of group attributes  - currently inserted
		in GroupSummaryDef
--%><%--
  @author Gary Brown.
  @version $Id: groupInfo.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="groupInfo">
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.id"/>
	</div>
	<div class="formRight">
		<c:out value="${group.id}"/>
	</div>
</div> 
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.extension"/>
	</div>
	<div class="formRight">
		<c:out value="${group.extension}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.name"/>
	</div>
	<div class="formRight">
		<c:out value="${group.name}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.display-extension"/>
	</div>
	<div class="formRight">
		<c:out value="${group.displayExtension}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.display-name"/>
	</div>
	<div class="formRight">
		<c:out value="${group.displayName}"/>
	</div>
</div>	
<c:if test="${!empty group.description}">
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.description"/>
	</div>
	<div class="formRight">
		<c:out value="${group.description}"/>
	</div>
</div>
</c:if>
<div class="formRow"></div>
<tiles:insert definition="groupLinksDef"/>
</div>
</grouper:recordTile>