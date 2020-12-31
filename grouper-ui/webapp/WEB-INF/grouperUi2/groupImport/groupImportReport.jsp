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
                  <small>
                    <c:choose>
                      <c:when test="${grouperRequestContainer.groupImportContainer.progressBean.complete}">
                        ${textContainer.text['groupImportReportSubheading']}
                      </c:when>
                      <c:otherwise>
                        <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['groupImportReportSubheading']}
                      </c:otherwise>
                    </c:choose>
                   </small>
                 </h1>
              </div>
            </div>

            <div class="row-fluid">
              <div class="span12">
                <p class="lead">${textContainer.text['groupImportReportPageSummary']}</p>
                ${grouperRequestContainer.groupImportContainer.report}
                <c:choose>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromSubject}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.groupImportContainer.subjectId}&sourceId=${grouperRequestContainer.groupImportContainer.sourceId}');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromGroup}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupImportContainer.groupId}');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
