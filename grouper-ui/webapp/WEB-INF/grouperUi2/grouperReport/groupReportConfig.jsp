<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <ul class="nav nav-tabs">
        <li><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupMembersTab'] }</a></li>
        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
        </c:if>
        <%@ include file="../group/groupMoreTab.jsp" %>
      </ul>
    </div>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['groupReportOnGroupDescription'] }</div>
      <div class="span3" id="grouperReportGroupMoreActionsButtonContentsDivId">
        <%@ include file="grouperReportGroupMoreActionsButtonContents.jsp"%>
      </div>
    </div>
    
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['grouperReportDescription'] }</p></div>
    </div>
    
    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.grouperReportContainer.guiReportConfigs) > 0}">
        
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportName']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportEnabled']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportLastRunTime']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportStatus']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportNumberOfRows']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportCronSchedule']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportActions']}</th>
            </tr>
            </thead>
            <tbody>
              <c:set var="i" value="0" />
              <c:forEach items="${grouperRequestContainer.grouperReportContainer.guiReportConfigs}" var="guiReportConfig">
              
                <tr>
                   <td style="white-space: nowrap;">
                    <a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">
                    ${guiReportConfig.reportConfigBean.reportConfigName}</a>
                   </td>
                   <td style="white-space: nowrap;">
                     <c:choose>
                      <c:when test="${guiReportConfig.reportConfigBean.reportConfigEnabled}">
                        ${textContainer.text['grouperReportConfigTableReportEnabledTrueValue']}
                      </c:when>
                      <c:otherwise>
                        ${textContainer.text['grouperReportConfigTableReportEnabledFalseValue']}
                      </c:otherwise>
                     </c:choose>
                   </td>
                   <td style="white-space: nowrap;">
                     <c:choose>
                      <c:when test="${not empty guiReportConfig.lastRunTime}">                    
                        ${guiReportConfig.lastRunTime}
                      </c:when>
                      <c:otherwise>
                        ${textContainer.text['grouperReportInstanceNeverRun']}
                      </c:otherwise>
                     </c:choose>
                   </td>
                   <td style="white-space: nowrap;">
                    <c:choose>
                      <c:when test="${not empty guiReportConfig.mostRecentReportInstance}">                    
                        ${guiReportConfig.mostRecentReportInstance.reportInstanceStatus}
                      </c:when>
                      <c:otherwise>
                        ${textContainer.text['grouperReportInstanceNeverRun']}
                      </c:otherwise>
                     </c:choose>
                   </td>
                   <td style="white-space: nowrap;">
                     <c:choose>
                      <c:when test="${not empty guiReportConfig.mostRecentReportInstance}">                    
                        ${guiReportConfig.mostRecentReportInstance.reportInstanceRows}
                      </c:when>
                      <c:otherwise>
                        ${textContainer.text['grouperReportInstanceNeverRun']}
                      </c:otherwise>
                     </c:choose>
                   </td>
                   <td style="white-space: nowrap;">
                   
                    ${guiReportConfig.reportConfigBean.reportConfigQuartzCron}
                    <br/>
                    ${guiReportConfig.userFriendlyCron}
                   </td>
                   <td>
                     <div class="btn-group">
                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                             aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                             ${textContainer.text['stemViewActionsButton'] }
                             <span class="caret"></span>
                           </a>
                           <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                             
                             <c:if test="${not empty guiReportConfig.mostRecentReportInstance }">
                               <li><a href="../app/UiV2GrouperReport.downloadReportForGroup?attributeAssignId=${guiReportConfig.mostRecentReportInstance.attributeAssignId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}">${textContainer.text['grouperReportConfigTableReportActionsDownloadMostRecent'] }</a></li>
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewReportInstanceDetailsForGroup&attributeAssignId=${guiReportConfig.mostRecentReportInstance.attributeAssignId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperReportConfigTableReportActionsViewMostRecent'] }</a></li>
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperReportConfigTableReportActionsReportInstances'] }</a></li>
                               
                               <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports}">                               
                                 <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=grouper_report_${grouperRequestContainer.groupContainer.guiGroup.group.id}_${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}');">${textContainer.text['grouperReportConfigTableReportActionsReportLogs'] }</a></li>
                               </c:if>
                               
                             </c:if>
                             
                             <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports }">
                               <c:choose>
                                 <c:when test="${guiReportConfig.reportConfigBean.reportConfigEnabled}">
                                   <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.changeReportConfigStatusForGroup&newStatus=disable&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperReportConfigTableReportActionsDisbleReportConfig'] }</a></li>
                                 </c:when>
                                 <c:otherwise>
                                   <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.changeReportConfigStatusForGroup&newStatus=enable&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperReportConfigTableReportActionsEnableReportConfig'] }</a></li>
                                 </c:otherwise>
                               </c:choose>
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.deleteReportConfigForGroup&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');">${textContainer.text['grouperReportConfigTableReportActionsDeleteReportConfig'] }</a></li>                             
                             </c:if>
                           </ul>
                         </div>
                   </td>
              </c:forEach>
             
             </tbody>
         </table>
        
      </c:when>
      <c:otherwise>
        <div class="row-fluid">
          <div class="span9"> <p><b>${textContainer.text['grouperReportNoEntitiesFound'] }</b></p></div>
        </div>
      </c:otherwise>
    </c:choose>
    
  </div>
</div>
        