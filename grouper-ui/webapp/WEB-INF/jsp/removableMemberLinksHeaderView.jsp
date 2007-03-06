<%-- @annotation@
		  Dynamic tile used  to provide form header for removing all, or selected members
--%><%--
  @author Gary Brown.
  @version $Id: removableMemberLinksHeaderView.jsp,v 1.2 2007-03-06 11:05:49 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	  			<tiles:put name="view" value="findNewHeader"/>
				<tiles:put name="listInstruction" beanName="listInstruction"/>
  			</tiles:insert>

<div class="browseForFindMembersForm">
<form action="removeGroupMembers.do" method="post">
<input type="hidden" name="groupId" value="<c:out value="${grouperForm.map.groupId}"/>"/>
<input type="hidden" name="contextSubject" value="<c:out value="${grouperForm.map.contextSubject}"/>"/>
<input type="hidden" name="contextSubjectId" value="<c:out value="${grouperForm.map.contextSubjectId}"/>"/>
<input type="hidden" name="contextSubjectType" value="<c:out value="${grouperForm.map.contextSubjectType}"/>"/>
<input type="hidden" name="callerPageId" value="<c:out value="${grouperForm.map.callerPageId}"/>"/>
<input type="hidden" name="listField" value="<c:out value="${listField}"/>"/>
<fieldset>
		
   



