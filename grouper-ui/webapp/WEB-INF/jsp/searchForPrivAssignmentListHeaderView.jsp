<%-- @annotation@
		  Dynamic tile used by genericList mechanism to render
		  content above list items - i.e. is an alternative header
		  when searching for subjects in 'Find' mode
--%><%--
  @author Gary Brown.
  @version $Id: searchForPrivAssignmentListHeaderView.jsp,v 1.2 2005-11-08 16:17:42 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.select-privs">
		<fmt:param value="${subtitleArgs[0]}"/>
	</fmt:message>
</h2>
<div class="assignMembersForm">
    <form class="AssignMembersForm" action="doAssignNewMembers.do">
	<input type="hidden" name="callerPageId" value="<c:out value="${SearchFormBean.map.callerPageId}"/>"/>
	<fieldset>
		<c:if test="${forStems}">
			<input type="hidden" name="stems" value="true"/>
			<input type="hidden" name="stemId" value="<c:out value="${SearchFormBean.map.stemId}"/>"/>
    		
		</c:if>
		<c:if test="${!forStems}">
    	<input type="hidden" name="groupId" value="<c:out value="${SearchFormBean.map.groupId}"/>"/> 
		</c:if>
		<div class="privilegeCheckBoxes">
			<c:if test="${forStems}">
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="CREATE" id="privCreate"/>&#160;<label for="privCreate"><fmt:message bundle="${nav}" key="priv.create"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="STEM" id="privStem"/>&#160;<label for="privCreate"><fmt:message bundle="${nav}" key="priv.stem"/></label></span>
				<input type="hidden" name="stems" value="true"/>
			</c:if>

                      
			<c:if test="${!forStems}">
    			<span class="checkbox"><input type="checkbox" name="privileges" value="member"  id="privMember"/> 
					<label for="privMember"><fmt:message bundle="${nav}" key="priv.member"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" value="OPTIN"  id="privOptin"/> 
					<label for="privOptin"><fmt:message bundle="${nav}" key="priv.optin"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" value="OPTOUT"  id="privOptout"/> 
					<label for="privOptout"><fmt:message bundle="${nav}" key="priv.optout"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" value="VIEW"  id="privView"/> 
					<label for="privView"><fmt:message bundle="${nav}" key="priv.view"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" value="READ"  id="privRead"/> 
					<label for="privRead"><fmt:message bundle="${nav}" key="priv.read"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" value="UPDATE"  id="privUpdate"/> 
					<label for="privUpdate"><fmt:message bundle="${nav}" key="priv.update"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="ADMIN"  id="privAdmin"/>&#160;<label for="privAdmin"><fmt:message bundle="${nav}" key="priv.admin"/></label></span>
			</c:if>


		</div>
		</fieldset>
	<h2 class="actionheader">
		<fmt:message bundle="${nav}" key="find.heading.select-results"/>
	</h2>
	<tiles:importAttribute ignore="true"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	  			<tiles:put name="view" value="privilegeLinksHeader"/>
				<tiles:put name="noResultsMsg" beanName="noResultsMsg"/>
				<tiles:put name="allowPageSizeChange" value="false"/>
				<tiles:put name="listInstruction" beanName="listInstruction"/>
  			</tiles:insert>