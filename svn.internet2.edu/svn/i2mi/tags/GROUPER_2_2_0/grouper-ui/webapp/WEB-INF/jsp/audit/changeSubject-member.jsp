<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: changeSubject-member.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:out value="${viewObject.fields.oldSourceId}"/>.<c:out value="${viewObject.fields.oldSubjectId}"/> -&gt;
<c:out value="${viewObject.fields.newSourceId}"/>.<c:out value="${viewObject.fields.newSubjectId}"/>
<grouper:message key="audit.result.label.for"/><br/>
<c:choose>
	<c:when test='${viewObject.fields.memberIdChanged=="F"}'>
	<c:set var="subject" value="${viewObject.fieldObjects.oldMemberId}"/>
	<c:set var="memberIdName">oldMemberId</c:set>
	<c:set var="idChangedMessage"><grouper:message key="audit.result.label.member-id-did-not-change"/></c:set>
	</c:when>
	<c:otherwise>
	<c:set var="subject" value="${viewObject.fieldObjects.newMemberId}"/>
	<c:set var="memberIdName">newMemberId</c:set>
	<c:set var="idChangedMessage"><grouper:message key="audit.result.label.member-id-did-change"/></c:set>
	</c:otherwise>
</c:choose>

<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
<c:choose>
    	<c:when test="${!empty subject }">
    
    <c:set target="${linkParams}" property="callerPageId" value="${thisPageId}"/>
	<c:set target="${linkParams}" property="subjectId" value="${subject.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${subject.subjectType}"/>
	<c:set target="${linkParams}" property="sourceId" value="${subject.sourceId}"/>
    <c:set var="linkTitle"><grouper:message key="audit.result.label.subject" tooltipDisable="true"/></c:set>
    <tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="subject"/>
		  <tiles:put name="view" value="subjectSummaryLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
		  <tiles:put name="linkTitle" beanName="linkTitle"/>
	</tiles:insert></c:when>
		<c:otherwise>
		<grouper:message key="audit.result.label.unavailable"/> 
		(<c:out value="${viewObject.fields[memberIdName]}"/>)
		</c:otherwise>
	</c:choose> 
 - <c:out value="${idChangedMessage}"/>
 <br/>
