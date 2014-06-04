<%-- @annotation@
		  Shows effective privileges for subject over group or stem
--%><%--
  @author Gary Brown.
  @version $Id: effectivePriv.jsp,v 1.10 2009-11-07 15:34:54 isgwb Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:set target="${params}" property="subjectId" value="${subject.id}"/>
<c:set target="${params}" property="subjectType" value="${subject.subjectType}"/>
<c:set target="${params}" property="sourceId" value="${subject.sourceId}"/>
	<c:set var="privMap" value="${groupOrStemPrivEntry.value}"/>
	<c:set var="group" value="${privMap.group}"/>
	<c:if test="${groupOrStemPrivEntry.key!='subject'}"> 
	<div class="effectivePriv">
	<c:choose>
	<c:when test="${groupOrStemPrivEntry.key=='GrouperSystem'}">
					<c:set target="${params}" property="subjectId" value="GrouperSystem"/>
				<c:set target="${params}" property="subjectType" value="application"/>
				<c:set target="${params}" property="sourceId" value="g:isa"/>
				<c:set target="${params}" property="asMemberOf" value="${browseParent.id}"/>
				<c:set var="memberPage" value="/populateGroupMember.do"/>
			<grouper:message key="subject.privileges.from-groupersystem"/>
	</c:when>
		<c:when test="${groupOrStemPrivEntry.key!='GrouperAll'}">
			<c:set target="${params}" property="asMemberOf" value="${group.id}"/>
			<%--  Use params to make link title descriptive for accessibility --%>		
			<c:set var="linkTitle"><grouper:message key="browse.assign.title" tooltipDisable="true">
						<grouper:param value="${viewObject.desc}"/>
						<grouper:param value="${group.desc}"/>
				</grouper:message></c:set>
			
			<c:choose>
				<c:when test="${group.hasComposite}">
					<c:set var="memberOfView" value="isIndirectMemberOf"/>
				</c:when>
				<c:otherwise>
					<c:set var="memberOfView" value="isMemberOf"/>
				</c:otherwise>
			</c:choose>
				
			<tiles:insert definition="dynamicTileDef" flush="false">
			  <tiles:put name="viewObject" beanName="subject"/>
			  <tiles:put name="view" value="${memberOfView}"/>
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
					<c:set target="${params}" property="sourceId" value="g:gsa"/>
				<c:set target="${params}" property="asMemberOf" value="${browseParent.id}"/>
				<c:set var="memberPage" value="/populateGroupMember.do"/>
				<c:if test="${!browseParent.isGroup}">
					<c:set var="memberPage" value="/populateStemMember.do"/>
				</c:if>
				 <%--  Use params to make link title descriptive for accessibility --%>		
			<c:set var="linkTitle"><grouper:message key="browse.assign.title" tooltipDisable="true">
						<grouper:param value="${group.desc}"/>
						<grouper:param value="${browseParent.displayExtension}"/>
			</grouper:message></c:set>
		</c:when>
		<c:otherwise>
				<c:set target="${params}" property="subjectId" value="GrouperAll"/>
				<c:set target="${params}" property="subjectType" value="application"/>
					<c:set target="${params}" property="sourceId" value="g:isa"/>
				<c:set target="${params}" property="asMemberOf" value="${browseParent.id}"/>
				<c:set var="memberPage" value="/populateGroupMember.do"/>
			<grouper:message key="subject.privileges.from-grouperall"/>
		</c:otherwise>
		</c:choose>
		<c:out value="${linkSeparator}" escapeXml="false"/>
		<c:set target="${params}" property="listField" value="${listField}"/>
		<c:set target="${subject}" property="privMap" value="${privMap}"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
				  <tiles:put name="viewObject" beanName="subject"/>
				  <tiles:put name="view" value="hasPrivilege"/>
				  <tiles:put name="linkParams" beanName="params"/>
		  		  <tiles:put name="linkTitle" beanName="linkTitle"/>
				  <tiles:put name="memberPage" beanName="memberPage"/>

				  <tiles:put name="linkSeparator" beanName="linkSeparator"/>
				</tiles:insert>
	
			<c:set target="${params}" property="listField" value=""/>
		</div>
	</c:if>

</grouper:recordTile>  