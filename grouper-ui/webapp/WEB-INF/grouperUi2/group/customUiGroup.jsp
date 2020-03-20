<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

              <div class="page-header blue-gradient span12" style="margin-left:0">
                <div class="row-fluid">

                  <div class="span10" style="margin-left: 0">
                    <c:set var="theHeader" value="${grouperRequestContainer.customUiContainer.textTypeToText['header']}"/>
                    ${ !grouper:isBlank(theHeader) ? theHeader : textContainer.text['guiCustomUiGroupHeader']}
                  </div>
                  <div class="span11" style="margin-left: 0.2em">
                    
                    <c:if test="${ grouperRequestContainer.customUiContainer.manager }">
                    
                      <c:set var="theManagerInstructions" value="${grouperRequestContainer.customUiContainer.textTypeToText['managerInstructions']}"/>
                      ${ !grouper:isBlank(theManagerInstructions) ? theManagerInstructions : textContainer.text['guiCustomUiGroupManagerInstructions']}
  
                      <br /><br />
                      <div id="add-block-container">
                        <div id="add-members">
                          <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                            <div class="control-group" id="add-member-control-group" aria-live="polite" aria-expanded="false">
                              <label for="groupAddMemberComboID" class="control-label">Person name or ID:</label>
                              <div class="controls">
                                <div id="add-members-container">
  
                                  <%-- placeholder: Enter the name of a person, group, or other entity --%>
                                  <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 45em" 
                                    value="${grouperRequestContainer.customUiContainer.userComboboxValue}"
                                    filterOperation="../app/UiV2Group.addMemberFilter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}"/>
                                  ${textContainer.text['customUiSearchLabelPreComboLink']}
                                  
                                </div>
                              </div>
                            </div>
                            
                            <div class="control-group">
                              <div class="controls">
                                <button onclick="ajax('../app/UiV2CustomUi.customUiGroupSubjectSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'add-members-form'}); return false;" 
                                  id="add-members-submit" type="submit" class="btn btn-primary" style="color:black; letter-spacing: .1em;">${textContainer.text['guiCustomUiSelectUser']}</button> 
                              </div>
                            </div>
                          </form>
                        </div>
                        
                      </div>
                      <br /><br />
                    </c:if>

                    <c:set var="theInstructions1" value="${grouperRequestContainer.customUiContainer.textTypeToText['instructions1']}"/>
                    ${ !grouper:isBlank(theInstructions1) ? theInstructions1 : ''}
                    
                    <br />


                  </div>
                </div>
              </div>

            <div class="row-fluid">
              <div class="span11" style="margin-left: 1em">
                <div id="messages"></div>

                <c:set var="theEnrollmentLabel" value="${grouperRequestContainer.customUiContainer.textTypeToText['enrollmentLabel']}"/>
                ${ !grouper:isBlank(theEnrollmentLabel) ? theEnrollmentLabel : ''}
                
                <br /><br />
                
                <c:choose>
                  <c:when test="${grouperRequestContainer.customUiContainer.enrollButtonShow}">
                  
                    <form class="form form-inline" id="enrollFormId">
                      <input name="memberId" type="hidden" value="${grouperRequestContainer.customUiContainer.member.id}"/> 
                      <input name="groupId" type="hidden" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}"/> 
                      <a id="show-add-block" href="javascript:void(0);" onclick="ajax('../app/UiV2CustomUi.joinGroup', {formIds: 'enrollFormId'}); return false;"
                          class="btn btn-medium btn-primary btn-block span2" role="button" style="margin-left: 0; color:black; letter-spacing: .1em;">
                       ${grouperRequestContainer.customUiContainer.enrollButtonText}
                      </a><br />
                    </form>
                  
                  </c:when>
                  
                  <c:otherwise>
                    <c:if test="${grouperRequestContainer.customUiContainer.unenrollButtonShow}">
                      <form class="form form-inline" id="unenrollFormId">
                      <input name="memberId" type="hidden" value="${grouperRequestContainer.customUiContainer.member.id}"/> 
                        <input name="groupId" type="hidden" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}"/> 
                        <a id="show-add-block" href="javascript:void(0);" onclick="ajax('../app/UiV2CustomUi.leaveGroup', {formIds: 'unenrollFormId'}); return false;"
                            class="btn btn-medium btn-primary btn-block span2" role="button" style="margin-left: 0; color:black; letter-spacing: .1em;">
                        ${grouperRequestContainer.customUiContainer.unenrollButtonText}
                        </a><br />
                      </form>
                    </c:if>
                  
                  </c:otherwise>
                
                </c:choose>

                <br /><br />
                <c:if test="${ grouperRequestContainer.customUiContainer.manager }">
                
                <br /><br />
                <h3>${textContainer.text['guiCustomUiUserEnvironmentHeader']}</h3>
                <br />
                <table class="table table-condensed table-striped" style="width: 1200px">
                  <tr>
                    <th>${textContainer.text['guiCustomUiTableVariableLabel']}</th>
                    <th>${textContainer.text['guiCustomUiTableVariableValue']}</th>
                    <th>${textContainer.text['guiCustomUiTableVariableName']}</th>
                    <th>${textContainer.text['guiCustomUiTableVariableType']}</th>
                    <th>${textContainer.text['guiCustomUiTableUserQueryType']}</th>
                    <th>${textContainer.text['guiCustomUiTableVariableDescription']}</th>
                  </tr>
                  <c:forEach items="${grouperRequestContainer.customUiContainer.customUiUserQueryDisplayBeans}" 
                          var="customUiUserQueryDisplayBean" >
                    <tr>
                      <td class="span5">${customUiUserQueryDisplayBean.label}</td>
                      <td>${customUiUserQueryDisplayBean.variableValue}</td>
                      <td>${customUiUserQueryDisplayBean.variableName}</td>
                      <td>${customUiUserQueryDisplayBean.variableType}</td>
                      <td>${customUiUserQueryDisplayBean.userQueryType}</td>
                      <td>${customUiUserQueryDisplayBean.description}</td>
                    
                    </tr>
                  </c:forEach>
                  
                </table>
                
                <br /><br /><br /><br />
                <h3>${textContainer.text['guiCustomUiScreenState']}</h3>
                <br />
                <table class="table table-condensed table-striped" style="width: 1200px">
                  <tr>
                    <th>${textContainer.text['guiCustomUiTableTextType']}</th>
                    <th>${textContainer.text['guiCustomUiTableTextIndex']}</th>
                    <th>${textContainer.text['guiCustomUiTableTextScript']}</th>
                    <th>${textContainer.text['guiCustomUiTableTextConfigured']}</th>
                    <th>${textContainer.text['guiCustomUiTableTextEvaluated']}</th>
                  </tr>
                  <c:forEach items="${grouperRequestContainer.customUiContainer.customUiEngine.customUiTextResults}" 
                          var="customUiTextResult" >
                    <tr>
                      <td>${customUiTextResult.customUiTextType.name}</td>
                      <td>${customUiTextResult.customUiTextConfigBean.index}</td>
                      <td>${grouper:escapeHtml(customUiTextResult.customUiTextConfigBean.script)}</td>
                      <td><grouper:abbreviateTextarea text="${customUiTextResult.customUiTextConfigBean.text}" showCharCount="200" /></td>
                      <td><grouper:abbreviateTextarea text="${customUiTextResult.textResult}"  showCharCount="200"/></td>
                    </tr>
                  </c:forEach>
                  
                </table>
                </c:if>                
              </div>
            </div>
            
            ${ grouperRequestContainer.customUiContainer.logCustomUiEngine }
            <!-- end group/viewGroup.jsp -->
