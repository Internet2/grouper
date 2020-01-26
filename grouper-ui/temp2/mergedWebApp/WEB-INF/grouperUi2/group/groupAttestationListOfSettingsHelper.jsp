<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <div class="span12">
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.attestationContainer.guiAttestations) == 0}">
                    <p>${textContainer.text['miscellaneousAttestationOverallNoSettings'] }</p>
                  </c:when>
                  <c:otherwise>
      
                    <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                      <thead>
                        <tr>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipOwner']}">${textContainer.text['grouperAttestationOverallColumnHeaderOwner'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipEnabled']}">${textContainer.text['grouperAttestationOverallColumnHeaderEnabled'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipDaysUntilRecertify']}">${textContainer.text['grouperAttestationOverallColumnHeaderDaysUntilRecertify'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipEmailAddresses']}">${textContainer.text['grouperAttestationOverallColumnHeaderEmailAddresses'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipSendEmail']}">${textContainer.text['grouperAttestationOverallColumnHeaderSendEmail'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipStemScope']}">${textContainer.text['grouperAttestationOverallColumnHeaderStemScope'] }</span></th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:set var="i" value="0" />
                        <c:forEach  items="${grouperRequestContainer.attestationContainer.guiAttestations}" 
                          var="guiAttestation">
                          <tr>
                            <td class="expand foo-clicker" style="white-space: nowrap;">
                              ${guiAttestation.guiGroup != null ? guiAttestation.guiGroup.linkWithIcon :  guiAttestation.guiStem.linkWithIcon}
                            </td>
                            <td class="expand foo-clicker" style="white-space: nowrap;">
                              ${guiAttestation.hasAttestation ? textContainer.text['grouperAttestationOverallColumnEnabled'] :  textContainer.text['grouperAttestationOverallColumnNotEnabled']}
                            </td>
                            <c:choose>
                              <c:when test="${guiAttestation.hasAttestation}">
                                <td class="expand foo-clicker" style="white-space: nowrap;">
                                  ${guiAttestation.grouperAttestationDaysUntilRecertify != null ? guiAttestation.grouperAttestationDaysUntilRecertify : textContainer.text['grouperAttestationOverallColumnDefaultRecertify']}
                                </td>
                                <td class="expand foo-clicker" style="white-space: nowrap;">
                                  ${guiAttestation.grouperAttestationEmailAddresses != null ? guiAttestation.grouperAttestationEmailAddresses : textContainer.text['grouperAttestationOverallColumnDefaultEmailAddresses']}
                                </td>
                                <td class="expand foo-clicker" style="white-space: nowrap;">
                                  ${guiAttestation.grouperAttestationSendEmail != null ? guiAttestation.grouperAttestationSendEmail : textContainer.text['grouperAttestationOverallColumnDefaultSendEmail']}
                                </td>
                                <td class="expand foo-clicker" style="white-space: nowrap;">
                                  ${guiAttestation.guiGroup != null ? textContainer.text['grouperAttestationOverallSettingsNotApplicable'] : 
                                    ( guiAttestation.grouperAttestationStemScope == null ? textContainer.text['grouperAttestationOverallColumnScopeDefault'] 
                                      : guiAttestation.grouperAttestationStemScope )}
                                </td>
                                
                              
                              </c:when>
                              <c:otherwise>
                                <td class="expand foo-clicker" style="white-space: nowrap;"></td>
                                <td class="expand foo-clicker" style="white-space: nowrap;"></td>
                                <td class="expand foo-clicker" style="white-space: nowrap;"></td>
                                <td class="expand foo-clicker" style="white-space: nowrap;"></td>
                              
                              </c:otherwise>
                            </c:choose>
                            
                          </tr>
                          <c:set var="i" value="${i+1}" />
                        </c:forEach>
                      </tbody>
                    </table>
                  </c:otherwise>
                </c:choose>
              </div>
