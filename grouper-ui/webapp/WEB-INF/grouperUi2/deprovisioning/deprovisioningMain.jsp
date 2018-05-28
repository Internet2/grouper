<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousDeprovisioningOverallBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9"><h1>${textContainer.text['miscellaneousDeprovisioningMainDecription'] }</h1></div>
                  <c:if test="${grouperRequestContainer.deprovisioningContainer.allowedToDeprovision}">
	                  <div class="span3" id="deprovisioningMainMoreActionsButtonContentsDivId">
	                    <%@ include file="deprovisioningMainMoreActionsButtonContents.jsp" %>
	                  </div>
                  </c:if>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousDeprovisioningMainSubtitle']}</p>
                  </div>
                </div>
                <c:if test="${not empty grouperRequestContainer.deprovisioningContainer.affiliation}">
                  <div class="row-fluid">
                    <div class="span12">
                      <label class="control-label help-inline">${textContainer.text['deprovisioningAffiliationLabel'] }</label>
                      ${grouperRequestContainer.deprovisioningContainer.affiliation}
                    </div>               
                  </div>
                </c:if>
              </div>
              
              <!-- <div class="row-fluid">
			         
			        </div> -->
              
              
            </div>

            <div class="row-fluid" id="deprovisioningUsers">
              <%@ include file="../deprovisioning/deprovisioningSelectAffiliation.jsp"%>
            </div>
