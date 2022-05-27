<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <script language="javascript">
              $('#membershipTimelineForm input[type="checkbox"]').on('change', function(e) {
                if($(this).prop('checked')) {
                  $(this).val("true");
                 } else {
                  $(this).val("false");
                 }
              });
            </script>

            <div class="bread-header-container">
              <ul class="breadcrumb">
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
                <h1> <i class="fa fa-group"> </i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['membershipTraceSubHeader']}</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <c:if test="${grouperRequestContainer.membershipGuiContainer.traceMembershipsString != null}">
                  <p class="lead">${textContainer.text['membershipTracePageLead'] }</p>
                
                  <%-- 
                    <p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
                    <p style="margin-left:20px;"><i class="fa fa-arrow-circle-o-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
                    <p style="margin-left:40px;"><i class="fa fa-arrow-circle-o-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
                    <p style="margin-left:60px"><i class="fa fa-arrow-circle-o-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
                    <hr />
                  --%>
                  <%-- note, this is generated in Java in UiV2Membership.traceMembership --%>
                  ${grouperRequestContainer.membershipGuiContainer.traceMembershipsString }
                </c:if>
                <c:if test="${grouperRequestContainer.membershipGuiContainer.tracePITMembershipString != null}">
                  <p class="lead">${textContainer.text['pitMembershipTracePageLead'] }</p>

                  ${grouperRequestContainer.membershipGuiContainer.tracePITMembershipString }
                </c:if>
                <c:if test="${grouperRequestContainer.membershipGuiContainer.traceMembershipsString != null || grouperRequestContainer.membershipGuiContainer.tracePITMembershipString != null}">
                  <p class="lead">${textContainer.text['membershipTraceTimelinePageLead'] }</p>

                  <form class="form-inline form-small form-filter" action="#" id="membershipTimelineForm" onsubmit="return guiV2link('operation=UiV2Membership.traceMembership', {optionalFormElementNamesToSend: 'groupId,memberId,field,showTimeline,showUserAudit,showPITAudit,backTo', dontScrollTop: true});">
                    <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                    <input type="hidden" name="memberId" value="${grouperRequestContainer.subjectContainer.guiSubject.memberId}" />
                    <input type="hidden" name="field" value="members" />
                    <input type="hidden" name="showTimeline" value="true" />

                    <c:choose>
                      <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                        <input type="hidden" name="backTo" value="subject" />
                      </c:when>
                      <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromMembership}">
                        <input type="hidden" name="backTo" value="membership" />
                      </c:when>
                      <c:otherwise>
                        <input type="hidden" name="backTo" value="group" />
                      </c:otherwise>
                    </c:choose>

                    <input type="checkbox" name="showUserAudit" id="membership-timeline-show-user-audit" ${grouperRequestContainer.membershipGuiContainer.traceMembershipTimelineShowUserAudit? 'value="true" checked="checked"' : 'value="false"'} />
                    <label for="membership-timeline-show-user-audit">${textContainer.text['membershipTraceTimelineShowUserAudit']}</label>
                    <br />
                    <input type="checkbox" name="showPITAudit" id="membership-timeline-show-pit-audit" ${grouperRequestContainer.membershipGuiContainer.traceMembershipTimelineShowPITAudit? 'value="true" checked="checked"' : 'value="false"'} />
                    <label for="membership-timeline-show-pit-audit">${textContainer.text['membershipTraceTimelineShowPITAudit']}</label>

                    <br /><br />
                    <input type="submit" class="btn" value="${textContainer.text['membershipTraceTimelineButton'] }" />
                    <br /><br />

                    <c:if test="${grouperRequestContainer.membershipGuiContainer.traceMembershipTimelineString != null}">
                      <p>${textContainer.text['membershipTraceTimelineDescription'] }</p>

                      ${grouperRequestContainer.membershipGuiContainer.traceMembershipTimelineString}
                    </c:if>
                  </form>
                </c:if>

                <c:choose>
                  <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromSubject}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToSubjectButton']}</a>
                  </c:when>
                  <c:when test="${grouperRequestContainer.membershipGuiContainer.traceMembershipFromMembership}">
                    <a href="#" onclick="return guiV2link('operation=UiV2Membership.editMembership&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${grouperRequestContainer.subjectContainer.guiSubject.memberId}&field=members');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToMembershipButton']}</a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                       class="pull-right btn btn-primary btn-cancel">${textContainer.text['membershipTraceBackToGroupButton']}</a>
                  </c:otherwise>
                </c:choose>
                
              </div>
            </div>
