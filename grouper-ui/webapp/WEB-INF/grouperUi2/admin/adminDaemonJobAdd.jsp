<%@ include file="../assetsJsp/commonTaglib.jsp"%>

		<c:set value="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration}" var="guiGrouperDaemonConfiguration"/>
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsLink'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['daemonJobsAddDaemon'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <div class="row-fluid">
              
                <div class="span2 pull-right" id="daemonMoreActionsButtonContentsDivId">
                  <%@ include file="adminDaemonJobsMoreActionsButtonContents.jsp"%>
                </div>
                <div class="span10 pull-left">
                  <h1>${textContainer.text['daemonJobsAddDaemon'] }</h1>
                </div>
              </div>
              
            </div>
            <div class="row-fluid">
              <div class="lead span9">${textContainer.text['daemonJobsAddDaemon']}</div>


              <form class="form-inline form-small form-filter" id="addDaemonFormId">

                <table class="table table-condensed table-striped">
                  <tbody>

                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="daemonTypeId">${textContainer.text['daemonJobTypeLabel']}</label></strong></td>
                      <td>
						<select name="daemonConfigType" id="daemonTypeId" style="width: 30em"
						      onchange="ajax('../app/UiV2Admin.addDaemon', {formIds: 'addDaemonFormId'}); return false;">
						       
					        <option value=""></option>
					        <c:forEach items="${grouperRequestContainer.adminContainer.allGrouperDaemonTypesConfiguration}" var="grouperDaemonConfiguration">
					          <option value="${grouperDaemonConfiguration['class'].name}"
					              ${guiGrouperDaemonConfiguration.grouperDaemonConfiguration['class'].name == grouperDaemonConfiguration['class'].name ? 'selected="selected"' : '' }
					              >${grouperDaemonConfiguration.title}</option>
					        </c:forEach>
					      </select>
					      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
					      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
					      <br />
					      <span class="description">${textContainer.text['grouperDaemonTypeHint']}</span>
                      </td>
                    </tr>
                    
                    <c:forEach items="${grouperRequestContainer.adminContainer.guiGrouperDaemonConfiguration.grouperDaemonConfiguration.configAttributes}" var="attribute">
  				
		  				<grouper:configFormElement 
		  					formElementType="${attribute.formElement}" 
		  					configId="${attribute.configSuffix}" label="${attribute.label}"
		  					helperText="${attribute.description}"
		  					helperTextDefaultValue="${attribute.defaultValue}"
		  					required="${attribute.required}"
		  					shouldShow="true"
		  					value="${attribute.value}"
		  					hasExpressionLanguage="false"
		  					ajaxCallback="ajax('../app/UiV2ExternalSystem.addExternalSystem?externalSystemConfigId=${guiGrouperExternalSystem.grouperExternalSystem.configId}&externalSystemType=${guiGrouperExternalSystem.grouperExternalSystem['class'].name}', {formIds: 'externalSystemConfigDetails'}); return false;"
		  				/>
		  				
		  			</c:forEach>

                    <tr>
                      <td></td>
                      <td style="white-space: nowrap; padding-top: 1em; padding-bottom: 1em;">
                        <input type="submit" class="btn btn-primary"
                        aria-controls="addDaemonFormId" id="addDaemonSubmitButtonId"
                        value="${textContainer.text['grouperDaemonAddEditSubmitButton'] }"
                        onclick="ajax('../app/UiV2Admin.addDaemonSubmit', {formIds: 'addDaemonFormId'}); return false;">
                        
                      </td>
                    </tr>

                  </tbody>
                </table>
                
              </form>

            </div>
          </div>