<%-- @annotation@
		  Dynamic tile used to render a membership link for a Subject and a group which is a factor in a composite
--%><%--
  @author Gary Brown.
  @version $Id: compositeMembershipView.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="memberLink">
<c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${linkParams}" property="asMemberOf" value="${viewObject.group.id}"/>
<c:set target="${linkParams}" property="isComposite" value="true"/>
<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>
<c:set target="${linkParams}" property="sourceId" value="${viewObject.subject.sourceId}"/>

<c:if test="${!empty listField}">
	<c:set target="${linkParams}" property="listField" value="${listField}"/>
</c:if>

<c:set var="linkTitle"><grouper:message key="browse.to.subject.summary" tooltipDisable="true">
		 		<grouper:param value="${viewObject.desc}"/>
		</grouper:message></c:set>
		<c:set var="subject" value="${viewObject.subject}"/>
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="group"/>
	  <tiles:put name="view" value="compositeMember"/>
  </tiles:insert> (			
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
	</tiles:insert>	)
</div>