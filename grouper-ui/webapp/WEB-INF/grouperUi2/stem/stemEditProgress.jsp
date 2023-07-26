<%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
             <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.displayExtension)}
                <br /><small>
                <c:choose>
                    <c:when test="${grouperRequestContainer.stemContainer.progressBean.complete}">
                      ${textContainer.text['stemEditSuccess']}
                    </c:when>
                    <c:otherwise>
                      <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['stemEditProgressSubheading']}
                    </c:otherwise>
                  </c:choose></small>
 
                </h1>
              </div>

            </div>
 
 
