<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupDeletePageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupDeleteTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <p>${textContainer.text['groupDeleteText'] }</p>
                
                <c:if test="${grouperRequestContainer.groupContainer.guiGroup.rulesDeleteCount > 0}">
                  <p>${textContainer.text['groupDeleteRulesDeleteCount'] }</p>
                  
                   <div class="row-fluid">
        
                    <table class="table table-hover table-bordered table-striped table-condensed data-table">
                      <thead>        
                        <tr>
                          <th>${textContainer.text['rulesTableHeaderPattern']}</th>
                          <th>${textContainer.text['rulesTableHeaderAssignedOn']}</th>
                          <th>${textContainer.text['rulesTableHeaderCheck']}</th>
                          <th>${textContainer.text['rulesTableHeaderCondition']}</th>
                          <th>${textContainer.text['rulesTableHeaderResult']}</th>
                        </tr>
                     </thead>
                    <tbody>
                      <c:set var="i" value="0" />
                       <c:forEach items="${grouperRequestContainer.groupContainer.guiGroup.rulesToBeDeleted}" var="guiRuleDefinition" >
            
                       <c:set target="${grouperRequestContainer.rulesContainer}"
                                                      property="currentGuiRuleDefinition"
                                                      value="${guiRuleDefinition}" />
                       <c:set var="ruleDefinition" value="${guiRuleDefinition.ruleDefinition}" />
                
                      <tr>
                          <td style="white-space: nowrap;">
                           <c:if test="${guiRuleDefinition.ruleDefinition.pattern != null}">
                             ${guiRuleDefinition.ruleDefinition.pattern.userFriendlyText}
                           </c:if>
                          </td>
                          
                          <td style="white-space: nowrap;">
                              ${guiRuleDefinition.owner}
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
                          
                       </tr>
                            
                      </c:forEach>
                     
                     </tbody>
                 </table>
          
              </div>
                  
                 </c:if>
                
                <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupDeleteSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['groupDeleteDeleteButton'] }</a> 
                <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['groupDeleteCancelButton'] }</a></div>
              </div>
            </div>
