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

			<%-- References section: everywhere in Grouper this external system is used --%>
			<c:if test="${!empty grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystemUsages}">
			  <div class="row-fluid">
			    <div class="span12">
			      <h4>${textContainer.text['grouperExternalSystemReferencesHeader'] }</h4>
			      <p>${textContainer.text['grouperExternalSystemReferencesDescription'] }</p>
			      <c:if test="${grouperRequestContainer.externalSystemContainer.externalSystemUsagesTruncated}">
			        <div class="alert alert-info">${textContainer.text['grouperExternalSystemReferencesTruncatedNote'] }</div>
			      </c:if>
			      <table class="table table-condensed table-striped">
			        <thead>
			          <tr>
			            <th>${textContainer.text['grouperExternalSystemReferencesTypeHeader'] }</th>
			            <th>${textContainer.text['grouperExternalSystemReferencesReferenceHeader'] }</th>
			            <th>${textContainer.text['grouperExternalSystemReferencesDescriptionHeader'] }</th>
			          </tr>
			        </thead>
			        <tbody>
			          <c:forEach items="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystemUsages}" var="usage">
			            <tr>
			              <td>${grouper:escapeHtml(usage.usageType)}</td>
			              <td>
			                <c:choose>
			                  <c:when test="${usage.hasLink}">
			                    <a href="?${usage.linkOperation}" onclick="return handleGuiV2LinkClick(event, '${usage.linkOperation}');">${grouper:escapeHtml(usage.name)}</a>
			                  </c:when>
			                  <c:otherwise>
			                    ${grouper:escapeHtml(usage.name)}
			                  </c:otherwise>
			                </c:choose>
			              </td>
			              <td>${grouper:escapeHtml(usage.description)}</td>
			            </tr>
			          </c:forEach>
			        </tbody>
			      </table>
			    </div>
			  </div>
			</c:if>

			<div class="row-fluid">
			  <div class="span12">

				<h4>${textContainer.text['grouperExternalSystemConfigurationHeader'] }</h4>
				<table class="table table-condensed table-striped">
                  <tbody>
               	    <tr>
					  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperExternalSystemConfigIdLabel']}</label></strong></td>
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