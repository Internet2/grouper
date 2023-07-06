<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupEditCompositeTitle'] }<br />
                  <c:choose>
                    <c:when test="${grouperRequestContainer.groupContainer.progressBean.complete}">
                      ${textContainer.text['groupCompositeProgressSubheading']}
                    </c:when>
                    <c:otherwise>
                      <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['groupCompositeProgressSubheading']}
                    </c:otherwise>
                  </c:choose>
              
                </small></h1>
              </div>

            </div>
                        
            