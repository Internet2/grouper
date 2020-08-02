<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start configure/configureFilesMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreConfigureFilesActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                      	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#configure-files-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#configure-files-more-options li').first().focus();return true;});">
                      		${textContainer.text['configurationFilesViewMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="configure-files-more-options">
                        <%-- main link --%>
                            <li><a href="#" 
                            onclick="return guiV2link('operation=UiV2Configure.configure', {optionalFormElementNamesToSend: 'configFile'}); return false;" 
                            >${textContainer.text['configurationFilesMenuIndex'] }</a></li>
                            <li>
                            <a href="#" 
                            onclick="return guiV2link('operation=UiV2Configure.configurationFileAddConfig', {optionalFormElementNamesToSend: 'configFile'}); return false;" 
                            >${textContainer.text['configurationFilesMenuAddConfig'] }</a>
                            </li>
                            
                            <c:choose>
	                            <c:when test="${not empty grouperRequestContainer.configurationContainer.configFileName}">
	                            	<li><a href="../app/UiV2Configure.configurationFileExport?configFile=${grouperRequestContainer.configurationContainer.configFileName}">${textContainer.text['configurationFilesMenuExport'] }</a></li>
	                            </c:when>
	                            <c:otherwise>
		                            <li><a href="#"
		                            onclick="return guiV2link('operation=UiV2Configure.configurationFileExport', {optionalFormElementNamesToSend: 'configFile'}); return false;" 
		                            >${textContainer.text['configurationFilesMenuExport'] }</a>
		                            </li>
	                            </c:otherwise>
                            </c:choose>
                            <li><a href="#" 
                            onclick="return guiV2link('operation=UiV2Configure.configurationFileImport', {optionalFormElementNamesToSend: 'configFile'}); return false;" 
                            >${textContainer.text['configurationFilesMenuImport'] }</a></li>

                      </ul>
                    </div>

                    <!-- end configure/configureFilesMoreActionsButtonContents.jsp  -->
