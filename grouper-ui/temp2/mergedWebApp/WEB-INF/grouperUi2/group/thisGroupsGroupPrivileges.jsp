<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				<div class="tab-interface">
                  <ul class="nav nav-tabs">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                    <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                      <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                    </c:if>
                    <%@ include file="groupMoreTab.jsp" %>
                </ul>
				</div>

                <p class="lead">${textContainer.text['thisGroupsGroupPrivilegesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterPrivilegesFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" class="span12" name="privilegeField">
                        <option value="">${textContainer.text['groupFilterAllGroups'] }</option>
                        <option value="admin">${textContainer.text['thisGroupsPrivilegesPriv_admin'] }</option>
                        <option value="update">${textContainer.text['thisGroupsPrivilegesPriv_update'] }</option>
                        <option value="read">${textContainer.text['thisGroupsPrivilegesPriv_read'] }</option>
                        <option value="view">${textContainer.text['thisGroupsPrivilegesPriv_view'] }</option>
                        <option value="optin">${textContainer.text['thisGroupsPrivilegesPriv_optin'] }</option>
                        <option value="optout">${textContainer.text['thisGroupsPrivilegesPriv_optout'] }</option>
                        <option value="groupAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_groupAttrRead'] }</option>
                        <option value="groupAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_groupAttrUpdate'] }</option>
                        <%-- option value="create">${textContainer.text['thisGroupsPrivilegesPriv_create'] }</option>
                        <option value="stemAdmin">${textContainer.text['thisGroupsPrivilegesPriv_stemAdmin'] }</option>
                        <option value="stemAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrRead'] }</option>
                        <option value="stemAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrUpdate'] }</option>
                        <option value="attrAdmin">${textContainer.text['thisGroupsPrivilegesPriv_attrAdmin'] }</option>
                        <option value="attrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_attrUpdate'] }</option>
                        <option value="attrRead">${textContainer.text['thisGroupsPrivilegesPriv_attrRead'] }</option>
                        <option value="attrView">${textContainer.text['thisGroupsPrivilegesPriv_attrView'] }</option>
                        <option value="attrOptin">${textContainer.text['thisGroupsPrivilegesPriv_attrOptin'] }</option>
                        <option value="attrOptout">${textContainer.text['thisGroupsPrivilegesPriv_attrOptout'] }</option>
                        <option value="attrDefAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_attrDefAttrRead'] }</option>
                        <option value="attrDefAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_attrDefAttrUpdate'] }</option --%>
                      </select>

                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['thisGroupsPrivilegesGroupFilterFormPlaceholder']}" 
                         name="privilegeFilterText" id="table-filter" class="span12" aria-label="${textContainer.text['ariaLabelGuiEntityName']}"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" aria-controls="thisGroupsGroupPrivilegesFilterResultsId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Group.filterThisGroupsGroupPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisGroupsGroupPrivilegesFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end group/thisGroupsGroupPrivileges.jsp -->
