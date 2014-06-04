<%-- @annotation@
		  Dynamic tile which represents a group or subject
		  which can be selected for privilege assignment.
--%><%--
  @author Gary Brown.
  @version $Id: assignFoundMemberView.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><grouper:message key="browse.to.subject.summary" tooltipDisable="true">
		 		<grouper:param value="${viewObject.description}"/>
		</grouper:message></c:set>
	<input type="hidden" name="subjectType:<c:out value="${viewObject.id}"/>" value="<c:out value="${viewObject.subjectType}"/>" />
	<input type="hidden" name="sourceId:<c:out value="${viewObject.id}"/>" value="<c:out value="${viewObject.sourceId}"/>" />
	<label for="members<c:out value="${itemPos}"/>" class="noCSSOnly">
	   	<grouper:message key="browse.select.subject"/> <c:out value="${viewObject.desc}"/>
		</label>
	<input type="checkbox" name="members" value="<c:out value="${viewObject.id}"/>" <c:out value="${checked}"/>/>
	<c:set var="areAssignableChildren" value="true" scope="request"/> 
	  <c:set target="${viewObject}" property="callerPageId" value="${thisPageId}"/>
	  <html:link page="/populateSubjectSummary.do" name="viewObject" title="${linkTitle}">
					  <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="viewObject"/>
		  <tiles:put name="view" value="browseForFindMember"/>
	  </tiles:insert>
			</html:link>



