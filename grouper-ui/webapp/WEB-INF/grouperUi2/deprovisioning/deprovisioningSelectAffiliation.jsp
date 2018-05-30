<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<div id="affiliation-select-container">
                <form id="affiliationSelectForm" class="form-horizontal">
                   <div class="control-group">
                     <label class="control-label">${textContainer.text['deprovisioningSelectAffiliationLabel'] }</label>
                     <div class="controls">
                       
                       <select id="affiliationFilter" class="span2" name="affiliation" onchange="">
                          <option value=""></option>
                          <c:forEach items="${grouperRequestContainer.deprovisioningContainer.guiDeprovisioningAffiliationsUserCanDeprovision}" var="guiDeprovisioningAffiliation">
                            <option value="${guiDeprovisioningAffiliation.label}">${guiDeprovisioningAffiliation.translatedLabel}</option>
                          </c:forEach>
                          
                       </select>
                       
                       <span class="help-block">${textContainer.text['deprovisioningSelectAffiliationDescription'] }</span>
                     
                     </div>
                   </div>
                   
                   <div class="form-actions"><a href="#" class="btn btn-primary" role="button"
                     onclick="ajax('../app/UiV2Deprovisioning.deprovisioningAffiliationSubmit', {formIds: 'affiliationSelectForm'}); return false;">
                     ${textContainer.text['deprovisioningSelectAffiliationSubmitButton'] }</a> 
                   </div>
                 </form>
               
               </div>