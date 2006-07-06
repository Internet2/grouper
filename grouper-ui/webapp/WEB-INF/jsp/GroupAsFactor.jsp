<%-- @annotation@ 
			Top level JSP which shows where a group is present as a factor
--%><%--
  @author Gary Brown.
  @version $Id: GroupAsFactor.jsp,v 1.1 2006-07-06 15:20:57 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef" controllerUrl="/prepareBrowsePath.do"/>
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.where-is-factor">
		<fmt:param value="${browseParent.displayExtension}"/>
	</fmt:message>
</h2>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="composites"/>
			<tiles:put name="view" value="compositesAsFactor"/>
			<tiles:put name="itemView" value="asFactor"/>
		</tiles:insert>
<div class="linkButton">
	<tiles:insert definition="callerPageButtonDef"/>
</div>
