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
                          <td><strong>${textContainer.text['grouperLoaderSourceType']}</strong></td>
                          <td>${textContainer.text['grouperLoaderSql'] }</td>
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
                          <td><strong>${textContainer.text['grouperLoaderSqlQuery']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlQuery)}</td>
                        </tr>
                        <tr>
                          <td><strong>${textContainer.text['grouperLoaderSqlScheduleType']}</strong></td>
                          <td>${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlScheduleType)}
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
                          <br />
                          </td>
                        </tr>
                        <tr>
                          <td><strong>${textContainer.text['grouperLoaderSqlAndGroups']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlAndGroups)}<br />
                          
                            <c:forEach var="guiGroup" items="${grouperRequestContainer.grouperLoaderContainer.sqlAndGuiGroups}">
                            
                              ${guiGroup.shortLinkWithIcon} &nbsp; 
                            
                            </c:forEach>
                          
                          </td>
                        </tr>
                        
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType == 'SQL_GROUP_LIST' }">
                        
                          <tr>
                            <td><strong>${textContainer.text['grouperLoaderSqlGroupQuery']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupQuery)}
                            
                            </td>
                          </tr>
                          
                          <tr>
                            <td><strong>${textContainer.text['grouperLoaderSqlGroupsLike']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupsLike)}
                            
                            </td>
                          </tr>
                          
                          <tr>
                            <td><strong>${textContainer.text['grouperLoaderSqlGroupTypes']}</strong></td>
                            <td style="vertical-align: top;">
                            
                              ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.sqlGroupTypes)}<br />
                            
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
                            ${textContainer.text['grouperLoaderLdap'] }
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
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapGroupAttributeName']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapGroupAttributeName)}<br />
                            <span class="description">${textContainer.text['grouperLoaderLdapGroupAttributeNameDescription']}</span>
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
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperLoaderLdapQuartzCron']}</strong></td>
                          <td style="vertical-align: top;">
                          
                            ${grouper:escapeHtml(grouperRequestContainer.grouperLoaderContainer.ldapSearchDn)}<br />
                            <span class="description">${textContainer.text['grouperLoaderLdapSearchDnDescription']}</span>
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
                        
<%--                         
                        # schedule string
 = Schedule
# quartz cron schedule
grouperLoaderLdapCronDescriptionError = Error: could not parse quartz cron string

# source id
grouperLoaderLdapSourceId = Subject source ID
grouperLoaderLdapSourceIdDescription = source ID from the sources.xml that narrows the search for subjects.  This is optional though makes the loader job more efficient

# subject id type
grouperLoaderLdapSubjectLookupType = Subject lookup type
grouperLoaderLdapSubjectLookupTypeDescription = can be either: subjectId (most efficient, default), subjectIdentifier (2nd most efficient), or subjectIdOrIdentifier

# search scope in ldap
grouperLoaderLdapSearchScope = Search scope
grouperLoaderLdapSearchScopeDescription = how deep to search in LDAP.  Can be OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE (default)

# require groups
grouperLoaderLdapAndGroups = Require members in other group(s)

# ldap priority
grouperLoaderLdapPriority = Priority

# describe the priority
grouperLoaderSqlPriorityInvalid = This priority is invalid
grouperLoaderSqlPriorityAverage = This job has the default and middle priority of 5
grouperLoaderSqlPriorityLow = This job has lower than the default and middle priority of 5
grouperLoaderSqlPriorityHigh = This job has higher than the default and middle priority of 5

# groups like
grouperLoaderLdapGroupsLike =
grouperLoaderLdapGroupsLikeDescription = sql like string (e.g. school:orgs:%org%_systemOfRecord), and the loader should be able to query group names to see which names are managed by this loader job. So if a group falls off the loader resultset (or is moved), this will help the loader remove the members from this group. Note, if the group is used anywhere as a member or composite member, it wont be removed.

# extra attributes
grouperLoaderLdapExtraAttributes = Extra attributes
grouperLoaderLdapExtraAttributesDescription = attribute names (comma separated) to get LDAP data for expressions in group name, displayExtension, description

# filter expression: LDAP_GROUPS_FROM_ATTRIBUTES
grouperLoaderLdapAttributeFilterExpression = Attribute filter expression
grouperLoaderLdapAttributeFilterExpressionDescription = e.g. ${attributeValue == 'a' || attributeValue == 'b'} &nbsp; ${attributeValue != 'a' && attributeValue != 'b'} &nbsp; ${attributeName.toLowerCase().startsWith('st')} &nbsp; ${attributeName =~ '^fa.*$' }

# group name expression: LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES
grouperLoaderLdapGroupNameExpression = Group name expression
grouperLoaderLdapGroupNameExpressionDescription = JEXL expression language fragment that evaluates to the group name (relative to the stem of the group which has the loader definition).  groupAttributes['dn'] is a variable in scope as is groupAttributes['cn'] etc 

# group display name expression: LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES
grouperLoaderLdapGroupDisplayNameExpression = Group display name expression
grouperLoaderLdapGroupDisplayNameExpressionDescription = JEXL expression language fragment that evaluates to the group display name.  groupAttributes['dn'] is a variable in scope as is groupAttributes['cn'] etc 

# group description: LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES
grouperLoaderLdapGroupDescriptionExpression = Group description expression
grouperLoaderLdapGroupDescriptionExpressionDescription = JEXL expression language fragment that evaluates to the group description.  groupAttributes['dn'] is a variable in scope as is groupAttributes['cn'] etc

# subject expression
grouperLoaderLdapSubjectExpression = Subject expression
grouperLoaderLdapSubjectExpressionDescription = JEXL expression language fragment that processes the subject string before passing it to the subject API.  e.g. ${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}

# group types for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
grouperLoaderLdapGroupTypes = Grouper types applied to groups
grouperLoaderLdapGroupTypesDescription = comma separated GroupTypes which will be applied to the loaded groups.  e.g. addIncludeExclude

# readers for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
grouperLoaderLdapReaders = Readers
grouperLoaderLdapReadersDescription = comma separated subjectIds or subjectIdentifiers who will be allowed to READ the group memberships
grouperLoaderLdapViewers = Viewers
grouperLoaderLdapViewersDescription = comma separated subjectIds or subjectIdentifiers who will be allowed to VIEW the groups
grouperLoaderLdapAdmins = Admins
grouperLoaderLdapAdminsDescription = comma separated subjectIds or subjectIdentifiers who will be allowed to ADMIN the groups
grouperLoaderLdapUpdaters = Updaters
grouperLoaderLdapUpdatersDescription = comma separated subjectIds or subjectIdentifiers who will be allowed to UPDATE the group memberships
grouperLoaderLdapOptins = Opt ins
grouperLoaderLdapOptinsDescription = comma separated subjectIds or subjectIdentifiers who will be allowed to OPTIN to the group
grouperLoaderLdapOptouts = Opt outs
grouperLoaderLdapOptinsDescription = comma separated subjectIds or subjectIdentifiers who will be allowed to OPTOUT from the group
                        
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
