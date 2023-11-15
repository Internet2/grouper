<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemPrivilegesPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['stemPrivileges'] }</a></li>
                  <%@ include file="stemMoreTab.jsp" %>
                </ul>
                <p class="lead">${textContainer.text['stemPrivilegesDecription'] }</p>
                <form class="form-inline form-filter" id="stemFilterPrivilegesFormId">
                  <div class="row-fluid stemPrivilegeAdvancedShow" style="display: none">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">${textContainer.text['stemPrivilegeFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" name="privilegeField">
                        <option value="">${textContainer.text['stemPrivilegesFilterEveryone']}</option>
                        <option value="creators">${textContainer.text['stemPrivilegesFilterCreators']}</option>
                        <option value="stemAdmins">${textContainer.text['stemPrivilegesFilterStemAdmins']}</option>
                        <option value="stemAttrReaders">${textContainer.text['stemPrivilegesFilterAttrReaders']}</option>
                        <option value="stemAttrUpdaters">${textContainer.text['stemPrivilegesFilterAttrUpdaters']}</option>
                        <option value="stemViewers">${textContainer.text['stemPrivilegesFilterStemViewers']}</option>
                      </select>
                    </div>
                  </div>
                  <div class="row-fluid stemPrivilegeAdvancedShow" style="margin-top: 5px; display: none;">
                    <div class="span1">&nbsp;</div>
                    <div class="span4">
                      <select id="people-filter2" name="privilegeMembershipType">
                        <option value="">${textContainer.text['stemPrivilegesFilterAllAssignments']}</option>
                        <option value="IMMEDIATE">${textContainer.text['stemPrivilegesFilterDirectAssignments']}</option>
                        <%-- this doesnt work since doesnt show inherited privs  <option value="NONIMMEDIATE">${textContainer.text['stemPrivilegesFilterIndirectAssignments']}</option> --%>
                      </select>
                    </div>
                  </div>
                  <div class="row-fluid" style="margin-top: 5px;">
                    <div class="span1">
                      <span class="stemPrivilegeAdvancedHide"><label for="table-filter" style="white-space: nowrap;">${textContainer.text['stemPrivilegeFilterFor'] }</label></span>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['stemFilterPrivilegeFormPlaceholder']}" class="span12"
                       name="privilegeFilterText" id="table-filter" aria-label="${textContainer.text['ariaLabelGuiEntityName']}">
                    </div>
                    <div class="span4"><input type="submit" class="btn" aria-controls="stemPrivilegeFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['stemApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Stem.filterPrivileges?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#people-filter2').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['stemResetButton'] }</a>
                      <a class="btn stemPrivilegeAdvancedHide" role="button" onclick="$('.stemPrivilegeAdvancedShow').show('slow'); $('.stemPrivilegeAdvancedHide').hide('slow'); return false;">${textContainer.text['stemAdvancedButton'] }</a>
                    </div>
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="stemPrivilegeFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
