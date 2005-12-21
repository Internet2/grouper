<%-- @annotation@
		 Tile which displays a standard set of stem management links based
		 on the privileges of the current user for the current stem
--%><%--
  @author Gary Brown.
  @version $Id: stemLinks.jsp,v 1.3 2005-12-21 15:52:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endStemLinks" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.stem-links"/></a>
<c:if test="${browsePrivs.STEM || browsePrivs.CREATE}">

<h2 class="actionheader">
<fmt:message bundle="${nav}" key="stems.heading.manage"/>
</h2>
<br/>
<div class="linkButton">
<c:if test="${browsePrivs.STEM && !empty currentLocation.id}">
<tiles:insert definition="selectStemPrivilegeDef"/>
</c:if>
<c:if test="${browsePrivs.STEM && !empty currentLocation.id}">
<html:link page="/populateEditStem.do"><fmt:message bundle="${nav}" key="stems.action.edit"/></html:link>

</c:if>


<c:if test="${!stemHasChildren && browsePrivs.STEM}"> 
<!-- <html:link page="/deleteStem.do"><fmt:message bundle="${nav}" key="stems.action.delete"/></html:link> -->
</c:if>
<c:if test="${browsePrivs.STEM}"> 
<html:link page="/populateCreateStem.do"><fmt:message bundle="${nav}" key="stems.action.create"/></html:link>
</c:if>
<c:if test="${browsePrivs.CREATE}"> 
<html:link page="/populateCreateGroup.do" ><fmt:message bundle="${nav}" key="groups.action.create"/></html:link>
</c:if>
</div><!--linkbutton1-->
<br/>
</c:if>
<!--/stemLinks.jsp-->

<a name="endStemLinks" id="endStemLinks"></a>
</grouper:recordTile>