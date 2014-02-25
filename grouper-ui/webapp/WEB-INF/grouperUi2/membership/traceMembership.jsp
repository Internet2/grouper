<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />


            <div class="bread-header-container">
              <ul class="breadcrumb">
                <%-- <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="#">Root </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-folder.html">Applications </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-folder.html">Wiki </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-group.html">Editors</a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Trace membership</li> --%>
                <c:choose>
                  <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                    ${grouperRequestContainer.subjectContainer.guiSubject.breadcrumbBullets}
                  </c:when>
                  <c:otherwise>
                    ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbBullets}
                  </c:otherwise>
                </c:choose>
                <li class="active">${textContainer.text['membershipTraceBreadcrumb']}</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1> <i class="icon-group icon-header"> </i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['membershipTraceSubHeader']}</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <p class="lead">${textContainer.text['membershipTracePageLead'] }</p>
                
                <%-- 
                  <p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
                  <p style="margin-left:20px;"><i class="icon-circle-arrow-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
                  <p style="margin-left:40px;"><i class="icon-circle-arrow-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
                  <p style="margin-left:60px"><i class="icon-circle-arrow-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
                  <hr />
                --%>
                <%-- note, this is generated in Java in UiV2Membership.traceMembership --%>
                ${grouperRequestContainer.membershipGuiContainer.traceMembershipsString }

                <c:choose>
                  <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToSubjectButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToGroupButton']}</a>
                  </c:otherwise>
                </c:choose>
                
              </div>
            </div>
