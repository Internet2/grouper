<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreRulesActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#rules-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#rules-more-options li').first().focus();return true;});">
                          ${textContainer.text['rulesMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="rules-more-options">

                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges && grouperRequestContainer.rulesContainer.canAddRule}" > 
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.addRuleOnStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['rulesMoreActionsAddRule'] }</a></li>
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.stemContainer.canCreateGroups}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStemRules&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['rulesMoreActionsViewRule'] }</a></li>
                        </c:if>

                      </ul>
                    </div>
