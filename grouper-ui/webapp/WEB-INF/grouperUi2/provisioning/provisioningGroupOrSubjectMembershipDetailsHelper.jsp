<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                      <table class="table table-condensed table-striped">
                        <tbody>
                        
                        <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningTargetNameLabel'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.grouperSync.provisionerName}
                            </td>
                        </tr>
                        
                        <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningGroupLabel'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.grouperSyncGroup.groupName}
                            </td>
                        </tr>
                        
                        <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningSubjectLabel'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.grouperSyncMember.subjectId}
                            </td>
                        </tr>

                         <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderInTarget'] }</strong></td>
                            <td>
                            
                            <c:if test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership != null}">
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.inTarget}">
			                    ${textContainer.text['provisioningConfigTableHeaderInTargetYesLabel']}
			                  </c:when>
			                  <c:otherwise>
			                    ${textContainer.text['provisioningConfigTableHeaderInTargetNoLabel']}
			                  </c:otherwise>
			                 </c:choose>
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                            </c:if>
                            
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExists'] }</strong></td>
                            <td>
                            <c:if test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership != null}">
                            <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.inTargetInsertOrExistsDb}">
			                    ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsTrueLabel']}
			                  </c:when>
			                  <c:otherwise>
			                    ${textContainer.text['privsioningConfigDetailsInTargetInsertsOrExistsFalseLabel']}
			                  </c:otherwise>
			                 </c:choose>
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                              </c:if>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetStart'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.inTargetStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.inTargetEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.lastUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMetadataUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.metadataUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMetadataUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipId'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.membershipId}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipIdDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipId2'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.membershipId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMembership.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                        </tbody>
                      </table>
