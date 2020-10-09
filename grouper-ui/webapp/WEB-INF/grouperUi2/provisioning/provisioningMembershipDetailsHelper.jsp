                <c:choose>
                  <c:when test="${not empty grouperRequestContainer.provisioningContainer.gcGrouperSyncMemberships}">
                    <c:forEach items="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMemberships}" var="grouperSyncMembership" >
                    
                      <h4>${textContainer.text['provisioningTargetNameLabel'] }: ${grouperSyncMembership.grouperSync.provisionerName}</h4> 
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperSyncMembership.inTarget}">
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
                                <c:when test="${grouperSyncMembership.inTargetInsertOrExists }">
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
                             ${grouperSyncMembership.inTargetStart }
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectInTargetEndLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMembership.inTargetEnd}
                            </td>
                          </tr>
                          
                          <tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectLastUpdatedLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMembership.lastUpdated}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMembershipIdLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMembership.membershipId}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMembershipId2Label'] }</strong></td>
                            <td>
                             ${grouperSyncMembership.membershipId2}
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectMembershipMetadataUpdatedLabel'] }</strong></td>
                            <td>
                             ${grouperSyncMembership.metadataUpdated}
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