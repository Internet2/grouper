<%-- @annotation@
		  Shows effective privileges for subject over group or stem
--%><%--
  @author Gary Brown.
  @version $Id: effectivePrivs.jsp,v 1.2 2005-12-14 15:11:01 isgwb Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<jsp:useBean id="membershipMap" class="java.util.HashMap"/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:set target="${membershipMap}" property="subjectId" value="${subject.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="${subject.subjectType}"/>
<c:set target="${membershipMap}" property="callerPageId" value="${thisPageId}"/>

<c:set var="linkSeparator">  
	<tiles:insert definition="linkSeparatorDef" flush="false">
	</tiles:insert>
</c:set>
 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.description}"/>
		</fmt:message></c:set>

<div class="effectivePrivs">
<span class="subjectSummaryLink">
<fmt:message bundle="${nav}" key="subject.privileges.current"/>
<html:link page="/populateSubjectSummary.do" name="membershipMap" title="${linkTitle}" ><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="subject"/>
	  <tiles:put name="view" value="extendedPrivSubjet"/>
  </tiles:insert></html:link>
 </span>
<c:forEach var="groupOrStemPrivEntry" items="${extendedSubjectPriv}">
<c:set target="${membershipMap}" property="subjectId" value="${subject.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="${subject.subjectType}"/>
	<c:set var="privMap" value="${groupOrStemPrivEntry.value}"/>
	<c:set var="group" value="${privMap.group}"/>
	<c:if test="${groupOrStemPrivEntry.key!='subject'}">
	<div class="effectivePriv">
	<c:choose>
		<c:when test="${groupOrStemPrivEntry.key!='GrouperAll'}">
			<c:set target="${membershipMap}" property="asMemberOf" value="${group.id}"/>
			<%--  Use params to make link title descriptive for accessibility --%>		
			<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
						<fmt:param value="${viewObject.desc}"/>
						<fmt:param value="${group.desc}"/>
				</fmt:message></c:set>
			<span class="isMemberof">
			<html:link page="/populateGroupMember.do" name="membershipMap" title="${linkTitle}">
			<fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></html:link></span><c:out value="${linkSeparator}" escapeXml="false"/>
			<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
			<span class="groupSummaryLink">
			<tiles:insert definition="dynamicTileDef" flush="false">
			  <tiles:put name="viewObject" beanName="group"/>
			  <tiles:put name="view" value="chainPath"/>
			</tiles:insert> 
			 </span>
				<c:set target="${membershipMap}" property="subjectId" value="${group.id}"/>
				<c:set target="${membershipMap}" property="subjectType" value="group"/>
				<c:set target="${membershipMap}" property="asMemberOf" value="${browseParent.id}"/>
				<c:set var="memberPage" value="/populateGroupMember.do"/>
				<c:if test="${!browseParent.isGroup}">
					<c:set var="memberPage" value="/populateStemMember.do"/>
				</c:if>
				 <%--  Use params to make link title descriptive for accessibility --%>		
			<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
						<fmt:param value="${group.desc}"/>
						<fmt:param value="${browseParent.displayExtension}"/>
			</fmt:message></c:set>
		</c:when>
		<c:otherwise>
				<c:set target="${membershipMap}" property="subjectId" value="GrouperAll"/>
				<c:set target="${membershipMap}" property="subjectType" value="application"/>
				<c:set target="${membershipMap}" property="asMemberOf" value="${browseParent.id}"/>
				<c:set var="memberPage" value="/populateGroupMember.do"/>
			<fmt:message bundle="${nav}" key="subject.privileges.from-grouperall"/>
		</c:otherwise>
		</c:choose>
		<span class="hasPrivFor">
		<html:link page="${memberPage}" name="membershipMap" title="${linkTitle}">
			<fmt:message bundle="${nav}" key="subject.privileges.has-for"/></html:link> 
		<c:forEach var="priv" items="${possiblePrivs}">
			<c:if test="${!empty privMap[priv]}">
				:<c:out value="${priv}"/>
			</c:if>
		</c:forEach>
		</span>
		</div>
	</c:if>
</c:forEach>

</div>
</grouper:recordTile>