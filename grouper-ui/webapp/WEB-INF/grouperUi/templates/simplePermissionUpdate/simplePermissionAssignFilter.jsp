<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simplePermissionUpdate/simplePermissionAssignFilter.jsp: main page -->

<input type="hidden" name="limitSimulationHiddenField" value="false" />

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

  <tr class="formTableRow limitSimulationRow" style="display: none">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="enabledDisabled">
        <grouper:message key="simplePermissionAssign.limitProcessor" />
      </label>
    </td>
    <td class="formTableRight">
      <select name="processLimitsProcessor">
        <option value=""><grouper:message key="simplePermissionAssign.limitDropDownNone" /></option>
        <option value="PROCESS_LIMITS" selected="selected"><grouper:message key="simplePermissionAssign.limitDropDownProcessLimits" /></option>
      </select>
    </td>
  </tr>

  <tr class="formTableRow limitSimulationRow" style="display: none">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="enabledDisabled">
        <grouper:message key="simplePermissionAssign.limitEnvironmentVariables" />
      </label>
    </td>
    <td class="formTableRight">
    <table>
      <tr>
        <th style="white-space: nowrap; text-align: left; " class="privilegeHeader"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableName" /></th>
        <th style="white-space: nowrap; text-align: left; " class="privilegeHeader"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableValue" /></th>
        <th style="white-space: nowrap; text-align: left; " class="privilegeHeader"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableType" /></th>
      </tr>
      <c:forEach var="i" begin="0" end="50">

        <tr style="${i>=2 ? 'display:none;' : '' }" id="limitEnvVarRow${i}">
          <td><input name="envVarName${i}" type="text" style="width: 15em" /></td>
          <td><input name="envVarValue${i}" type="text" style="width: 25em"/></td>
          <td>
            <select name="envVarType${i}" type="text">
              <option value="string"></option>
              <option value="string"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeString" /></option>
              <option value="integer"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeInteger" /></option>
              <option value="double"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeDecimal" /></option>
              <option value="timestamp"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeTimestamp" /></option>
              <option value="boolean"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeBoolean" /></option>
              <option value="empty"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeEmpty" /></option>
              <option value="null"><grouper:message key="simplePermissionAssign.limitEnvironmentVariableTypeNull" /></option>
            </select>
          </td>
        </tr>
      </c:forEach>      
      <script type="text/javascript">var nextFreeEnvVarRow = 2;</script>
      <tr><td colspan="2"><a href="#" onclick="$('#limitEnvVarRow' + nextFreeEnvVarRow).show(); nextFreeEnvVarRow++; return false;"
        ><grouper:message key="simplePermissionAssign.moreLimitEnvVars" /></a></td></tr>
    </table>
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
   
     <input class="blueButton" type="submit"  id="simulateLimitsButton"
      onclick="ajax('../app/SimplePermissionUpdate.limitSimulation', {formIds: 'simplePermissionFilterForm'}); return false;" 
      value="${grouper:message('simplePermissionAssign.limitSimulationButton', true, false) }" style="margin-top: 2px" />
   
   </td>
  </tr>
</table>

<!-- End: simplePermissionUpdate/simplePermissionAssignFilter.jsp: main page -->
