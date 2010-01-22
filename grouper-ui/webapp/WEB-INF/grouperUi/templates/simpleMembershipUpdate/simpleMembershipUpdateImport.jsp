<%@ include file="../common/commonTaglib.jsp"%>

<div class="simpleMembershipUpload">

  <div class="section">
  
    <grouper:subtitle key="simpleMembershipUpdate.importSubtitle" />
  
    <div class="sectionBody">
      <form action="whatever" id="simpleMembershipUploadForm" 
          name="simpleMembershipUploadForm" method="post" enctype="multipart/form-data">
        <div class="combohint"><grouper:message key="simpleMembershipUpdate.importLabel" /></div><br />
        
        <table class="formTable" cellspacing="2" cellspacing="0">
          <tr class="formTableRow">
            <td class="formTableLeft"><grouper:message key="simpleMembershipUpdate.importAvailableSourceIds" /></td>
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
            <td class="formTableLeft"><grouper:message key="simpleMembershipUpdate.importReplaceExistingMembers" /></td>
            <td class="formTableRight"><input type="checkbox" name="importReplaceMembers" value="true" /></td>
          </tr> 
          <tr class="formTableRow">
            <td class="formTableLeft"><grouper:message key="simpleMembershipUpdate.importCommaSeparatedValuesFile" /></td>
            <td class="formTableRight"><input type="file" name="importCsvFile" /></td>
          </tr> 
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message key="simpleMembershipUpdate.importCancelButton" /></button> 
              &nbsp;
              <button class="blueButton" 
                onclick="return guiSubmitFileForm(event, '#simpleMembershipUploadForm', '../app/SimpleMembershipUpdateImportExport.importCsv')"
                style="margin-top: 2px" ><grouper:message key="simpleMembershipUpdate.importButton" /></button>
            </td>
          </tr>
        </table>

      </form>
    </div>
  </div>
</div>