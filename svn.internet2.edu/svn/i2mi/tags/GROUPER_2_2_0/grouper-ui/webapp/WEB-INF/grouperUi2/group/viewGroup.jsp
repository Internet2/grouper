<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li class="active"><a href="#" onclick="return false;" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>

                <p class="lead">${textContainer.text['groupViewMembersDescription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" name="membershipType">
                        <option value="">${textContainer.text['groupFilterAllAssignments']}</option>
                        <option value="IMMEDIATE">${textContainer.text['groupFilterDirectAssignments']}</option>
                        <option value="NONIMMEDIATE">${textContainer.text['groupFilterIndirectAssignments']}</option>
                      </select>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['groupFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" class="span12"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Group.filter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterFormId,groupPagingFormId'}); return false;"> 
                      <a class="btn" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="groupFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end group/viewGroup.jsp -->