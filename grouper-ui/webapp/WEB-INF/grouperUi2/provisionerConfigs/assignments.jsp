<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousProvisionerConfigurationsAssignmentsBreadcrumb'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsAssignmentsMainDescription'] }</h1></div>
                  <div class="span2 pull-right">
                   <c:set var="buttonSize" value="btn-medium" />
                   <c:set var="buttonBlock" value="btn-block" />
                   <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
              
            </div>
              
              
			<div class="row-fluid">
			    <div class="row-fluid">
            <div class="lead span9 pull-left">
              <h4>${textContainer.text['miscellaneousProvisionerConfigurationsAssignmentsTotalAssignments'] }</h4>
            </div>
          </div>
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.provisionerConfigurationContainer.guiProvisioningAssignments) > 0}">
			        <div>
			        <form class="form-inline form-small" name="provisionerConfigGroupsProvisionableFormName" id="provisionerConfigGroupsProvisionableFormId">
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['provisionerAssignmentsTableHeaderType']}</th>
			              <th>${textContainer.text['provisionerAssignmentsTableHeaderName']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisioningAssignments}" var="guiProvisioningAssignment">
			              
			                <tr>
                          <td style="white-space: nowrap;">
                            ${guiProvisioningAssignment.type}
                         </td>
			                   <td style="white-space: nowrap;">
                    			${guiProvisioningAssignment.guiGroupOrFolder.shortLink}
			                   </td>
			                </tr>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			         </form>
			        </div>
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9">
				          <p><b>
				          ${textContainer.text['provisionerConfigNoAssignmentsFound'] } 
				          ${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}
				          </b></p>
			          </div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
