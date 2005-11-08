<%-- @annotation@ 
			Form which allows user to change an individual
			Subject`s membership of / privileges for, the
			active group
--%><%--
  @author Gary Brown.
  @version $Id: GroupMember.jsp,v 1.2 2005-11-08 16:08:28 isgwb Exp $
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
<%-- Indirect assignments - juggling for fact there is a context group--%>
	<c:when test="${!empty GroupOrStemMemberFormBean.map.contextGroup}">
		<jsp:useBean id="contextParams" class="java.util.HashMap"/>
		<c:set target="${contextParams}" property="subjectId" value="${GroupOrStemMemberFormBean.map.contextSubjectId}"/>
		<c:set target="${contextParams}" property="contextSubject" value="${GroupOrStemMemberFormBean.map.contextSubject}"/>
		<c:set target="${contextParams}" property="subjectType" value="${GroupOrStemMemberFormBean.map.contextSubjectType}"/>
		<c:set target="${contextParams}" property="groupId" value="${GroupOrStemMemberFormBean.map.contextGroup}"/>
				<c:if test="${GroupOrStemMemberFormBean.map.contextSubject!='true'}">
					<html:link page="/populateChains.do" name="contextParams">
						<fmt:message bundle="${nav}" key="privs.group.member.return-to-chains"/>
					</html:link>
				</c:if>
			<c:choose>
				<c:when test="${GroupOrStemMemberFormBean.map.contextSubject=='true'}">
					
					<tiles:insert definition="callerPageButtonDef"  flush="false"/>
					<html:link page="/populateSubjectSummary.do">
						<fmt:message bundle="${nav}" key="groups.action.summary.return-to-subject-summary"/>
					</html:link>
				</c:when>
				<c:when test="${!empty GroupOrStemMemberFormBean.map.callerPageId}">
					<tiles:insert definition="callerPageButtonDef" flush="false"/>
				</c:when>
				<c:otherwise>
					<html:link page="/populateGroupMembers.do" paramName="browseParent" paramProperty="id" paramId="groupId">
						<fmt:message bundle="${nav}" key="privs.group.member.cancel"/>
					</html:link>
				</c:otherwise>
			</c:choose>
	</c:when>
	<c:otherwise>
		<%-- Direct assignments--%>
		<c:choose>
			<c:when test="${!empty GroupOrStemMemberFormBean.map.privilege}">
				<c:set target="${params}" property="groupId" value="${GroupOrStemMemberFormBean.map.asMemberOf}"/>
				<c:set target="${params}" property="privilege" value="${GroupOrStemMemberFormBean.map.privilege}"/>
				<html:link page="/populateGroupPriviligees.do" name="params">
					<fmt:message bundle="${nav}" key="privs.group.member.cancel"/>
				</html:link>	
			</c:when>
			<c:when test="${!empty GroupOrStemMemberFormBean.map.callerPageId}">
				<tiles:insert definition="callerPageButtonDef" flush="false"/>
			</c:when>
			<c:otherwise>
				<html:link page="/populateGroupMembers.do" paramName="GroupOrStemMemberFormBean" paramProperty="asMemberOf" paramId="groupId">
					<fmt:message bundle="${nav}" key="privs.group.member.cancel"/>
				</html:link>
				
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>
</div>

