<%-- @annotation@
		  Dynamic tile used in all browse modes except 'Find'
		  to display a child group
--%><%--
  @author Gary Brown.
  @version $Id: browseChildGroup.jsp,v 1.2 2005-11-08 15:46:44 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
[<html:link page="/populateGroupSummary.do" 
			paramId="groupId" 
			paramName="viewObject" 
			paramProperty="id"
			title="${navMap['browse.to.group.summary']} ${viewObject.displayExtension}">
				<span class="groupSubject"><c:out value="${viewObject.displayExtension}"/></span>
 </html:link>]
