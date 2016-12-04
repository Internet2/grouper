<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <li class="dropdown">
                	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" href="#" class="dropdown-toggle"
                		aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#subject-more-tab-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#subject-more-tab-options li').first().focus();return true;});">
                		${textContainer.text['subjectMoreTab'] } 
                		<b class="caret"></b>
                	</a>
                  <ul class="dropdown-menu" id="subject-more-tab-options">
                    <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">   
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsPrivilegesInheritedFromFolders&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});">${textContainer.text['thisSubjectsPrivilegesFromFolders'] }</a></li>
                    </c:if>
                  </ul>
                </li>
