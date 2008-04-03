<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members
--%><%--
  @author Gary Brown.
  @version $Id: defaultMembershipView.jsp,v 1.5 2008-04-03 13:30:21 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="memberLink">
<c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${linkParams}" property="groupId" value="${viewObject.group.id}"/>
<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>
<c:set target="${linkParams}" property="sourceId" value="${viewObject.subject.sourceId}"/>

<c:if test="${!empty listField}">
	<c:set target="${linkParams}" property="listField" value="${listField}"/>
</c:if>
<c:set var="subject" value="${viewObject.subject}"/>
<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.to.subject.summary">
		 		<grouper:param><tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="subject"/>
		  <tiles:put name="view" value="subjectSummaryLinkTitle"/>
		  <tiles:put name="inLink" value="true"/>
	</tiles:insert></grouper:param>
		</grouper:message></c:set>
		
			
<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="subject"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert>
	<c:out value="${linkSeparator}" escapeXml="false"/>
	<c:if test="${!empty contextSubject}"><c:set var="memberLinkView" value="memberWithoutLink"/></c:if>
	<c:if test="${empty memberLinkView}"><c:set var="memberLinkView" value="memberLink"/></c:if>
	<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="viewObject"/>
		  <tiles:put name="view" beanName="memberLinkView"/>
		  <tiles:put name="params" beanName="linkParams"/>
	</tiles:insert>
</div>