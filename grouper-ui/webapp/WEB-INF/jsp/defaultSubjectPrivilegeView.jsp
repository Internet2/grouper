<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual privilegees
--%><%--
  @author Gary Brown.
  @version $Id: defaultSubjectPrivilegeView.jsp,v 1.3.6.1 2009-01-04 08:16:16 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="privilegeLink">
	<c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
	<c:set target="${linkParams}" property="groupId" value="${viewObject.groupOrStem.id}"/>
	<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>
	<c:set target="${linkParams}" property="sourceId" value="${viewObject.subject.sourceId}"/>
	
	
	<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.to.subject.summary" tooltipDisable="true">
		<grouper:param value="${viewObject.subject.desc}"/>
	</grouper:message></c:set>
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