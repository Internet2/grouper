<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<div id="realm-select-container">
                <form id="realmSelectForm" class="form-horizontal">
                   <div class="control-group">
                     <label class="control-label">${textContainer.text['deprovisioningSelectRealmLabel'] }</label>
                     <div class="controls">
                       
                       <select id="realmFilter" class="span2" name="realm" onchange="">
                          <option value=""></option>
                          <c:forEach items="${grouperRequestContainer.deprovisioningContainer.realms}" var="realm">
                            <option value="${realm.label}">${realm.translatedLabel}</option>
                          </c:forEach>
                          
                       </select>
                       
                       <span class="help-block">${textContainer.text['deprovisioningSelectRealmDescription'] }</span>
                     
                     </div>
                   </div>
                   
                   <div class="form-actions"><a href="#" class="btn btn-primary" role="button"
                     onclick="ajax('../app/UiV2Deprovisioning.deprovisioningRealmSubmit', {formIds: 'realmSelectForm'}); return false;">
                     ${textContainer.text['deprovisioningSelectRealmSubmitButton'] }</a> 
                   </div>
                 </form>
               
               </div>