<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start group/thisGroupsStemPrivileges.jsp -->
            
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				<div class="tab-interface">
                  <ul class="nav nav-tabs">
                    <li><a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                    <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                    </c:if>
                    <%@ include file="groupMoreTab.jsp" %>
                  </ul>
				</div>

                <p class="lead">${textContainer.text['thisGroupsStemPrivilegesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterPrivilegesFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" class="span12" name="privilegeField">
                        <option value="">${textContainer.text['groupFilterAllStems'] }</option>
                        <option value="stemAdmin">${textContainer.text['thisGroupsPrivilegesPriv_stemAdmin'] }</option>
                        <option value="create">${textContainer.text['thisGroupsPrivilegesPriv_create'] }</option>
                        <option value="stemAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrRead'] }</option>
                        <option value="stemAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrUpdate'] }</option>
                      </select>

                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['thisGroupsPrivilegesStemFilterFormPlaceholder']}" 
                         name="privilegeFilterText" id="table-filter" class="span12" aria-label="${textContainer.text['ariaLabelGuiEntityName']}"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Group.filterThisGroupsStemPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisGroupsStemPrivilegesFilterResultsId">
                </div>                
              </div>
            </div>
            <script>setupAccessibilityTabs();</script>
            <!-- end group/thisGroupsStemPrivileges.jsp -->
