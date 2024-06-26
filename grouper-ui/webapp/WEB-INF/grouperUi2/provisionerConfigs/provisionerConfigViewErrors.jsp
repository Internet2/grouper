<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerConfigViewErrorsBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsErrorsDescription'] }</h1></div>
           
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
         <div class="row-fluid">
          <div class="lead span9 pull-left">
            <p>${textContainer.text['provisionerConfigErrorSubtitle']}</p>
          </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="provisionerConfigViewErrors">
         	<input type="hidden" name="provisionerConfigId" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}" />
         	<input type="hidden" name="provisionerConfigType" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}" />
        <table class="table table-condensed table-striped">
              <tbody>
              	<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisionerConfigIdLabel']}</strong></td>
				    <td>
				     ${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId)}
				    </td>
				</tr>
				
				<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisionerConfigObjectTypeId">${textContainer.text['provisionerConfigObjectType']}</label></strong></td>
				    <td>
               <select name="provisionerConfigObjectType" id="provisionerConfigObjectTypeId" style="width: 30em">
                <option value=""></option>
                 <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'group' ? 'selected="selected"' : '' } value="group">${textContainer.textEscapeXml['provisionerConfigObjectTypeGroup']}</option>
                 <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'entity' ? 'selected="selected"' : '' } value="entity">${textContainer.textEscapeXml['provisionerConfigObjectTypeEntity']}</option>
                 <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'membership' ? 'selected="selected"' : '' } value="membership">${textContainer.textEscapeXml['provisionerConfigObjectTypeMembership']}</option>
               </select>
               <br />
               <span class="description">${textContainer.text['provisionerConfigObjectTypeHint']}</span>
             </td>
				</tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisionerConfigErrorTypeId">${textContainer.text['provisionerConfigErrorType']}</label></strong></td>
            <td>
               <select name="provisionerConfigErrorType" id="provisionerErrorTypeId" style="width: 30em">
                <option value=""></option>
                <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.allGcGrouperSyncErrorCodes}" var="errorCode">
                  <option value="${errorCode}"
                    ${grouperRequestContainer.provisionerConfigurationContainer.selectedErrorCode == errorCode ? 'selected="selected"' : '' }>${errorCode}</option>
                </c:forEach>
               </select>
               <br />
               <span class="description">${textContainer.text['provisionerConfigErrorCodeHint']}</span>
             </td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisionerConfigErrorDurationId">${textContainer.text['provisionerConfigErrorDuration']}</label></strong></td>
            <td>
               <select name="provisionerConfigErrorDuration" id="provisionerConfigErrorDurationId" style="width: 30em">
                <option value=""></option>
                 <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorDuration == 'last_15_minutes' ? 'selected="selected"' : '' } value="last_15_minutes">${textContainer.textEscapeXml['provisionerConfigErrorDurationLast15Minutes']}</option>
                 <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorDuration == 'last_hour' ? 'selected="selected"' : '' } value="last_hour">${textContainer.textEscapeXml['provisionerConfigErrorDurationLastHour']}</option>
                 <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorDuration == 'last_day' ? 'selected="selected"' : '' } value="last_day">${textContainer.textEscapeXml['provisionerConfigErrorDurationLastDay']}</option>
               </select>
               <br />
               <span class="description">${textContainer.text['provisionerConfigErrorDurationHint']}</span>
             </td>
        </tr>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisionerConfigErrorGroupId">${textContainer.text['provisionerConfigErrorGroup']}</label></strong></td>
          <td>
             <input type="text" name="provisionerConfigErrorGroup" id="provisionerConfigErrorGroupId" value="${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorGroup}" style="width: 30em" />
             <br />
             <span class="description">${textContainer.text['provisionerConfigErrorGroupHint']}</span>
           </td>
        </tr>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisionerConfigErrorEntityId">${textContainer.text['provisionerConfigErrorEntity']}</label></strong></td>
          <td>
             <input type="text" name="provisionerConfigErrorEntity" id="provisionerConfigErrorEntityId" value="${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorEntity}" style="width: 30em" />
             <br />
             <span class="description">${textContainer.text['provisionerConfigErrorEntityHint']}</span>
           </td>
        </tr>
				
                <tr>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                    aria-controls="provisionerConfigSubmitId" id="submitId"
                    value="${textContainer.text['provisionerConfigRunFullSyncFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2ProvisionerConfiguration.viewProvisionerErrorsSubmit', {formIds: 'provisionerConfigViewErrors'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                          >${textContainer.text['provisionerConfigViewActivityFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>

        <script language="javascript">
          $(document).ready(function() {
            if ($('#provisionerConfigObjectTypeId').val() === 'group') {
              $('#provisionerConfigErrorEntityId').prop('disabled', true);
            } else if ($('#provisionerConfigObjectTypeId').val() === 'entity') {
              $('#provisionerConfigErrorGroupId').prop('disabled', true);
            }

            $("#provisionerConfigObjectTypeId").on('change', function() {
              if (this.value === 'group') {
                $('#provisionerConfigErrorGroupId').prop('disabled', false);
                $('#provisionerConfigErrorEntityId').prop('disabled', true).val('');
              } else if (this.value === 'entity') {
                $('#provisionerConfigErrorGroupId').prop('disabled', true).val('');
                $('#provisionerConfigErrorEntityId').prop('disabled', false);
              } else {
                $('#provisionerConfigErrorGroupId').prop('disabled', false);
                $('#provisionerConfigErrorEntityId').prop('disabled', false);
              }
            });
          });
        </script>
	
  <%@ include file="provisioningConfigErrorsSummary.jsp"%>	
  <%@ include file="provisioningConfigErrors.jsp"%>	
	
	
