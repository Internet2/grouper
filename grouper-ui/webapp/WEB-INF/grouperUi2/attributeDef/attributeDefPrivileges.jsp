<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <script>
            $( document ).ready(function() {
              $('#priv2').click();
            });
            </script>
            <c:set var="defaultMemberUnchecked" value="${true}" />

            <%-- for the new group or new stem or new attributeDef button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.attributeDefContainer}" property="showAddMember" value="true" />

            <%@ include file="attributeDefHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">

                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});" >${textContainer.text['attributeDefAttributeDefNameTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'perm'}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDefAction.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['attributeDefAttributeDefActionTab'] }</a></li>
                  </c:if>
                  <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['attributeDefPrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canReadPrivilegeInheritance}">
                    <%@ include file="attributeDefMoreTab.jsp" %>
                  </c:if>
                </ul>
                <p class="lead">${textContainer.text['attributeDefPrivilegesDecription'] }</p>
                <form class="form-inline form-small form-filter" id="attributeDefFilterPrivilegesFormId">

                  <div class="row-fluid attributeDefPrivilegeAdvancedShow" style="display: none">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">${textContainer.text['attributeDefPrivilegeFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" name="privilegeField">
                        <option value="">${textContainer.text['attributeDefPrivilegesFilterEveryone']}</option>
                        <option value="attrAdmins">${textContainer.text['attributeDefPrivilegesFilterAdmins']}</option>
                        <option value="attrUpdaters">${textContainer.text['attributeDefPrivilegesFilterUpdaters']}</option>
                        <option value="attrReaders">${textContainer.text['attributeDefPrivilegesFilterReaders']}</option>
                        <option value="attrViewers">${textContainer.text['attributeDefPrivilegesFilterViewers']}</option>
                        <option value="attributeDefAttrReaders">${textContainer.text['attributeDefPrivilegesFilterAttrReaders']}</option>
                        <option value="attributeDefAttrUpdaters">${textContainer.text['attributeDefPrivilegesFilterAttrUpdaters']}</option>
                        <option value="attrOptins">${textContainer.text['attributeDefPrivilegesFilterOptins']}</option>
                        <option value="attrOptouts">${textContainer.text['attributeDefPrivilegesFilterOptouts']}</option>

                      </select>
                    </div>
                  </div>
                  <div class="row-fluid attributeDefPrivilegeAdvancedShow" style="margin-top: 5px; display: none;">
                    <div class="span1">&nbsp;</div>
                    <div class="span4">
                      <select id="people-filter2" name="privilegeMembershipType">
                        <option value="">${textContainer.text['attributeDefPrivilegesFilterAllAssignments']}</option>
                        <option value="IMMEDIATE">${textContainer.text['attributeDefPrivilegesFilterDirectAssignments']}</option>
                        <%-- this doesnt work since doesnt show inherited privs  <option value="NONIMMEDIATE">${textContainer.text['attributeDefPrivilegesFilterIndirectAssignments']}</option> --%>
                      </select>
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 5px;">
                    <div class="span1">
                      <span class="attributeDefPrivilegeAdvancedHide"><label for="table-filter" style="white-space: nowrap;">${textContainer.text['attributeDefPrivilegeFilterFor'] }</label></span>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['attributeDefFilterPrivilegeFormPlaceholder']}" class="span12"
                       name="privilegeFilterText" id="table-filter" aria-label="${textContainer.text['ariaLabelGuiEntityName']}">
                    </div>
                    <div class="span4"><input type="submit" class="btn" aria-controls="attributeDefPrivilegeFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['attributeDefApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2AttributeDef.filterPrivileges?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'attributeDefFilterPrivilegesFormId,attributeDefPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#people-filter2').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['attributeDefResetButton'] }</a>
                      <a class="btn attributeDefPrivilegeAdvancedHide" role="button" onclick="$('.attributeDefPrivilegeAdvancedShow').show('slow'); $('.attributeDefPrivilegeAdvancedHide').hide('slow'); return false;">${textContainer.text['attributeDefAdvancedButton'] }</a>
                    </div>
                  </div>
                </form>
                
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="attributeDefPrivilegeFilterResultsId" role="region" aria-live="polite">
                </div>
                
              </div>
            </div>
