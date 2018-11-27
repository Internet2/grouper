<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperObjectTypeNameId">${textContainer.text['objectTypeNameLabel']}</label></strong></td>
                          <td>
                            <input type="hidden" name="grouperObjectTypePreviousTypeName" value="${grouperRequestContainer.objectTypeContainer.objectTypeName}" />
                            <select name="grouperObjectTypeName" id="grouperObjectTypeNameId" style="width: 30em"
                            onchange="ajax('../app/UiV2GrouperObjectTypes.editObjectTypesOn${ObjectType}', {formIds: 'editGrouperObjectTypeFormId'}); return false;">
                              <option value=""></option>
                              <c:forEach items="${grouperRequestContainer.objectTypeContainer.objectTypeNames}" var="objectTypeName">
                                <option value="${objectTypeName}"
                                    ${grouperRequestContainer.objectTypeContainer.objectTypeName == objectTypeName ? 'selected="selected"' : '' }
                                    >${objectTypeName}</option>
                              </c:forEach>
                            </select>
                            <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
                            <br />
                            <span class="description">${textContainer.text['objectTypeNameHint']}</span>
                          </td>
                        </tr>
      
                      <%-- if the type is selected --%>
                      <c:if test="${!grouper:isBlank(grouperRequestContainer.objectTypeContainer.objectTypeName)}">
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperObjectTypeHasConfigurationId">${textContainer.text['objectTypeHasTypeLabel']}</label></strong></td>
                          <td>
                            <select name="grouperObjectTypeHasConfigurationName" id="grouperObjectTypeHasConfigurationId" style="width: 30em"
                              onchange="ajax('../app/UiV2GrouperObjectTypes.editObjectTypesOn${ObjectType}', {formIds: 'editGrouperObjectTypeFormId'}); return false;">
                              <option value="false" ${grouperRequestContainer.objectTypeContainer.grouperObjectTypesAttributeValue.directAssignment ? '' : 'selected="selected"' } >${textContainer.textEscapeXml['objectTypeNoDoesNotHaveTypeLabel']}</option>
                              <option value="true" ${grouperRequestContainer.objectTypeContainer.grouperObjectTypesAttributeValue.directAssignment ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['objectTypeYesHasTypeLabel']}</option>
                            </select>
                            <br />
                            <span class="description">${textContainer.text['objectTypeHasTypeHint']}</span>
                          </td>
                        </tr>
                      </c:if>
                        
                        <%-- if there is configuration then show the rest --%>
                        <c:if test="${grouperRequestContainer.objectTypeContainer.grouperObjectTypesAttributeValue.directAssignment}">
                          
                          <c:if test="${grouperRequestContainer.objectTypeContainer.showDataOwnerMemberDescription }">
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperObjectTypeDataOwnerId">${textContainer.text['objectTypeDataOwnerLabel']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.objectTypeContainer.grouperObjectTypesAttributeValue.objectTypeDataOwner)}"
	                                         name="grouperObjectTypeDataOwner" id="grouperObjectTypeDataOwnerId" />
	                              <br />
	                              <span class="description">${textContainer.text['objectTypeDataOwnerHint']}</span>
	                            </td>
	                          </tr>
	                          
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperObjectTypeMemberDescriptionId">${textContainer.text['objectTypeMemberDescriptionLabel']}</label></strong></td>
	                            <td>
	                              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.objectTypeContainer.grouperObjectTypesAttributeValue.objectTypeMemberDescription)}"
	                                         name="grouperObjectTypeMemberDescription" id="grouperObjectTypeMemberDescriptionId" />
	                              <br />
	                              <span class="description">${textContainer.text['objectTypeMemberDescriptionHint']}</span>
	                            </td>
	                          </tr>
                          </c:if>
                          
                          <c:if test="${grouperRequestContainer.objectTypeContainer.showServiceName}">
	                          <tr>
	                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="grouperObjectTypeServiceNameId">${textContainer.text['objectTypeServiceNameLabel']}</label></strong></td>
	                            <td>
	                              <select name="grouperObjectTypeServiceName" id="grouperObjectTypeServiceNameId" style="width: 30em">
                                  <option value=""></option>
                                  <c:forEach items="${grouperRequestContainer.objectTypeContainer.serviceStems}" var="serviceStem">
		                                <option value="${serviceStem.stem.name}"
		                                    ${grouperRequestContainer.objectTypeContainer.grouperObjectTypesAttributeValue.objectTypeServiceName == serviceStem.stem.name ? 'selected="selected"' : '' }
		                                    >${serviceStem.stem.name}</option>
		                              </c:forEach>
                                </select>
	                              <br />
	                              <span class="description">${textContainer.text['objectTypeServiceNameHint']}</span>
	                            </td>
	                          </tr>
                          </c:if>
                        </c:if>

