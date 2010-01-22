<%-- @annotation@
		  	Standard tile which displays links which allow a user to show
		 	initial stems (if configured) and toggle whether to show the group 
		 	hierarchy or a flat list of groups or stems
--%>
<%--
  @author Gary Brown.
  @version $Id: flatten.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic"
  tile="${requestScope['javax.servlet.include.servlet_path']}"
>
  <tiles:importAttribute />
  <c:choose>
    <c:when test="${isFlat}">
      <c:set var="makeFlatFalse" value="false" />
      <html:link styleClass="underline subtitleLink" page="/${pageName}.do" paramId="flat" paramName="makeFlatFalse">
        <grouper:message key="${flattenType}.action.unflatten" />
      </html:link>
    </c:when>
    <c:otherwise>
      <c:set var="makeFlatTrue" value="true" />
      <html:link styleClass="underline subtitleLink" page="/${pageName}.do" paramId="flat" paramName="makeFlatTrue">
        <grouper:message key="${flattenType}.action.flatten" />
      </html:link>
    </c:otherwise>
  </c:choose>
  <c:if test="${isQuickLinks && empty initialStems}">
    <c:set var="reset" value="Y" />
    &nbsp; <html:link styleClass="underline subtitleLink" page="/${pageName}.do" paramId="resetBrowse" paramName="reset">
      <grouper:message key="find.browse.return-to-quick-links" />
    </html:link>
  </c:if>
  <c:if test="${isQuickLinks && !empty initialStems}">
    <c:set var="reset" value="Y" />
    &nbsp; <html:link styleClass="underline subtitleLink" page="/${pageName}.do" paramId="hideQuickLinks" paramName="reset">
      <grouper:message key="find.browse.hide-quick-links" />
    </html:link>
  </c:if>
</grouper:recordTile>