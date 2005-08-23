<%-- @annotation@
		  Tile which is embedded in other pages - 
		  displays child stems and groups and group
		  memberships of the curent node
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsFind.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute />
<div class="browseChildren">
	
		<tiles:importAttribute name="browseChildGroup"/>
		<c:set var="browseMode" scope="request" value="Find"/>
		<tiles:insert attribute="browseLocation"/>

		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
			<tiles:put name="view" value="findNew"/>
			<tiles:put name="headerView" value="browseStemsFindHeader"/>
			<tiles:put name="itemView" value="assignFoundMember"/>
			<tiles:put name="footerView" value="browseStemsFindFooter"/>
			<tiles:put name="pager" beanName="pager"/>
			<tiles:put name="skipText" value="${navMap['page.skip.children']} ${browseParent.displayExtension}"/>
			<tiles:put name="listInstruction" value="list.instructions.find-new"/>
		</tiles:insert>  
</div>
</grouper:recordTile>