<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('groupLoaderPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}

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
                  <div class="lead span10">${textContainer.text['grouperLoaderGroupDecription'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <c:choose>
                  <c:when test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
                    <c:choose>
                      <c:when test="${grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}">
                        <p>${textContainer.text['grouperLoaderIsGrouperLoaderRecent'] }</p>
                      </c:when>
                      <c:when test="${grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}">
                        <p>${textContainer.text['grouperLoaderIsGrouperLoaderJexlScript'] }</p>
                      </c:when>
                      <c:otherwise>
                        <p>${textContainer.text['grouperLoaderIsGrouperLoader'] }</p>
                      </c:otherwise>
                    </c:choose>
                  </c:when>
                  <c:otherwise>
<%-- style="margin-top: -1em;" --%>
                    <p>${textContainer.text['grouperLoaderIsNotGrouperLoader'] }</p>
                  </c:otherwise>
                </c:choose>
                
                <c:if test="${not empty grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup}">
                
                	<p>
                	
                		<c:choose>
						  <c:when test="${grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLoaded}">
						    <grouper:message key="grouperLoaderGroupManagedByLoader">
	            				<grouper:param>${grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.controllingGroup.shortLinkWithIcon}</grouper:param>
	          				</grouper:message>
						  </c:when>
						  <c:otherwise>
						    <grouper:message key="grouperLoaderGroupWasManagedByLoader">
	            				<grouper:param>${grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.controllingGroup.shortLinkWithIcon}</grouper:param>
	          				</grouper:message>
						  </c:otherwise>
						</c:choose>
                	
                		<c:if test="${not empty grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLastFullMillisSince1970}">
                			<grouper:message key="grouperLoaderGroupManagedByLoaderFullyLoaded">
            					<grouper:param>${grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLastFullMillisSince1970}</grouper:param>
          					</grouper:message>
                		</c:if>
                		<c:if test="${not empty grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLastIncrementalMillisSince1970}">
                			<grouper:message key="grouperLoaderGroupManagedByLoaderIncrementallyLoaded">
            					<grouper:param>${grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLastIncrementalMillisSince1970}</grouper:param>
          					</grouper:message>
                		</c:if>
                		<c:if test="${not empty grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLastSummary}">
                			<grouper:message key="grouperLoaderGroupManagedByLoaderSummary">
            					<grouper:param>${grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup.grouperLoaderMetadataLastSummary}</grouper:param>
          					</grouper:message>
                		</c:if>
                	</p>
                
                </c:if>

                <c:choose>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.hasAttrDefNameGrouperLoader}">
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSchedulerState']}</strong></td>
                          <td>${grouperRequestContainer.grouperLoaderContainer.schedulerState}</td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSourceType']}</strong></td>
                          <td>${textContainer.text['grouperLoaderSql'] }<br />
                            <span class="description">${textContainer.text['grouperLoaderSourceType__SQL']}</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlLoaderType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlLoaderType)}<br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderSqlLoaderType__',grouperRequestContainer.grouperLoaderContainer.sqlLoaderType)]}</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderDatabaseName']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlDatabaseName)}<br />
                              <span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlDatabaseNameUrlText)}</span><br />
                              <span class="description">${textContainer.text['grouperLoaderDatabaseNameDescription'] }</span></td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlQuery']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlQuery)}
                            <br /><span class="description">${textContainer.text[grouper:concat2('grouperLoaderSqlQueryDescription__',grouperRequestContainer.grouperLoaderContainer.sqlLoaderType)]}</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlScheduleType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlScheduleType)}
                            <br /><span class="description">${textContainer.text[grouper:concat2('grouperLoaderSqlScheduleType__',grouperRequestContainer.grouperLoaderContainer.sqlScheduleType)]}</span>
                          </td>
                        </tr>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.grouperLoaderContainer.sqlScheduleType == 'CRON'}">
                            <tr>
                              <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderSqlCron']}</strong></td>
                              <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlCron)}
                              <br /><span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlCronDescription)}</span>
                              </td>
                            </tr>
                          </c:when>
                          <c:when test="${grouperRequestContainer.grouperLoaderContainer.sqlScheduleType == 'START_TO_START_INTERVAL'}">
                            <tr>
                              <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderSqlScheduleInterval']}</strong></td>
                              <td>
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlScheduleInterval)}<br />
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlScheduleIntervalHumanReadable)}
                              </td>
                            </tr>
                          </c:when>
                        </c:choose>
                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderSqlPriority']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlPriority)}<br />
                          
                            <c:choose>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.sqlPriorityInt == -200}">
                                ${textContainer.text['grouperLoaderSqlPriorityInvalid']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.sqlPriorityInt < 5}">
                                ${textContainer.text['grouperLoaderSqlPriorityLow']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.sqlPriorityInt == 5}">
                                ${textContainer.text['grouperLoaderSqlPriorityAverage']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.sqlPriorityInt > 5}">
                                ${textContainer.text['grouperLoaderSqlPriorityHigh']}
                              </c:when>
                              
                            </c:choose>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlAndGroups']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlAndGroups)}<br />
                          
                            <c:forEach var="guiGroup" items="${grouperRequestContainer.grouperLoaderContainer.sqlAndGuiGroups}">
                            
                              ${guiGroup.shortLinkWithIcon} &nbsp; 
                            
                            </c:forEach>
                          
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType == 'SQL_GROUP_LIST' }">
                        
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlGroupQuery']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupQuery)}
                              <br />
                              <span class="description">${textContainer.text['grouperLoaderSqlGroupQueryDescription']}</span>
                              
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlGroupsLike']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupsLike)}
                              <br /><span class="description">${textContainer.text['grouperLoaderSqlGroupsLikeDescription']}</span>
                            
                            </td>
                          </tr>
                          
                          
                          
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlGroupsSyncDisplayNameConfig']}</strong></td>
                            <td style="vertical-align: top;">
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.displayNameSyncType)}
                              <br /><span class="description">${textContainer.text['grouperLoaderSqlGroupsLikeDescription']}</span>
                            </td>
                          </tr>
                          
                          <c:choose>
                            <c:when test="${grouperRequestContainer.grouperLoaderContainer.displayNameSyncType == 'BASE_FOLDER_NAME'}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSyncDisplayNameBaseFolderName']}</strong></td>
                                <td style="vertical-align: top;">
		                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.displayNameSyncBaseFolderName)}
		                              <br /><span class="description">${textContainer.text['grouperLoaderSyncDisplayNameBaseFolderNameDescription']}</span>
		                            </td>
                              </tr>
  
                            </c:when>
                            <c:when test="${grouperRequestContainer.grouperLoaderContainer.displayNameSyncType == 'LEVELS'}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSyncDisplayNameLevels']}</strong></td>
                                <td style="vertical-align: top;">
                                  ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.displayNameSyncLevels)}
                                  <br /><span class="description">${textContainer.text['grouperLoaderSyncDisplayNameLevelsDescription']}</span>
                                </td>
                              </tr>
                            </c:when>
                          </c:choose>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlGroupTypes']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupTypes)}
                              <br /><span class="description">${textContainer.text['grouperLoaderSqlGroupTypesDescription']}</span>
                            
                            </td>
                          </tr>
                          
                        </c:if>

                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderFailsafeLabel']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.customizeFailsafeTrue ? textContainer.text['grouperLoaderYesFailsafeLabel'] : textContainer.text['grouperLoaderNoFailsafeLabel'])}

                            <br /><span class="description">${textContainer.text['grouperLoaderFailsafeDescription']}</span>
                          
                          </td>
                        </tr>
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.customizeFailsafeTrue }">
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderFailsafeUseLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.getSqlFailsafeUseOrDefault() == "true" ? textContainer.text['grouperLoaderYesUseFailsafeLabel'] : 
                                 (grouperRequestContainer.grouperLoaderContainer.getSqlFailsafeUseOrDefault() == "false" ? textContainer.text['grouperLoaderNoDoNotUseFailsafeLabel'] : textContainer.text['grouperLoaderDefaultUseFailsafeLabel']))}
                              <br /><span class="description">${textContainer.text['grouperLoaderFailsafeUseDescription']}</span>
                            
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderFailsafeSendEmailLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.getSqlFailsafeSendEmailOrDefault() == "true" ? textContainer.text['grouperLoaderYesSendEmailFailsafeLabel'] : 
                                 (grouperRequestContainer.grouperLoaderContainer.getSqlFailsafeSendEmailOrDefault() == "false" ? textContainer.text['grouperLoaderNoDoNotSendEmailFailsafeLabel'] : textContainer.text['grouperLoaderDefaultSendEmailLabel']))}
                              <br /><span class="description">${textContainer.text['grouperLoaderFailsafeSendEmailDescription']}</span>
                            
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinGroupSizeLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMinGroupSize)}
                              <br /><span class="description">${textContainer.text['grouperLoaderMinGroupSizeDescription']}</span>
                            
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMaxGroupPercentRemoveLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMaxGroupPercentRemove)}
                              <br /><span class="description">${textContainer.text['grouperLoaderMaxGroupPercentRemoveDescription']}</span>
                            
                            </td>
                          </tr>
  
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType == 'SQL_SIMPLE' }">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinGroupNumberOfMembersLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMinGroupNumberOfMembers)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMinGroupNumberOfMembersDescription']}</span>
                              
                              </td>
                            </tr>
                          </c:if>
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType == 'SQL_GROUP_LIST' }">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinManagedGroupsLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMinManagedGroups)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMinManagedGroupsDescription']}</span>
                              
                              </td>
                            </tr>
  
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMaxOverallPercentGroupsRemoveLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMaxOverallPercentGroupsRemove)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMaxOverallPercentGroupsRemoveDescription']}</span>
                              
                              </td>
                            </tr>
  
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMaxOverallPercentMembershipsRemoveLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMaxOverallPercentMembershipsRemove)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMaxOverallPercentMembershipsRemoveDescription']}</span>
                              
                              </td>
                            </tr>
  
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinOverallNumberOfMembersLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlMinOverallNumberOfMembers)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMinOverallNumberOfMembersDescription']}</span>
                              
                              </td>
                            </tr>
  
                          </c:if>
                          
                        </c:if>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderJobName']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouperRequestContainer.grouperLoaderContainer.jobName}
                            <br /><span class="description">${textContainer.text['grouperLoaderJobNameDescription']}</span>
                          
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.hasRecentMembershipsGrouperLoader}">
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderRecentFromGroup']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouperRequestContainer.grouperLoaderContainer.recentFromGuiGroup.shortLinkWithIcon}
                            <br /><span class="description">${textContainer.text['grouperLoaderRecentFromGroupDescription']}</span>
                          
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderRecentDays']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouperRequestContainer.grouperLoaderContainer.recentDays}
                            <br /><span class="description">${textContainer.text['grouperLoaderRecentDaysDescription']}</span>
                          
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderRecentIncludeCurrent']}</strong></td>
                          <td style="vertical-align: top;">
                            <c:choose>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.recentIncludeCurrent == 'T'}">
                                ${textContainer.text['grouperLoaderRecentIncludeCurrentTrue']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.recentIncludeCurrent == 'F'}">
                                ${textContainer.text['grouperLoaderRecentIncludeCurrentFalse']}
                              </c:when>
                            </c:choose>
                            <br /><span class="description">${textContainer.text['grouperLoaderRecentIncludeCurrentDescription']}</span>
                          
                          </td>
                        </tr>
                        
                      </tbody>
                    </table>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.hasJexlScriptGrouperLoader}">
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderEntityJexlScript']}</strong></td>
                          <td style="vertical-align: top;">
