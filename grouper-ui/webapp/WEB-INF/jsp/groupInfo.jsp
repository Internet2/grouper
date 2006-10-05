<%-- @annotation@
		Tile which displays a summary of group attributes  - currently inserted
		in GroupSummaryDef
--%><%--
  @author Gary Brown.
  @version $Id: groupInfo.jsp,v 1.4 2006-10-05 15:28:15 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="groupInfo"> 
<div class="formRow">
	<div class="formLeft">
		<c:out value="${fieldList.extension.displayName}"/>
	</div>
	<div class="formRight">
		<c:out value="${group.extension}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<c:out value="${fieldList.name.displayName}"/>
	</div>
	<div class="formRight">
		<c:out value="${group.name}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<c:out value="${fieldList.displayExtension.displayName}"/>
	</div>
	<div class="formRight">
		<c:out value="${group.displayExtension}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<c:out value="${fieldList.displayName.displayName}"/>
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
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="groups.summary.id"/>
	</div>
	<div class="formRight">
		<c:out value="${group.id}"/>
	</div>
</div>
<c:if test="${!empty group.types}">
<div class="formRow">
	<div class="formLeft"><fmt:message bundle="${nav}" key="groups.summary.types"/></div>
</div>
<div class="formRight">
<div class="groupTypes">

<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="group" beanProperty="types"/>
	<tiles:put name="view" value="groupSummaryGroupTypes"/>
	<tiles:put name="itemView" value="groupSummary"/>
	<tiles:put name="listless" value="TRUE"/>
</tiles:insert>
</div>
</div>
</c:if>
	</grouper:recordTile>