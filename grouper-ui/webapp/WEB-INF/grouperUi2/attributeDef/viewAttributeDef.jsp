<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('viewAttributeDefPageTitle', grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.name)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" />

            <%@ include file="attributeDefHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="attributeNames" />
                <%@ include file="../attributeDef/attributeDefTabs.jsp" %>

                <p class="lead">${textContainer.text['attributeDefViewAttributeDefNamesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="attributeDefFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="table-filter">${textContainer.text['attributeDefFilterFor'] }</label>
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
