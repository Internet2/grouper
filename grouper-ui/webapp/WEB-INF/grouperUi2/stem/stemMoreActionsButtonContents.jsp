<!-- ./webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start stem/stemMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">
                      <%-- on the privs tab, show the add member button --%>
                      <c:choose>
                        <c:when test="${grouperRequestContainer.stemContainer.showAddMember}">
                          <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="fa fa-plus"></i> ${textContainer.text['stemViewMoreActionsAddMembers'] }</a>
                        </c:when>
                        <c:otherwise>
                          <a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['stemViewEditStemButton'] }</a>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                    <div class="btn-group btn-block">
                    	<a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle" id="moreActionsButton"
                    		aria-haspopup="true" aria-label="${textContainer.text['ariaLabelGuiMoreStemActions']}" aria-expanded="false" role="menu" 
                    		onclick="if ($('#stem-more-options').is(':visible') === true) { $(this).attr('aria-expanded','false') } else if ($('#firstStemMoreActionsMenuItem').length) { $(this).attr('aria-expanded',function(index, currentValue) { $('#stem-more-options li').first().focus();return true;})} else { ajax('../app/UiV2Stem.populateMoreActionsButton?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return true; } ">
                    		${textContainer.text['stemViewMoreActionsButton'] } <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right" id="stem-more-options">

                      </ul>
                    </div>

