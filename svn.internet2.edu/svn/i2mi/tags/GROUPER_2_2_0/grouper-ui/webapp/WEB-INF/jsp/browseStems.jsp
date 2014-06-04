<%-- @annotation@
		  Tile which is embedded in other pages - 
		  displays child stems and groups of the 
		  active stem
--%><%--
  @author Gary Brown.
  @version $Id: browseStems.jsp,v 1.4 2008-10-10 10:16:04 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${!empty message || !empty messages}">
                    <tiles:insert definition="messageDef" />   
            </c:if>
<div class="sectionBody">
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:choose>
	<c:when test="${!empty initialStems}">
		<tiles:insert definition="initialStemsDef"/>
	</c:when>
	<c:otherwise>
		<tiles:insert attribute="browseLocation" controllerUrl="/prepareBrowsePath.do"/>
		<div class="browseChildren">
			<tiles:insert definition="dynamicTileDef">
				<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
				<tiles:put name="view" value="browse"/>
				<tiles:put name="headerView" value="browseHeader"/>
				<tiles:put name="itemView" value="browseHierarchy"/>
				<tiles:put name="footerView" value="browseFooter"/>
				<tiles:put name="pager" beanName="pager"/>
				<tiles:put name="skipText" value="${navMap['page.skip.children']} ${browseParent.displayExtension}"/>
				<tiles:put name="listInstruction" value="list.instructions.browse"/>
			</tiles:insert>
		</div>
	</c:otherwise>
	</c:choose>
</grouper:recordTile>
</div>