<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:choose>
     <c:when test="${grouperRequestContainer.rulesContainer.guiRuleDefinitions != null && grouperRequestContainer.rulesContainer.guiRuleDefinitions.size() > 0}">
      
      <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['rulesTableHeaderActions']}</th>
              <th>${textContainer.text['rulesTableHeaderPattern']}</th>
               <th>${textContainer.text['rulesTableHeaderAssignedOn']}</th>
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
              
             <c:if test="${guiRuleDefinition.canViewRule}"> 
              <tr>
              
                <td>
                <c:if test="${guiRuleDefinition.canEditRule}">
                   <div class="btn-group">
                         <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                           aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                           ${textContainer.text['rulesTableActionsButton'] }
                           <span class="caret"></span>
                         </a>
                         <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                          
                          <c:if test="${ruleDefinition.attributeAssignType.ownerGroupId == grouperRequestContainer.groupContainer.guiGroup.group.id }">
                              <li><a href="#" onclick="return guiV2link('operation=UiV2Group.deleteRuleOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&ruleId=${ruleDefinition.attributeAssignType.id}');">${textContainer.text['rulesTableActionsDeleteRuleSettings'] }</a></li>
                          </c:if>
                          
                          <c:if test="${ruleDefinition.attributeAssignType.ownerGroupId != null}">
                           <li><a href="#" onclick="return guiV2link('operation=UiV2Group.editRuleOnGroup&groupId=${ruleDefinition.attributeAssignType.ownerGroupId}&ruleId=${ruleDefinition.attributeAssignType.id}');">${textContainer.text['rulesTableActionsEditRuleSettings'] }</a></li>
                          </c:if>
                          <c:if test="${ruleDefinition.attributeAssignType.ownerStemId != null}">
                           <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.editRuleOnStem&stemId=${ruleDefinition.attributeAssignType.ownerStemId}&ruleId=${ruleDefinition.attributeAssignType.id}');">${textContainer.text['rulesTableActionsEditRuleSettings'] }</a></li>
                          </c:if>
                          
                         </ul>
                       </div>
                  </c:if>
                 </td>
              
              
              <td style="white-space: nowrap;">
                <c:if test="${guiRuleDefinition.ruleDefinition.pattern != null}">
                  ${guiRuleDefinition.ruleDefinition.pattern.userFriendlyText}
                </c:if>
              </td>
              
              <td style="white-space: nowrap;">
                <c:choose>
                  <c:when test="${ruleDefinition.attributeAssignType.ownerGroupId == grouperRequestContainer.groupContainer.guiGroup.group.id }">
                  ${textContainer.text['provisioningConfigTableHeaderAssignedOnThisGroup']}
                  </c:when>
                  <c:otherwise>
                  ${guiRuleDefinition.owner}
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
                ${guiRuleDefinition.willRunDaemon}
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
                 </c:if>
                    
         </c:forEach>
          
          </tbody>
        </table>
     
    </c:when>
      <c:otherwise>
      <p>${textContainer.text['rulesNoneAllConfigured']}</p>
      </c:otherwise>
    </c:choose>