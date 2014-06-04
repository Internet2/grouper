<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members which can be removed
--%><%--
  @author Gary Brown.
  @version $Id: removableMembershipView.jsp,v 1.2 2008-11-10 09:36:51 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:choose>
	<c:when test="${!empty viewObject.subject}">
<input type="checkbox" name="subjectIds" value="<c:out value="${viewObject.subject.id}"/>"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="viewObject" />
	  			<tiles:put name="view" value="membershipInfo"/>
  			</tiles:insert>
</c:when>
<c:otherwise>
Problem rendering member <c:out value="${viewObject.memberUuid}"/>
</c:otherwise>
</c:choose>