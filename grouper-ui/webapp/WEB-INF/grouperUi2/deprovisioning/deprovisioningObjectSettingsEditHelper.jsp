<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                      <%-- first select the affiliation --%>
                      <tr>
                        <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningHasAffiliationId">${textContainer.text['deprovisioningAffiliationLabel']}</label></strong></td>
                        <td>
                          <input type="hidden" name="grouperDeprovisioningPreviousAffiliationName" value="${grouperRequestContainer.deprovisioningContainer.affiliation}" />
                          <select name="grouperDeprovisioningHasAffiliationName" id="grouperDeprovisioningHasAffiliationId" style="width: 30em"
                              onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
                            
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
                              onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
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
                                  onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
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
                                    onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
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
                                      onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
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
                                      <c:if test="${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningEmailGuiGroup != null}">
                                        ${grouperRequestContainer.deprovisioningContainer.grouperDeprovisioningEmailGuiGroup.shortLinkWithIcon}<br />
                                      </c:if>
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
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningShowForRemovalId">${textContainer.text['deprovisioningShowForRemovalLabel']}</label></strong></td>
                            <td>
                              <select name="grouperDeprovisioningShowForRemovalName" id="grouperDeprovisioningShowForRemovalId" style="width: 30em"
                                  onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                <option value="true" ${grouperDeprovisioningAttributeValue.showForRemoval ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['deprovisioningYesShowForRemovalLabel']}</option>
                                <option value="false" ${grouperDeprovisioningAttributeValue.showForRemoval ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['deprovisioningNoDontShowForRemovalLabel']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningShowForRemovalHint']}</span>
                            </td>
                          </tr>

                          <c:if test="${grouperDeprovisioningAttributeValue.showForRemoval}">
                          
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningAutoselectForRemovalId">${textContainer.text['deprovisioningAutoselectForRemovalLabel']}</label></strong></td>
                              <td>
                                <select name="grouperDeprovisioningAutoselectForRemovalName" id="grouperDeprovisioningAutoselectForRemovalId" style="width: 30em"
                                    onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                  <option value="true" ${grouperDeprovisioningAttributeValue.autoselectForRemoval ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['deprovisioningYesAutoselectForRemoval']}</option>
                                  <option value="false" ${grouperDeprovisioningAttributeValue.autoselectForRemoval ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['deprovisioningNoAutoselectForRemovalHint']}</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['deprovisioningAutoselectForRemovalHint']}</span>
                              </td>
                            </tr>
                          
                          </c:if>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningAllowAddsId">${textContainer.text['deprovisioningAllowAddsLabel']}</label></strong></td>
                            <td>
                              <select name="grouperDeprovisioningAllowAddsName" id="grouperDeprovisioningAllowAddsId" style="width: 30em"
                                  onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                <option value="true" ${grouperDeprovisioningAttributeValue.allowAddsWhileDeprovisioned ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['deprovisioningYesAllowAddsLabel']}</option>
                                <option value="false" ${grouperDeprovisioningAttributeValue.allowAddsWhileDeprovisioned ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['deprovisioningNoDontAllowAddsLabel']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningAllowAddsHint']}</span>
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningAutoChangeLoaderId">${textContainer.text['deprovisioningAutoChangeLoaderLabel']}</label></strong></td>
                            <td>
                              <select name="grouperDeprovisioningAutochangeLoaderName" id="grouperDeprovisioningAutoChangeLoaderId" style="width: 30em"
                                  onchange="ajax('../app/UiV2Deprovisioning.deprovisioningOn${ObjectType}Edit', {formIds: 'editDeprovisioningFormId'}); return false;">
                                <option value="true" ${grouperDeprovisioningAttributeValue.autoChangeLoader ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['deprovisioningYesAutoChangeLoader']}</option>
                                <option value="false" ${grouperDeprovisioningAttributeValue.autoChangeLoader ? '' : 'selected="selected"' }>${textContainer.textEscapeXml['deprovisioningNoDontAutoChangeLoader']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['deprovisioningAutoChangeLoaderHint']}</span>
                            </td>
                          </tr>

                        </c:if>



                      </c:if>
