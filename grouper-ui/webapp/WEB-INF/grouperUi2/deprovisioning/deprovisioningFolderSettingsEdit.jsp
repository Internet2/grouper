<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                    <%@ include file="../stem/stemMoreTab.jsp" %>
                  </c:if>
                </ul>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['deprovisioningStemEditSettingsTitle'] }</div>
                  <div class="span3" id="deprovisioningFolderMoreActionsButtonContentsDivId">
                    <%@ include file="deprovisioningFolderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <form class="form-inline form-small form-filter" id="editDeprovisioningFormId">
                  <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <%-- first select the affiliation --%>
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningHasAffiliationId">${textContainer.text['deprovisioningAffiliationLabel']}</label></strong></td>
                        <td>
                          <select name="grouperDeprovisioningHasAffiliationName" id="grouperDeprovisioningHasAffiliationId" style="width: 30em"
                              onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEdit', {formIds: 'editDeprovisioningFormId'}); return false;">
                            
                            <option value=""></option>
                            <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningAffiliationsAll}" var="guiDeprovisioningAffiliation">
                              <option value="${guiDeprovisioningAffiliation.label}"
                                  ${grouperRequestContainer.deprovisioningContainer.affiliation == guiDeprovisioningAffiliation.label ? 'selected="selected"' : '' }
                                  >${guiDeprovisioningAffiliation.translatedLabel}</option>
                            </c:forEach>
                          </select>
                          <br />
                          <span class="description">${textContainer.text['deprovisioningAffiliationHint']}</span>
                        </td>
                      </tr>
                      <%-- if the affiliation is selected, see if enabled --%>
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.deprovisioningContainer.affiliation)}">
                        <c:set var="grouperDeprovisioningAttributeValue" 
                          value="${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningAttributeValueNew}" />
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningHasConfigurationId">${textContainer.text['deprovisioningHasDeprovisioningLabel']}</label></strong></td>
                          <td>
                            <select name="grouperDeprovisioningHasConfigurationName" id="grouperDeprovisioningHasConfigurationId" style="width: 30em"
                              onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEdit', {formIds: 'editDeprovisioningFormId'}); return false;">
                              <option value="false" ${grouperDeprovisioningAttributeValue.directAssignment ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['deprovisioningNoDoesNotHaveDeprovisioningLabel']}</option>
                              <option value="true" ${grouperDeprovisioningAttributeValue.directAssignment ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['deprovisioningYesHasDeprovisioningLabel']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['deprovisioningHasDeprovisioningHint']}</span>
                          </td>
                        </tr>
                        <%-- if there is configuration then show the rest --%>
                        <c:if test="${grouperDeprovisioningAttributeValue.directAssignment}">
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningDeprovisionId">${textContainer.text['deprovisioningDeprovisionLabel']}</label></strong></td>
                            <td>
                              <select name="grouperDeprovisioningDeprovisionName" id="grouperDeprovisioningDeprovisionId" style="width: 30em">
                                <option value="true" ${grouperDeprovisioningAttributeValue.deprovision ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['deprovisioningYesDeprovisionLabel']}</option>
                                <option value="false" ${grouperDeprovisioningAttributeValue.deprovision ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['deprovisioningNoDontDeprovisionLabel']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningDeprovisionHint']}</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningSendEmailId">${textContainer.text['deprovisioningSendEmailLabel']}</label></strong></td>
                            <td>
                              <select name="grouperDeprovisioningSendEmailName" id="grouperDeprovisioningSendEmailId" style="width: 30em"
                                  onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEdit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                <option value="false" ${grouperDeprovisioningAttributeValue.sendEmail ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['deprovisioningNoDoNotSendEmailLabel']}</option>
                                <option value="true" ${grouperDeprovisioningAttributeValue.sendEmail ? 'selected="selected"' : '' }>${textContainer.textEscapeXml['deprovisioningYesSendEmailLabel']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningSendEmailHint']}</span>
                            </td>
                          </tr>

                          <c:if test="${grouperDeprovisioningAttributeValue.sendEmail}">
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningEmailManagersId">${textContainer.text['deprovisioningEmailManagersLabel']}</label></strong></td>
                              <td>
                                <select name="grouperDeprovisioningEmailManagersName" id="grouperDeprovisioningEmailManagersId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEdit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                  <option value="true" ${grouperDeprovisioningAttributeValue.emailManagers ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['deprovisioningYesEmailManagersLabel']}</option>
                                  <option value="false" ${grouperDeprovisioningAttributeValue.emailManagers ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['deprovisioningDontEmailManagersLabel']}</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningEmailManagersDescription']}</span>
                              </td>
                            </tr>
                            <c:if test="${!grouperDeprovisioningAttributeValue.emailManagers}">
                            
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningEmailGroupMembersId">${textContainer.text['deprovisioningEmailMembersGroupLabel']}</label></strong></td>
                                <td>
                                  <select name="grouperDeprovisioningEmailGroupMembersName" id="grouperDeprovisioningEmailGroupMembersId" style="width: 30em"
                                      onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEdit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                    <option value="true" ${grouperDeprovisioningAttributeValue.emailGroupMembers ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['deprovisioningYesEmailGroupMembersLabel']}</option>
                                    <option value="false" ${grouperDeprovisioningAttributeValue.emailGroupMembers ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['deprovisioningDontEmailGroupMembersLabel']}</option>
                                  </select>
                                  <br />
                                  <span class="description">${textContainer.text['deprovisioningEmailGroupMembersDescription']}</span>
                                </td>
                              </tr>

                              <c:choose>
                                <c:when test="${grouperDeprovisioningAttributeValue.emailGroupMembers}">
                                  <tr>
                                    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningEmailGroupIdMembersId">${textContainer.text['deprovisioningEmailGroupIdMembersLabel']}</label></strong></td>
                                    <td>
                                      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperDeprovisioningAttributeValue.mailToGroupString)}"
                                         name="grouperDeprovisioningEmailGroupIdMembersName" id="grouperDeprovisioningEmailGroupIdMembersId" />
                                      <br />
                                      <span class="description">${textContainer.text['deprovisioningEmailGroupIdMembersDescription']}</span>
                                    </td>
                                  </tr>
                                </c:when>
                                <c:otherwise>
                                  <tr>
                                    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningEmailAddressesId">${textContainer.text['deprovisioningEmailAddressesLabel']}</label></strong></td>
                                    <td>
                                      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperDeprovisioningAttributeValue.emailAddressesString)}"
                                         name="grouperDeprovisioningEmailAddressesName" id="grouperDeprovisioningEmailAddressesId" />
                                      <br />
                                      <span class="description">${textContainer.text['deprovisioningEmailAddressesDescription']}</span>
                                    </td>
                                  </tr>
                                </c:otherwise>
                              </c:choose>
                            
                            </c:if>
                          
                          </c:if>

                        </c:if>


<%--                        

emailBodyString : String
emailSubjectString : String
mailToGroupString : String
emailAddressesString : String

allowAddsWhileDeprovisionedString : String
autoChangeLoaderString : String
autoselectForRemovalString : String
showForRemovalString : String
inheritedFromFolderIdString : String
    --%>                    

                      </c:if>
                      <tr>
                        <td></td>
                        <td
                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                          <input type="submit" class="btn btn-primary"
                          aria-controls="deprovisioningSubmitId" id="submitId"
                          value="${textContainer.text['deprovisioningEditButtonSave'] }"
                          onclick="ajax('../app/UiV2Deprovisioning.deprovisioningOnFolderEditSave?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'editDeprovisioningFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                          >${textContainer.text['deprovisioningEditButtonCancel'] }</a>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </form>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
              <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
            </c:if>