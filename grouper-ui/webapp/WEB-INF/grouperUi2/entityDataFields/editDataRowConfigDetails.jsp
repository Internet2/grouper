<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
               <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
                  class="divider"><i class='fa fa-angle-right'></i></span></li>
              <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
                class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataFieldsSummary');">${textContainer.text['miscellaneousEntityDataFieldsBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
              <li><a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataRows');">${textContainer.text['miscellaneousDataRowsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
            
              <li class="active">${textContainer.text['miscellaneousDataRowEditBreadcrumb'] }</li>
           </ul>
              
            <div class="page-header blue-gradient">
            
              <div class="row-fluid">
                <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousDataRowsMainDescription'] }</h4></div>
                <div class="span3 pull-right">
                     <%@ include file="dataRowConfigsMoreActionsButtonContents.jsp"%>
                 </div>
                 </div>
              </div>
            </div>
              
      <div class="row-fluid">
        <div class="span12">
          <form class="form-inline form-small form-filter" id="dataRowConfigDetails">
            <input type="hidden" name="previousDataRowConfigId" value="${grouperRequestContainer.entityDataFieldsContainer.guiDataRowConfiguration.grouperDataRowConfiguration.configId}" />
          
          <table class="table table-condensed table-striped">
                    <tbody>
                    
                      <tr>
              <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['dataRowConfigIdLabel']}</label></strong></td>
                <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
                <td>
                 ${grouper:escapeHtml(grouperRequestContainer.entityDataFieldsContainer.guiDataRowConfiguration.grouperDataRowConfiguration.configId)}
                </td>
            </tr>
                    
                      
              <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.guiDataRowConfiguration.grouperDataRowConfiguration.subSections}" var="subSection">
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
                      ajaxCallback="ajax('../app/UiV2EntityDataFields.editDataRowConfig?dataRowConfigId=${grouperRequestContainer.entityDataFieldsContainer.guiDataRowConfiguration.grouperDataRowConfiguration.configId}', {formIds: 'dataRowConfigDetails'}); return false;"
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
                          aria-controls="dataRowConfigDetails" id="submitId"
                          value="${textContainer.text['sqlSyncConfigAddFormSubmitButton'] }"
                          onclick="ajax('../app/UiV2EntityDataFields.editDataRowConfigSubmit?dataRowConfigId=${grouperRequestContainer.entityDataFieldsContainer.guiDataRowConfiguration.grouperDataRowConfiguration.configId}', {formIds: 'dataRowConfigDetails'}); return false;">
                          &nbsp; 
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataRows'); return false;"
                          >${textContainer.text['sqlSyncConfigAddFormCancelButton'] }</a>
                   
                   </div>
        </form>
        </div>
      </div>