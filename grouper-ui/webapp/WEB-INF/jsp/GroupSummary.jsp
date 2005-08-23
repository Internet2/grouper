<%-- @annotation@ 
			Displays summary of a group and provides links for 
			the maintenance of the group
--%><%--
  @author Gary Brown.
  @version $Id: GroupSummary.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>
<tiles:insert definition="groupInfoDef"/>
<tiles:insert definition="groupStuffDef"/>

<c:if test="${isFlat}">
<div class="linkButton">
<html:link page="/populate${functionalArea}.do">
	<fmt:message bundle="${nav}" key="groups.summary.cancel"/>
</html:link>
</div>
</c:if>
<c:if test="${!isFlat}">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.select-other"/>
</h2>
<tiles:insert definition="browseStemsDef"/>
</c:if>