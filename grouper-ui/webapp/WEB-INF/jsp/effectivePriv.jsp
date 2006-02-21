<%-- @annotation@
		  Shows effective privileges for subject over group or stem
--%><%--
  @author Gary Brown.
  @version $Id: effectivePriv.jsp,v 1.2 2006-02-21 16:21:57 isgwb Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:set target="${params}" property="subjectId" value="${subject.id}"/>
<c:set target="${params}" property="subjectType" value="${subject.subjectType}"/>
	<c:set var="privMap" value="${groupOrStemPrivEntry.value}"/>
	<c:set var="group" value="${privMap.group}"/>
	<c:if test="${groupOrStemPrivEntry.key!='subject'}">
	<div class="effectivePriv">
	<c:choose>
		<c:when test="${groupOrStemPrivEntry.key!='GrouperAll'}">
			<c:set target="${params}" property="asMemberOf" value="${group.id}"/>
			<%--  Use params to make link title descriptive for accessibility --%>		
			<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
						<fmt:param value="${viewObject.desc}"/>
						<fmt:param value="${group.desc}"/>
				</fmt:message></c:set>
				
			<tiles:insert definition="dynamicTileDef" flush="false">
			  <tiles:put name="viewObject" beanName="subject"/>
			  <tiles:put name="view" value="isMemberOf"/>
			  <tiles:put name="params" beanName="params"/>
	  		  <tiles:put name="linkTitle" beanName="linkTitle"/>
			</tiles:insert>
		<c:out value="${linkSeparator}" escapeXml="false"/>	
			<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
			
			
			<tiles:insert definition="dynamicTileDef" flush="false">
			  <tiles:put name="viewObject" beanName="group"/>
			  <tiles:put name="view" value="chainPath"/>
			</tiles:insert> 
			 
				<c:set target="${params}" property="subjectId" value="${group.id}"/>
				<c:set target="${params}" property="subjectType" value="group"/>
				<c:set target="${params}" property="asMemberOf" value="${browseParent.id}"/>
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
				<c:set target="${params}" property="subjectId" value="GrouperAll"/>
				<c:set target="${params}" property="subjectType" value="application"/>
				<c:set target="${params}" property="asMemberOf" value="${browseParent.id}"/>
				<c:set var="memberPage" value="/populateGroupMember.do"/>
			<fmt:message bundle="${nav}" key="subject.privileges.from-grouperall"/>
		</c:otherwise>
		</c:choose>
		<c:out value="${linkSeparator}" escapeXml="false"/>
		<c:set target="${params}" property="listField" value="${listField}"/>
		<tiles:insert definition="dynamicTileDef" flush="false">
			  <tiles:put name="viewObject" beanName="subject"/>
			  <tiles:put name="view" value="hasPrivilege"/>
			  <tiles:put name="params" beanName="params"/>
	  		  <tiles:put name="linkTitle" beanName="linkTitle"/>
			  <tiles:put name="memberPage" beanName="memberPage"/>
			  <tiles:put name="privMap" beanName="privMap"/>
			  <tiles:put name="linkSeparator" beanName="linkSeparator"/>
			</tiles:insert>
			<c:set target="${params}" property="listField" value=""/>
		</div>
	</c:if>

</grouper:recordTile>