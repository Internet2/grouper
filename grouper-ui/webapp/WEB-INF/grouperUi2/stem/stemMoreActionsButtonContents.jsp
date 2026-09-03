<!-- ./webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start stem/stemMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">
                      <%-- on the privs tab, show the add member button --%>
                      <c:choose>
                        <c:when test="${grouperRequestContainer.stemContainer.showAddMember}">
                          <button type="button" id="show-add-block" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i aria-hidden="true" class="fa fa-plus"></i> ${textContainer.text['stemViewMoreActionsAddMembers'] }</button>
                        </c:when>
                        <c:otherwise>
                          <a href="?operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['stemViewEditStemButton'] }</a>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                    <div class="btn-group btn-block">
                    	<%-- a11y: the trigger uses role="button" (not the menu role). Marking it as a menu made WAVE report
                    	     "Broken ARIA menu: does not contain required menu items" -- the trigger holds only a label + caret;
                    	     the real menu is the ul#stem-more-options below, ajax-populated on first open. --%>
                    	<button type="button" data-toggle="dropdown" class="btn btn-medium btn-block dropdown-toggle" id="moreActionsButton"
                    		aria-haspopup="true" aria-label="${textContainer.text['ariaLabelGuiMoreStemActions']}" aria-expanded="false" 
                    		onclick="if ($('#stem-more-options').is(':visible') === true) { $(this).attr('aria-expanded','false') } else if ($('#firstStemMoreActionsMenuItem').length) { $(this).attr('aria-expanded',function(index, currentValue) { $('#stem-more-options li').first().focus();return true;})} else { ajax('../app/UiV2Stem.populateMoreActionsButton?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return true; } ">
                    		${textContainer.text['stemViewMoreActionsButton'] } <span class="caret"></span></button>
                      <ul class="dropdown-menu dropdown-menu-right" id="stem-more-options">

                      </ul>
                    </div>

