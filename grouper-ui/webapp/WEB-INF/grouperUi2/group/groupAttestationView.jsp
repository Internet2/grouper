<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['groupAttestationTitle'] }</div>
                  <div class="span3" id="groupAttestationMoreActionsButtonContentsDivId">
                    <%@ include file="groupAttestationMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                

                <c:choose>
                  <c:when test="${grouperRequestContainer.attestationContainer.directGroupAttestationAssignment}">
                    <p>${textContainer.text['attestationConfiguredForGroup'] }</p>
                  </c:when>

                  <c:when test="${grouperRequestContainer.attestationContainer.ancestorStemAttestationAssignment}">
                    <p>${textContainer.text['attestationConfiguredOnGroupForAncestorStem'] }</p>
                  </c:when>
                  
                  <c:otherwise>
                    <p>${textContainer.text['noAttestationConfiguredOnGroup'] }</p>
                  </c:otherwise>
                                    
                </c:choose>

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
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationStatusLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${!grouperRequestContainer.attestationContainer.guiAttestation.needsRecertify}">
                                  ${textContainer.textEscapeXml['attestationStatusOk']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['attestationStatusNotOk']}                              
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <span class="description">${textContainer.text['attestationStatusDescription']}</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDateCertifiedLabel'] }</strong></td>
                            <td>${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDateCertified}
                              <br />
                              <span class="description">${textContainer.text['attestationDateCertifiedDescription']}</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDateNeedsRecertifyLabel'] }</strong></td>
                            <td>${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDateNeedsCertify}
                              <br />
                              <span class="description">${textContainer.text['attestationDateNeedsRecertifyDescription']}</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationDaysLeftUntilRecertifyLabel'] }</strong></td>
                            <td>${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationDaysLeftUntilRecertify}
                              <br />
                              <span class="description">${textContainer.text['attestationDaysLeftUntilRecertifyDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationLastEmailedDateLabel']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationLastEmailedDate)}
                              <br />
                              <span class="description">${textContainer.text['attestationLastEmailedDateDescription']}</span>
                            </td>
                          </tr>
                          <c:if test="${grouperRequestContainer.attestationContainer.ancestorStemAttestationAssignment}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationParentFolderLabel']}</strong></td>
                              <td>${grouperRequestContainer.attestationContainer.guiAttestation.guiFolderWithSettings.shortLinkWithIcon}
                                <br />
                                <span class="description">${textContainer.text['attestationParentFolderDescription']}</span>
                              </td>
                            </tr>
                          </c:if>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attestationSendEmailLabel']}</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail == null 
                                    || grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail}">
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
                            test="${grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail == null 
                               || grouperRequestContainer.attestationContainer.guiAttestation.grouperAttestationSendEmail}">
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
                        </c:if>
                      </tbody>
                    </table>
                  </c:if>
