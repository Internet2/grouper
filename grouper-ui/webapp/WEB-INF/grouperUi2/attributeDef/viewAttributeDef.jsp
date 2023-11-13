<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('viewAttributeDefPageTitle', grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.name)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" />

            <%@ include file="attributeDefHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['attributeDefAttributeDefNameTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'perm'}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDefAction.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['attributeDefAttributeDefActionTab'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefPrivileges&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});" >${textContainer.text['attributeDefPrivilegesTab'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canReadPrivilegeInheritance}">
                    <%@ include file="attributeDefMoreTab.jsp" %>
                  </c:if>
                </ul>

                <p class="lead">${textContainer.text['attributeDefViewAttributeDefNamesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="attributeDefFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['attributeDefFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['attributeDefFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" class="span12"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn" aria-controls="attributeDefFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['attributeDefApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2AttributeDef.filter?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'attributeDefFilterFormId,attributeDefPagingFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['attributeDefResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="attributeDefFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end attributeDef/viewAttributeDef.jsp -->
