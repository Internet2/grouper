<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

<%@ include file="../stem/stemHeader.jsp" %>

<div class="row-fluid">
  <div class="span12 tab-interface">
    <ul class="nav nav-tabs">
      <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
      <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
      </c:if>
      <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
        <%@ include file="../stem/stemMoreTab.jsp" %>
      </c:if>
    </ul>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['groupReportOnFolderDescription'] }</div>
      <div class="span3" id="grouperReportFolderMoreActionsButtonContentsDivId">
        <%@ include file="grouperReportFolderMoreActionsButtonContents.jsp"%>
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
                    <a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">
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
                             
                             <c:if test="${not empty guiReportConfig.mostRecentReportInstance}">
                               <li><a href="../app/UiV2GrouperReport.downloadReportForFolder?attributeAssignId=${guiReportConfig.mostRecentReportInstance.attributeAssignId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}">${textContainer.text['grouperReportConfigTableReportActionsDownloadMostRecent'] }</a></li>
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewReportInstanceDetailsForFolder&attributeAssignId=${guiReportConfig.mostRecentReportInstance.attributeAssignId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsViewMostRecent'] }</a></li>
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsReportInstances'] }</a></li>
                               
                               <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports}">                               
                                 <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=grouper_report_${grouperRequestContainer.stemContainer.guiStem.stem.id}_${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}');">${textContainer.text['grouperReportConfigTableReportActionsReportLogs'] }</a></li>
                               </c:if>
                               
                             </c:if>
                             
                             <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports}">
                               <c:choose>
	                               <c:when test="${guiReportConfig.reportConfigBean.reportConfigEnabled}">
	                                 <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.changeReportConfigStatusForFolder&newStatus=disable&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsDisbleReportConfig'] }</a></li>
	                               </c:when>
	                               <c:otherwise>
	                                 <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.changeReportConfigStatusForFolder&newStatus=enable&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsEnableReportConfig'] }</a></li>
	                               </c:otherwise>
	                             </c:choose>
                               <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.deleteReportConfigForFolder&attributeAssignmentMarkerId=${guiReportConfig.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsDeleteReportConfig'] }</a></li>                             
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
                