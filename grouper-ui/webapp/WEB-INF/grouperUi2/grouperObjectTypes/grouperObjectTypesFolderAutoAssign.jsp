<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <%@ include file="../stem/stemMoreTab.jsp" %>
                </ul>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['objectTypeFolderSettingsTitle'] }</div>
                  <div class="span3" id="grouperTypesFolderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperObjectTypesFolderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <c:choose>
	                <c:when test="${fn:length(grouperRequestContainer.objectTypeContainer.guiStemObjectTypes) > 0}">
	                  <form class="form-inline form-small form-filter" id="grouperObjectTypeAutoAssignFormId">
	                  	  
	                  	  <div class="control-group">
	                  	  	<label class="control-label" for="notImportantXyzId">${textContainer.text['objectTypeAssignAllTypesLabel']}</label>
		                    <input type="checkbox" style="margin-top: 0px;" name="notImportantXyzName" id="notImportantXyzId"
		                      onchange="$('.objectType').prop('checked', $('#notImportantXyzId').prop('checked'));" />
	                  	  </div>
	                  	  
	                  	  <c:if test="${grouperRequestContainer.objectTypeContainer.totalAutoAssignSize > grouperRequestContainer.objectTypeContainer.maxAutoAssignSize}">
		                  	  <div class="row-fluid">
		                  	  	<div class="span12">
		                  	  		<p class="lead" style="color: red;">${textContainer.text['objectTypeAutoAssignSizeLimitExceedMessage']}</p> 
		                  	  	</div>
		                  	  </div>
	                  	  </c:if>
	                  
		                  <c:forEach var="guiStemObjectType" items="${grouperRequestContainer.objectTypeContainer.guiStemObjectTypes}">
		                    <table class="table table-condensed" 
		                      style="background-color: #ffffff; border:1px #d5d0d0 solid;">
		                    <tr>
                          <td style="vertical-align: top; white-space: nowrap; width: 30%"><strong><label>${textContainer.text['objectTypeNameLabel']}</label></strong></td>
                          <td>
                            ${guiStemObjectType.stemObjectType.objectType}
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap; width: 30%">
                          <strong><label>
	                          <c:if test="${guiStemObjectType.stem}">
	                           ${textContainer.text['objectTypeAutoAssignTableFolderLabel']}
	                          </c:if>
	                          <c:if test="${guiStemObjectType.stem == false}"> 
	                           ${textContainer.text['objectTypeAutoAssignTableGroupLabel']}
	                          </c:if>
                          </label></strong>
                          </td>
                          <td>
                           ${guiStemObjectType.guiObject.linkWithIcon}
                          </td>
                        </tr>
                        
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap; width: 30%"><strong><label>${textContainer.text['objectTypeAutoAssignTableSelectLabel']}</label></strong></td>
                          <td>
                           <label class="checkbox">
                           	<c:if test="${guiStemObjectType.stem}">
                              <input type="checkbox" class="objectType" name="stemObjectType" value="${guiStemObjectType.guiObject.grouperObject.id}_${guiStemObjectType.stemObjectType.objectType}_stem"
                               aria-label="${textContainer.text['objectTypeAutoAssignCheckboxAriaLabel'] }"/>
                            </c:if>
                            <c:if test="${guiStemObjectType.stem == false}">
	                            <input type="checkbox" class="objectType" name="stemObjectType" value="${guiStemObjectType.guiObject.grouperObject.id}_${guiStemObjectType.stemObjectType.objectType}_group"
	                               aria-label="${textContainer.text['objectTypeAutoAssignCheckboxAriaLabel'] }"/>
                            </c:if>
                            </label>
                            
                          </td>
                        </tr>
                        
                        <c:if test="${guiStemObjectType.showDataOwnerMemberDescription}">
	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap; width: 30%"><strong><label>${textContainer.text['objectTypeAutoAssignTableDataOwnerLabel']}</label></strong></td>
	                          <td>
	                            <input type="text" name="${guiStemObjectType.guiObject.grouperObject.id}_${guiStemObjectType.stemObjectType.objectType}_dataOwner"/>
	                          </td>
	                        </tr>
	                        
	                        <tr>
	                          <td style="vertical-align: top; white-space: nowrap; width: 30%"><strong><label>${textContainer.text['objectTypeAutoAssignTableMemberDescriptionLabel']}</label></strong></td>
	                          <td>
	                            <input type="text" name="${guiStemObjectType.guiObject.grouperObject.id}_${guiStemObjectType.stemObjectType.objectType}_memberDescription"/>
	                          </td>
	                        </tr>
                        </c:if>
                        
                        <c:if test="${guiStemObjectType.showServiceName}">
                        
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap; width: 30%"><strong><label>${textContainer.text['objectTypeAutoAssignTableServiceLabel']}</label></strong></td>
                            <td>
                              <input type="text" name="${guiStemObjectType.guiObject.grouperObject.id}_${guiStemObjectType.stemObjectType.objectType}_service"/>
                            </td>
                          </tr>
                        
                        </c:if>
                        
                        </br>
		                   </table>
		                  </c:forEach>
		                  <table>
			                  <tr>
	                        <td></td>
	                        <td
	                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
	                          <input type="submit" class="btn btn-primary"
	                          aria-controls="objectTypeSubmitId" id="submitId"
	                          value="${textContainer.text['objectTypeAutoAssignSubmitButton'] }"
	                          onclick="ajax('../app/UiV2GrouperObjectTypes.objectTypeAutoAssignFolderSave?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'grouperObjectTypeAutoAssignFormId'}); return false;">
	                          &nbsp; <a class="btn btn-cancel" role="button"
	                          onclick="return guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                          >${textContainer.text['objectTypeEditButtonCancel'] }</a>
	                        </td>
	                      </tr>
	                   </table>
	                  </form>
	                </c:when>
	                <c:otherwise>
	                  <p>${textContainer.text['objectTypeAutoAssignNoFoldersFound'] }</p>
	                </c:otherwise>
                </c:choose>
                

              </div>
            </div>
