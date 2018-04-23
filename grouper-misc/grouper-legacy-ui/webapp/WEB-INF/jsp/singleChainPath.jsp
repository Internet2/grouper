<%-- @annotation@ 
	Displays single step in chain of effective memberships by which a subject is a member of a group.
	Called from chainPath which iterates over all steps	
 --%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="chainPath"/>
  </tiles:insert> <c:out value="${linkSeparator}" escapeXml="false"/>
  	<c:choose>
		<c:when test="${group.listField=='members'}">
			<c:set var="linkTitle" value="${navMap['groups.membership.through.title']} ${group.desc}"/>
			<c:set var="linkText"><grouper:message key="groups.membership.chain.member-of" tooltipDisable="true"/></c:set>
		</c:when>
		<c:otherwise>
			<c:set var="linkTitle" value="${navMap['groups.membership.through.title']} ${group.desc}"/>
			<c:set var="linkText"><grouper:message key="groups.membership.chain.member-of-list" tooltipDisable="true">
				<grouper:param value="${group.listField}"/>
			</grouper:message></c:set>
			<c:set target="${params}" property="listField" value="${group.listField}"/>
		</c:otherwise>
	</c:choose>
   	<tiles:insert definition="dynamicTileDef" flush="false">
		<tiles:put name="viewObject" beanName="currentSubject"/>
		<tiles:put name="view" value="isMemberOf"/>
		<tiles:put name="params" beanName="params"/>
	  	<tiles:put name="linkTitle" value="${linkTitle}"/>
		<tiles:put name="linkText" value="${linkText}"/>
	</tiles:insert>
</li>
</grouper:recordTile>