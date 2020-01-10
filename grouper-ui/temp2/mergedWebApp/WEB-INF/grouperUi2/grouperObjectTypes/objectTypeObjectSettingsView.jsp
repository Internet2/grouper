<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                <c:choose>
                  <c:when test="${grouperRequestContainer.objectTypeContainer.hasObjectTypeOnThisObjectOrParent}">
                    <c:forEach items="${grouperRequestContainer.objectTypeContainer.guiGrouperObjectTypesAttributeValues}" var="guiGrouperObjectTypesAttributeValue" >
                    
	                    <c:set var="grouperObjectTypesAttributeValue" 
	                        value="${guiGrouperObjectTypesAttributeValue.grouperObjectTypesAttributeValue}" />
                      
                      <h4>
                        ${textContainer.text['objectTypeHasTypeLabel'] }: ${grouperObjectTypesAttributeValue.objectTypeName }
                        
                        <c:if test="${grouperObjectTypesAttributeValue.directAssignment}">
	                        <c:choose>
	                          <c:when test="${ObjectType == 'Folder'}">
	                            
	                            <a title="${textContainer.text['objectTypeTitleRemoveThisObjectType'] }" href="#" 
	                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['objectTypeConfirmObjectTypeRemoval']}')) { return guiV2link('operation=UiV2GrouperObjectTypes.removeObjectTypeFromFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&grouperObjectTypeName=${grouperObjectTypesAttributeValue.objectTypeName}'); } return false;"
	                                  >
	                                  <span style="font-size: 14px; font-weight: normal;">${textContainer.text['objectTypeTextRemoveThisObjectType'] }</span>
	                            </a>
	                          </c:when>
	                          <c:otherwise>
	                          
	                          <a title="${textContainer.text['objectTypeTitleRemoveThisObjectType'] }" href="#" 
	                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['objectTypeConfirmObjectTypeRemoval']}')) { return guiV2link('operation=UiV2GrouperObjectTypes.removeObjectTypeFromGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&grouperObjectTypeName=${grouperObjectTypesAttributeValue.objectTypeName}'); } return false;"
	                                  >
	                                  <span style="font-size: 14px; font-weight: normal;">${textContainer.text['objectTypeTextRemoveThisObjectType'] }</span>
	                          </a>
	                          </c:otherwise>
	                        </c:choose>
                        </c:if>
                        
                      </h4>
                      <h5>
	                      ${textContainer.text['objectTypeViewTypeDescriptionLabel'] }:
	                        <span style="font-size: 12px; font-weight: normal;">${textContainer.text[guiGrouperObjectTypesAttributeValue.objectTypeDescriptionKey]}</span>
                      </h5>
                      <table class="table table-condensed table-striped">
                        <tbody>
                         
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['objectTypeHasConfigurationLabel'] }</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperObjectTypesAttributeValue.directAssignment || not empty grouperObjectTypesAttributeValue.objectTypeOwnerStemId }">
                                  ${textContainer.textEscapeXml['objectTypeYesHasConfigurationLabel']}
                                </c:when>
                                <c:otherwise>
                                  ${textContainer.textEscapeXml['objectTypeNoHasConfigurationLabel']}                              
                                </c:otherwise>
                              </c:choose>
                              <br />
                              <span class="description">${textContainer.text['objectTypeHasConfigurationHint']}</span>
                            </td>
                          </tr>
                          
                          <c:if test="${grouperObjectTypesAttributeValue.directAssignment || not empty grouperObjectTypesAttributeValue.objectTypeOwnerStemId}">
                          
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['objectTypeHasDirectTypeLabel'] }</strong></td>
                              <td>
                                <c:choose>
                                  <c:when test="${grouperObjectTypesAttributeValue.directAssignment}">
                                    ${textContainer.textEscapeXml['objectTypeYesHasTypeLabel']}
                                  </c:when>
                                  <c:otherwise>
                                    ${textContainer.textEscapeXml['objectTypeNoDoesNotHaveTypeLabel']}                           
                                  </c:otherwise>
                                </c:choose>
                                <br />
                                <span class="description">${textContainer.text['objectTypeHasTypeHint']}</span>
                              </td>
                            </tr>
                            <c:if test="${!grouperObjectTypesAttributeValue.directAssignment}">

                              <tr>
                                <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['objectTypeParentFolderLabel'] }</strong></td>
                                <td>
                                  ${guiGrouperObjectTypesAttributeValue.guiFolderWithSettings.shortLinkWithIcon}
                                  <br />
                                  <span class="description">${textContainer.text['objectTypeParentFolderDescription']}</span>
                                </td>
                              </tr>

                            </c:if>
                            
                            <c:if test="${grouperObjectTypesAttributeValue.objectTypeDataOwner != null}">
	                            <tr>
	                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['objectTypeDataOwnerLabel'] }</strong></td>
	                              <td>
	                                ${grouperObjectTypesAttributeValue.objectTypeDataOwner}
	                                <br />
	                                <span class="description">${textContainer.text['objectTypeDataOwnerHint']}</span>
	                              </td>
	                            </tr>
                            </c:if>
                            
                            <c:if test="${grouperObjectTypesAttributeValue.objectTypeMemberDescription != null}">
                              <tr>
	                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['objectTypeMemberDescriptionLabel'] }</strong></td>
	                              <td>
	                                ${grouperObjectTypesAttributeValue.objectTypeMemberDescription}
	                                <br />
	                                <span class="description">${textContainer.text['objectTypeMemberDescriptionHint']}</span>
	                              </td>
                              </tr>
                            </c:if>
                            
                            <c:if test="${grouperObjectTypesAttributeValue.objectTypeServiceName != null}">
                              <tr>
	                              <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['objectTypeServiceNameLabel'] }</strong></td>
	                              <td>
	                                ${grouperObjectTypesAttributeValue.objectTypeServiceName}
	                                <br />
	                                <span class="description">${textContainer.text['objectTypeServiceNameHint']}</span>
	                              </td>
                              </tr>
                            </c:if>
                            
                          </c:if>

                        </tbody>
                      </table>
                    
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <p>${textContainer.text['objectTypeNoneAllConfigured'] }</p>
                  </c:otherwise>
                </c:choose>