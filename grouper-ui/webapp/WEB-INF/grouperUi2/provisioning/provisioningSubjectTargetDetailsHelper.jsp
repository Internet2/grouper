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
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderLastTimeWorkWasDone'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.lastTimeWorkWasDone}
                              <br />
                              <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningConfigTableHeaderInTarget'] }</strong></td>
                            <td>
                            
                            <c:if test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember != null}">
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.inTarget}">
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
                            <c:if test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember != null}">
                            <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.inTargetInsertOrExists}">
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
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.inTargetStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsInTargetEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.inTargetEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsInTargetEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsProvisionable'] }</strong></td>
                            <td>
                             <c:choose>
			                  <c:when test="${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.provisionable}">
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
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.provisionableStart}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsProvisionableStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsProvisionableEnd'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.provisionableEnd}
                              <br />
                              <span class="description">${textContainer.text['privsioningConfigDetailsProvisionableEndDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.lastUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUserSyncStart'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.lastUserSyncStart}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUserSyncStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUserSync'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.lastUserSync}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUserSyncDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUserMetadataSyncStart'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.lastUserMetadataSyncStart}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUserMetadataSyncStartDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsLastUserMetadataSync'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.lastUserMetadataSync}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsLastUserMetadataSyncDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMetadataUpdated'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.metadataUpdated}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMetadataUpdatedDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberFromId2'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.memberFromId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberFromId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberFromId3'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.memberFromId3}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberFromId3Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberToId2'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.memberToId2}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberToId2Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsMemberToId3'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.memberToId3}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsMemberToId3Description']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorMessage'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.errorMessage}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorMessageDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorTimestamp'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.errorTimestamp}
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorTimestampDescription']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['privsioningConfigDetailsErrorCode'] }</strong></td>
                            <td>
                           
                           <c:if test="${not empty grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.errorCode}">
	                            <c:set var="errorText" value="privsioningConfigDetailsErrorCode.${grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.errorCode}"></c:set>
	                            ${textContainer.text[errorText]}
                            </c:if>
                           
                            <br />
                            <span class="description">${textContainer.text['privsioningConfigDetailsErrorCodeDescription']}</span>
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
