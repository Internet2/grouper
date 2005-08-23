<%-- @annotation@
		  Dynamic tile which represents a group or subject
		  which can be selected for privilege assignment.
--%><%--
  @author Gary Brown.
  @version $Id: assignFoundMemberView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
	<input type="hidden" name="subjectType:<c:out value="${viewObject.id}"/>" value="<c:out value="${viewObject.subjectType}"/>" />
	<label for="members<c:out value="${itemPos}"/>" class="noCSSOnly">
	   	<fmt:message bundle="${nav}" key="browse.select.subject"/> <c:out value="${viewObject.desc}"/>
		</label>
	<input type="checkbox" name="members" value="<c:out value="${viewObject.id}"/>" <c:out value="${checked}"/>/>
	<c:set var="areAssignableChildren" value="true" scope="request"/> 
	  <tiles:insert definition="dynamicTileDef">
		  <tiles:put name="viewObject" beanName="viewObject"/>
		  <tiles:put name="view" value="browseForFindMember"/>
	  </tiles:insert>

