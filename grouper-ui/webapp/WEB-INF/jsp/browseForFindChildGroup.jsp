<%-- @annotation@
		  Dynamic tile used whilst browsing in 'Find' mode
		  to render child groups
--%><%--
  @author Gary Brown.
  @version $Id: browseForFindChildGroup.jsp,v 1.9 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
	<c:choose>
		<c:when test="${viewObject.isGroup}">
			<c:set var="linkTitle" value="${navMap['browse.expand.group']} ${viewObject.displayExtension}"/>
		</c:when>
		<c:otherwise>
			<c:set var="linkTitle" value="${navMap['browse.expand.stem']} ${viewObject.displayExtension}"/>
		</c:otherwise>
	</c:choose>
	   <input type="hidden" name="subjectType:<c:out value="${viewObject.id}"/>" value="group"/>
	   <input type="hidden" name="sourceId:<c:out value="${viewObject.id}"/>" value="g:gsa"/>
       <label for="members<c:out value="${itemPos}"/>" class="noCSSOnly">
	   	<grouper:message key="browse.select.group"/> <c:out value="${viewObject.displayExtension}"/>
		</label>
		<c:set var="areAssignableChildren" value="true" scope="request"/>
	   <input type="checkbox" name="members" id="members<c:out value="${itemPos}"/>" value="<c:out value="${viewObject.id}"/>" <c:out escapeXml="false" value="${checked}"/>/> 
	   <jsp:useBean id="attrLink" class="java.util.HashMap"/>
		<c:set target="${attrLink}" property="groupId" value="${viewObject.id}"/>
		<c:set target="${attrLink}" property="currentNode" value="${viewObject.id}"/>
		<c:set target="${attrLink}" property="callerPageId" value="${thisPageId}"/>
	   <html:link 	page="/browseStemsFind.do" 
					name="attrLink" 
					title="${linkTitle}">
					
						<grouper:message key="groups.membership.view-members"/>
		</html:link> /
		
			   <html:link 	page="/populateGroupSummary.do" 
					name="attrLink"
					title="${linkTitle}">
					
						<grouper:message key="groups.membership.view-group-attributes"/>
		</html:link>	<grouper:message key="groups.membership.for"/> [<c:out value="${viewObject[groupSearchResultField]}"/>]


		