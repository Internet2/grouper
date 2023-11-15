<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('stemPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <%@ include file="stemMoreTab.jsp" %>
                </ul>

                <c:if test="${grouperRequestContainer.attestationContainer.hasAttestationConfigured && grouperRequestContainer.attestationContainer.canAttestReport}" >
                  <c:choose>
                    <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.needsRecertify}">
                      <p class="lead" style="color: red">${textContainer.text['attestationReportNeedsAttestationNow'] }
                        <input type="submit" class="btn" value="${textContainer.textEscapeDouble['reportAttestationMarkAsReviewed'] }"
                          onclick="ajax('../app/UiV2Attestation.attestFolderFromOutside?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" />
                      </p>
                      <br />
                    </c:when>
                    <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.needsRecertifySoon}">
                      <p class="lead" style="color: red">${textContainer.text['attestationReportNeedsAttestationSoon'] }
                        <input type="submit" class="btn" value="${textContainer.textEscapeDouble['reportAttestationMarkAsReviewed'] }"
                          onclick="ajax('../app/UiV2Attestation.attestFolderFromOutside?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" />
                      </p>
                      <br />
                    </c:when>
                  </c:choose>
                </c:if>

                <form class="form-inline form-filter" id="stemFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">${textContainer.text['stemFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['stemFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" class="span12"/>
                    </div>
                    <div class="span3"><input type="submit" class="btn" aria-controls="stemFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['stemApplyFilterButton'] }"
                      onclick="ajax('../app/UiV2Stem.filter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterFormId,stemPagingFormId'}); return false;"> 
                    <a class="btn" role="button" onclick="$('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['stemResetButton'] }</a></div>
                  </div>
                </form>
                <%-- this div will be filled with stemContents.jsp via ajax --%>
                <div id="stemFilterResultsId" role="region" aria-live="polite">
                </div>
              </div>
            </div>
            <grouper:performanceTimingGate label="StemUiView" key="post_viewStemInJsp" />