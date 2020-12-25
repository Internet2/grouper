               <%@ include file="../assetsJsp/commonTaglib.jsp"%>
                <c:choose>
                  <c:when test="${grouperRequestContainer.provisioningContainer.hasProvisioningOnThisObjectOrParent}">
                    <c:forEach items="${grouperRequestContainer.provisioningContainer.guiGrouperProvisioningAttributeValues}" var="guiGrouperProvisioningAttributeValue" >
                    
	                    <c:set var="grouperProvisioningAttributeValue" 
	                        value="${guiGrouperProvisioningAttributeValue.grouperProvisioningAttributeValue}" />
                      
                      <h4>${textContainer.text['provisioningTargetNameLabel'] }: 
                      
                      <c:if test="${ObjectType == 'Folder' }">
	                      <a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetDetailsOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&provisioningTargetName=${grouperProvisioningAttributeValue.targetName}'); return false;"
	                              >${guiGrouperProvisioningAttributeValue.externalizedName}</a>
                      </c:if>
                      <c:if test="${ObjectType == 'Group' }">
	                      <a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningTargetDetailsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${grouperProvisioningAttributeValue.targetName}'); return false;"
	                              >${guiGrouperProvisioningAttributeValue.externalizedName}</a>
                      </c:if>
                      </h4>
                      <table class="table table-condensed table-striped">
                        <tbody>
                         
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningHasConfigurationLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperProvisioningAttributeValue.directAssignment || not empty grouperProvisioningAttributeValue.ownerStemId }">
                                  ${textContainer.textEscapeXml['provisioningYesHasConfigurationLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['provisioningNoHasConfigurationLabel']}                              
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <span class="description">${textContainer.text['provisioningHasConfigurationHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningProvisionLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperProvisioningAttributeValue.doProvision}">
                                    ${textContainer.textEscapeXml['provisioningYesProvisionLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['provisioningNoDontProvisionLabel']}                           
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['provisioningProvisionHint']}</span>
                              </td>
                          </tr>
                          
                          <c:if test="${grouperProvisioningAttributeValue.directAssignment || not empty grouperProvisioningAttributeValue.ownerStemId}">
                          
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningHasDirectConfigLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperProvisioningAttributeValue.directAssignment}">
                                    ${textContainer.textEscapeXml['provisioningYesHasDirectLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['provisioningNoDoesNotHaveDirectLabel']}                           
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['provisioningHasTypeHint']}</span>
                              </td>
                            </tr>
                            
                            <c:if test="${!grouperProvisioningAttributeValue.directAssignment}">

                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningParentFolderLabel'] }</strong></td>
                                <td>
                                  ${guiGrouperProvisioningAttributeValue.guiFolderWithSettings.shortLinkWithIcon}
                                  <br />
                                  <span class="description">${textContainer.text['provisioningParentFolderDescription']}</span>
                                </td>
                              </tr>

                            </c:if>
                            
                            <c:if test="${ObjectType == 'Folder' && grouperProvisioningAttributeValue.directAssignment }">
                            
                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningStemScopeLabel']}</strong></td>
                                <td>
                                  <c:choose>
                                    <c:when test="${grouperProvisioningAttributeValue.stemScopeSub}">
                                      ${textContainer.textEscapeXml['provisioningStemScopeAllLabel']}
                                    </c:when>
                                    <c:otherwise>
                                      ${textContainer.textEscapeXml['provisioningStemScopeOneLabel']}                              
                                    </c:otherwise>
                                  </c:choose>
                                  <br />
                                  <span class="description">${textContainer.text['provisioningStemScopeHint']}</span>
                                </td>
                              </tr>
                            </c:if>
                            
                          </c:if>
                          
                          <c:forEach items="${guiGrouperProvisioningAttributeValue.metadataItems}" var="metadataItem">
			  				
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
                    
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <p>${textContainer.text['provisioningNoneAllConfigured'] }</p>
                  </c:otherwise>
                </c:choose>