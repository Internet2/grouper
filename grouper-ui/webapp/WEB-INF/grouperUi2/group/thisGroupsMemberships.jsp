<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupMembershipsPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}

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

                <p class="lead">${textContainer.text['thisGroupsMembershipsDescription'] }</p>

                <c:if test="${mediaMap['uiV2.group.show.compositeAndFactors']=='true' && grouperRequestContainer.groupContainer.guiGroup.group.composite }" >

                  <div class="compositeInfo">${textContainer.text['groupLabelCompositeFactorMainPanel'] }<br />
                    <div class="compositeFactors">${grouperRequestContainer.groupContainer.guiGroup.compositeFactorOfOtherGroupsText}</div>
                  </div>

                </c:if>

                <form class="form-inline form-small form-filter" id="groupFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" name="membershipType">
                        <option value="">${textContainer.text['groupFilterAllGroups']}</option>
                        <option value="IMMEDIATE">${textContainer.text['groupFilterDirectAssignments']}</option>
                        <option value="NONIMMEDIATE">${textContainer.text['groupFilterIndirectAssignments']}</option>
                      </select>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['thisGroupsMembershipsFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" aria-label="${textContainer.textEscapeXml['thisGroupsMembershipsFilterFormPlaceholder']}" class="span12"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn" aria-controls="thisGroupsMembershipsFilterResultsId"  id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Group.filterThisGroupsMemberships?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterFormId,groupPagingFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisGroupsMembershipsFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end group/thisGroupsMemberships.jsp -->