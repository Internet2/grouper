<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleMembershipUpdateImport.jsp -->

<div class="simpleMembershipUpload">

  <div class="section">
  
    <grouper:subtitle infodotValue="${simpleMembershipUpdateContainer.text.importSubtitleInfodot}" label="${simpleMembershipUpdateContainer.text.importSubtitle}" />
  
    <div class="sectionBody">
      <form id="simpleMembershipUploadForm" 
          name="simpleMembershipUploadForm" method="post" enctype="multipart/form-data">
        <div class="combohint"><grouper:message value="${simpleMembershipUpdateContainer.text.importLabel}" /></div><br />
        
        <%-- make sure too many browsers arent open at once --%>
        <c:choose>
          <c:when test="${! empty simpleMembershipUpdateContainer.guiGroup.group.id}">
            <input name="groupId" type="hidden" value="${simpleMembershipUpdateContainer.guiGroup.group.id}" />
          </c:when>
          <c:otherwise>
            <c:if test="${! empty simpleMembershipUpdateContainer.guiGroup.group.name}">
              <input name="groupName" type="hidden" value="${simpleMembershipUpdateContainer.guiGroup.group.name}" />
            </c:if>        
          </c:otherwise>    
        </c:choose>

        <input name="membershipLiteName" type="hidden" value="${simpleMembershipUpdateContainer.membershipLiteName}" />
        
        <table class="formTable" cellspacing="2" cellspacing="0">
          <tr class="formTableRow">
            <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.importAvailableSourceIds}" /></td>
            <td class="formTableRight"><%-- ${fn:escapeXml(simpleMembershipUpdateContainer.sourceIds) } --%>
              <table class="sourceTable">
                <tr>
                  <th>sourceId</th><th>Source name</th>
                </tr>
                <c:forEach items="${contextContainer.sources}" var="source">
                  <tr>
                    <td><c:out value="${source.id}" escapeXml="true"/></td>
                    <td><c:out value="${source.name }" escapeXml="true"/></td>
                  </tr>
                </c:forEach>
              </table>
            </td>
          </tr> 
          <tr class="formTableRow">
            <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.importReplaceExistingMembers}" /></td>
            <td class="formTableRight"><input type="checkbox" name="importReplaceMembers" value="true" /></td>
          </tr> 
          <tr class="formTableRow shows_membershipLiteImportFile" style="${grouper:hideShowStyle('membershipLiteImportFile', true)}">
            <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.importCommaSeparatedValuesFile}" /></td>
            <td class="formTableRight"><input type="file" name="importCsvFile" /></td>
          </tr> 
          <tr class="formTableRow hides_membershipLiteImportFile" style="${grouper:hideShowStyle('membershipLiteImportFile', false)}">
            <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.importDirectInput}" /></td>
            <td class="formTableRight"><textarea rows="20" cols="40" name="importCsvTextarea"></textarea> </td>
          </tr>
          <tr>
            <td colspan="2" align="right"  class="buttonRow">
              <a href="#" id="importFileOrDirectInputButton" class="buttons_membershipLiteImportFile"
                onclick="return guiHideShow(event, 'membershipLiteImportFile');">${grouper:hideShowButtonText('membershipLiteImportFile')}</a>
              &nbsp;
              <button class="simplemodal-close blueButton"><grouper:message value="${simpleMembershipUpdateContainer.text.importCancelButton}" /></button>
              &nbsp;
              <button class="blueButton" 
                onclick="return guiSubmitFileForm(event, '#simpleMembershipUploadForm', '../app/SimpleMembershipUpdateImportExport.importCsv')"
                style="margin-top: 2px" ><grouper:message value="${simpleMembershipUpdateContainer.text.importButton}" /></button>
            </td>
          </tr>
        </table>

      </form>
    </div>
  </div>
</div>
<!-- End: simpleMembershipUpdateImport.jsp -->
