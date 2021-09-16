<%@ include file="../assetsJsp/commonTaglib.jsp"%>

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations');">${textContainer.text['miscellaneousSqlSyncOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousSqlSyncAddBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousSqlSyncMainDescription'] }</h4></div>
           <div class="span2 pull-right">
             <%@ include file="sqlSyncConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="sqlSyncConfigDetails">
            <table class="table table-condensed table-striped">
              <tbody>
                <%@ include file="sqlSyncConfigAddHelper.jsp" %>
                <tr>
                  <td>
                    <input type="hidden" name="mode" value="add">
                  </td>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                    aria-controls="sqlSyncConfigSubmitId" id="submitId"
                    value="${textContainer.text['sqlSyncConfigAddFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2SqlSyncConfiguration.addSqlSyncConfigSubmit', {formIds: 'sqlSyncConfigDetails'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations'); return false;"
                          >${textContainer.text['sqlSyncConfigAddFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>
