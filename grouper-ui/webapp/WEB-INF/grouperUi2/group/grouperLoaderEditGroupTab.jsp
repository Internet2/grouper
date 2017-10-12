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
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderType == 'SQL'}">
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
                                    <option value="${grouper:escapeHtml(sqlDatabaseName.id)}" ${grouperRequestContainer.grouperLoaderContainer.editLoaderSqlDatabaseName == sqlDatabaseName.id ? 'selected="selected"' : '' } 
                                      >${grouper:escapeHtml(sqlDatabaseName.name)}</option>
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
                                <input type="text" style="width: 60em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderSqlQuery)}"
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
                            <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == 'CRON' || grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == null}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderCronId">${textContainer.text['grouperLoaderSqlCron']}</label></strong></td>
                                <td>
                                  <span style="white-space: nowrap">
                                    <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderCron)}"
                                       name="editLoaderCronName" id="editLoaderCronId" />
                                    <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                                  </span>
                                  <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription != null && grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription != ''}">
                                    <br /><span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription)}</span>
                                  </c:if>
                                  <br /><span class="description">${textContainer.text['grouperLoaderSqlCronDescription']}</span>
                                </td>
                              </tr>
  
                            </c:when>
                            <c:when test="${grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleType == 'START_TO_START_INTERVAL'}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderScheduleIntervalId">${textContainer.text['grouperLoaderSqlScheduleInterval']}</label></strong></td>
                                <td>
                                  <span style="white-space: nowrap">
                                    <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderScheduleInterval)}"
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
                                <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderPriority)}"
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
                              </span>
                              <br /><span class="description"></span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderAndGroupsId">${textContainer.text['grouperLoaderSqlAndGroups']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderAndGroups)}"
                                 name="editLoaderAndGroupsName" id="editLoaderAndGroupsId" />
                              <br />
                              <c:forEach var="guiGroup" items="${grouperRequestContainer.grouperLoaderContainer.editLoaderAndGuiGroups}">
                              
                                ${guiGroup.shortLinkWithIcon} &nbsp; 
                              
                              </c:forEach>
                              
                            </td>
                          </tr>
                                       
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderSqlType == 'SQL_GROUP_LIST'}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlGroupQueryId">${textContainer.text['grouperLoaderSqlGroupQuery']}</label></strong></td>
                              <td>
                                <input type="text" style="width: 60em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderSqlGroupQuery)}"
                                   name="grouperLoaderSqlGroupQueryName" id="grouperLoaderSqlGroupQueryId" />
                                <br />
                                <span class="description">
                                ${textContainer.text['grouperLoaderSqlGroupQueryDescription']}</span>
                              </td>
                            </tr>
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlGroupsLikeId">${textContainer.text['grouperLoaderSqlGroupsLike']}</label></strong></td>
                              <td>
                                <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderGroupsLike)}"
                                   name="grouperLoaderSqlGroupsLikeName" id="grouperLoaderSqlGroupsLikeId" />
                                <br />
                                <span class="description">
                                ${textContainer.text['grouperLoaderSqlGroupsLikeDescription']}</span>
                              </td>
                            </tr>
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlGroupTypesId">${textContainer.text['grouperLoaderSqlGroupTypes']}</label></strong></td>
                              <td>
                                <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderGroupTypes)}"
                                   name="grouperLoaderSqlGroupTypesName" id="grouperLoaderSqlGroupTypesId" />
                                <br />
                                <span class="description">
                                ${textContainer.text['grouperLoaderSqlGroupTypesDescription']}</span>
                              </td>
                            </tr>
                                                      
                          </c:if>
                        </c:if>
                      </c:if>

                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderType == 'LDAP'}">
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowLdapLoaderType}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapTypeId">${textContainer.text['grouperLoaderLdapLoaderType']}</label></strong></td>
                            <td>
                              <span style="white-space: nowrap">
                                <select name="grouperLoaderLdapTypeName" id="grouperLoaderLdapTypeId" style="width: 40em"
                                  onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                                  <option value="" ></option>
                                  <option value="LDAP_SIMPLE" ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_SIMPLE' ? 'selected="selected"' : '' } 
                                    >${textContainer.textEscapeXml['grouperLoaderLdapLoaderTypeOption__LDAP_SIMPLE']}</option>
                                  <option value="LDAP_GROUP_LIST" ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUP_LIST' ? 'selected="selected"'  : '' }
                                    >${textContainer.textEscapeXml['grouperLoaderLdapLoaderTypeOption__LDAP_GROUP_LIST']}</option>
                                  <option value="LDAP_GROUPS_FROM_ATTRIBUTES" ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUPS_FROM_ATTRIBUTES' ? 'selected="selected"'  : '' }
                                    >${textContainer.textEscapeXml['grouperLoaderLdapLoaderTypeOption__LDAP_GROUPS_FROM_ATTRIBUTES']}</option>
                                </select>
                                <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                  data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              </span>
                              <br />
                              <span class="description">${textContainer.text[grouper:concat2('grouperLoaderLdapLoaderType__',grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType)]}</span>
                            </td>
                          </tr>
                        </c:if>
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowLdapServerId}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapServerIdId">${textContainer.text['grouperLoaderLdapServerId']}</label></strong></td>
                            <td>
                              <span style="white-space: nowrap">
                                <select name="grouperLoaderLdapServerIdName" id="grouperLoaderLdapServerIdId" style="width: 40em"
                                  onchange="ajax('../app/UiV2GrouperLoader.editGrouperLoader', {formIds: 'editLoaderFormId'}); return false;">
                                  <option value="" ></option>
                                  <c:forEach items="${grouperRequestContainer.grouperLoaderContainer.ldapServerIds}" var="ldapServerId">
                                    <option value="${grouper:escapeHtml(ldapServerId.id)}" ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapServerId == ldapServerId.id ? 'selected="selected"' : '' } 
                                      >${grouper:escapeHtml(ldapServerId.name)}</option>
                                  </c:forEach>
                                </select>
                                <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                  data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              </span>
                              <br />
                              <span class="description">
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapServerIdUrlText)}<br />
                              ${textContainer.text['grouperLoaderLdapServerIdDescription']}</span>
                            </td>
                          </tr>
                        </c:if>
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowLdapFilter}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapFilterId">${textContainer.text['grouperLoaderLdapFilter']}</label></strong></td>
                            <td>
                              <span style="white-space: nowrap">
                                <input type="text" style="width: 60em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapFilter)}"
                                   name="grouperLoaderLdapFilterName" id="grouperLoaderLdapFilterId" />
                                <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                  data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              </span>
                              <br />
                              <span class="description">
                                ${textContainer.text[grouper:concat2('grouperLoaderLdapFilterDescription__',grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType)]}</span>
                            </td>
                          </tr>
                        </c:if>
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderShowFields}">

	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapSubjectAttributeId">${textContainer.text['grouperLoaderLdapSubjectAttributeName']}</label></strong></td>
	                          <td>
	                            <span style="white-space: nowrap">
		                          <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSubjectAttributeName)}"
		                              name="editLoaderLdapSubjectAttributeName" id="editLoaderLdapSubjectAttributeId" />
	                                <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUPS_FROM_ATTRIBUTES'
	                                    || grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_SIMPLE'}">
	                                  <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
	                                    data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
	                                </c:if>
	                            </span>
	                               
	                            <br />
	                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderLdapSubjectAttributeNameDescription__',grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType)]}</span>
	                            
	                          </td>
	                        </tr>

	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapSearchDnId">${textContainer.text['grouperLoaderLdapSearchDn']}</label></strong></td>
	                          <td>
	                            <span style="white-space: nowrap">
		                          <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSearchDn)}"
		                              name="editLoaderLdapSearchDnName" id="editLoaderLdapSearchDnId" />
	                            </span>
	                               
	                            <br />
	                            <span class="description">${textContainer.text['grouperLoaderLdapSearchDnDescription']}</span>
	                            
	                          </td>
	                        </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderCronId">${textContainer.text['grouperLoaderLdapQuartzCron']}</label></strong></td>
                            <td>
                              <span style="white-space: nowrap">
                                <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderCron)}"
                                   name="editLoaderCronName" id="editLoaderCronId" />
                                <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                  data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                              </span>
                              <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription != null && grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription != ''}">
                                <br /><span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderCronDescription)}</span>
                              </c:if>
                              <br /><span class="description">${textContainer.text['grouperLoaderSqlCronDescription']}</span>
                            </td>
                          </tr>
	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapSourceId">${textContainer.text['grouperLoaderLdapSourceId']}</label></strong></td>
	                          <td>
	                            <span style="white-space: nowrap">
                                <select name="editLoaderLdapSourceName" id="editLoaderLdapSourceId" style="width: 40em">
                                  <option value="" ></option>
                                  <c:forEach items="${grouperRequestContainer.grouperLoaderContainer.sources}" var="source">
                                    <option value="${grouper:escapeHtml(source.id)}" ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSourceId == source.id ? 'selected="selected"' : '' } 
                                      >${grouper:escapeHtml(source.name)}</option>
                                  </c:forEach>
                                </select>
	                            </span>
	                               
	                            <br />
	                            <span class="description">${textContainer.text['grouperLoaderLdapSourceIdDescription']}</span>
	                            
	                          </td>
	                        </tr>

	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapSubjectLookupTypeId">${textContainer.text['grouperLoaderLdapSubjectLookupType']}</label></strong></td>
	                          <td>
	                            <span style="white-space: nowrap">

                                <select name="editLoaderLdapSubjectLookupTypeName" id="editLoaderLdapSubjectLookupTypeId" style="width: 20em">
                                  <option value="subjectId" 
	                                  ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSubjectLookupType == 'subjectId' ? 'selected="selected"' : '' }
                                  >subjectId</option>
                                  <option value="subjectIdentifier" 
	                                  ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSubjectLookupType == 'subjectIdentifier' ? 'selected="selected"' : '' }
                                  >subjectIdentifier</option>
                                  <option value="subjectIdOrIdentifier" 
	                                  ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSubjectLookupType == 'subjectIdOrIdentifier' ? 'selected="selected"' : '' }
                                  >subjectIdOrIdentifier</option>
                                </select>
			                              
	                            </span>
	                               
	                            <br />
	                            <span class="description">${textContainer.text['grouperLoaderLdapSubjectLookupTypeDescription']}</span>
	                            
	                          </td>
	                        </tr>

	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapSearchScopeId">${textContainer.text['grouperLoaderLdapSearchScope']}</label></strong></td>
	                          <td>
	                            <span style="white-space: nowrap">
	                               
	                              <select name="editLoaderLdapSearchScopeName" id="editLoaderLdapSearchScopeId" style="width: 20em">
	                                <option value="OBJECT_SCOPE" 
	                                 ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSearchScope == 'OBJECT_SCOPE' ? 'selected="selected"' : '' }
	                                >OBJECT_SCOPE</option>
	                                <option value="ONELEVEL_SCOPE" 
	                                 ${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSearchScope == 'ONELEVEL_SCOPE' ? 'selected="selected"' : '' }
	                                >ONELEVEL_SCOPE</option>
	                                <option value="SUBTREE_SCOPE" 
	                                 ${(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSearchScope == 'SUBTREE_SCOPE' 
	                                   || grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSearchScope == null) ? 'selected="selected"' : '' }
	                                >SUBTREE_SCOPE</option>
	                              </select>
	                            </span>
	                              
	                            <br />
	                            <span class="description">${textContainer.text['grouperLoaderLdapSearchScopeDescription']}</span>
	                            
	                          </td>
	                        </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderPriorityId">${textContainer.text['grouperLoaderSqlPriority']}</label></strong></td>
                            <td>
                              <span style="white-space: nowrap">
                                <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderPriority)}"
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
                              </span>
                            </td>
                          </tr>
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUPS_FROM_ATTRIBUTES'}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapAttributeFilterExpressionId">${textContainer.text['grouperLoaderLdapAttributeFilterExpression']}</label></strong></td>
                              <td>
                                <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapAttributeFilterExpression)}"
                                   name="editLoaderLdapAttributeFilterExpressionName" id="editLoaderLdapAttributeFilterExpressionId" />
                                <br /><span class="description">${textContainer.text['grouperLoaderLdapAttributeFilterExpressionDescription']}</span>
                                
                              </td>
                            </tr>
                          
                          </c:if>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapSubjectExpressionId">${textContainer.text['grouperLoaderLdapSubjectExpression']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapSubjectExpression)}"
                                 name="editLoaderLdapSubjectExpressionName" id="editLoaderLdapSubjectExpressionId" />
                              <br /><span class="description">${textContainer.text['grouperLoaderLdapSubjectExpressionDescription']}</span>
                            </td>
                          </tr>
	                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUP_LIST'}">

	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapExtraAttributesId">${textContainer.text['grouperLoaderLdapExtraAttributes']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapExtraAttributes)}"
	                                 name="editLoaderLdapExtraAttributesName" id="editLoaderLdapExtraAttributesId" />
	                              <br /><span class="description">${textContainer.text['grouperLoaderLdapExtraAttributesDescription']}</span>
	                            </td>
	                          </tr>

	                        </c:if>
	                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUPS_FROM_ATTRIBUTES' }">
	                            
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderLdapGroupAttributeId">${textContainer.text['grouperLoaderLdapGroupAttributeName']}</label></strong></td>
	                            <td>
                                <span style="white-space: nowrap">
  	                              <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapGroupAttributeName)}"
  	                                 name="editLoaderLdapGroupAttributeName" id="editLoaderLdapGroupAttributeId" />
                                  <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                                    data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                                 </span>
                                   
	                              <br /><span class="description">${textContainer.text['grouperLoaderLdapGroupAttributeNameDescription']}</span>
	                            </td>
	                          </tr>
	                        </c:if>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="editLoaderAndGroupsId">${textContainer.text['grouperLoaderSqlAndGroups']}</label></strong></td>
                            <td>
                              <input type="text" style="width: 20em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderAndGroups)}"
                                 name="editLoaderAndGroupsName" id="editLoaderAndGroupsId" />
                              <br />
                              <c:forEach var="guiGroup" items="${grouperRequestContainer.grouperLoaderContainer.editLoaderAndGuiGroups}">
                              
                                ${guiGroup.shortLinkWithIcon} &nbsp; 
                              
                              </c:forEach>
                              
                            </td>
                          </tr>

	                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUP_LIST'
	                            || grouperRequestContainer.grouperLoaderContainer.editLoaderLdapType == 'LDAP_GROUPS_FROM_ATTRIBUTES' }">
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlGroupsLikeId">${textContainer.text['grouperLoaderLdapGroupsLike']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderGroupsLike)}"
	                                 name="grouperLoaderSqlGroupsLikeName" id="grouperLoaderSqlGroupsLikeId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapGroupsLikeDescription']}</span>
	                            </td>
	                          </tr>
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderGroupNameExpressionId">${textContainer.text['grouperLoaderLdapGroupNameExpression']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapGroupNameExpression)}"
	                                 name="grouperLoaderGroupNameExpressionName" id="grouperLoaderGroupNameExpressionId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapGroupNameExpressionDescription']}</span>
	                            </td>
	                          </tr>
	                          
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapGroupDisplayNameId">${textContainer.text['grouperLoaderLdapGroupDisplayNameExpression']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapGroupDisplayNameExpression)}"
	                                 name="grouperLoaderLdapGroupDisplayNameName" id="grouperLoaderLdapGroupDisplayNameId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapGroupDisplayNameExpressionDescription']}</span>
	                            </td>
	                          </tr>
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapGroupDescriptionId">${textContainer.text['grouperLoaderLdapGroupDescriptionExpression']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapGroupDescriptionExpression)}"
	                                 name="grouperLoaderLdapGroupDescriptionName" id="grouperLoaderLdapGroupDescriptionId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapGroupDescriptionExpressionDescription']}</span>
	                            </td>
	                          </tr>
                          
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderSqlGroupTypesId">${textContainer.text['grouperLoaderLdapGroupTypes']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderGroupTypes)}"
	                                 name="grouperLoaderSqlGroupTypesName" id="grouperLoaderSqlGroupTypesId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapGroupTypesDescription']}</span>
	                            </td>
	                          </tr>
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapReadersId">${textContainer.text['grouperLoaderLdapReaders']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapReaders)}"
	                                 name="grouperLoaderLdapReadersName" id="grouperLoaderLdapReadersId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapReadersDescription']}</span>
	                            </td>
	                          </tr>
                          
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapViewersId">${textContainer.text['grouperLoaderLdapViewers']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapViewers)}"
	                                 name="grouperLoaderLdapViewersName" id="grouperLoaderLdapViewersId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapViewersDescription']}</span>
	                            </td>
	                          </tr>
                          
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapAdminsId">${textContainer.text['grouperLoaderLdapAdmins']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapAdmins)}"
	                                 name="grouperLoaderLdapAdminsName" id="grouperLoaderLdapAdminsId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapAdminsDescription']}</span>
	                            </td>
	                          </tr>
                        
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapUpdatersId">${textContainer.text['grouperLoaderLdapUpdaters']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapUpdaters)}"
	                                 name="grouperLoaderLdapUpdatersName" id="grouperLoaderLdapUpdatersId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapUpdatersDescription']}</span>
	                            </td>
	                          </tr>
                        
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapOptinsId">${textContainer.text['grouperLoaderLdapOptins']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapOptins)}"
	                                 name="grouperLoaderLdapOptinsName" id="grouperLoaderLdapOptinsId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapOptinsDescription']}</span>
	                            </td>
	                          </tr>
                        
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapOptoutsId">${textContainer.text['grouperLoaderLdapOptouts']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapOptouts)}"
	                                 name="grouperLoaderLdapOptoutsName" id="grouperLoaderLdapOptoutsId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapOptoutsDescription']}</span>
	                            </td>
	                          </tr>

	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapAttrReadersId">${textContainer.text['grouperLoaderLdapAttrReaders']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapAttrReaders)}"
	                                 name="grouperLoaderLdapAttrReadersName" id="grouperLoaderLdapAttrReadersId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapAttrReadersDescription']}</span>
	                            </td>
	                          </tr>
                          
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperLoaderLdapUpdatersId">${textContainer.text['grouperLoaderLdapAttrUpdaters']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 40em" value="${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.editLoaderLdapAttrUpdaters)}"
	                                 name="grouperLoaderLdapUpdaters" id="grouperLoaderLdapUpdatersId" />
	                              <br />
	                              <span class="description">
	                              ${textContainer.text['grouperLoaderLdapAttrUpdatersDescription']}</span>
	                            </td>
	                          </tr>
	                          
	                        </c:if>

                        </c:if>
                      </c:if>

                      
                      <tr>
                        <td></td>
                        <td style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;"><input type="submit" class="btn btn-primary" aria-controls="groupFilterResultsId" id="filterSubmitId" 
                          value="${textContainer.text['grouperLoaderEditButtonSave'] }" 
                          onclick="ajax('../app/UiV2GrouperLoader.editGrouperLoaderSave', {formIds: 'editLoaderFormId'}); return false;"> 
                          &nbsp; 
                          <a class="btn btn-cancel" role="button" 
                            onclick="return guiV2link('operation=UiV2GrouperLoader.loader?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['grouperLoaderEditButtonCancel'] }</a>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </form>
              </div>
            </div>
