<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.viewSubjectSources');">${textContainer.text['miscellaneousSubjectSourcesOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           		  <li class="active">${textContainer.text['miscellaneousSubjectSourceConfigAddBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
              
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousSubjectSourcesMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="subjectSourcesMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
              
            </div>

           <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>

         ${textContainer.text['miscellaneousSubjectSourcesMainDescriptionBelow'] }
         <br /><br />
         
         <form class="form-inline form-small form-filter" id="sourceConfigDetails">
         	<input type="hidden" name="previousSubjectSourceConfigId" value="${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration.configId}" />
         	<input type="hidden" name="previousSubjectSourceConfigType" value="${grouperRequestContainer.subjectSourceContainer.guiSubjectSourceConfiguration.subjectSourceConfiguration['class'].name}" />
            <table class="table table-condensed table-striped">
              <tbody>
                <%@ include file="subjectSourceConfigAddHelper.jsp" %>
                <tr>
                  <td>
                    <input type="hidden" name="mode" value="add">
                  </td>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary" id="submitId"
                    value="${textContainer.text['subjectSourcesAddFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2SubjectSource.addSubjectSourceSubmit', {formIds: 'sourceConfigDetails'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2SubjectSource.viewSubjectSources'); return false;"
                          >${textContainer.text['subjectSourcesAddFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>
           
