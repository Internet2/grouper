<%@ include file="../assetsJsp/commonTaglib.jsp"%>

           <div class="span12">
              <c:choose>
                <c:when test="${fn:length(grouperRequestContainer.deprovisioningContainer.deprovisionedGuiMembers) == 0}">
                  <p>${textContainer.text['deprovisioningMainNoMembers'] }</p>
                </c:when>
                <c:otherwise>
    
                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                    <thead>
                      <tr>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                      data-original-title="${textContainer.textEscapeDouble['grouperAttestationOverallColumnTooltipGroup']}">${textContainer.text['deprovisioningMainColumnHeaderSubject'] }</span></th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:set var="i" value="0" />
                      <c:forEach  items="${grouperRequestContainer.deprovisioningContainer.deprovisionedGuiMembers}" 
                        var="guiMember">
                        <tr>
                          <td class="expand foo-clicker" style="white-space: nowrap;">${guiMember.shortLinkWithIcon}
                          </td>
                        </tr>
                        <c:set var="i" value="${i+1}" />
                      </c:forEach>
                    </tbody>
                  </table>
                </c:otherwise>
              </c:choose>
            </div>
