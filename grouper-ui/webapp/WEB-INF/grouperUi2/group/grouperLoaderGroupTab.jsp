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
                <p class="lead">${textContainer.text['grouperLoaderGroupDecription'] }</p>
                <c:choose>
                  <c:when test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
                    <p>${textContainer.text['grouperLoaderIsGrouperLoader'] }</p>
                  </c:when>
                  <c:otherwise>
<%-- style="margin-top: -1em;" --%>
                    <p>${textContainer.text['grouperLoaderIsNotGrouperLoader'] }</p>
                  </c:otherwise>
                </c:choose>

                <c:choose>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.hasAttrDefNameGrouperLoader}">
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSourceType']}</strong></td>
                          <td>${textContainer.text['grouperLoaderSql'] }<br />
                            <span class="description">${textContainer.text['grouperLoaderSourceType__SQL']}</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlLoaderType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlLoaderType)}<br />
                            <span class="description">${textContainer.text['grouperLoaderSqlLoaderType__'.concat(grouperRequestContainer.grouperLoaderContainer.sqlLoaderType)]}</span>
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
                            <br /><span class="description">${textContainer.text['grouperLoaderSqlQueryDescription__'.concat(grouperRequestContainer.grouperLoaderContainer.sqlLoaderType)]}</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlScheduleType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlScheduleType)}
                            <br /><span class="description">${textContainer.text['grouperLoaderSqlScheduleType__'.concat(grouperRequestContainer.grouperLoaderContainer.sqlScheduleType)]}</span>
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
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderSqlGroupTypes']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupTypes)}
                              <br /><span class="description">${textContainer.text['grouperLoaderSqlGroupTypesDescription']}</span>
                            
                            </td>
                          </tr>
                          
                          
                          
                        </c:if>
                      </tbody>
                    </table>
                  </c:when>
                  <c:when test="${grouperRequestContainer.groupContainer.guiGroup.hasAttrDefNameGrouperLoaderLdap}">
                    <table class="table table-condensed table-striped">
                      <tbody>
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
                            <span class="description">${textContainer.text['grouperLoaderLdapLoaderType__'.concat(grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)]}</span>
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
                            <span class="description">${textContainer.text['grouperLoaderLdapFilterDescription__'.concat(grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)]}</span>
                          </td>
                        </tr>
                          
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapSubjectAttributeName']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSubjectAttributeName)}<br />
                            <span class="description">${textContainer.text['grouperLoaderLdapSubjectAttributeNameDescription__'.concat(grouperRequestContainer.grouperLoaderContainer.ldapLoaderType)]}</span>
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
                        
<%--                         


 = Opt outs
 = comma separated subjectIds or subjectIdentifiers who will be allowed to OPTOUT from the group
                        
                        --%>
                                           
                      </tbody>
                    </table>
                  </c:when>
                </c:choose>

<%--
                <a class="btn" role="button" onclick="ajax('../app/UiV2GrouperLoader.editGrouperLoader?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false; return false;">${textContainer.text['grouperLoaderEditConfiguration'] }</a>
--%>

              </div>
            </div>
