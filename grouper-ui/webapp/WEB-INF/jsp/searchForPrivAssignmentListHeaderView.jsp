<%-- @annotation@
		  Dynamic tile used by genericList mechanism to render
		  content above list items - i.e. is an alternative header
		  when searching for subjects in 'Find' mode
--%><%--
  @author Gary Brown.
  @version $Id: searchForPrivAssignmentListHeaderView.jsp,v 1.9 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
    <grouper:subtitle key="find.heading.select-privs" param1="${subtitleArgs[0]}"/>
<div class="assignMembersForm">
    <form class="AssignMembersForm" action="doAssignNewMembers.do" method="post">
	<input type="hidden" name="callerPageId" value="<c:out value="${SearchFormBean.map.callerPageId}"/>"/>
	<fieldset>
		<c:if test="${forStems}">
			<input type="hidden" name="stems" value="true"/>
			<input type="hidden" name="stemId" value="<c:out value="${SearchFormBean.map.stemId}"/>"/>
    		
		</c:if>
		<c:if test="${!forStems}">
    	<input type="hidden" name="groupId" value="<c:out value="${SearchFormBean.map.groupId}"/>"/> 
		</c:if>
		
		<c:choose>
			<c:when test="${empty findForPriv}">
				<c:set var="memberChecked"> checked="checked"</c:set>
			</c:when>
			<c:otherwise>
				<%pageContext.setAttribute((String)session.getAttribute("findForPriv")+"Checked","checked='CHECKED'");%>
			</c:otherwise>	
		</c:choose>
		<div class="privilegeCheckBoxes">
			<c:if test="${forStems}">
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="create" id="privCreate" <c:out value="${createChecked}"/>/>&#160;<label for="privCreate"><grouper:message key="priv.create"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="stemAdmin" id="privStemAdmin" <c:out value="${stemAdminChecked}"/>/>&#160;<label for="privStemAdmin"><grouper:message key="priv.stemAdmin"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="stemAttrRead" id="privStemAttrRead" <c:out value="${stemAttrReadChecked}"/>/>&#160;<label for="privStemAttrRead"><grouper:message key="priv.stemAttrRead"/></label></span>
    			<span class="checkbox"><input type="checkbox" name="privileges" 
				value="stemAttrUpdate" id="privStemAttrUpdate" <c:out value="${stemAttrUpdateChecked}"/>/>&#160;<label for="privStemAttrUpdate"><grouper:message key="priv.stemAttrUpdate"/></label></span>
				<input type="hidden" name="stems" value="true"/>
			</c:if>

                      
			<c:if test="${!forStems}">
    			<span class="checkbox"><input type="checkbox" name="privileges" value="member"  id="privMember" <c:out value="${memberChecked}" escapeXml="false" />/> 
					<label for="privMember"><grouper:message key="priv.member"/></label></span>
    			<c:if test="${groupPrivResolver.canManagePrivileges}">
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="optin"  id="privOptin" <c:out value="${optinChecked}" escapeXml="false" />/> 
						<label for="privOptin"><grouper:message key="priv.optin"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="optout"  id="privOptout" <c:out value="${optoutChecked}" escapeXml="false" />/> 
						<label for="privOptout"><grouper:message key="priv.optout"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="view"  id="privView" <c:out value="${viewChecked}" escapeXml="false"/>/> 
						<label for="privView"><grouper:message key="priv.view"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="read"  id="privRead" <c:out value="${readChecked}" escapeXml="false" />/> 
						<label for="privRead"><grouper:message key="priv.read"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="update"  id="privUpdate" <c:out value="${updateChecked}" escapeXml="false"/>/> 
						<label for="privUpdate"><grouper:message key="priv.update"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="groupAttrRead"  id="privGroupAttrRead" <c:out value="${groupAttrReadChecked}" escapeXml="false"/>/> 
						<label for="privGroupAttrRead"><grouper:message key="priv.groupAttrRead"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" value="groupAttrUpdate"  id="privGroupAttrUpdate" <c:out value="${groupAttrUpdateChecked}" escapeXml="false"/>/> 
						<label for="privGroupAttrUpdate"><grouper:message key="priv.groupAttrUpdate"/></label></span>
	    			<span class="checkbox"><input type="checkbox" name="privileges" 
					value="admin"  id="privAdmin" <c:out value="${adminChecked}"/>/>&#160;<label for="privAdmin"><grouper:message key="priv.admin"/></label></span>
				</c:if>
			</c:if>


		</div>
		</fieldset>
    <grouper:subtitle key="find.heading.select-results" />
	<tiles:importAttribute ignore="true"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	  			<tiles:put name="view" value="privilegeLinksHeader"/>
				<tiles:put name="noResultsMsg" beanName="noResultsMsg"/>
				<tiles:put name="allowPageSizeChange" value="false"/>
				<tiles:put name="listInstruction" beanName="listInstruction"/>
  			</tiles:insert>
