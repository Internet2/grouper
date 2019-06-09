<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12 tab-interface">
    <ul class="nav nav-tabs">
      <li><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupMembersTab'] }</a></li>
      <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
      </c:if>
      <%@ include file="../group/groupMoreTab.jsp" %>
    </ul>
    
    <c:choose>    
      <c:when test="${not empty grouperRequestContainer.grouperReportContainer.guiReportInstance}">
        <div class="row-fluid">
		      <div class="lead span9">${textContainer.text['groupReportOnGroupDescription'] }</div>
		      <div class="span3" id="grouperReportGroupMoreActionsButtonContentsDivId">
		        <%@ include file="grouperReportGroupMoreActionsButtonContents.jsp"%>
		      </div>
		    </div>
		    <div class="row-fluid">
		      <div class="span9"> <p>${textContainer.text['grouperReportDescription'] }</p></div>
		    </div>
		    <c:set var="guiReportInstance" value="${grouperRequestContainer.grouperReportContainer.guiReportInstance}" />
		    <table class="table table-condensed table-striped">
		      <tbody>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsReportRunTime']}</label></strong></td>
		          <td>${guiReportInstance.runTime}</td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsConfigName']}</label></strong></td>
		          <td>${guiReportInstance.reportConfigBean.reportConfigName}</td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsConfigDescription']}</label></strong></td>
		          <td>${guiReportInstance.reportConfigBean.reportConfigDescription}</td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsDownload']}</label></strong></td>
		          <td>
  		          <a href="../app/UiV2GrouperReport.downloadReportForGroup?attributeAssignId=${guiReportInstance.reportInstance.attributeAssignId}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}">${textContainer.text['grouperReportConfigInstanceDetailsDownloadReport'] }</a>
		          </td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsFileSize']}</label></strong></td>
		          <td>${guiReportInstance.unencryptedReportFileSize}</td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsFilename']}</label></strong></td>
		          <td>${guiReportInstance.reportInstance.reportInstanceFileName}</td>
		        </tr>
		                
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsNumberOfRows']}</label></strong></td>
		          <td>${guiReportInstance.reportInstance.reportInstanceRows}</td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsReportDownloadCount']}</label></strong></td>
		          <td>${guiReportInstance.reportInstance.reportInstanceDownloadCount}</td>
		        </tr>
		
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsSuccessSubjects']}</label></strong></td>
		          <td>${guiReportInstance.reportInstance.reportInstanceEmailToSubjects}</td>
		        </tr>
		        
		        <tr>
		          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsErrorSubjects']}</label></strong></td>
		          <td>${guiReportInstance.reportInstance.reportInstanceEmailToSubjectsError}</td>
		        </tr>
		        
		        <c:if test="${grouperRequestContainer.grouperReportContainer.showPartialEncryptionKey}">
		          <tr>
		            <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsEncryptionKey']}</label></strong></td>
		            <td>${guiReportInstance.reportInstanceEncryptionKey}</td>
		          </tr>
		        </c:if>
		
		      </tbody>
		    </table>
      </c:when>
      <c:otherwise>
      <div class="row-fluid">
          <div class="span9"> <p><b>${textContainer.text['grouperReportNoInstancesFound'] }</b></p></div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>