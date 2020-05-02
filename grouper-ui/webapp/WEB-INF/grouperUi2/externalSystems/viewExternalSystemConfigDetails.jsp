<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2ExternalSystem.viewExternalSystems');">${textContainer.text['miscellaneousGrouperExternalSystemsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGrouperExternalSystemsViewDetailsBreadcrumb'] }</li>
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
					
				<table class="table table-condensed table-striped">
                  <tbody>
               	    <tr>
					  <td style="vertical-align: top; white-space: nowrap;"><strong><label for="externalSystemConfigId">${textContainer.text['grouperExternalSystemConfigIdLabel']}</label></strong></td>
					  <td>
					    ${grouper:escapeHtml(grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.configId)}
					  </td>
					</tr>
             		
             		<c:forEach items="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem.grouperExternalSystem.subSections}" var="section">
             		  <tbody>
             		  
             		  <c:if test="${!grouper:isBlank(section.label)}">
             		   	<tr>
             		   		<th colspan="2">
             		   			<h4>${grouper:escapeHtml(section.title)}</h4>
             		   			<p>${grouper:escapeHtml(section.description)}</p>
             		   		</th>
             		   	</tr>
             		  </c:if>
             		  
             		  <c:forEach items="${section.attributes}" var="attribute">
             		    <tr>
             		   	  <td style="vertical-align: top; white-space: nowrap;">
             		   	    <strong><label>
             		   		  ${grouper:escapeHtml(attribute.value.label)}
             		   		</label></strong>
             		   	  </td>
             		   	  <td>
             		   	 	${grouper:escapeHtml(attribute.value.valueOrExpressionEvaluation)}
             		   	   </td>
             		    </tr>
             		  </c:forEach>
             		   	
             		  </tbody>
             		  
             		</c:forEach>
             		
                 </tbody>
               </table>
					
					<div class="span6">
                   
                     <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ExternalSystem.viewExternalSystems'); return false;"
                          >${textContainer.text['grouperExternalSystemConfigEditFormCancelButton'] }</a>
                   
                   </div>
			  </div>
			</div>