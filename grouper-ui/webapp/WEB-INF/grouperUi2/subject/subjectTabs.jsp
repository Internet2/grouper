<%@ include file="../assetsJsp/commonTaglib.jsp" %>

<ul class="nav nav-tabs">

  <%-- Subject Memberships tab --%>
  <c:choose>
    <c:when test="${grouperCurrentTab == 'memberships'}">
      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectMembershipsTab'] }</a></li>
    </c:when>
    <c:otherwise>
      <li><a role="tab" href="?operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
    </c:otherwise>
  </c:choose>

  <%-- Local entity privileges (only for grouperEntities and canAdmin) --%>
  <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.groupContainer.canAdmin}">
    <c:choose>
      <c:when test="${grouperCurrentTab == 'localEntityPrivileges'}">
        <li class="active"><a aria-selected="true" href="#" onclick="return false;"
          >${textContainer.text['groupPrivilegesTab'] }</a></li>
      </c:when>
      <c:otherwise>
        <li><a role="tab" href="?operation=UiV2LocalEntity.localEntityPrivileges&groupId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2LocalEntity.localEntityPrivileges&groupId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
      </c:otherwise>
    </c:choose>
  </c:if>



  <%-- Subject Privileges tab --%>
  <c:choose>
    <c:when test="${grouperCurrentTab == 'groupPrivileges'}">
      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
    </c:when>
    <c:otherwise>
      <li><a role="tab" href="?operation=UiV2Subject.thisSubjectsGroupPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.thisSubjectsGroupPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
    </c:otherwise>
  </c:choose>

  <%-- Subject Stem Privileges tab --%>
  <c:choose>
    <c:when test="${grouperCurrentTab == 'stemPrivileges'}">
      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
    </c:when>
    <c:otherwise>
      <li><a role="tab" href="?operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
    </c:otherwise>
  </c:choose>

  <%-- Subject AttributeDef Privileges tab --%>
  <c:choose>
    <c:when test="${grouperCurrentTab == 'attributeDefPrivileges'}">
      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
    </c:when>
    <c:otherwise>
      <li><a role="tab" href="?operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
    </c:otherwise>
  </c:choose>

  <%-- Optional More tab include --%>
  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
    <%@ include file="subjectMoreTab.jsp" %>
  </c:if>

</ul>
