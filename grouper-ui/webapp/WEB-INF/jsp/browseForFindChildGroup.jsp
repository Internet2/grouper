<%-- @annotation@
		  Dynamic tile used whilst browsing in 'Find' mode
		  to render child groups
--%><%--
  @author Gary Brown.
  @version $Id: browseForFindChildGroup.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
	<c:choose>
		<c:when test="${viewObject.isGroup}">
			<c:set var="linkTitle" value="${navMap['browse.expand.group']} ${viewObject.displayExtension}"/>
		</c:when>
		<c:otherwise>
			<c:set var="linkTitle" value="${navMap['browse.expand.stem']} ${viewObject.displayExtension}"/>
		</c:otherwise>
	</c:choose>
	   <input type="hidden" name="subjectType:<c:out value="${viewObject.id}"/>" value="group"/>
       <label for="members<c:out value="${itemPos}"/>" class="noCSSOnly">
	   	<fmt:message bundle="${nav}" key="browse.select.group"/> <c:out value="${viewObject.displayExtension}"/>
		</label>
		<c:set var="areAssignableChildren" value="true" scope="request"/>
	   <input type="checkbox" name="members" id="members<c:out value="${itemPos}"/>" value="<c:out value="${viewObject.id}"/>" <c:out escapeXml="false" value="${checked}"/>/> 
	   [<html:link 	page="/browseStemsFind.do" 
					paramId="currentNode" 
					paramName="viewObject" 
					paramProperty="id"
					title="${linkTitle}">
					
						<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject.displayExtension}"/></span>
		</html:link>]


		