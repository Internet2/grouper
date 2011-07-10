<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: poc/fileManagerIndex.jsp: main page -->

<grouper:title label="Manage files"  
  infodotValue="Use this screen to view files, or create new files or folders" />

<div class="section">

  <grouper:subtitle label="Manage panel" />

  <div class="sectionBody">
    <form id="fileManagerFormId" name="fileManagerFormName" onsubmit="return false;" >

      <table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            Backdoor user:
          </td>
          <td class="formTableRight">
            <select name="backdoorSubjectId" onchange="ajax('PocFileManager.assignBackdoorUser', {formIds: 'fileManagerFormId'}); return false;" >
              <option></option>
              <c:forEach items="${pocFileManagerRequestContainer.allBackdoorSubjects}" var="subject">
                <option>${fn:escapeXml(subject.id)}</option>
              </c:forEach>
            </select>
          </td>
        </tr>
      </table>
      
      <div id="fileManagerEditPanel"></div>
    </form>
  </div>
</div>
<div id="fileManagerMessageId"></div>
<script type="text/javascript">
  //hide this after it shows for a while
  function hideFileManagerMessage() {
    $("#fileManagerMessageId").hide('slow');
  }
</script>

<div id="filesAndFolders">
</div>

<!-- End: poc/fileManagerIndex.jsp: main page -->
