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
                                  <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningEmailMembersGroupLabel'] }</strong></td>
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
                              
                                <c:choose>
                                  <c:when test="${grouperDeprovisioningAttributeValue.emailGroupMembers}">
                                    <tr>
                                      <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningEmailGroupIdMembersLabel']}</strong></td>
                                      <td>
                                        <c:if test="${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningEmailGuiGroup != null}">
                                          ${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningEmailGuiGroup.shortLinkWithIcon}<br />
                                        </c:if>
                                        ${grouper:escapeHtml(grouperDeprovisioningAttributeValue.mailToGroupString)}
                                        <br />
                                        <span class="description">${textContainer.text['deprovisioningEmailGroupIdMembersDescription']}</span>
                                      </td>
                                    </tr>
                                  </c:when>
                                  <c:otherwise>
                                    <tr>
                                      <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningEmailAddressesLabel']}</strong></td>
                                      <td>
                                        ${grouper:escapeHtml(grouperDeprovisioningAttributeValue.emailAddressesString)}
                                        <br />
                                        <span class="description">${textContainer.text['deprovisioningEmailAddressesDescription']}</span>
                                      </td>
                                    </tr>
                                  </c:otherwise>
                                </c:choose>
                              
                              </c:if>
                              
                            </c:if>
                            
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningShowForRemovalLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperDeprovisioningAttributeValue.showForRemoval}">
                                    ${textContainer.textEscapeXml['deprovisioningYesShowForRemovalLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['deprovisioningNoDontShowForRemovalLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningShowForRemovalHint']}</span>
                              </td>
                            </tr>   
                            
                            <c:if test="${grouperDeprovisioningAttributeValue.showForRemoval}">
                            
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningAutoselectForRemovalLabel'] }</strong></td>
                                <td>
                                  <c:choose>
                                    <c:when test="${grouperDeprovisioningAttributeValue.showForRemoval}">
                                      ${textContainer.textEscapeXml['deprovisioningYesAutoselectForRemoval']}
                                    </c:when>
                                    <c:otherwise>
                                      ${textContainer.textEscapeXml['deprovisioningNoAutoselectForRemovalHint']}                              
                                    </c:otherwise>
                                  </c:choose>
                                  <br />
                                  <span class="description">${textContainer.text['deprovisioningAutoselectForRemovalHint']}</span>
                                </td>
                              </tr>   
                              
                            </c:if>
                                      
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningAllowAddsLabel']}</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperDeprovisioningAttributeValue.allowAddsWhileDeprovisioned}">
                                    ${textContainer.textEscapeXml['deprovisioningYesAllowAddsLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['deprovisioningNoDontAllowAddsLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningAllowAddsHint']}</span>
                              </td>
                            </tr>
                            
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningAutoChangeLoaderLabel']}</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperDeprovisioningAttributeValue.autoChangeLoader}">
                                    ${textContainer.textEscapeXml['deprovisioningYesAutoChangeLoader']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['deprovisioningNoDontAutoChangeLoader']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningAutoChangeLoaderHint']}</span>
                              </td>
                            </tr>
                            
                          </c:if>

                          <c:if test="${grouperDeprovisioningAttributeValue.sendEmail || grouperDeprovisioningAttributeValue.lastEmailedDate != null}">

                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningLastEmailedDateLabel']}</strong></td>
                              <td>
                                ${grouperDeprovisioningAttributeValue.lastEmailedDate}
                                <br />
                                <span class="description">${textContainer.text['deprovisioningLastEmailedDateHint']}</span>
                              </td>
                            </tr>
                          </c:if>

                          <c:if test="${grouperDeprovisioningAttributeValue.sendEmail || grouperDeprovisioningAttributeValue.certifiedDate != null}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['deprovisioningCertifiedDateLabel']}</strong></td>
                              <td>
                                ${grouperDeprovisioningAttributeValue.certifiedDate}
                                <br />
                                <span class="description">${textContainer.text['deprovisioningCertifiedDateHint']}</span>
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