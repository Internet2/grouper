<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members
--%><%--
  @author Gary Brown.
  @version $Id: memberLinkView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set target="${pagerParams}" property="chainGroupIds" value="${viewObject.chainGroupIds}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>
<c:set var="linkText"><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="groupMember"/>
  </tiles:insert></c:set>
  <c:choose>

  	<c:when test="${!empty viewObject.via}">
		<c:set var="viaText"><tiles:insert definition="dynamicTileDef" flush="false">
	  	<tiles:put name="viewObject" beanName="viewObject" beanProperty="via"/>
	  	<tiles:put name="view" value="viaGroup"/>
  		</tiles:insert>
		</c:set>
		<c:out value="${linkText}" escapeXml="false"/>  
		<%--<html:link page="/populateGroupMember.do" name="viewObject" property="via" title="${navMap['groups.membership.through.title']} ${viewObject.via.desc}">
 		<c:out value="${viaText}" escapeXml="false"/></html:link> 
		
		<html:link page="/populateChain.do" name="pagerParams" title="${navMap['groups.membership.chain.title']} ${viewObject.desc}">
 <fmt:message bundle="${nav}" key="groups.membership.chain"/></html:link>--%>
		
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="viewObject" title="${navMap['browse.assign']} ${viewObject.desc}">
 		<c:out value="${linkText}" escapeXml="false"/></html:link>
	</c:otherwise>
  </c:choose>
 
