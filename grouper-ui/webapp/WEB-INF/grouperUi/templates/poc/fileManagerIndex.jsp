<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignInit.jsp: main page -->

<grouper:title label="Manage files"  
  infodotValue="Use this screen to view files, or create new files or folders" />

<div class="section">

  <grouper:subtitle label="Folders and files" />

  <div class="sectionBody">
    <form id="simplePermissionFilterForm" name="simplePermissionFilterFormName" onsubmit="return false;" >

      <table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="ownerType">
              <grouper:message key="simplePermissionAssign.ownerType" />
            </label>
            <sup class="requiredIndicator">*</sup>
          </td>
          <td class="formTableRight">
            <select name="permissionAssignType" onchange="ajax('SimplePermissionUpdate.assignSelectOwnerType', {formIds: 'simplePermissionFilterForm'}); return false;" >
              <option></option>
              <option value="role">${grouper:message('simplePermissionAssign.ownerTypeRole', true, false)}</option>
              <option value="role_subject">${grouper:message('simplePermissionAssign.ownerTypeEntity', true, false)}</option>
            </select>
          </td>
        </tr>
      </table>
      <div id="permissionAssignFilter">
      </div>
    </form>
  </div>
</div>

<div id="permissionAssignAssignments">
</div>

<!-- End: simplePermissionUpdate/simplePermissionAssignInit.jsp: main page -->
