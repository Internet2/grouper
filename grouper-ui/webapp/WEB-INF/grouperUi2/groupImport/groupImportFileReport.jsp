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
            
            <script>
              
              var timeout; 
              function showImportProgress() {
                var progressSummary = '${textContainer.text["groupFileImportProgressSummary"] }';
                var totalEntriesInFile = $("#totalEntriesInFile").val();
                var entriesProcessed = $("#entriesProcessed").val();
                progressSummary = progressSummary.replace("$$entriesProcessed$$", entriesProcessed);
                progressSummary = progressSummary.replace("$$totalEntries$$", totalEntriesInFile);
                $("#progressMessageId").text(progressSummary);
                  
                var isFinished = $("#isImportFinished").val();
                if (isFinished === 'true') {
                  var processingDone = '${textContainer.text["groupFileImportDoneProcessingMessage"] }';
                  $("#progressMessageId").append(" "+processingDone);
                  return;
                }
                
                ajax('../app/UiV2GroupImport.groupImportProgress?key=${grouperRequestContainer.groupImportContainer.importProgress.key}');
                timeout = setTimeout(showImportProgress, 2000);
                
              }
              
              showImportProgress();
              
            </script>
            
            <div>
              <p class="lead">${textContainer.text['groupFileImportProgressReport']}</p>
            </div>
            
            <div>
              <p id="progressMessageId" style="font-weight: bold;"></p>
            </div>
            
            <hr />
            
            <div class="row-fluid" id="importFileProgressId">
              <%@ include file="groupImportFileReportContents.jsp"%>
            </div>
            
            <div class="row-fluid">
              <div class="span12">
                <c:choose>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromSubject}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupImportContainer.importFromGroup}">
                    <a href="#" onclick="clearTimeout(timeout); return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="clearTimeout(timeout); return guiV2link('operation=UiV2Main.indexMain');"
                       class="btn btn-primary pull-right">${textContainer.text['groupImportReportOkButton']}</a>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
