<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li class="active"><a href="#" onclick="return false;" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                    <%@ include file="stemMoreTab.jsp" %>
                  </c:if>
                </ul>
                <form class="form-inline form-filter" id="stemFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">${textContainer.text['stemFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['stemFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" class="span12"/>
                    </div>
                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['stemApplyFilterButton'] }"
                      onclick="ajax('../app/UiV2Stem.filter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterFormId,stemPagingFormId'}); return false;"> 
                    <a class="btn" onclick="$('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['stemResetButton'] }</a></div>
                  </div>
                </form>
                <%-- this div will be filled with stemContents.jsp via ajax --%>
                <div id="stemFilterResultsId">
                </div>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>