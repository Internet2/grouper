<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <%@ include file="stemMoreTab.jsp" %>
                </ul>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['stemAttestationTitle'] }</div>
                  <div class="span3" id="stemAttestationMoreActionsButtonContentsDivId">
                    <%@ include file="stemAttestationMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <c:choose>
                  <c:when test="${grouperRequestContainer.attestationContainer.directStemAttestationAssignment}">
                    <p>${textContainer.text['attestationConfiguredForStem'] }</p>
                  </c:when>

                  <c:when test="${grouperRequestContainer.attestationContainer.ancestorStemAttestationAssignment}">
                    <p>${textContainer.text['attestationConfiguredOnStemForAncestorStem'] }</p>
                  </c:when>
                  
                  <c:otherwise>
                    <p>${textContainer.text['noAttestationConfiguredOnStem'] }</p>
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
                      
                        <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.hasAttestation}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationTypeLabel']}</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType == null || grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType == 'group'}">
                                  ${textContainer.textEscapeXml['grouperAttestationTypeGroupLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['grouperAttestationTypeReportLabel']}
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <span class="description">${textContainer.text['grouperAttestationTypeDescription']}</span>
                            </td>
                          </tr>
                          <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType == 'report'}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationReportConfigurationLabel']}</strong></td>
                              <td>
                                <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationReportConfiguration != null}">
                                  <a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationReportConfiguration.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${grouper:escapeHtml(grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationReportConfiguration.getReportConfigName())}</a>
                                </c:if>
                                <br />
                                <span class="description">${textContainer.text['grouperAttestationReportConfigurationDescription']}</span>
                              </td>
                            </tr>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationAuthorizedGroupLabel']}</strong></td>
                              <td>
                                <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationAuthorizedGroup != null}">
                                  ${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationAuthorizedGuiGroup.shortLinkWithIcon}
                                </c:if>
                                <br />
                                <span class="description">${textContainer.text['grouperAttestationAuthorizedGroupDescription']}</span>
                              </td>
                            </tr>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationStatusLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${!grouperRequestContainer.attestationContainer.guiAttestation.needsRecertify}">
                                    ${textContainer.textEscapeXml['attestationReportStatusOk']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['attestationReportStatusNotOk']}
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['attestationReportStatusDescription']}</span>
                              </td>
                            </tr>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDateCertifiedLabel'] }</strong></td>
                              <td>${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDateCertified}
                                <br />
                                <span class="description">${textContainer.text['attestationReportDateCertifiedDescription']}</span>
                              </td>
                            </tr>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDateNeedsRecertifyLabel'] }</strong></td>
                              <td>${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDateNeedsCertify}
                                <br />
                                <span class="description">${textContainer.text['attestationReportDateNeedsRecertifyDescription']}</span>
                              </td>
                            </tr>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDaysLeftUntilRecertifyLabel'] }</strong></td>
                              <td>${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDaysLeftUntilRecertify}
                                <br />
                                <span class="description">${textContainer.text['attestationReportDaysLeftUntilRecertifyDescription']}</span>
                              </td>
                            </tr>
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationLastEmailedDateLabel']}</strong></td>
                              <td>${grouper:escapeHtml(grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationLastEmailedDate)}
                                <br />
                                <span class="description">${textContainer.text['attestationReportLastEmailedDateDescription']}</span>
                              </td>
                            </tr>
                          </c:if>
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
                                ${textContainer.text[grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType == 'report' ? 'attestationReportEmailManagersLabel' : 'attestationEmailManagersLabel']}</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationEmailAddresses == null}">
                                    ${textContainer.textEscapeXml[grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType == 'report' ? 'grouperAttestationReportEmailManagersLabel' : 'grouperAttestationEmailManagersLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['grouperAttestationDontEmailManagersLabel']}                              
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['grouperAttestationSendEmailDescription']}</span>
                              </td>
                            </tr>
                            <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationEmailAddresses != null}">
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label
                                    for="grouperAttestationEmailAddressesId">${textContainer.text['attestationEmailAddressesLabel']}</label></strong></td>
                                <td>
                                ${grouper:escapeHtml(grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationEmailAddresses)}
                                  <br /> <span class="description">${textContainer.text[grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType == 'report' ? 'grouperAttestationReportEmailAddressesDescription' : 'grouperAttestationEmailAddressesDescription']}</span>
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
                          <c:if test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationType != 'report'}">
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
                        </c:if>
                          
                      </tbody>
                    </table>
                  </c:if>
                </div>
              </div>
            </div>
