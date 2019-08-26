<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <input type="hidden" id="isImportFinished"
               value=${grouperRequestContainer.groupImportContainer.importProgress.finished} />

              <div class="span12">
                
                <c:if test="${grouperRequestContainer.groupImportContainer.importProgress.viewPrivilegeError}">
                  <p>${textContainer.text['groupImportGroupCantView']}</p>
                </c:if>
                
                <c:if test="${fn:length(grouperRequestContainer.groupImportContainer.importProgress.subjectErrors) > 0 }">
                
                  <c:forEach items="${grouperRequestContainer.groupImportContainer.importProgress.subjectErrors}" var="subjectError">
                  
                    <div>
                      There is error on row ${subjectError.rowNumber}
                    </div>
                  
                  </c:forEach>
                
                </c:if>
                
                <div>
                
                  <p class="lead">${textContainer.text['groupImportReportPageSummary']}</p>
                  
                  <c:forEach items="${grouperRequestContainer.groupImportContainer.importProgress.groupImportProgress}" var="entry" >
                    
                    <div>
                      <b>
                        ${entry.key.linkWithIcon}
                      </b>
                    </div>
                    <div>
                      <p>There were originally ${entry.value.originalCount} members</p>
                      <p>Added: ${entry.value.addedCount}</p>
                      <p>Deleted: ${entry.value.deletedCount}</p>
                    </div>
                    
                  </c:forEach>
                
                </div>
              
              </div>