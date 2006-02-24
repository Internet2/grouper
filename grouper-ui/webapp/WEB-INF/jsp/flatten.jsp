<%-- @annotation@
		  	Standard tile which displays links which allow a user to show
		 	initial stems (if configured) and toggle whether to show the group 
		 	hierarchy or a flat list of groups or stems
--%><%--
  @author Gary Brown.
  @version $Id: flatten.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute />
<div class="linkButton">
	<c:choose>
		<c:when test="${browseMode=='Create'}">
			<c:set var="flattenType" value="stems"/>
		</c:when>
		<c:otherwise>
			<c:set var="flattenType" value="groups"/>
		</c:otherwise>
	</c:choose>
	<%--<c:choose>
		<c:when test="${empty initialStems}">--%>
			<c:choose>
				<c:when test="${isFlat}">
					<c:set var="makeFlatFalse" value="false"/>
					<html:link page="/${pageName}.do" paramId="flat" paramName="makeFlatFalse"><fmt:message bundle="${nav}" key="${flattenType}.action.unflatten"/></html:link>
				</c:when>
				<c:otherwise>
					<c:set var="makeFlatTrue" value="true"/>
					<html:link page="/${pageName}.do" paramId="flat" paramName="makeFlatTrue"><fmt:message bundle="${nav}" key="${flattenType}.action.flatten"/></html:link>
				</c:otherwise>
			</c:choose>
			<c:if test="${isQuickLinks && empty initialStems}">
			<c:set var="reset" value="Y"/>
			<html:link page="/${pageName}.do" paramId="resetBrowse" paramName="reset"><fmt:message bundle="${nav}" key="find.browse.return-to-quick-links"/></html:link>
			</c:if>
		<%--</c:when>
		<c:otherwise>--%>
		<c:if test="${isQuickLinks && !empty initialStems}">
		<c:set var="reset" value="Y"/>
			<html:link page="/${pageName}.do" paramId="hideQuickLinks" paramName="reset"><fmt:message bundle="${nav}" key="find.browse.hide-quick-links"/></html:link>
		</c:if>
		</div><!--linkbutton-->
		<%--</c:otherwise>
	</c:choose> 
	
	--%>


<br/>
</grouper:recordTile>