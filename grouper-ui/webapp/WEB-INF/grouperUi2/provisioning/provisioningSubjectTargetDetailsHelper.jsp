<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                      <table class="table table-condensed table-striped">
                        <tbody>
                        
                        <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningTargetNameLabel'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.targetName}
                            </td>
                        </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderLastTimeWorkWasDone'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.lastTimeWorkWasDone}
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderInTarget'] }</strong></td>
                            <td>
                            
                            <c:if test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember != null}">
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.inTarget}">
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
                            <c:if test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember != null}">
                            <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.inTargetInsertOrExistsDb}">
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
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.inTargetStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.inTargetEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsProvisionable'] }</strong></td>
                            <td>
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.provisionable}">
			                    ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
			                  </c:when>
			                  <c:otherwise>
			                    ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
			                  </c:otherwise>
			                 </c:choose>
                              <br />
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsProvisionableStart'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.provisionableStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsProvisionableStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsProvisionableEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.provisionableEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsProvisionableEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.lastUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUserSync'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.lastUserSync}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUserSyncDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUserMetadataSync'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.lastUserMetadataSync}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUserMetadataSyncDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMetadataUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.metadataUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMetadataUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberFromId2'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.memberFromId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberFromId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberFromId3'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.memberFromId3}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberFromId3Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberToId2'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.memberToId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberToId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberToId3'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.memberToId3}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberToId3Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncMember.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                        </tbody>
                      </table>
