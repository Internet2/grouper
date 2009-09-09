<%-- @annotation@
		  Query audit log
--%><%--
  @author Gary Brown.
  @version $Id: AuditQuery.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>

<div class="pageBlurb">
	<grouper:message key="audit.query.can"/>
</div>
<div class="buttonContainer">
<c:set var="auditbuttonTitle"><grouper:message key="cancel.to.caller-page-title.from-audit"/></c:set>
	<c:set var="auditButtonText"><grouper:message key="cancel.to.caller-page.from-audit"/></c:set>
	<tiles:insert definition="callerPageButtonDef">
		<tiles:put name="forceCallerPageId" beanName="forceCallerPageId"/>
		<tiles:put name="buttonTitle" beanName="auditButtonTitle"/>
		<tiles:put name="buttonText" beanName="auditButtonText"/>
	</tiles:insert></div>
<div class="section searchAudit">
<grouper:subtitle key="find.heading.audit-search">

 <a href="#" onclick="return grouperHideShow(event, 'auditForm');" 
      class="underline subtitleLink"><grouper:message key="find.search.audit.show-search-form" ignoreTooltipStyle="true"/></a>     
</grouper:subtitle>

<div class="pageBlurb sectionBody"><grouper:message key="${auditInfoKey}"><grouper:param value="${auditInfoEntity}" /></grouper:message></div>
<div id="auditForm0" class="sectionBody" >

<tiles:insert definition="auditSearchDef"/>
</div>
<p></p>
<div class="section auditResults"> 
<grouper:subtitle key="find.heading.audit-search-results"/>
<div class="sectionBody">
<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
			<tiles:put name="view" value="auditQueryResults"/>
			<tiles:put name="headerView" value="genericListHeader"/>
			<tiles:put name="itemView" value="queryResult"/>
			<tiles:put name="footerView" value="genericListFooter"/>
			<tiles:put name="pager" beanName="pager"/>
			<tiles:put name="useTable" value="true"/>
			<tiles:put name="headerRow"><tr align="left">
			<th><grouper:message key="audit.result.header.date"/></th>
			<th><grouper:message key="audit.result.header.actor"/></th>
			<th><grouper:message key="audit.result.header.engine"/></th>
			<th><grouper:message key="audit.result.header.summary"/></th>
			<c:if test="${extendedResults}">
			<th><grouper:message key="audit.result.header.duration"/></th>
			<th><grouper:message key="audit.result.header.query-count"/></th>
			<th><grouper:message key="audit.result.header.user-name"/></th>
			<th><grouper:message key="audit.result.header.server-name"/></th>
			<th><grouper:message key="audit.result.header.id"/></th>
			<th><grouper:message key="audit.result.header.description"/></th>
			</c:if>
			</tr></tiles:put>
			<tiles:put name="tableClass" value="auditQueryTable"/>
			<tiles:put name="noResultsMsg" value="${navMap[noResultsKey]}"/>
			 
			<tiles:put name="linkSeparator">  
				<tiles:insert definition="linkSeparatorDef" flush="false"/>
			</tiles:put>
		</tiles:insert>
		</div>
</div>






