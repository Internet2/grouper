<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisioningTargetNameId">${textContainer.text['provisioningTargetNameLabel']}</label></strong></td>
                          <td>
                            <input type="hidden" name="provisioningPreviousTargetName" value="${grouperRequestContainer.provisioningContainer.targetName}" />
                            <select name="provisioningTargetName" id="provisioningTargetNameId" style="width: 30em"
                            onchange="ajax('../app/UiV2Provisioning.editProvisioningOn${ObjectType}', {formIds: 'editProvisioningFormId'}); return false;">
                              <option value=""></option>
                                <c:forEach items="${grouperRequestContainer.provisioningContainer.editableTargets}" var="target">
                                <option value="${target.name}"
                                  ${grouperRequestContainer.provisioningContainer.targetName == target.name ? 'selected="selected"' : '' }
                                  >${target.externalizedName}
                                </option>
                              </c:forEach>
                            </select>
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right"
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['provisioningTargetNameHint']}</span>
                          </td>
                        </tr>
      
                      <%-- if the target is selected --%>
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.provisioningContainer.targetName)}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperObjectTypeHasConfigurationId">${textContainer.text['provisioningDirectIndirectTypeLabel']}</label></strong></td>
                          <td>
                            <select name="provisioningHasConfigurationName" id="provisioningHasConfigurationId" style="width: 30em"
                              onchange="ajax('../app/UiV2Provisioning.editProvisioningOn${ObjectType}', {formIds: 'editProvisioningFormId'}); return false;">
                              <option value="false" ${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.directAssignment ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['provisioningNoDoesNotHaveDirectLabel']}</option>
                              <option value="true" ${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.directAssignment ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['provisioningYesHasDirectLabel']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['provisioningHasTypeHint']}</span>
                          </td>
                        </tr>
                      </c:if>
                        
                        <%-- if there is configuration then show the rest --%>
                        <c:if test="${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.directAssignment}">
                        
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperDeprovisioningDeprovisionId">${textContainer.text['provisioningProvisionLabel']}</label></strong></td>
                            <td>
                              <select name="provisioningProvisionName" id="provisioningProvisionId" style="width: 30em">
                                <option value="true" ${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.doProvision ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['provisioningYesProvisionLabel']}</option>
                                <option value="false" ${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.doProvision ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['provisioningNoDontProvisionLabel']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['provisioningProvisionHint']}</span>
                            </td>
                          </tr> 
                          
                          <c:if test="${ObjectType == 'Folder' }">
                          
                            <tr>
                              <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisioningStemScopeId">${textContainer.text['provisioningStemScopeLabel']}</label></strong></td>
                              <td>
                                <select name="provisioningStemScopeName" id="provisioningStemScopeId" style="width: 30em">
                                  <option value="sub" ${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.stemScopeSub ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['provisioningStemScopeAllLabel']}</option>
                                  <option value="one" ${grouperRequestContainer.provisioningContainer.grouperProvisioningAttributeValue.stemScopeSub ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['provisioningStemScopeOneLabel']}</option>
                                </select>
                                <br />
                                <span class="description">${textContainer.text['provisioningStemScopeHint']}</span>
                              </td>
                            </tr>
                          </c:if>
                          
                          <c:forEach items="${grouperRequestContainer.provisioningContainer.grouperProvisioningObjectMetadataItems}" var="metadataItem">
			  				
			  				<grouper:provisioningMetadataItemFormElement
			  				    name="${metadataItem.name}"
			  					formElementType="${metadataItem.formElementType}" 
			  					labelKey="${metadataItem.labelKey}"
			  					descriptionKey="${metadataItem.descriptionKey}"
			  					required="${metadataItem.required}"
			  					value="${metadataItem.defaultValue}"
			  					valuesAndLabels="${metadataItem.keysAndLabelsForDropdown}"
			  				/>
			  				
			  		    </c:forEach>
                          
                        </c:if>

