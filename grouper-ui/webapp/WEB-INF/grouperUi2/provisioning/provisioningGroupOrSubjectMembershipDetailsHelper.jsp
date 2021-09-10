<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                      <table class="table table-condensed table-striped">
                        <tbody>
                        
                        <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningTargetNameLabel'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.targetName}
                            </td>
                        </tr>
                        
                         <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderInTarget'] }</strong></td>
                            <td>
                            
                            <c:if test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership != null}">
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
                            <c:if test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership != null}">
                            <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.inTargetInsertOrExists}">
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
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.inTargetStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.inTargetEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.lastUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMetadataUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.metadataUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMetadataUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipId'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.membershipId}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipIdDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipId2'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.membershipId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMembershipErrorCode'] }</strong></td>
                            <td>
                            <c:if test="${not empty grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.errorCode}">
	                            <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.errorCode}"></c:set>
	                            ${textContainer.text[errorText]}
                            </c:if>
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMembershipErrorCodeDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncGroup.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncGroup.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsGroupErrorCode'] }</strong></td>
                            <td>
                            <c:if test="${not empty grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncGroup.errorCode}">
	                            <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncGroup.errorCode}"></c:set>
	                            ${textContainer.text[errorText]}
                            </c:if>
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsGroupErrorCodeDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncMember.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncMember.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberErrorCode'] }</strong></td>
                            <td>
                            <c:if test="${not empty grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncMember.errorCode}">
	                            <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.grouperSyncMember.errorCode}"></c:set>
	                            ${textContainer.text[errorText]}
                            </c:if>
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberErrorCodeDescription']}</span>
                            </td>
                          </tr>
                          
                          <c:forEach items="${grouperRequestContainer.provisioningContainer.grouperProvisioningObjectMetadataItems}" var="metadataItem">
			  				
			  				<grouper:provisioningMetadataItemFormElement
			  				    name="${metadataItem.name}"
			  				    readOnly="true"
			  					formElementType="${metadataItem.formElementType}" 
			  					labelKey="${metadataItem.labelKey}"
			  					descriptionKey="${metadataItem.descriptionKey}"
			  					required="${metadataItem.required}"
			  					value="${metadataItem.defaultValue}"
			  					valuesAndLabels="${metadataItem.keysAndLabelsForDropdown}"
			  				/>
			  				
			  		    </c:forEach>
			  		    
                        </tbody>
                      </table>
