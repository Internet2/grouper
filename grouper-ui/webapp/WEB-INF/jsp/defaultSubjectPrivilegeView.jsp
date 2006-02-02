<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual privilegees
--%><%--
  @author Gary Brown.
  @version $Id: defaultSubjectPrivilegeView.jsp,v 1.1 2006-02-02 16:40:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="privilegeLink">
	<c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
	<c:set target="${linkParams}" property="groupId" value="${viewObject.groupOrStem.id}"/>
	<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>
	
	<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		<fmt:param value="${viewObject.subject.desc}"/>
	</fmt:message></c:set>
	<c:set var="subject" value="${viewObject.subject}"/>
			
	<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="subject"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert>
	<c:out value="${linkSeparator}" escapeXml="false"/>
	<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="viewObject"/>
		  <tiles:put name="view" value="privilegesLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
	</tiles:insert>
</div>