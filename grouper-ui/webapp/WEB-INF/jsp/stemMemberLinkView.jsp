<%-- @annotation@
			Form which allows user to change an individual
			Subject`s membership of / privileges for, the
			active stem
--%><%--
  @author Gary Brown.
  @version $Id: stemMemberLinkView.jsp,v 1.2 2005-11-08 16:25:06 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set target="${viewObject}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${pagerParams}" property="chainGroupIds" value="${viewObject.chainGroupIds}"/>
<c:set target="${pagerParams}" property="stemId" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>
<c:set var="linkText"><fmt:message bundle="${nav}" key="stems.membership.view-privileges"/></c:set>
 
		<html:link page="/populateStemMember.do" name="pagerParams" title="${navMap['browse.assign']} ${viewObject.desc}">
 		<c:out value="${linkText}" escapeXml="false"/></html:link>
 / <html:link page="/populateSubjectSummary.do" name="viewObject">
				<fmt:message bundle="${nav}" key="stems.membership.view-subject-attributes"/>
			</html:link>
  	<fmt:message bundle="${nav}" key="stems.membership.for"/>
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="stemMember"/>
  </tiles:insert>
 
