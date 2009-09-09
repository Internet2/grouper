<%-- @annotation@
		  Group types
--%><%--
  @author Gary Brown.
  @version $Id: GroupTypes.jsp,v 1.2 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>

<div class="pageBlurb">
	<grouper:message key="grouptypes.list.can"/>
</div>

<c:forEach var="groupType" items="${groupTypes}">
<tiles:insert definition="dynamicTileDef" flush="false">
				<tiles:put name="viewObject" beanName="groupType"/>
				<tiles:put name="view" value="schema-summary"/>
</tiles:insert>
</c:forEach>






