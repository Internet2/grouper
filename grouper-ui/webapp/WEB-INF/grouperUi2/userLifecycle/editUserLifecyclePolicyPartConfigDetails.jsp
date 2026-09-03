<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousUserLifecyclePolicyPartEditBreadcrumb')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
               <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
                  class="divider"><i class='fa fa-angle-right'></i></span></li>
              <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
                class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2UserLifecycle.viewUserLifecycleSummary" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecycleSummary');">${textContainer.text['miscellaneousUserLifecycleBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
            
            <li><a href="?operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts');">${textContainer.text['miscellaneousUserLifecyclePolicyPartsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
            
              <li class="active">${textContainer.text['miscellaneousUserLifecyclePolicyPartEditBreadcrumb'] }</li>
           </ul>
               
            <div class="page-header blue-gradient">
            
              <div class="row-fluid">
                <div class="lead span9 pull-left"><h1 class="grouper-heading-as-h4">${textContainer.text['miscellaneousUserLifecyclePolicyPartsMainDescription'] }</h1></div>
                <div class="span3 pull-right">
                     <%@ include file="userLifecyclePolicyPartsMoreActionsButtonContents.jsp"%>
                 </div>
                 </div>
              </div>
            </div>
              
      <div class="row-fluid">
        <div class="span12">
          <form class="form-inline form-small form-filter" id="userLifecyclePolicyPartsConfigDetails">
            <input type="hidden" name="previousConfigId" value="${grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyPartConfiguration.userLifecyclePolicyPartConfiguration.configId}" />
          
          <table class="table table-condensed table-striped">
                    <tbody>
                    
                      <tr>
              <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['dataFieldConfigIdLabel']}</label></strong></td>
                <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                <td>
                 ${grouper:escapeHtml(grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyPartConfiguration.userLifecyclePolicyPartConfiguration.configId)}
                </td>
            </tr>
                    
                      
              <c:forEach items="${grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyPartConfiguration.userLifecyclePolicyPartConfiguration.subSections}" var="subSection">
                <tbody>
                  <c:if test="${!grouper:isBlank(subSection.label) and subSection.show }">
                    <tr>
                      <th colspan="3">
                        <h4>${subSection.title}</h4>
                        <p style="font-weight: normal;">${subSection.description} </p>
                      </th>
                    </tr>
                  
                  </c:if>
                  
                  <c:forEach items="${subSection.attributesValues}" var="attribute">
                    
                    <grouper:configFormElement 
                      formElementType="${attribute.formElement}" 
                      configId="${attribute.configSuffix}" 
                      label="${attribute.label}"
                      readOnly="${attribute.readOnly}"
                      helperText="${attribute.description}"
                      helperTextDefaultValue="${attribute.defaultValue}"
                      required="${attribute.required}"
                      shouldShow="${attribute.show}"
                      value="${attribute.valueOrExpressionEvaluation}"
                      hasExpressionLanguage="${attribute.expressionLanguage}"
                      ajaxCallback="ajax('../app/UiV2UserLifecycle.editUserLifecyclePolicyPartConfig?configId=${grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyPartConfiguration.userLifecyclePolicyPartConfiguration.configId}', {formIds: 'userLifecyclePolicyPartsConfigDetails'}); return false;"
                      valuesAndLabels="${attribute.dropdownValuesAndLabels }"
                      checkboxAttributes="${attribute.checkboxAttributes}"
                      indent="${attribute.configItemMetadata.indent}"
                    />
                    
                  </c:forEach>
                  
                </tbody>
            
            </c:forEach>
                      
                    </tbody>
                  </table>
          
          <div class="span6">
                   
                     <input type="submit" class="btn btn-primary"
                          aria-controls="dataFieldConfigDetails" id="submitId"
                          value="${textContainer.text['dataFieldConfigAddFormSubmitButton'] }"
                          onclick="ajax('../app/UiV2UserLifecycle.editUserLifecyclePolicyPartConfigSubmit?configId=${grouperRequestContainer.userLifecycleContainer.guiUserLifecyclePolicyPartConfiguration.userLifecyclePolicyPartConfiguration.configId}', {formIds: 'userLifecyclePolicyPartsConfigDetails'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts'); return false;"
                          >${textContainer.text['dataFieldConfigAddFormCancelButton'] }</a>
                   
                   </div>
        </form>
        </div>
      </div>