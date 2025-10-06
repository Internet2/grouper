<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupStemPrivilegesPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}
            <!-- start group/thisGroupsStemPrivileges.jsp -->
            
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				        <div class="tab-interface">
                  <c:set var="grouperCurrentTab" value="none" />
                  <%@ include file="../group/groupTabs.jsp" %>
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

                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" aria-controls="thisGroupsStemPrivilegesFilterResultsId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Group.filterThisGroupsStemPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisGroupsStemPrivilegesFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end group/thisGroupsStemPrivileges.jsp -->
