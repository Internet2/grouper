<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:choose>
     <c:when test="${grouperRequestContainer.rulesContainer.guiRuleDefinitions != null && grouperRequestContainer.rulesContainer.guiRuleDefinitions.size() > 0}">
      
      <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['rulesTableHeaderActions']}</th>
              <th>${textContainer.text['rulesTableHeaderPattern']}</th>
              <th>${textContainer.text['rulesTableHeaderAssignedOnThisFolder']}</th>
              <th>${textContainer.text['rulesTableHeaderCheck']}</th>
              <th>${textContainer.text['rulesTableHeaderCondition']}</th>
              <th>${textContainer.text['rulesTableHeaderResult']}</th>
              <th>${textContainer.text['rulesTableHeaderHasNightlyDaemon']}</th>
              <th>${textContainer.text['rulesTableHeaderFiresImmediately']}</th>
              <th>${textContainer.text['rulesTableHeaderValid']}</th>
            </tr>
          </thead>
          <tbody>
          
          <c:forEach items="${grouperRequestContainer.rulesContainer.guiRuleDefinitions}" var="guiRuleDefinition" >
          
           <c:set target="${grouperRequestContainer.rulesContainer}"
                                          property="currentGuiRuleDefinition"
                                          value="${guiRuleDefinition}" />
           <c:set var="ruleDefinition" value="${guiRuleDefinition.ruleDefinition}" />
              
              <tr>
              
                <td>
                   <div class="btn-group">
                         <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                           aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                           ${textContainer.text['rulesTableActionsButton'] }
                           <span class="caret"></span>
                         </a>
                         <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                         
                          
                          <c:if test="${grouperRequestContainer.rulesContainer.canReadRules}">          
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewtRuleSettingsOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&ruleId=${ruleDefinition.attributeAssignType.id}');">${textContainer.text['rulesTableActionsViewRuleSettings'] }</a></li>
                          </c:if>
                          

                          <c:if test="${grouperRequestContainer.rulesContainer.canUpdateRules}">          
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.editRuleSettingsOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&ruleId=${ruleDefinition.attributeAssignType.id}');">${textContainer.text['rulesTableActionsEditRuleSettings'] }</a></li>
                          </c:if>
                          
                           <c:if test="${grouperRequestContainer.rulesContainer.canUpdateRules}">          
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.deleteRuleSettingsOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&ruleId=${ruleDefinition.attributeAssignType.id}');">${textContainer.text['rulesTableActionsDeleteRuleSettings'] }</a></li>
                          </c:if>

                         </ul>
                       </div>
                 </td>
              
              
              <td style="white-space: nowrap;">
                ${guiRuleDefinition.ruleDefinition.pattern}
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${ruleDefinition.attributeAssignType.ownerStemId == grouperRequestContainer.stemContainer.guiStem.stem.id }">
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                  </c:when>
                  <c:otherwise>
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                  </c:otherwise>
                </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                ${guiRuleDefinition.check}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiRuleDefinition.condition}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiRuleDefinition.result}
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${guiRuleDefinition.canRunDaemon}">
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                  </c:when>
                  <c:otherwise>
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                  </c:otherwise>
                </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${guiRuleDefinition.firesImmeditately }">
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                  </c:when>
                  <c:otherwise>
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                  </c:otherwise>
                </c:choose>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${guiRuleDefinition.ruleDefinition.validInAttributes }">
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
                  </c:when>
                  <c:otherwise>
                  ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
                  </c:otherwise>
                </c:choose>
              </td>
              
                 </tr>
                    
         </c:forEach>
          
          </tbody>
        </table>
     
    </c:when>
      <c:otherwise>
      <p>${textContainer.text['rulesNoneAllConfigured']}</p>
      </c:otherwise>
    </c:choose>