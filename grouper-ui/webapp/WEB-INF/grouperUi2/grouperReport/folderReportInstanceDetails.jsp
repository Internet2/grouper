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
    
    <c:choose>    
      <c:when test="${not empty grouperRequestContainer.grouperReportContainer.guiReportInstance}">
        <div class="row-fluid">
		      <div class="lead span9">${textContainer.text['groupReportOnFolderDescription'] }</div>
		      <div class="span3" id="grouperReportFolderMoreActionsButtonContentsDivId">
		        <%@ include file="grouperReportFolderMoreActionsButtonContents.jsp"%>
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
		        
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">
			        <tr>
	              <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsDownload']}</label></strong></td>
	              <td>
	                <a href="../app/UiV2GrouperReport.downloadReportForFolder?attributeAssignId=${guiReportInstance.reportInstance.attributeAssignId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}">${textContainer.text['grouperReportConfigInstanceDetailsDownloadReport'] }</a>
	              </td>
	            </tr>
		        </c:if>
		        
		        
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">
			        <tr>
			          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsFileSize']}</label></strong></td>
			          <td>${guiReportInstance.unencryptedReportFileSize}</td>
			        </tr>
		        </c:if>
		        
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">
			        <tr>
			          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsFilename']}</label></strong></td>
			          <td>${guiReportInstance.reportInstance.reportInstanceFileName}</td>
			        </tr>
			      </c:if>
		                
            <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">		                
			        <tr>
			          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsNumberOfRows']}</label></strong></td>
			          <td>${guiReportInstance.reportInstance.reportInstanceRows}</td>
			        </tr>
			      </c:if>
		        
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">
			        <tr>
			          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsReportDownloadCount']}</label></strong></td>
			          <td>${guiReportInstance.reportInstance.reportInstanceDownloadCount}</td>
			        </tr>
			      </c:if>
		
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">
			        <tr>
			          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsSuccessSubjects']}</label></strong></td>
			          <td>${guiReportInstance.reportInstance.reportInstanceEmailToSubjects}</td>
			        </tr>
		        </c:if>
		        
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS'}">
			        <tr>
			          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['grouperReportConfigInstanceDetailsErrorSubjects']}</label></strong></td>
			          <td>${guiReportInstance.reportInstance.reportInstanceEmailToSubjectsError}</td>
			        </tr>
			      </c:if>
		        
		        <c:if test="${guiReportInstance.reportInstance.reportInstanceStatus == 'SUCCESS' and grouperRequestContainer.grouperReportContainer.showPartialEncryptionKey}">
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