<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
            
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['stemTemplateCustomGshTemplateHeader'] }
                  <br />
                  <small>
                    <c:choose>
                      <c:when test="${grouperRequestContainer.gshTemplateContainer.gshTemplateExec.progressBean.complete}">
                        ${textContainer.text['gshTemplateReportSubheading']}
                      </c:when>
                      <c:otherwise>
                        <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['stemTemplateCustomGshTemplateSubheading']}
                      </c:otherwise>
                    </c:choose>
                   </small>
                 </h1>
              </div>
            </div>
