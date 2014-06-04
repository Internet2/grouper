<%-- @annotation@
		  Dynamic tile used to render a GroupType - for group summary
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeSchemaSummaryView.jsp,v 1.2 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>


<div class="section">
<table border="0" cellpadding="0" cellspacing="0" class="actionheaderContainer" width="100%"><tr><td><span id="grouptype-<c:out value="${viewObject.uuid}"/>" class="actionheader"><c:out value="${viewObject.name}"/></span>
</td></tr></table>
<div class="sectionBody">
<div class="buttonContainer">
<c:set var="cancelButtonTitle"><grouper:message key="cancel.to.caller-page-title.from-group-types"/></c:set>
	<c:set var="cancelButtonText"><grouper:message key="cancel.to.caller-page.from-group-types"/></c:set>
	<tiles:insert definition="callerPageButtonDef">
		<tiles:put name="forceCallerPageId" beanName="forceCallerPageId"/>
		<tiles:put name="buttonTitle" beanName="cancelButtonTitle"/>
		<tiles:put name="buttonText" beanName="cancelButtonText"/>
	</tiles:insert></div><br/>
<table class="groupTypeFields">
<tr>
		<th><grouper:message key="grouptypes.label.field"/></th>
		<th><grouper:message key="grouptypes.label.type"/></th>
		<th><grouper:message key="grouptypes.label.read-priv"/></th>
		<th><grouper:message key="grouptypes.label.write-priv"/></th>
	
	</tr>
<c:forEach var="field" items="${viewObject.fields}">
<tiles:insert definition="dynamicTileDef" flush="false">
				<tiles:put name="viewObject" beanName="field"/>
				<tiles:put name="view" value="schema"/>
</tiles:insert>
</c:forEach>
</table>
<c:if test="${empty param.callerPageId}"><br/>
<a href="userAudit.do?schemaChangesOnly=true&groupTypeId=<c:out value="${viewObject.uuid}"/>&callerPageId=<c:out value="${thisPageId}"/>"><grouper:message key="grouptypes.action.audit"/></a>
</c:if>
</div>

</div>
