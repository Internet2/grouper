<%@ include file="../assetsJsp/commonTaglib.jsp" %>

<ul class="nav nav-tabs">

  <%-- Stem Contents tab --%>
  <c:choose>
    <c:when test="${grouperCurrentTab == 'contents'}">
      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['stemContents'] }</a></li>
    </c:when>
    <c:otherwise>
      <li><a role="tab" href="?operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
    </c:otherwise>
  </c:choose>

  <%-- Privileges tab (only if canAdminPrivileges) --%>
  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
    <c:choose>
      <c:when test="${grouperCurrentTab == 'privileges'}">
        <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['stemPrivileges'] }</a></li>
      </c:when>
      <c:otherwise>
        <li><a role="tab" href="?operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
      </c:otherwise>
    </c:choose>
  </c:if>

  <%-- More tab include (conditional) --%>
  <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
    <%@ include file="../stem/stemMoreTab.jsp" %>
  </c:if>

</ul>
