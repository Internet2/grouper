<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="span12">
              <c:choose>
                <c:when test="${fn:length(grouperRequestContainer.attestationContainer.guiAttestations) == 0}">
                  <p>${textContainer.text['miscellaneousAttestationOverallNoGroups'] }</p>
                </c:when>
                <c:otherwise>
    
                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                    <thead>
                      <tr>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                      data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipGroup']}">${textContainer.text['grouperAttestationOverallColumnHeaderGroup'] }</span></th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                      data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipNeedsAttestation']}">${textContainer.text['grouperAttestationOverallColumnHeaderNeedsAttestation'] }</span></th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                      data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipDaysLeftUntilNeedsAttestation']}">${textContainer.text['grouperAttestationOverallColumnHeaderDaysLeftUntilNeedsAttestation'] }</span></th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                      data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipLastCertifiedDate']}">${textContainer.text['grouperAttestationOverallColumnHeaderLastCertifiedDate'] }</span></th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:set var="i" value="0" />
                      <c:forEach  items="${grouperRequestContainer.attestationContainer.guiAttestations}" 
                        var="guiAttestation">
                        <tr>
                          <td class="expand foo-clicker" style="white-space: nowrap;">${guiAttestation.guiGroup.linkWithIcon}
                          </td>
                          <td class="expand foo-clicker" style="white-space: nowrap;">
                            <c:choose>
                              <c:when test="${!guiAttestation.needsRecertify}">
                                ${textContainer.textEscapeXml['grouperAttestationOverallColumnStatusOk']}
                              </c:when>
                              <c:otherwise>
                                ${textContainer.textEscapeXml['grouperAttestationOverallColumnStatusNotOk']}                              
                              </c:otherwise>
                            </c:choose>
                          </td>
                          <td class="expand foo-clicker" style="white-space: nowrap;">${guiAttestation.grouperAttestationDaysLeftUntilRecertify}
                          </td>
                          <td class="expand foo-clicker" style="white-space: nowrap;">${guiAttestation.grouperAttestationDateCertified}
                          </td>
                        </tr>
                        <c:set var="i" value="${i+1}" />
                      </c:forEach>
                    </tbody>
                  </table>
                </c:otherwise>
              </c:choose>
            </div>
