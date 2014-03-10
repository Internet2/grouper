<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />


            <div class="bread-header-container">
              <ul class="breadcrumb">
                <%-- <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#">Root </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="view-folder.html">Applications </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="view-folder.html">Wiki </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="view-group.html">Editors</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Trace membership</li> --%>
                <c:choose>
                  <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                    ${grouperRequestContainer.subjectContainer.guiSubject.breadcrumbBullets}
                  </c:when>
                  <c:otherwise>
                    ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.breadcrumbBullets}
                  </c:otherwise>
                </c:choose>
                <li class="active">${textContainer.text['privilegesTraceBreadcrumb']}</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group fa-header"> </i> 
                  <c:choose>
                    <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                      ${grouperRequestContainer.subjectContainer.guiSubject.screenLabelShort2noLink}
                    </c:when>
                    <c:otherwise>
                      ${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.displayExtension)}
                    </c:otherwise>
                  </c:choose>
                <br /><small>
                  <c:choose>
                    <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                      ${textContainer.text['privilegesTraceAttributeDefSubjectSubHeader']}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['privilegesTraceAttributeDefSubHeader']}
                    </c:otherwise>
                  </c:choose>
                </small></h1>
                
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <p class="lead">${textContainer.text['privilegesTraceAttributeDefPageLead'] }</p>
                
                <%-- 
                  <p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
                  <p style="margin-left:20px;"><i class="fa fa-arrow-circle-o-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
                  <p style="margin-left:40px;"><i class="fa fa-arrow-circle-o-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
                  <p style="margin-left:60px"><i class="fa fa-arrow-circle-o-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
                  <hr />
                --%>
                <%-- note, this is generated in Java in UiV2Membership.traceMembership --%>
                ${grouperRequestContainer.membershipGuiContainer.traceMembershipsString }

                <c:choose>
                  <%-- we only have subject at this point, take out the || true when we can do attribute def screen --%>
                  <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject || true}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToSubjectButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefPrivileges&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToAttributeDefButton']}</a>
                  </c:otherwise>
                </c:choose>
                
              </div>
            </div>
