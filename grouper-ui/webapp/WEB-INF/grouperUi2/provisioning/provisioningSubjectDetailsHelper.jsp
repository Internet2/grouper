                <c:choose>
                  <c:when test="${not empty grouperRequestContainer.provisioningContainer.gcGrouperSyncMembers}">
                    <c:forEach items="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembers}" var="grouperSyncMember" >
                    
                      <h4>${textContainer.text['provisioningTargetNameLabel'] }: 
                      
                      <a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetDetailsOnSubject&memberId=${grouperSyncMember.memberId}&provisioningTargetName=${grouperSyncMember.grouperSync.provisionerName}'); return false;"
                              >${grouperSyncMember.grouperSync.provisionerName}</a></h4> 
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperSyncMember.inTarget}">
                                  ${textContainer.textEscapeXml['provisioningSubjectInTargetYesLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['provisioningSubjectInTargetNoLabel']}                              
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningHasConfigurationHint']}</span> --%>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetInsertOrExistsLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperSyncMember.inTargetInsertOrExists }">
                                  ${textContainer.textEscapeXml['provisioningSubjectInTargetInsertOrExistsYesLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['provisioningSubjectInTargetInsertOrExistsNoLabel']}                              
                                </c:otherwise>
                              </c:choose>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetStartLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMember.inTargetStart }
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetEndLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMember.inTargetEnd}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetProvisionableLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperSyncMember.provisionable}">
                                  ${textContainer.textEscapeXml['provisioningSubjectInTargetProvisionableYesLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['provisioningSubjectInTargetProvisionableNoLabel']}                              
                                </c:otherwise>
                              </c:choose>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectProvisionableStartLabel'] }</strong></td>
                            <td>
                            	${grouperSyncMember.provisionableStart}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectProvsionableEndLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMember.provisionableEnd}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectLastUpdatedLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMember.lastUpdated}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectLastUserSyncLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMember.lastUserSync}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectLastUserMetadataSyncLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMember.lastUserMetadataSync}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMemberFromId2Label'] }</strong></td>
                            <td>
                             ${grouperSyncMember.memberFromId2}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMemberFromId3Label'] }</strong></td>
                            <td>
                             ${grouperSyncMember.memberFromId3}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMemberToId2Label'] }</strong></td>
                            <td>
                             ${grouperSyncMember.memberToId2}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMemberToId3Label'] }</strong></td>
                            <td>
                             ${grouperSyncMember.memberToId3}
                            </td>
                          </tr>

                        </tbody>
                      </table>
                    
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <p>${textContainer.text['provisioningSubjectNoDetailsFound'] }</p>
                  </c:otherwise>
                </c:choose>