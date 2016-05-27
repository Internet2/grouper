<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle">${textContainer.text['subjectMoreTab'] } <b class="caret"></b></a>
                  <ul class="dropdown-menu">
                    <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">   
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsPrivilegesInheritedFromFolders&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});">${textContainer.text['thisSubjectsPrivilegesFromFolders'] }</a></li>
                    </c:if>
                  </ul>
                </li>
