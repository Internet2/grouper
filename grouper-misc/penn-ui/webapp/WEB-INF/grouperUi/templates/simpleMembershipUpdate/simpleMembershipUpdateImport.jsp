<%@ include file="../common/commonTaglib.jsp"%>

<div class="simpleMembershipUpload">

  <div class="section">
  
    <grouperGui:subtitle key="simpleMembershipUpdate.importSubtitle" />
  
    <div class="sectionBody">
      <form action="whatever" id="simpleMembershipUploadForm" 
          name="simpleMembershipUploadForm" method="post" enctype="multipart/form-data">
        <div class="combohint"><grouperGui:message key="simpleMembershipUpdate.importLabel" /></div><br />
        
        <table class="formTable" cellspacing="2" cellspacing="0">
          <tr class="formTableRow">
            <td class="formTableLeft"><grouperGui:message key="simpleMembershipUpdate.importAvailableSourceIds" /></td>
            <td class="formTableRight">${fn:escapeXml(simpleMembershipUpdateContainer.sourceIds) }</td>
          </tr> 
          <tr class="formTableRow">
            <td class="formTableLeft"><grouperGui:message key="simpleMembershipUpdate.importReplaceExistingMembers" /></td>
            <td class="formTableRight"><input type="checkbox" name="importReplaceMembers" value="true" /></td>
          </tr> 
          <tr class="formTableRow">
            <td class="formTableLeft"><grouperGui:message key="simpleMembershipUpdate.importCommaSeparatedValuesFile" /></td>
            <td class="formTableRight"><input type="file" name="importCsvFile" /></td>
          </tr> 
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouperGui:message key="simpleMembershipUpdate.importCancelButton" /></button> 
              &nbsp;
              <button class="blueButton" 
                onclick="return guiSubmitFileForm(event, '#simpleMembershipUploadForm', '../app/SimpleMembershipUpdate.importCsv')"
                style="margin-top: 2px" ><grouperGui:message key="simpleMembershipUpdate.importButton" /></button>
            </td>
          </tr>
        </table>

      </form>
    </div>
  </div>
</div>