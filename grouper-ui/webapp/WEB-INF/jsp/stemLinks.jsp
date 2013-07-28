<%-- @annotation@
		 Tile which displays a standard set of stem management links based
		 on the privileges of the current user for the current stem
--%><%--
  @author Gary Brown.
  @version $Id: stemLinks.jsp,v 1.12 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="section">
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endStemLinks" class="noCSSOnly"><grouper:message key="page.skip.stem-links"/></a>

<grouper:subtitle key="stems.heading.manage" />
<div class="sectionBody">
      <tiles:insert definition="browseStemsLocationDef" />


<br/>
<div class="linkButton">
<c:if test="${browsePrivs.stem && !empty currentLocation.id}">
<div style="margin-bottom: 10px;">
  <tiles:insert definition="selectStemPrivilegeDef"/>
</div>
</c:if>
<c:if test="${!currentLocation.isRootStem}">
<html:link page="/addSavedStem.do" name="saveStemParams"><grouper:message key="saved-stems.add.stem" /></html:link>
</c:if>
<c:if test="${browsePrivs.stem && !empty currentLocation.id}">
<html:link page="/populateEditStem.do"><grouper:message key="stems.action.edit"/></html:link>
</c:if>




<c:if test="${!stemHasChildren && browsePrivs.stem}"> 
<html:link page="/deleteStem.do" onclick="return confirm('${navMap['stems.delete.warn']}')"><grouper:message key="stems.action.delete" /></html:link>
</c:if>
<c:if test="${browsePrivs.stem}"> 
<html:link page="/populateCreateStem.do"><grouper:message key="stems.action.create"/></html:link>
</c:if>
<c:if test="${browsePrivs.create}"> 
<html:link page="/populateCreateGroup.do" ><grouper:message key="groups.action.create"/></html:link>
</c:if>
<c:if test="${showStemMovesCopies}">
<html:link page="/populateMovesCopiesLinks.do" name="stemMovesCopiesParams"><grouper:message key="stems.action.movesandcopies"/></html:link>
</c:if>

<c:if test="${browsePrivs.stem || browsePrivs.create}"> 
<jsp:useBean id="auditParams" class="java.util.HashMap" scope="page"></jsp:useBean>
		<c:set target="${auditParams}" property="origCallerPageId" value="${thisPageId}"/>
		<c:set target="${auditParams}" property="stemId" value="${currentLocation.id}"/>
		<html:link page="/userAudit.do"  name="auditParams">
			<grouper:message key="groups.action.audit"/>
		</html:link>

</c:if>
</div><!--linkbutton1-->
<br/>

<!--/stemLinks.jsp-->
</div>
<a name="endStemLinks" id="endStemLinks"></a>
</grouper:recordTile>
</div>
