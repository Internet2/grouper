<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('configurationReviewPageTitle')}
            <grouper:browserPage jspName="configureConfigurationReview" />
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Configure.index" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.index');">${textContainer.text['configurationIndexBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['configurationReviewBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['configurationReviewTitle'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['configurationReviewSubtitle']}</p>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <div class="form-actions">
                  <button type="button" class="btn btn-primary" onclick="ajax('../app/UiV2Configure.configurationReviewSubmit'); return false;">${textContainer.text['configurationReviewRunButton'] }</button>
                </div>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.configurationContainer.configurationCheckResults != null}">
              <c:choose>
                <c:when test="${empty grouperRequestContainer.configurationContainer.configurationCheckResults}">
                  <div class="row-fluid">
                    <div class="span12">
                      <div class="alert alert-success">${textContainer.text['configurationReviewNoIssues'] }</div>
                    </div>
                  </div>
                </c:when>
                <c:otherwise>
                  <div class="row-fluid">
                    <div class="span12">
                      <table class="table table-hover table-bordered table-striped table-condensed data-table">
                        <thead>
                          <tr>
                            <th style="white-space: nowrap;">${textContainer.text['configurationReviewColumnSeverity'] }</th>
                            <th>${textContainer.text['configurationReviewColumnProblem'] }</th>
                            <th>${textContainer.text['configurationReviewColumnRecommendation'] }</th>
                            <th>${textContainer.text['configurationReviewColumnProperty'] }</th>
                            <th>${textContainer.text['configurationReviewColumnCurrentValue'] }</th>
                          </tr>
                        </thead>
                        <tbody>
                          <c:forEach items="${grouperRequestContainer.configurationContainer.configurationCheckResults}" var="configurationCheckResult">
                            <tr>
                              <%-- severity badge: errors red, warnings orange --%>
                              <td style="vertical-align: top; white-space: nowrap;">
                                <c:choose>
                                  <c:when test="${configurationCheckResult.severity == 'ERROR'}">
                                    <span style="color: #8b0000; font-weight: bold;">${textContainer.text['configurationReviewSeverityError'] }</span>
                                  </c:when>
                                  <c:otherwise>
                                    <span style="color: #cc6600; font-weight: bold;">${textContainer.text['configurationReviewSeverityWarning'] }</span>
                                  </c:otherwise>
                                </c:choose>
                              </td>
                              <%-- description of the problem --%>
                              <td style="vertical-align: top;">${grouper:escapeHtml(configurationCheckResult.message)}</td>
                              <%-- recommended action --%>
                              <td style="vertical-align: top;">${grouper:escapeHtml(configurationCheckResult.recommendation)}</td>
                              <%-- offending property, deep-linked to the config editor when known --%>
                              <td style="vertical-align: top;">
                                <c:choose>
                                  <c:when test="${configurationCheckResult.configFileName != null && configurationCheckResult.propertyName != null}">
                                    <a href="?operation=UiV2Configure.configure&configFile=${configurationCheckResult.configFileName.configFileName}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.configure&configFile=${configurationCheckResult.configFileName.configFileName}');">${grouper:escapeHtml(configurationCheckResult.propertyName)}</a>
                                    <div style="color: #888; font-size: 0.85em;">${grouper:escapeHtml(configurationCheckResult.configFileName.configFileName)}</div>
                                  </c:when>
                                  <c:when test="${configurationCheckResult.propertyName != null}">
                                    ${grouper:escapeHtml(configurationCheckResult.propertyName)}
                                  </c:when>
                                  <c:otherwise>&nbsp;</c:otherwise>
                                </c:choose>
                              </td>
                              <%-- current (effective) value, redacted upstream if sensitive --%>
                              <td style="vertical-align: top;">
                                <c:choose>
                                  <c:when test="${not empty configurationCheckResult.currentValue}">${grouper:escapeHtml(configurationCheckResult.currentValue)}</c:when>
                                  <c:otherwise><span style="color: #888;">${textContainer.text['configurationReviewValueBlank'] }</span></c:otherwise>
                                </c:choose>
                              </td>
                            </tr>
                          </c:forEach>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </c:otherwise>
              </c:choose>
            </c:if>
