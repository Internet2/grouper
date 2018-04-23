<%-- @annotation@
		  Dynamic tile used to show how a Subject is a member of a left / right group in a composite
--%><%--
  @author Gary Brown.
  @version $Id: compositeGroupChainMemberView.jsp,v 1.1 2006-07-06 15:12:34 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:choose>
	<c:when test="${!empty viewObject.membership}">
		<tiles:insert definition="dynamicTileDef" flush="false">
	  		<tiles:put name="viewObject" beanName="viewObject" beanProperty="membership"/>
	  		<tiles:put name="view" value="compositeMember"/>
		</tiles:insert>
	</c:when>
	<c:otherwise>
		<tiles:insert definition="dynamicTileDef" flush="false">
	  		<tiles:put name="viewObject" beanName="viewObject"/>
	  		<tiles:put name="view" value="compositeMember"/>
		</tiles:insert>
	</c:otherwise>
</c:choose>
