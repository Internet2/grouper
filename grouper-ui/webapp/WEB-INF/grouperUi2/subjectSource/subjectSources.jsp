<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousSubjectSourcesOverallBreadcrumb'] }</li>
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
			    <c:choose>
			      <c:when test="${fn:length(grouperRequestContainer.subjectSourceContainer.sources) > 0}">
			        
			        <table class="table table-hover table-bordered table-striped table-condensed data-table">
			          <thead>        
			            <tr>
			              <th>${textContainer.text['subjectSourcesTableHeaderSourceId']}</th>
			              <th>${textContainer.text['subjectSourcesTableHeaderSourceName']}</th>
			              <th>${textContainer.text['subjectSourcesTableHeaderType']}</th>
			              <th>${textContainer.text['subjectSourcesTableHeaderEnabled']}</th>
			              <th>${textContainer.text['subjectSourcesTableHeaderActions']}</th>
			            </tr>
			            </thead>
			            <tbody>
			              <c:set var="i" value="0" />
			              <c:forEach items="${grouperRequestContainer.subjectSourceContainer.sources}" var="source">
			              
			                <tr>
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(source.id)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                    ${grouper:escapeHtml(source.name)}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                   	<c:if test="${source.editable == true}">
			                      ${textContainer.text['subjectSourcesTableTypeEditableTrueValue']}
			                    </c:if> 
			                    
			                    <c:if test="${source.editable == false}">
			                      ${textContainer.text['subjectSourcesTableTypeEditableFalseValue']} 
			                    </c:if>
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                   <c:if test="${source.enabled == true}">
			                      ${textContainer.text['subjectSourcesTableTypeEnabledTrueValue']}
			                    </c:if> 
			                    
			                    <c:if test="${source.enabled == false}">
			                      ${textContainer.text['subjectSourcesTableTypeEnabledFalseValue']} 
			                    </c:if>
			                   </td>
			                  
			                   <td>
			                     <div class="btn-group">
			                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
			                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
			                             ${textContainer.text['subjectSourcesTableHeaderActions'] }
			                             <span class="caret"></span>
			                           </a>
			                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
			                             <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.subjectApiDiagnosticsSourceIdChanged&subjectApiSourceIdName=${source.id}');">${textContainer.text['subjectSourcesDiagnosticsActionOption'] }</a></li>
			                             
			                             <c:if test="${source.editable}">
				                             <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.editSubjectSource&subjectSourceId=${source.id}');">${textContainer.text['subjectSourcesEditActionOption'] }</a></li>
			                             </c:if>
			                             
			                             
			                             <c:if test="${source.editable}">
			                             
				                             <c:if test="${guiGrouperExternalSystem.grouperExternalSystem.enabled == true}">
						                      <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.disableSource&subjectSourceId=${sourceId}');">${textContainer.text['subjectSourcesDisableActionOption'] }</a></li>
						                     </c:if>
						                     
						                     <c:if test="${guiGrouperExternalSystem.grouperExternalSystem.enabled == false}">
						                      <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.enableSource&subjectSourceId=${sourceId}');">${textContainer.text['subjectSourcesEnableActionOption'] }</a></li>
						                     </c:if>
			                             
			                             </c:if>
			                             
			                             <c:if test="${source.editable}">
			                             	<li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['subjectSourcesConfirmDeleteSource']}')) { return guiV2link('operation=UiV2SubjectSource.deleteSubjectSource&subjectSourceId=${source.id}');}">${textContainer.text['subjectSourcesDeleteActionOption'] }</a></li>
			                             </c:if>
			                           </ul>
			                         </div>
			                   </td>
			              </c:forEach>
			             
			             </tbody>
			         </table>
			        
			      </c:when>
			      <c:otherwise>
			        <div class="row-fluid">
			          <div class="span9"> <p><b>${textContainer.text['subjectSourcesNoSubjectSourcesFound'] }</b></p></div>
			        </div>
			      </c:otherwise>
			    </c:choose>
			    
			  </div>
