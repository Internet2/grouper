<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreProvisionerConfigsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#provisioner-configs-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#provisioner-configs-more-options li').first().focus();return true;});">
                          ${textContainer.text['provisionerConfigsMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="provisioner-configs-more-options">
                      	<li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                              >${textContainer.text['provisionerConfigMoreActionsViewButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.addProvisionerConfiguration'); return false;"
                              >${textContainer.text['provisionerConfigMoreActionsAddButton'] }</a></li>
                      </ul>

                    </div>