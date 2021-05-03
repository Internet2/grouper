<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisioningTargetNameId">${textContainer.text['provisioningTargetNameLabel']}</label></strong></td>
                          <td>
                            <input type="hidden" name="provisioningPreviousTargetName" value="${grouperRequestContainer.provisioningContainer.targetName}" />
                            <select name="provisioningTargetName" id="provisioningTargetNameId" style="width: 30em"
                            onchange="ajax('../app/UiV2Provisioning.editProvisioningOnSubjectMembership', {formIds: 'editProvisioningFormId'}); return false;">
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
                        
                      

