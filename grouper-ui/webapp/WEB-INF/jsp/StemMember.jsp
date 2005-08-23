<%-- @annotation@
			Form which allows user to change an individual
			Subject`s membership of / privileges for, the
			active stem
--%><%--
  @author Gary Brown.
  @version $Id: StemMember.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%> 
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.select-privs">
	<fmt:param><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="subject"/>
	  <tiles:put name="view" value="groupMember"/>
</tiles:insert></fmt:param>
</fmt:message>
</h2>
<c:set var="subject" value="${subject}" scope="request"/>

<c:if test="${authUserPriv.CREATE}">
	<tiles:insert definition="stemMemberPrivsDef"/>
</c:if>
<div class="linkButton">
<html:link page="/populateStemPriviligees.do" paramName="GroupOrStemMemberFormBean" paramProperty="asMemberOf" paramId="stemId">
	<fmt:message bundle="${nav}" key="cancel"/>
</html:link>
</div>