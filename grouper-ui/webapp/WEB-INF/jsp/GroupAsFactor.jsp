<%-- @annotation@ 
			Top level JSP which shows where a group is present as a factor
--%><%--
  @author Gary Brown.
  @version $Id: GroupAsFactor.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef" controllerUrl="/prepareBrowsePath.do"/>
<h2 class="actionheader">
	<grouper:message bundle="${nav}" key="groups.heading.where-is-factor">
		<grouper:param value="${browseParent.displayExtension}"/>
	</grouper:message>
</h2>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="composites"/>
			<tiles:put name="view" value="compositesAsFactor"/>
			<tiles:put name="itemView" value="asFactor"/>
		</tiles:insert>
<div class="linkButton">
	<tiles:insert definition="callerPageButtonDef"/>
</div>