<pre>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript)}</pre>
                            <span class="description">${textContainer.text['grouperLoaderEntityJexlScriptDescription']}</span>
                          
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderIncludeInternalSources']}</strong></td>
                          <td style="vertical-align: top;">
                            <c:choose>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.jexlScriptIncludeInternalSources ? grouperRequestContainer.grouperLoaderContainer.jexlScriptIncludeInternalSources : false}">
                                ${textContainer.text['grouperLoaderIncludeInternalSourcesTrue']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.jexlScriptIncludeInternalSources ? !grouperRequestContainer.grouperLoaderContainer.jexlScriptIncludeInternalSources : true}">
                                ${textContainer.text['grouperLoaderIncludeInternalSourcesFalse']}
                              </c:when>
                            </c:choose>
                            <br /><span class="description">${textContainer.text['grouperLoaderIncludeInternalSourcesDescription']}</span>
                          
                          </td>
                        </tr>
                        
                      </tbody>
                    </table>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.hasAttrDefNameGrouperLoaderLdap}">
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSchedulerState']}</strong></td>
                          <td>${grouperRequestContainer.grouperLoaderContainer.schedulerState}</td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSourceType']}</strong></td>
                          <td>
                            ${textContainer.text['grouperLoaderLdap'] }<br />
                            <span class="description">${textContainer.text['grouperLoaderSourceType__LDAP']}
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapLoaderType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)}<br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderLdapLoaderType__',grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)]}</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapServerId']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapServerId)}<br />
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapServerIdUrlText)}<br />
                              <span class="description">${textContainer.text['grouperLoaderLdapServerIdDescription'] }</span></td>
                        </tr>
                          
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapFilter']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter)}<br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderLdapFilterDescription__',grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)]}</span>
                          </td>
                        </tr>
                          
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapSubjectAttributeName']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSubjectAttributeName)}<br />
                            <span class="description">${textContainer.text[grouper:concat2('grouperLoaderLdapSubjectAttributeNameDescription__',grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)]}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapSearchDn']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSearchDn)}<br />
                            <span class="description">${textContainer.text['grouperLoaderLdapSearchDnDescription']}</span>
                          </td>
                        </tr>     

                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapQuartzCron']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapCron)}
                          <br /><span class="description">${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapCronDescription)}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapSourceId']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSourceId)}
                          <br /><span class="description">${textContainer.text['grouperLoaderLdapSourceIdDescription']}</span>
                          </td>
                        </tr>

                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapSubjectLookupType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSubjectLookupType)}
                          <br /><span class="description">${textContainer.text['grouperLoaderLdapSubjectLookupTypeDescription']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapSearchScope']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSearchScope)}
                          <br /><span class="description">${textContainer.text['grouperLoaderLdapSearchScopeDescription']}</span>
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapPriority']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapPriority)}<br />
                          
                            <c:choose>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.ldapPriorityInt == -200}">
                                ${textContainer.text['grouperLoaderSqlPriorityInvalid']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.ldapPriorityInt < 5}">
                                ${textContainer.text['grouperLoaderSqlPriorityLow']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.ldapPriorityInt == 5}">
                                ${textContainer.text['grouperLoaderSqlPriorityAverage']}
                              </c:when>
                              <c:when test="${grouperRequestContainer.grouperLoaderContainer.ldapPriorityInt > 5}">
                                ${textContainer.text['grouperLoaderSqlPriorityHigh']}
                              </c:when>
                              
                            </c:choose>
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType == 'LDAP_GROUPS_FROM_ATTRIBUTES' }">
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapAttributeFilterExpression']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapAttributeFilterExpression)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapAttributeFilterExpressionDescription']}</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapResultsTransformationClass']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapResultsTransformationClass)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapResultsTransformationClassDescription']}</span>
                            </td>
                          </tr>
                        </c:if>
                                                
                        <tr>
                          <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapSubjectExpression']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSubjectExpression)}
                          <br /><span class="description">${textContainer.text['grouperLoaderLdapSubjectExpressionDescription']}</span>
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType == 'LDAP_GROUP_LIST'
                            || grouperRequestContainer.grouperLoaderContainer.ldapLoaderType == 'LDAP_GROUPS_FROM_ATTRIBUTES' }">

                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapExtraAttributes']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapExtraAttributes)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapExtraAttributesDescription']}</span>
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapGroupAttributeName']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupAttributeName)}<br />
                              <span class="description">${textContainer.text['grouperLoaderLdapGroupAttributeNameDescription']}</span>
                            </td>
                          </tr>
                            
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapAndGroups']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapAndGroups)}
                            <br />
                              <c:forEach var="guiGroup" items="${grouperRequestContainer.grouperLoaderContainer.ldapAndGuiGroups}">
                              
                                ${guiGroup.shortLinkWithIcon} &nbsp; 
                              
                              </c:forEach>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapGroupsLike']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupsLike)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapGroupsLikeDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapGroupNameExpression']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupNameExpression)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapGroupNameExpressionDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapGroupDisplayNameExpression']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupDisplayNameExpression)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapGroupDisplayNameExpressionDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapGroupDescriptionExpression']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupDescriptionExpression)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapGroupDescriptionExpressionDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapGroupTypes']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupTypes)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapGroupTypesDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapReaders']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapReaders)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapReadersDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapViewers']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapViewers)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapViewersDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapAdmins']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapAdmins)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapAdminsDescription']}</span>
                            </td>
                          </tr>
                        
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapUpdaters']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapUpdaters)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapUpdatersDescription']}</span>
                            </td>
                          </tr>
                        
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapOptins']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapOptins)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapOptinsDescription']}</span>
                            </td>
                          </tr>
                        
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapOptouts']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapOptouts)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapOptoutsDescription']}</span>
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapAttrReaders']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapAttrReaders)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapAttrReadersDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top;"><strong>${textContainer.text['grouperLoaderLdapAttrUpdaters']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapAttrUpdaters)}
                            <br /><span class="description">${textContainer.text['grouperLoaderLdapAttrUpdatersDescription']}</span>
                            </td>
                          </tr>
                        
                        </c:if>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderFailsafeLabel']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.customizeFailsafeTrue ? textContainer.text['grouperLoaderYesFailsafeLabel'] : textContainer.text['grouperLoaderNoFailsafeLabel'])}

                            <br /><span class="description">${textContainer.text['grouperLoaderFailsafeDescription']}</span>
                          
                          </td>
                        </tr>
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.customizeFailsafeTrue }">
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderFailsafeUseLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.getLdapFailsafeUseOrDefault() == "true" ? textContainer.text['grouperLoaderYesUseFailsafeLabel'] : 
                                 (grouperRequestContainer.grouperLoaderContainer.getLdapFailsafeUseOrDefault() == "false" ? textContainer.text['grouperLoaderNoDoNotUseFailsafeLabel'] : textContainer.text['grouperLoaderDefaultUseFailsafeLabel']))}
                              <br /><span class="description">${textContainer.text['grouperLoaderFailsafeUseDescription']}</span>
                            
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderFailsafeSendEmailLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.getLdapFailsafeSendEmailOrDefault() == "true" ? textContainer.text['grouperLoaderYesSendEmailFailsafeLabel'] : 
                                 (grouperRequestContainer.grouperLoaderContainer.getLdapFailsafeSendEmailOrDefault() == "false" ? textContainer.text['grouperLoaderNoDoNotSendEmailFailsafeLabel'] : textContainer.text['grouperLoaderDefaultSendEmailLabel']))}
                              <br /><span class="description">${textContainer.text['grouperLoaderFailsafeSendEmailDescription']}</span>
                            
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinGroupSizeLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMinGroupSize)}
                              <br /><span class="description">${textContainer.text['grouperLoaderMinGroupSizeDescription']}</span>
                            
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMaxGroupPercentRemoveLabel']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMaxGroupPercentRemove)}
                              <br /><span class="description">${textContainer.text['grouperLoaderMaxGroupPercentRemoveDescription']}</span>
                            
                            </td>
                          </tr>
  
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType == 'LDAP_SIMPLE' }">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinGroupNumberOfMembersLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMinGroupNumberOfMembers)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMinGroupNumberOfMembersDescription']}</span>
                              
                              </td>
                            </tr>
                          </c:if>
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType == 'LDAP_GROUP_LIST'
                            || grouperRequestContainer.grouperLoaderContainer.ldapLoaderType == 'LDAP_GROUPS_FROM_ATTRIBUTES' }">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinManagedGroupsLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMinManagedGroups)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMinManagedGroupsDescription']}</span>
                              
                              </td>
                            </tr>
  
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMaxOverallPercentGroupsRemoveLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMaxOverallPercentGroupsRemove)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMaxOverallPercentGroupsRemoveDescription']}</span>
                              
                              </td>
                            </tr>
  
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMaxOverallPercentMembershipsRemoveLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMaxOverallPercentMembershipsRemove)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMaxOverallPercentMembershipsRemoveDescription']}</span>
                              
                              </td>
                            </tr>
  
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderMinOverallNumberOfMembersLabel']}</strong></td>
                              <td style="vertical-align: top;">
                              
                                ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapMinOverallNumberOfMembers)}
                                <br /><span class="description">${textContainer.text['grouperLoaderMinOverallNumberOfMembersDescription']}</span>
                              
                              </td>
                            </tr>
  
                          </c:if>
                          
                        </c:if>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderJobName']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouperRequestContainer.grouperLoaderContainer.jobName}
                            <br /><span class="description">${textContainer.text['grouperLoaderJobNameDescription']}</span>
                          
                          </td>
                        </tr>
         
                      </tbody>
                    </table>
                  </c:when>
                </c:choose>

              </div>
            </div>
