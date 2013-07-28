<%-- @annotation@
		  Dynamic tile which renders a group found
		  by a search (not 'Find' mode) as a link to 
		  the group summary and shows the appropriate privileges for the user selected through SubjectSummary page
--%><%--
  @author Gary Brown.
  @version $Id: groupSearchResultWithPrivsView.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<jsp:useBean id="params" class="java.util.HashMap"/>
<c:set target="${params}" property="subjectId" value="${subjectOfInterest.id}"/>
<c:set target="${params}" property="subjectType" value="${subjectOfInterest.type}"/>
<c:set target="${params}" property="sourceId" value="${subjectOfInterest.source.id}"/>
<c:set target="${params}" property="asMemberOf" value="${viewObject.groupId}"/>
<c:set target="${params}" property="callerPageId" value="${thisPageId}"/>

<tr><td>
<c:set target="${viewObject}" property="advancedSearch" value="false"/>
<html:link page="/populateGroupMember.do" name="params" title="${navMap['groups.access.modify-all.title']}">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="searchResultItem"/>
  </tiles:insert>
  </td>
  <c:forTokens var="priv" items="member admin updaet read view optin optout groupAttrRead groupAttrUpdate" delims=" ">
  	<td>
	<c:set var="hasPriv" value="false"/>
	<c:choose>
		<c:when test="${viewObject.subjectPrivs.subject[priv]}">
			<c:set var="hasPriv" value="Subject"/> 
		</c:when>
		<c:when test="${viewObject.subjectPrivs.effectivePrivs[priv]}">
			<c:set var="hasPriv" value="Effective"/>
		</c:when>
		<c:when test="${viewObject.subjectPrivs.GrouperAll[priv]}">
			<c:set var="hasPriv" value="GrouperAll"/>
		</c:when>
	</c:choose>
	
	<c:if test="${hasPriv != 'false'}">
		<span class="has<c:out value="${hasPriv}"/>PrivForGroup"><grouper:message key="${priv}"/></span>
	</c:if>
	</td>
  </c:forTokens>
  </tr>
</html:link>
