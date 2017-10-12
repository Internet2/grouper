<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGrouperLoaderOverallBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['miscellaneousAttestationOverallDecription'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousAttestationOverallSubtitle']}</p>
              </div>
            </div>

            <div class="row-fluid">
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
                        </tr>
                      </thead>
                      <tbody>
                        <c:set var="i" value="0" />
                        <c:forEach  items="${grouperRequestContainer.attestationContainer.guiAttestations}" 
                          var="guiAttestation">
                          <tr>
                            <td class="expand foo-clicker" style="white-space: nowrap;">${guiAttestation.guiGroup.shortLinkWithIcon}
                            </td>
                          </tr>
                          <c:set var="i" value="${i+1}" />
                        </c:forEach>
                      </tbody>
                    </table>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
