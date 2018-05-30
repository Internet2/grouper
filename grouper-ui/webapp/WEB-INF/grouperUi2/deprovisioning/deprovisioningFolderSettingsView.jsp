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
                    <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningAffiliationsAll}" var="guiDeprovisioningAffiliation">
                      <h3>${textContainer.text['deprovisioningAffiliationLabel'] }: ${guiDeprovisioningAffiliation.translatedLabel }</h3>
                      <c:set var="grouperDeprovisioningAttributeValue" 
                        value="${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningAttributeValueNew}" />
                    
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <p>${textContainer.text['deprovisioningNoneAllConfigured'] }</p>
                  </c:otherwise>
                </c:choose>

                <div id="stemAttestation">
                  <c:if test="${grouperRequestContainer.attestationContainer.hasAttestationConfigured}">
                    
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationHasAttestationLabel'] }</strong></td>
                          <td>
                            <c:choose>
                              <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.hasAttestation}">
                                ${textContainer.textEscapeXml['attestationHasAttestationYes']}
                              </c:when>
                              <c:otherwise>
                                ${textContainer.textEscapeXml['attestationHasAttestationNo']}                              
                              </c:otherwise>
                            </c:choose>
                            <br />
                            <span class="description">${textContainer.text['attestationHasAttestationDescription']}</span>
                          </td>
                        </tr>
                      
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
                  </c:if>
                </div>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>