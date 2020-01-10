<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <script>
            $( document ).ready(function() {
              $('#priv2').click();
            });
            </script>
            <c:set var="defaultMemberUnchecked" value="${true}" />

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">

                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                <p class="lead">${textContainer.text['groupPrivilegesDecription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterPrivilegesFormId">

                  <div class="row-fluid groupPrivilegeAdvancedShow" style="display: none">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">${textContainer.text['groupPrivilegeFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" name="privilegeField">
                        <option value="">${textContainer.text['groupPrivilegesFilterEveryone']}</option>
                        <option value="admins">${textContainer.text['groupPrivilegesFilterAdmins']}</option>
                        <option value="updaters">${textContainer.text['groupPrivilegesFilterUpdaters']}</option>
                        <option value="readers">${textContainer.text['groupPrivilegesFilterReaders']}</option>
                        <option value="viewers">${textContainer.text['groupPrivilegesFilterViewers']}</option>
                        <option value="groupAttrReaders">${textContainer.text['groupPrivilegesFilterAttrReaders']}</option>
                        <option value="groupAttrUpdaters">${textContainer.text['groupPrivilegesFilterAttrUpdaters']}</option>
                        <option value="optins">${textContainer.text['groupPrivilegesFilterOptins']}</option>
                        <option value="optouts">${textContainer.text['groupPrivilegesFilterOptouts']}</option>

                      </select>
                    </div>
                  </div>
                  <div class="row-fluid groupPrivilegeAdvancedShow" style="margin-top: 5px; display: none;">
                    <div class="span1">&nbsp;</div>
                    <div class="span4">
                      <select id="people-filter2" name="privilegeMembershipType">
                        <option value="">${textContainer.text['groupPrivilegesFilterAllAssignments']}</option>
                        <option value="IMMEDIATE">${textContainer.text['groupPrivilegesFilterDirectAssignments']}</option>
                        <%-- this doesnt work since doesnt show inherited privs  <option value="NONIMMEDIATE">${textContainer.text['groupPrivilegesFilterIndirectAssignments']}</option> --%>
                      </select>
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 5px;">
                    <div class="span1">
                      <span class="groupPrivilegeAdvancedHide"><label for="table-filter" style="white-space: nowrap;">${textContainer.text['groupPrivilegeFilterFor'] }</label></span>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['groupFilterPrivilegeFormPlaceholder']}" class="span12"
                       name="privilegeFilterText" id="table-filter" aria-label="${textContainer.text['ariaLabelGuiEntityName']}">
                    </div>
                    <div class="span4"><input type="submit" class="btn" aria-controls="groupPrivilegeFilterResultsId"  id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Group.filterPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#people-filter2').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                      <a role="button" class="btn groupPrivilegeAdvancedHide" onclick="$('.groupPrivilegeAdvancedShow').show('slow'); $('.groupPrivilegeAdvancedHide').hide('slow'); return false;">${textContainer.text['groupAdvancedButton'] }</a>
                    </div>
                  </div>
                </form>
                
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="groupPrivilegeFilterResultsId" aria-live="polite" role="region">
                </div>
                
              </div>
            </div>