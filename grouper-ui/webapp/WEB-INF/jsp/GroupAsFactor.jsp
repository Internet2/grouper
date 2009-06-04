<%-- @annotation@ 
			Top level JSP which shows where a group is present as a factor
--%><%--
  @author Gary Brown.
  @version $Id: GroupAsFactor.jsp,v 1.3 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef" controllerUrl="/prepareBrowsePath.do"/>
  <grouper:subtitle key="groups.heading.where-is-factor" 
    param1="${browseParent.displayExtension}" />
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="composites"/>
			<tiles:put name="view" value="compositesAsFactor"/>
			<tiles:put name="itemView" value="asFactor"/>
		</tiles:insert>
<div class="linkButton">
	<tiles:insert definition="callerPageButtonDef"/>
</div>
