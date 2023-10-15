<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<form id="newGroupTemplateFormId" class="form-horizontal">
 
  <c:if test="${grouperRequestContainer.groupStemTemplateContainer.showOnFolder}">
    <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
  </c:if>
  
   <div class="page-header blue-gradient span12" style="margin-left:0; margin-bottom:0">
     <div class="row-fluid">

       <div class="span10" style="margin-left: 0">
         <p class="lead" id="templateHeader"></p>
         <h1>${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateConfig.templateNameForUi}</h1>
         <div class="span11" style="margin-left: 0.10em">
           <span>${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.gshTemplateConfig.templateDescriptionForUi }</span><br /><br />
         </div>
       </div>
     </div>
   </div>  
  
  <table class="table table-condensed table-striped">
    <tbody>
      <c:if test="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig != null}">

      	<c:forEach items="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig.guiGshTemplateInputConfigs}" var="guiGshTemplateInputConfigMap">
			
			<c:set var="guiGshTemplateInputConfigName" value="${guiGshTemplateInputConfigMap.key}"></c:set>		  				
			<c:set var="guiGshTemplateInputConfig" value="${guiGshTemplateInputConfigMap.value}"></c:set>		  				
								  				
			<grouper:configFormElement 
				formElementType="${guiGshTemplateInputConfig.gshTemplateInputConfig.configItemFormElement}"
				configId="${guiGshTemplateInputConfig.gshTemplateInputConfig.name}" 
				label="${guiGshTemplateInputConfig.gshTemplateInputConfig.labelForUi}"
				readOnly="false"
				helperText="${guiGshTemplateInputConfig.gshTemplateInputConfig.descriptionForUi}"
				helperTextDefaultValue="${guiGshTemplateInputConfig.gshTemplateInputConfig.defaultValue}"
				required="${guiGshTemplateInputConfig.gshTemplateInputConfig.required}"
				shouldShow="true"
				shouldShowElCheckbox="false"
				value="${guiGshTemplateInputConfig.value}"
				hasExpressionLanguage="false"
				ajaxCallback="ajax('../app/UiV2Template.newTemplateSimplifiedUi?templateType=${grouperRequestContainer.groupStemTemplateContainer.templateType}', {formIds: 'newGroupTemplateFormId'}); return false;"
				valuesAndLabels="${guiGshTemplateInputConfig.gshTemplateInputConfig.dropdownKeysAndLabels}"
        indent="${attribute.configItemMetadata.indent}"
			/>
  				
  			</c:forEach>
      
      </c:if>

      <tr>
        <td></td>
        <td
          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
          
          <c:if test="${grouperRequestContainer.groupStemTemplateContainer.guiGshTemplateConfig != null}">
            <input type="submit" class="btn btn-primary"
	          aria-controls="groupFilterResultsId" id="filterSubmitId"
	          value="${textContainer.text['stemTemplateSubmitButton'] }"
            <c:if test="${grouperRequestContainer.groupStemTemplateContainer.showOnFolder}">
              onclick="$('#groupTemplateBody').empty(); guiScrollTop(); ajax('../app/UiV2Template.customTemplateExecute?templateType=${grouperRequestContainer.groupStemTemplateContainer.templateType}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&simplifiedUi=true', {formIds: 'newGroupTemplateFormId'}); return false;"
            </c:if>
            <c:if test="${grouperRequestContainer.groupStemTemplateContainer.showOnGroup}">
              onclick="$('#groupTemplateBody').empty(); guiScrollTop(); ajax('../app/UiV2Template.customTemplateExecute?templateType=${grouperRequestContainer.groupStemTemplateContainer.templateType}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&simplifiedUi=true', {formIds: 'newGroupTemplateFormId'}); return false;"
            </c:if>
            >
          </c:if>
          
          &nbsp;
          <a href="#" class="btn btn-cancel" role="button" onclick="location.reload()" >${textContainer.text['stemTemplateCancelButton'] }</a>
        </td>
      </tr>
    </tbody>
  </table>  
</form>
<div id="groupTemplateBody"></div>