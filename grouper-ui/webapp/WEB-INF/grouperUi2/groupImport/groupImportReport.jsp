<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myGroupsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['groupImportMembersBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['groupImportTitle'] }
                <br />
                <small>${textContainer.text['groupImportReportSubheading']}</small></h1>
              </div>
            </div>

            <div class="row-fluid">
              <div class="span12">
                <p class="lead">${textContainer.text['groupImportReportPageSummary']}</p>
                <ul>
                  <c:forEach items="${grouperRequestContainer.groupImportContainer.guiGroups}" var="guiGroup" >
                    <li>${guiGroup.linkWithIcon}</li>
                  </c:forEach>
                </ul>
                <%-- loop through all the groups and give each report --%>
                <c:forEach items="${grouperRequestContainer.groupImportContainer.guiGroups}" var="guiGroup" >
                  <hr />
                  <h4>${guiGroup.linkWithIcon}</h4>
                  ${grouperRequestContainer.groupImportContainer.reportForGroupNameMap[guiGroup.group.name]}
                </c:forEach>
                <c:choose>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromSubject}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromGroup}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
