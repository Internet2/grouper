<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				
				<div class="tab-interface">
				  <ul class="nav nav-tabs">
                    <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupMembersTab'] }</a></li>
		            <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
		              <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
		            </c:if>
                    <%@ include file="groupMoreTab.jsp" %>
                  </ul>
				</div>
                

                <c:choose>
                  <c:when test="${grouperRequestContainer.groupContainer.canRead}">

                    <p class="lead">${textContainer.text['groupViewMembersDescription'] }</p>
                    
                    <c:if test="${mediaMap['uiV2.group.show.compositeAndFactors']=='true' && grouperRequestContainer.groupContainer.guiGroup.group.hasComposite}" >

                      <p class="compositeInfo">${textContainer.text['groupLabelCompositeOwnerMainPanel'] }
                      ${grouperRequestContainer.groupContainer.guiGroup.compositeOwnerText}</p>
                      
                    </c:if>
                    
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
                          <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                        </div>
                        
                      </div>
                    </form>
                    <script>
                      //set this flag so we get one confirm message on this screen
                      confirmedChanges = false;
                    </script>
                    <div id="groupFilterResultsId">
                    </div>                
                  
                  </c:when>
                
                  <c:otherwise>

                    <p class="lead">${textContainer.text['groupViewMembersCantReadDescription']}</p>
                  
                  </c:otherwise>
                </c:choose>
                

              </div>
            </div>

            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>
            <!-- end group/viewGroup.jsp -->
