<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="affiliation-select-container">
                <form id="affiliationSelectForm" class="form-horizontal" method="get" action="UiV2Deprovisioning.${deprovisioningSelectAffiliationTarget}" >
                   <div class="control-group">
                     <label class="control-label">${textContainer.text['deprovisioningSelectAffiliationLabel'] }</label>
                     <div class="controls">
                       
                       <select id="affiliationFilter" class="span2" name="affiliation" 
                              onchange="return guiV2link('operation=UiV2Deprovisioning.${deprovisioningSelectAffiliationTarget}', {optionalFormElementNamesToSend: 'affiliation'});"
                              >
                          <option value=""></option>
                          <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningAffiliationsUserCanDeprovision}" 
                               var="guiDeprovisioningAffiliation">
                            <option value="${guiDeprovisioningAffiliation.label}"
                            ${grouperRequestContainer.deprovisioningContainer.affiliation == guiDeprovisioningAffiliation.label ? 'selected="selected"' : '' }>${guiDeprovisioningAffiliation.translatedLabel}</option>
                          </c:forEach>
                          
                       </select>
                       
                       <span class="help-block">${textContainer.text['deprovisioningSelectAffiliationDescription'] }</span>
                     
                     </div>
                   </div>
                   
                 </form>
               
               </div>