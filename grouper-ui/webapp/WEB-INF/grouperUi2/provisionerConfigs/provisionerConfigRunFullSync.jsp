<%@ include file="../assetsJsp/commonTaglib.jsp"%>

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerConfigRunFullSyncBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsMainDescription'] }</h1></div>
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="provisionerConfigRunFullSync">
         	<input type="hidden" name="provisionerConfigId" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}" />
         	<input type="hidden" name="provisionerConfigType" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}" />
            <table class="table table-condensed table-striped">
              <tbody>
              	<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerConfigIdLabel']}</label></strong></td>
				    <td>
				     ${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId)}
				    </td>
				</tr>
				
				<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerConfigSynchronous']}</label></strong></td>
				    <td>
                       <select name="provisionerConfigSynchoronous" id="provisionerConfigSynchoronousId" style="width: 30em">
                         <option value="false">${textContainer.textEscapeXml['provisionerConfigSynchronousYesOption']}</option>
                         <option value="true">${textContainer.textEscapeXml['provisionerConfigSynchronousNoOption']}</option>
                       </select>
                       <br />
                       <span class="description">${textContainer.text['provisionerConfigSynchronousHint']}</span>
                     </td>
				</tr>
				
				<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerConfigReadOnly']}</label></strong></td>
				    <td>
                       <select name="provisionerConfigReadOnly" id="provisionerConfigReadOnlyId" style="width: 30em">
                         <option value="false">${textContainer.textEscapeXml['provisionerConfigReadOnlyNoOption']}</option>
                         <option value="true">${textContainer.textEscapeXml['provisionerConfigReadOnlyYesOption']}</option>
                       </select>
                       <br />
                       <span class="description">${textContainer.text['provisionerConfigReadOnlyHint']}</span>
                     </td>
				</tr>
              
                <tr>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                    aria-controls="provisionerConfigSubmitId" id="submitId"
                    value="${textContainer.text['provisionerConfigRunFullSyncFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2ProvisionerConfiguration.runFullSyncSubmit', {formIds: 'provisionerConfigRunFullSync'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                          >${textContainer.text['provisionerConfigRunFullSyncFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>
