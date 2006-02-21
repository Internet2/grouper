<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members
--%><%--
  @author Gary Brown.
  @version $Id: defaultMembershipView.jsp,v 1.2 2006-02-21 16:18:45 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="memberLink">
<c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${linkParams}" property="groupId" value="${viewObject.group.id}"/>
<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>

<c:if test="${!empty listField}">
	<c:set target="${linkParams}" property="listField" value="${listField}"/>
</c:if>

<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.desc}"/>
		</fmt:message></c:set>
		<c:set var="subject" value="${viewObject.subject}"/>
			
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