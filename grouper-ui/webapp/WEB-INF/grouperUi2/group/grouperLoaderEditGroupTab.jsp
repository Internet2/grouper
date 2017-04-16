<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
              
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                <div class="row-fluid">
                  <div class="lead span10">${textContainer.text['grouperLoaderEditGroupDecription'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <form class="form-inline form-small form-filter" id="editLoaderFormId">
                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderHasLoaderId">${textContainer.text['grouperLoaderHasLoaderLabel']}</label></strong></td>
                        <td>
                          <select name="grouperLoaderHasLoaderName" id="grouperLoaderHasLoaderId" style="width: 25em"
                            onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                            <option value="false" ${grouperRequestContainer.grouperLoaderContainer.editLoaderIsLoader ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['grouperLoaderNoDoesNotHaveLoaderLabel']}</option>
                            <option value="true" ${grouperRequestContainer.grouperLoaderContainer.editLoaderIsLoader ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperLoaderYesHasLoaderLabel']}</option>
                          </select>
                          <br />
                          <span class="description">${textContainer.text['grouperLoaderHasLoaderDescription']}</span>
                        </td>
                      </tr>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowLoaderType}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderTypeId">${textContainer.text['grouperLoaderSourceType']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <select name="grouperLoaderTypeName" id="grouperLoaderTypeId" style="width: 10em"
                                onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                                <option value="" ></option>
                                <option value="SQL" ${grouperRequestContainer.grouperLoaderContainer.editLoaderType == 'SQL' ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['grouperLoaderSql']}</option>
                                <option value="LDAP" ${grouperRequestContainer.grouperLoaderContainer.editLoaderType == 'LDAP' ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['grouperLoaderLdap']}</option>
                              </select>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderSourceType__',grouperRequestContainer.grouperLoaderContainer.editLoaderType)]}</span>
                          </td>
                        </tr>
                      </c:if>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowSqlLoaderType}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlTypeId">${textContainer.text['grouperLoaderSqlLoaderType']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <select name="grouperLoaderSqlTypeName" id="grouperLoaderSqlTypeId" style="width: 40em"
                                onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                                <option value="" ></option>
                                <option value="SQL_SIMPLE" ${grouperRequestContainer.grouperLoaderContainer.editLoaderSqlType == 'SQL_SIMPLE' ? 'selected="selected"' : '' } 
                                  >${textContainer.textEscapeXml['grouperLoaderSqlLoaderTypeOption__SQL_SIMPLE']}</option>
                                <option value="SQL_GROUP_LIST" ${grouperRequestContainer.grouperLoaderContainer.editLoaderSqlType == 'SQL_GROUP_LIST' ? 'selected="selected"'  : '' }
                                  >${textContainer.textEscapeXml['grouperLoaderSqlLoaderTypeOption__SQL_GROUP_LIST']}</option>
                              </select>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderSqlLoaderType__',grouperRequestContainer.grouperLoaderContainer.editLoaderSqlType)]}</span>
                          </td>
                        </tr>
                      </c:if>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowSqlDatabaseName}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlDatabaseNameId">${textContainer.text['grouperLoaderDatabaseName']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <select name="grouperLoaderSqlDatabaseNameName" id="grouperLoaderSqlDatabaseNameId" style="width: 40em"
                                onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                                <option value="" ></option>
                                <c:forEach items="${grouperRequestContainer.grouperLoaderContainer.sqlDatabaseNames}" var="sqlDatabaseName">
                                  <option value="${grouper:escapeJavascript(sqlDatabaseName.id)}" ${grouperRequestContainer.grouperLoaderContainer.editLoaderSqlDatabaseName == sqlDatabaseName.id ? 'selected="selected"' : '' } 
                                    >${grouper:escapeJavascript(sqlDatabaseName.name)}</option>
                                </c:forEach>
                              </select>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderSqlDatabaseNameText)}<br />
                            ${textContainer.text['grouperLoaderDatabaseNameDescription']}</span>
                          </td>
                        </tr>
                      </c:if>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowSqlQuery}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlQueryId">${textContainer.text['grouperLoaderSqlQuery']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <input type="text" style="width: 60em" value="${grouper:escapeJavascript(grouperRequestContainer.grouperLoaderContainer.editLoaderSqlQuery)}"
                                 name="grouperLoaderSqlQueryName" id="grouperLoaderSqlQueryId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">
                            ${textContainer.text[grouper:concat2('grouperLoaderSqlQueryDescription__',grouperRequestContainer.grouperLoaderContainer.editLoaderSqlType)]}</span>
                          </td>
                        </tr>
                      </c:if>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowFields}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderScheduleTypeId">${textContainer.text['grouperLoaderSqlScheduleType']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <select name="editLoaderScheduleTypeName" id="editLoaderScheduleTypeId" style="width: 40em"
                                onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                                <option value="CRON" ${grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == 'CRON' ? 'selected="selected"' : '' } 
                                  >${textContainer.textEscapeXml['grouperLoaderSqlScheduleTypeOption__CRON']}</option>
                                <option value="START_TO_START_INTERVAL" ${grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == 'START_TO_START_INTERVAL' ? 'selected="selected"'  : '' }
                                  >${textContainer.textEscapeXml['grouperLoaderSqlScheduleTypeOption__START_TO_START_INTERVAL']}</option>
                              </select>
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderSqlScheduleType__',grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType)]}</span>
                          </td>
                        </tr>

                        <c:choose>
                          <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == 'CRON'}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderCronId">${textContainer.text['grouperLoaderSqlCron']}</label></strong></td>
                              <td>
                                <span style="white-space: nowrap">
                                  <input type="text" style="width: 20em" value="${grouper:escapeJavascript(grouperRequestContainer.grouperLoaderContainer.editLoaderCron)}"
                                     name="editLoaderCronName" id="editLoaderCronId" />
                                  <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                    data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                                </span>
                                <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription != null}">
                                  <br /><span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription)}</span>
                                </c:if>
                                <br /><span class="description">${textContainer.text['grouperLoaderSqlCronDescription']}</span>
                                sdfsdf
                              </td>
                            </tr>

                          </c:when>
                          <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == 'START_TO_START_INTERVAL'}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderScheduleIntervalId">${textContainer.text['grouperLoaderSqlScheduleInterval']}</label></strong></td>
                              <td>
                                <span style="white-space: nowrap">
                                  <input type="text" style="width: 20em" value="${grouper:escapeJavascript(grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleInterval)}"
                                     name="editLoaderScheduleIntervalName" id="editLoaderScheduleIntervalId" />
                                  <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                    data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                                </span>
                                <br /><span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleIntervalHumanReadable)}</span>
                              </td>
                            </tr>
                          </c:when>
                        </c:choose>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderPriorityId">${textContainer.text['grouperLoaderSqlPriority']}</label></strong></td>
                          <td>
                            <span style="white-space: nowrap">
                              <input type="text" style="width: 20em" value="${grouper:escapeJavascript(grouperRequestContainer.grouperLoaderContainer.editLoaderPriority)}"
                                 name="editLoaderPriorityName" id="editLoaderPriorityId" />
                              <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            </span>
                            <br /><span class="description"></span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderPriorityId">${textContainer.text['grouperLoaderSqlPriority']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 20em" value="${grouper:escapeJavascript(grouperRequestContainer.grouperLoaderContainer.editLoaderPriority)}"
                               name="editLoaderPriorityName" id="editLoaderPriorityId" />
                            <br />
                            <span class="description">
                              <c:choose>
                                <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderPriorityInt == -200}">
                                  ${textContainer.text['grouperLoaderSqlPriorityInvalid']}
                                </c:when>
                                <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderPriorityInt < 5}">
                                  ${textContainer.text['grouperLoaderSqlPriorityLow']}
                                </c:when>
                                <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderPriorityInt == 5}">
                                  ${textContainer.text['grouperLoaderSqlPriorityAverage']}
                                </c:when>
                                <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderPriorityInt > 5}">
                                  ${textContainer.text['grouperLoaderSqlPriorityHigh']}
                                </c:when>
                                
                              </c:choose>
                            </span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderAndGroupsId">${textContainer.text['grouperLoaderSqlAndGroups']}</label></strong></td>
                          <td>
                            <input type="text" style="width: 20em" value="${grouper:escapeJavascript(grouperRequestContainer.grouperLoaderContainer.editLoaderAndGroups)}"
                               name="editLoaderAndGroupsName" id="editLoaderAndGroupsId" />
                            <br />
                            <c:forEach var="guiGroup" items="${grouperRequestContainer.grouperLoaderContainer.editLoaderAndGuiGroups}">
                            
                              ${guiGroup.shortLinkWithIcon} &nbsp; 
                            
                            </c:forEach>
                            
                          </td>
                        </tr>
                                                
                      </c:if>
                      
                      <tr>
                        <td></td>
                        <td style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;"><input type="submit" class="btn" aria-controls="groupFilterResultsId" id="filterSubmitId" 
                          value="${textContainer.text['grouperLoaderEditButtonSave'] }" 
                          onclick="ajax('../app/UiV2GrouperLoader.editGrouperLoaderSave', {formIds: 'editLoaderFormId'}); return false;"> 
                          &nbsp; 
                          <a class="btn" role="button" 
                            onclick="return guiV2link('operation=UiV2GrouperLoader.loader?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['grouperLoaderEditButtonCancel'] }</a>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </form>
              </div>
            </div>
