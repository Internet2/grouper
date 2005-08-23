<%-- @annotation@
		 	Tile inserted in browseStemsDef if an 
			InitialStems plugin is defined and that
			mode is selected
--%><%--
  @author Gary Brown.
  @version $Id: initialStems.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<fmt:message bundle="${nav}" key="groups.message.showing-initial-stems"/>
<ul><c:forEach var="stem" items="${initialStems}">
	<li><tiles:insert definition="dynamicTileDef" flush="false">
			<tiles:put name="view" value="browseHierarchy"/>
			<tiles:put name="viewObject" beanName="stem"/>
		</tiles:insert></li>
</c:forEach>
</ul>
<br/>
</grouper:recordTile>
