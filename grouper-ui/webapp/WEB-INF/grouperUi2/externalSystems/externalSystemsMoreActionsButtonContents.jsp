<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperExternalSystemActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouper-external-system-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-external-system-more-options li').first().focus();return true;});">
                          ${textContainer.text['grouperExternalSystemMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-external-system-more-options">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2ExternalSystem.addExternalSystem'); return false;"
                              >${textContainer.text['grouperExternalSystemMoreActionsAddButton'] }</a></li>
                      </ul>

                    </div>