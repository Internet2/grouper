<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="row-fluid">

              <div id="member-search" tabindex="-1" role="dialog" aria-labelledby="member-search-label" aria-hidden="true" class="modal hide fade">
                <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                  <h3 id="member-search-label">${textContainer.text['groupSearchForEntityButton'] }</h3>
                </div>
                <div class="modal-body">
                  <form class="form form-inline" id="deprovisionSearchFormId">
                    <input name="addMemberSubjectSearch" type="text" placeholder=""/>
                    <button class="btn" onclick="ajax('../app/UiV2Deprovisioning.addMemberSearch', {formIds: 'deprovisionSearchFormId'}); return false;" >${textContainer.text['groupSearchButton'] }</button>
                    <br />
                    <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['groupLabelExactIdMatch'] }</span>
                    <br />
                    <span style="white-space: nowrap;">${textContainer.text['find.search-source'] } 
                    <select name="sourceId">
                      <option value="all">${textContainer.textEscapeXml['find.search-all-sources'] }</option>
                      <c:forEach items="${grouperRequestContainer.deprovisioningContainer.sources}" var="source" >
                        <option value="${grouper:escapeHtml(source.id)}">
                          ${grouper:escapeHtml(source.name) } (
                            <c:forEach var="subjectType" items="${source.subjectTypes}" varStatus="typeStatus">
                              <c:if test="${typeStatus.count>1}">, </c:if>
                              ${grouper:escapeHtml(subjectType)}
                            </c:forEach>
                          )                               
                        </option>
                      </c:forEach>
                    </select></span>
                  </form>
                  <div id="addMemberResults">
                  </div>
                </div>
                <div class="modal-footer">
                  <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupSearchCloseButton']}</button>
                </div>
              </div>
              <div id="add-members">
                <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                  <input type="hidden" name="realm" value="${grouperRequestContainer.deprovisioningContainer.realm}">
                  <div class="control-group" id="add-member-control-group" aria-live="polite" aria-expanded="false">
                    <label for="groupAddMemberComboID" class="control-label">${textContainer.text['groupSearchMemberOrId'] }</label>
                    <div class="controls">
                      <div id="add-members-container">

                        <%-- placeholder: Enter the name of a person, group, or other entity --%>
                        <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
                          filterOperation="../app/UiV2Deprovisioning.addMemberFilter"/>
                        ${textContainer.text['groupSearchLabelPreComboLink']} <a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['groupSearchForEntityLink']}</a>
                        
                      </div>
                    </div>
                  </div>

                  <div class="control-group">
                    <div class="controls">
                      <button onclick="ajax('../app/UiV2Deprovisioning.deprovisionUserSubmit', {formIds: 'add-members-form'}); return false;" 
                        id="add-members-submit" type="submit" class="btn btn-primary">${textContainer.text['deprovisionUserSubmitButton']}</button> 
                    </div>
                  </div>
                </form>
                
                <div id="deprovisioningUserResultsDivId">
                </div>
                
              </div>
            </div>