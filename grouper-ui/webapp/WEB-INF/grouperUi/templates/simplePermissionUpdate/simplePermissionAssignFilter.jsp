<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignFilter.jsp: main page -->

<table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="attributeDefinition">
        <grouper:message key="simplePermissionAssign.attributeDefinition" />
      </label>
    </td>
    <td class="formTableRight">
       <grouper:combobox 
         filterOperation="SimplePermissionUpdateFilter.filterPermissionAttributeDefs" 
         id="permissionAssignAttributeDef" 
         width="700"/>
    </td>
  </tr>
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="permissionResource">
        <grouper:message key="simplePermissionAssign.permissionResource" />
      </label>
    </td>
    <td class="formTableRight">
       <grouper:combobox 
         filterOperation="SimplePermissionUpdateFilter.filterPermissionResources" 
         id="permissionAssignAttributeName" additionalFormElementNames="permissionAssignAttributeDef"
         width="700"/>
    </td>
  </tr>
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="group">
        <grouper:message key="simplePermissionAssign.assignRole" />
      </label>
    </td>
    <td class="formTableRight">
       <grouper:combobox 
         filterOperation="SimplePermissionUpdateFilter.filterRoles" 
         id="permissionAssignRoleId" 
         width="700"/>
    </td>
  </tr>
  <c:choose>
    <c:when test="${permissionUpdateRequestContainer.permissionType.name == 'role_subject'}">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="member">
            <grouper:message key="simplePermissionAssign.assignMember" />
          </label>
        </td>
        <td class="formTableRight">
           <grouper:combobox 
             filterOperation="SimplePermissionUpdateFilter.filterSubjects" 
             id="permissionAssignMemberId" additionalFormElementNames="permissionAssignRoleId"
             width="700"/>
        </td>
      </tr>
    </c:when>
  </c:choose>
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="member">
        <grouper:message key="simplePermissionAssign.assignAction" />
      </label>
    </td>
    <td class="formTableRight">
       <grouper:combobox 
         filterOperation="SimplePermissionUpdateFilter.filterActions" 
         id="permissionAssignAction" 
         additionalFormElementNames="permissionAssignAttributeDef,permissionAssignAttributeName"
         width="700"/>
    </td>
  </tr>
  
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="enabledDisabled">
        <grouper:message key="simplePermissionAssign.assignFilterEnabledDisabled" />
      </label>
    </td>
    <td class="formTableRight">
      <select name="enabledDisabled">
        <option value="enabledOnly"><grouper:message key="simplePermissionAssign.assignFilterEnabledDisabledValueEnabled" /></option>
        <option value="disabledOnly"><grouper:message key="simplePermissionAssign.assignFilterEnabledDisabledValueDisabled" /></option>
        <option value="all"><grouper:message key="simplePermissionAssign.assignFilterEnabledDisabledValueAll" /></option>
      </select>
    </td>
  </tr>

  <tr>
   <td colspan="2">

     <input class="blueButton" type="submit" 
      onclick="ajax('../app/SimplePermissionUpdate.assignFilter', {formIds: 'simplePermissionFilterForm'}); return false;" 
      value="${grouper:message('simplePermissionAssign.assignFilterButton', true, false) }" style="margin-top: 2px" />
   
     <input class="blueButton" type="submit" 
      onclick="ajax('../app/SimplePermissionUpdate.assignPermissionButton', {formIds: 'simplePermissionFilterForm'}); return false;" 
      value="${grouper:message('simplePermissionAssign.assignPermissionButton', true, false) }" style="margin-top: 2px" />
   
   </td>
  </tr>
</table>

<!-- End: simplePermissionUpdate/simplePermissionAssignFilter.jsp: main page -->
