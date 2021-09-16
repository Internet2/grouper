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
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningUsersCountLabel'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.usersCount}
                              <br />
                              <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderLastTimeWorkWasDone'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.lastTimeWorkWasDone}
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderProvisionable'] }</strong></td>
                            <td>
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.provisionable}">
			                    ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
			                  </c:when>
			                  <c:otherwise>
			                    ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
			                  </c:otherwise>
			                 </c:choose>
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderInTarget'] }</strong></td>
                            <td>
                            
                            <c:if test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup != null}">
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.inTarget}">
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
                            <c:if test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup != null}">
                            <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.inTargetInsertOrExists}">
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
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.inTargetStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.inTargetEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetEndDescription']}</span>
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
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.provisionableEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsProvisionableEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.lastUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastGroupSyncStart'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.lastGroupSyncStart}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastGroupSyncStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastGroupSync'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.lastGroupSync}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastGroupSyncDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastGroupMetadataSyncStart'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.lastGroupMetadataSyncStart}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastGroupMetadataSyncStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastGroupMetadataSync'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.lastGroupMetadataSync}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastGroupMetadataSyncDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupFromId2'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.groupFromId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupFromId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupFromId3'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.groupFromId3}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupFromId3Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupToId2'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.groupToId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupToId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupToId3'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.groupToId3}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupToId3Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMetadataUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.metadataUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMetadataUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorCode'] }</strong></td>
                            <td>
                            <c:if test="${not empty grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.errorCode}">
	                            <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.errorCode}"></c:set>
	                            ${textContainer.text[errorText]}
                            </c:if>
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorCodeDescription']}</span>
                            </td>
                          </tr>
                          
                        </tbody>
                      </table>
