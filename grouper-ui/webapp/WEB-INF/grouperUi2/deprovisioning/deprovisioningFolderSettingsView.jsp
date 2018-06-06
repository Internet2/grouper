<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                    <%@ include file="../stem/stemMoreTab.jsp" %>
                  </c:if>
                </ul>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['deprovisioningStemSettingsTitle'] }</div>
                  <div class="span3" id="deprovisioningFolderMoreActionsButtonContentsDivId">
                    <%@ include file="deprovisioningFolderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <c:choose>
                  <c:when test="${grouperRequestContainer.deprovisioningContainer.hasDeprovisioningOnThisObjectOrParent}">
                    <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningAffiliationsAll}" var="guiDeprovisioningAffiliation" >
                      <h4>${textContainer.text['deprovisioningAffiliationLabel'] }: ${guiDeprovisioningAffiliation.translatedLabel }</h4>
                      <c:set var="guiDeprovisioningAffiliationFromJsp" scope="request" value="${guiDeprovisioningAffiliation}"/>
                      <c:set var="grouperDeprovisioningAttributeValue" 
                        value="${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningAttributeValueNew}" />
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningDeprovisionLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperDeprovisioningAttributeValue.deprovision}">
                                  ${textContainer.textEscapeXml['deprovisioningYesDeprovisionLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['deprovisioningNoDontDeprovisionLabel']}                              
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningDeprovisionHint']}</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningHasConfigurationLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperDeprovisioningAttributeValue.grouperDeprovisioningConfiguration.hasDatabaseConfiguration}">
                                  ${textContainer.textEscapeXml['deprovisioningYesHasConfigurationLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['deprovisioningNoHasConfigurationLabel']}                              
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningHasConfigurationHint']}</span>
                            </td>
                          </tr>
                          <c:if test="${grouperDeprovisioningAttributeValue.grouperDeprovisioningConfiguration.hasDatabaseConfiguration}">
                          
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningHasDirectDeprovisioningLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperDeprovisioningAttributeValue.directAssignment}">
                                    ${textContainer.textEscapeXml['deprovisioningYesHasDeprovisioningLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['deprovisioningNoDoesNotHaveDeprovisioningLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningHasDeprovisioningHint']}</span>
                              </td>
                            </tr>

                            <c:if test="${!grouperDeprovisioningAttributeValue.directAssignment}">

                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningParentFolderLabel'] }</strong></td>
                                <td>
                                  ${grouperRequestContainer.deprovisioningContainer.guiGrouperDeprovisioningAttributeValueNew.guiFolderWithSettings.shortLinkWithIcon}
                                  <br />
                                  <span class="description">${textContainer.text['deprovisioningParentFolderDescription']}</span>
                                </td>
                              </tr>

                            </c:if>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningSendEmailLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperDeprovisioningAttributeValue.sendEmail}">
                                    ${textContainer.textEscapeXml['deprovisioningYesSendEmailLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['deprovisioningNoDoNotSendEmailLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningSendEmailHint']}</span>
                              </td>
                            </tr>

                            <c:if test="${grouperDeprovisioningAttributeValue.sendEmail}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningEmailManagersLabel'] }</strong></td>
                                <td>
                                  <c:choose>
                                    <c:when test="${grouperDeprovisioningAttributeValue.emailManagers}">
                                      ${textContainer.textEscapeXml['deprovisioningYesEmailManagersLabel']}
                                    </c:when>
                                    <c:otherwise>
                                      ${textContainer.textEscapeXml['deprovisioningDontEmailManagersLabel']}                              
                                    </c:otherwise>
                                  </c:choose>
                                  <br />
                                  <span class="description">${textContainer.text['deprovisioningEmailManagersDescription']}</span>
                                </td>
                              </tr>
                              <c:if test="${!grouperDeprovisioningAttributeValue.emailManagers}">
                              
                                <tr>
                                  <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['grouperDeprovisioningEmailGroupMembersId'] }</strong></td>
                                  <td>
                                    <c:choose>
                                      <c:when test="${grouperDeprovisioningAttributeValue.emailGroupMembers}">
                                        ${textContainer.textEscapeXml['deprovisioningYesEmailGroupMembersLabel']}
                                      </c:when>
                                      <c:otherwise>
                                        ${textContainer.textEscapeXml['deprovisioningDontEmailGroupMembersLabel']}                              
                                      </c:otherwise>
                                    </c:choose>
                                    <br />
                                    <span class="description">${textContainer.text['deprovisioningEmailGroupMembersDescription']}</span>
                                  </td>
                                </tr>
                              
                              </c:if>
                              
                            </c:if>
                            
                          </c:if>


<%--

                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['label'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${}">
                                    ${textContainer.textEscapeXml['YesHasConfigurationLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['NoHasConfigurationLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['hint']}</span>
                              </td>
                            </tr>

                            
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="">${textContainer.text['']}</label></strong></td>
                                <td>
                                  <select name="grouperDeprovisioningEmailGroupMembersName" id="grouperDeprovisioningEmailGroupMembersId" style="width: 30em"
                                      onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEdit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                    <option value="true" ${ ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['']}</option>
                                    <option value="false" ${grouperDeprovisioningAttributeValue.emailGroupMembers ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['']}</option>
                                  </select>
                                  <br />
                                  <span class="description">${textContainer.text['']}</span>
                                </td>
                              </tr>


allowAddsWhileDeprovisionedString : String
autoChangeLoaderString : String
autoselectForRemovalString : String
deprovisionString : String
emailAddressesString : String
emailBodyString : String
emailGroupMembers : Boolean
emailManagers : Boolean
emailSubjectString : String
grouperDeprovisioningConfiguration : GrouperDeprovisioningConfiguration
inheritedFromFolderIdString : String
mailToGroupString : String
sendEmailString : String
showForRemovalString : String
stemScopeString : String

                        


                              <c:choose>
                                <c:when test="${grouperDeprovisioningAttributeValue.emailGroupMembers}">
                                  <tr>
                                    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningEmailGroupIdMembersId">${textContainer.text['deprovisioningEmailGroupIdMembersLabel']}</label></strong></td>
                                    <td>
                                      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperDeprovisioningAttributeValue.mailToGroupString)}"
                                         name="grouperDeprovisioningEmailGroupIdMembersName" id="grouperDeprovisioningEmailGroupIdMembersId" />
                                      <br />
                                      <span class="description">${textContainer.text['deprovisioningEmailGroupIdMembersDescription']}</span>
                                    </td>
                                  </tr>
                                </c:when>
                                <c:otherwise>
                                  <tr>
                                    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningEmailAddressesId">${textContainer.text['deprovisioningEmailAddressesLabel']}</label></strong></td>
                                    <td>
                                      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperDeprovisioningAttributeValue.emailAddressesString)}"
                                         name="grouperDeprovisioningEmailAddressesName" id="grouperDeprovisioningEmailAddressesId" />
                                      <br />
                                      <span class="description">${textContainer.text['deprovisioningEmailAddressesDescription']}</span>
                                    </td>
                                  </tr>
                                </c:otherwise>
                              </c:choose>
                            
                            </c:if>
                          
                        
--%>                        
                        
                        
                          <c:if test="${grouperRequestContainer.attestationContainer.ancestorStemAttestationAssignment}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationParentFolderLabel']}</strong></td>
                              <td>${grouperRequestContainer.attestationContainer.guiAttestation.guiFolderWithSettings.shortLinkWithIcon}
                                <br />
                                <span class="description">${textContainer.text['attestationFolderParentFolderDescription']}</span>
                              </td>
                            </tr>
                          </c:if>
                          <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.hasAttestation}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationSendEmailLabel']}</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail == null || grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail}">
                                    ${textContainer.textEscapeXml['grouperAttestationYesSendEmailLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['grouperAttestationNoDoNotSendEmailLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['grouperAttestationSendEmailDescription']}</span>
                              </td>
                            </tr>
                            <c:if
                              test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>
                                  ${textContainer.text['attestationEmailManagersLabel']}</strong></td>
                                <td>
                                  <c:choose>
                                    <c:when test="${grouperRequestContainer.attestationContainer.emailGroupManagers}">
                                      ${textContainer.textEscapeXml['grouperAttestationEmailManagersLabel']}
                                    </c:when>
                                    <c:otherwise>
                                      ${textContainer.textEscapeXml['grouperAttestationDontEmailManagersLabel']}                              
                                    </c:otherwise>
                                  </c:choose>
                                  <br />
                                  <span class="description">${textContainer.text['grouperAttestationSendEmailDescription']}</span>
                                </td>
                              </tr>
                              <c:if test="${!grouperRequestContainer.attestationContainer.emailGroupManagers}">
                                <tr>
                                  <td style="vertical-align: top; white-space: nowrap;"><strong><label
                                      for="grouperAttestationEmailAddressesId">${textContainer.text['attestationEmailAddressesLabel']}</label></strong></td>
                                  <td>
                                  ${grouper:escapeHtml(grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationEmailAddresses)}
                                    <br /> <span class="description">${textContainer.text['grouperAttestationEmailAddressesDescription']}</span>
                                  </td>
                                </tr>
                              </c:if>
                            </c:if>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>
                                ${textContainer.text['attestationDefaultCertifyLabel']}</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDaysUntilRecertify == null}">
                                    ${textContainer.textEscapeXml['attestationDoDefaultCertifyLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['attestationDontDefaultCertifyLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['attestationDefaultCertifyDescription']}</span>
                              </td>
                            </tr>
                            <c:if
                              test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDaysUntilRecertify != null}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDaysUntilRecertifyLabel']}</strong></td>
                                <td>
                                  ${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDaysUntilRecertify}
                                  <br /> <span class="description">${textContainer.text['attestationDaysUntilRecertifyDescription']}</span>
                                  
                                </td>
                              </tr>
                            </c:if>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>
                                ${textContainer.text['grouperAttestationStemScopeLabel']}</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationStemScopeSub}">
                                    ${textContainer.textEscapeXml['grouperAttestationYesStemScopeLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['grouperAttestationNoStemScopeLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['grouperAttestationStemScopeDescription']}</span>
                              </td>
                            </tr>
                          </c:if>
                            
                        </tbody>
                      </table>
                    
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <p>${textContainer.text['deprovisioningNoneAllConfigured'] }</p>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>