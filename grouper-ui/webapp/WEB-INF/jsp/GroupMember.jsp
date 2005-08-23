<%-- @annotation@ 
			Form which allows user to change an individual
			Subject`s membership of / privileges for, the
			active group
--%><%--
  @author Gary Brown.
  @version $Id: GroupMember.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:if test="${!empty failedRevocations}">
	<tiles:insert definition="failedRevocationsDef"/>
</c:if>
<jsp:useBean id="params" class="java.util.HashMap"/>
<tiles:insert definition="showStemsLocationDef"/>
<c:choose>
<c:when test="${authUserPriv.UPDATE || authUserPriv.ADMIN}">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.select-privs">
	<fmt:param>
	<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="subject"/>
	  <tiles:put name="view" value="groupMember"/>
	  </tiles:insert>
	</fmt:param>
	</fmt:message>

</h2>



	<tiles:insert definition="groupMemberPrivsDef"/> 
</c:when>
<c:otherwise>
<fmt:message bundle="${nav}" key="privs.group.member.none"/>
</c:otherwise>
</c:choose>
<div class="linkButton">

<c:choose>
	<c:when test="${empty GroupOrStemMemberFormBean.map.privilege}">
		<html:link page="/populateGroupMembers.do" paramName="GroupOrStemMemberFormBean" paramProperty="asMemberOf" paramId="groupId">
			<fmt:message bundle="${nav}" key="privs.group.member.cancel"/>
		</html:link>
	</c:when>
<c:otherwise>
	<c:set target="${params}" property="groupId" value="${GroupOrStemMemberFormBean.map.asMemberOf}"/>
	<c:set target="${params}" property="privilege" value="${GroupOrStemMemberFormBean.map.privilege}"/>
	<html:link page="/populateGroupPriviligees.do" name="params">
			<fmt:message bundle="${nav}" key="privs.group.member.cancel"/>
	</html:link>
</c:otherwise>
</c:choose>
</div>

