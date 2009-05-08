<%-- @annotation@ 
			Form with links for moving/copying a stem and moving/copying stems and groups to a stem.
--%>
<%--
  @author shilen
  @version $Id: MovesCopiesLinks.jsp,v 1.1 2009-05-08 12:03:37 shilen Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
  <div class="sectionBody">
    <tiles:insert definition="showStemsLocationDef" /> 
    <div class="linkButton">
      <c:if test="${canCopyStem}">
        <html:link page="/populateCopyStem.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.copy" /></html:link>
        <br />
      </c:if>
      <c:if test="${canMoveStem}">
        <html:link page="/populateMoveStem.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.move" /></html:link>
        <br />
      </c:if>
      <c:if test="${canCopyOtherStemToStem}">
        <html:link page="/populateCopyOtherStemToStem.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.copy-other-stem-to-stem" /></html:link>
        <br />
      </c:if>
      <c:if test="${canMoveOtherStemToStem}">
        <html:link page="/populateMoveOtherStemToStem.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.move-other-stem-to-stem" /></html:link>
        <br />
      </c:if>
      <c:if test="${canCopyGroupToStem}">
        <html:link page="/populateCopyGroupToStem.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.copy-group-to-stem" /></html:link>
        <br />
      </c:if>
      <c:if test="${canMoveGroupToStem}">
        <html:link page="/populateMoveGroupToStem.do" name="stemMovesCopiesParams"><grouper:message bundle="${nav}" key="stems.action.move-group-to-stem" /></html:link>
        <br />
      </c:if>
      <br />
      <html:link page="/populate${linkBrowseMode}Groups.do">
        <grouper:message bundle="${nav}" key="stems.movesandcopies.cancel"/>
      </html:link>
    </div>
  </div>
</div>

