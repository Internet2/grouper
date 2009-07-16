<%-- @annotation@
		 Tile which displays a standard set of stem management links based
		 on the privileges of the current user for the current stem
--%><%--
  @author Gary Brown.
  @version $Id: stemLinks.jsp,v 1.11 2009-07-16 11:33:34 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="section">
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endStemLinks" class="noCSSOnly"><grouper:message bundle="${nav}" key="page.skip.stem-links"/></a>

<grouper:subtitle key="stems.heading.manage" />
<div class="sectionBody">
      <tiles:insert definition="browseStemsLocationDef" />


<br/>
<div class="linkButton">
<c:if test="${browsePrivs.STEM && !empty currentLocation.id}">
<div style="margin-bottom: 10px;">
  <tiles:insert definition="selectStemPrivilegeDef"/>
</div>
</c:if>
<c:if test="${!currentLocation.isRootStem}">
<html:link page="/addSavedStem.do" name="saveStemParams"><grouper:message bundle="${nav}" key="saved-stems.add.stem" /></html:link>
</c:if>
<c:if test="${browsePrivs.STEM && !empty currentLocation.id}">
<html:link page="/populateEditStem.do"><grouper:message bundle="${nav}" key="stems.action.edit"/></html:link>
</c:if>




<c:if test="${!stemHasChildren && browsePrivs.STEM}"> 
<html:link page="/deleteStem.do" onclick="return confirm('${navMap['stems.delete.warn']}')"><grouper:message bundle="${nav}" key="stems.action.delete" /></html:link>
</c:if>
<c:if test="${browsePrivs.STEM}"> 
<html:link page="/populateCreateStem.do"><grouper:message bundle="${nav}" key="stems.action.create"/></html:link>
</c:if>
<c:if test="${browsePrivs.CREATE}"> 
<html:link page="/populateCreateGroup.do" ><grouper:message bundle="${nav}" key="groups.action.create"/></html:link>
</c:if>
<c:if test="${showStemMovesCopies}">
<html:link page="/populateMovesCopiesLinks.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.movesandcopies"/></html:link>
</c:if>

<c:if test="${browsePrivs.STEM || browsePrivs.CREATE}"> 
<jsp:useBean id="auditParams" class="java.util.HashMap" scope="page"></jsp:useBean>
		<c:set target="${auditParams}" property="origCallerPageId" value="${thisPageId}"/>
		<c:set target="${auditParams}" property="stemId" value="${currentLocation.id}"/>
		<html:link page="/userAudit.do"  name="auditParams">
			<grouper:message bundle="${nav}" key="groups.action.audit"/>
		</html:link>

</c:if>
</div><!--linkbutton1-->
<br/>

<!--/stemLinks.jsp-->
</div>
<a name="endStemLinks" id="endStemLinks"></a>
</grouper:recordTile>
</div>
