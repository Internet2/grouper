<%@ include file="../assetsJsp/commonTaglib.jsp"%>

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousGrouperExternalSystemsBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousGrouperExternalSystemsMainDescription'] }</h4></div>
           <div class="span2 pull-right">
             <%@ include file="externalSystemsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
	  	
	  	<%-- <c:if test="${fn:length(grouperRequestContainer.externalSystemContainer.errors) > 0}">
           <div class="externalSystemErrors alert alert-error">
            <button type="button" class="close" data-dismiss="alert">x</button>
            <c:forEach var="error" items="${grouperRequestContainer.externalSystemContainer.errors}">
             <div>${error}</div>
            </c:forEach>
           </div>
         </c:if> --%>
         
         <form class="form-inline form-small form-filter" id="externalSystemConfigDetails">
            <table class="table table-condensed table-striped">
              <tbody>
                <%@ include file="externalSystemConfigAddHelper.jsp" %>
                <tr>
                  <td>
                    <input type="hidden" name="mode" value="add">
                  </td>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                    aria-controls="externalSystemConfigSubmitId" id="submitId"
                    value="${textContainer.text['grouperExternalSystemConfigAddFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2ExternalSystem.addExternalSystemSubmit', {formIds: 'externalSystemConfigDetails'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ExternalSystem.viewExternalSystems'); return false;"
                          >${textContainer.text['grouperExternalSystemConfigAddFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>
