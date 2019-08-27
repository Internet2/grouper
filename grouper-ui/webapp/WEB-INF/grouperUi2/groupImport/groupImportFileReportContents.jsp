<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <input type="hidden" id="isImportFinished"
               value=${grouperRequestContainer.groupImportContainer.importProgress.finished} />
               
               <input type="hidden" id="totalEntriesInFile"
               value=${grouperRequestContainer.groupImportContainer.importProgress.totalEntriesInFile} />
               
               <input type="hidden" id="entriesProcessed"
               value=${grouperRequestContainer.groupImportContainer.importProgress.entriesProcessed} />

              <div class="span12">
                
                <c:if test="${grouperRequestContainer.groupImportContainer.importProgress.viewPrivilegeError}">
                  <p>${textContainer.text['groupImportGroupCantView']}</p>
                </c:if>
                
                <c:if test="${fn:length(grouperRequestContainer.groupImportContainer.importProgress.subjectNotFoundErrors) > 0 }">
                  
                  <p style="font-weight: bold;">${textContainer.text['groupFileImportSubjectErrorMessage']}</p>
                  
                  <c:forEach items="${grouperRequestContainer.groupImportContainer.importProgress.subjectNotFoundErrors}" var="subjectError">
                    <ul>${subjectError}</ul>
                  </c:forEach>
                  
                  <hr />
                
                </c:if>
                
                <div>
                
                  <c:forEach items="${grouperRequestContainer.groupImportContainer.importProgress.groupImportProgress}" var="entry" >
                    
                    <div>
                      <h4>
                        ${entry.key.linkWithIcon}
                      </h4>
                    </div>
                    <div>
                      <p>There were originally ${entry.value.originalCount} members</p>
                      <p>Added: ${entry.value.addedCount}</p>
                      <p>Deleted: ${entry.value.deletedCount}</p>
                    </div>
                    
                    <hr />
                    
                  </c:forEach>
                
                </div>
              
              </div>