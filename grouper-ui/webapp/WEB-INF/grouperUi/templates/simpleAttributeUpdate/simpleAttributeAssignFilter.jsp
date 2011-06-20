<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/simpleAttributeAssignFilter.jsp: main page -->

<table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="attributeDefinition">
        <grouper:message key="simpleAttributeAssign.attributeDefinition" />
      </label>
    </td>
    <td class="formTableRight">
       <grouper:combobox 
         filterOperation="SimpleAttributeUpdateFilter.filterAttributeDefsByOwnerType?attributeAssignType=${attributeUpdateRequestContainer.attributeAssignType}" 
         id="attributeAssignAttributeDef" 
         width="700"/>
    </td>
  </tr>
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="attributeName">
        <grouper:message key="simpleAttributeAssign.attributeName" />
      </label>
    </td>
    <td class="formTableRight">
       <grouper:combobox 
         filterOperation="SimpleAttributeUpdateFilter.filterAttributeNamesByOwnerType?attributeAssignType=${attributeUpdateRequestContainer.attributeAssignType}" 
         id="attributeAssignAttributeName" additionalFormElementNames="attributeAssignAttributeDef"
         width="700"/>
    </td>
  </tr>
  <c:choose>
    <c:when test="${attributeUpdateRequestContainer.attributeAssignType.name == 'group'}">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="group">
            <grouper:message key="simpleAttributeAssign.assignGroup" />
          </label>
        </td>
        <td class="formTableRight">
           <grouper:combobox 
             filterOperation="SimpleAttributeUpdateFilter.filterGroups" 
             id="attributeAssignGroup" 
             width="700"/>
        </td>
      </tr>
    </c:when>
    <c:when test="${attributeUpdateRequestContainer.attributeAssignType.name == 'stem'}">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="folder">
            <grouper:message key="simpleAttributeAssign.assignStem" />
          </label>
        </td>
        <td class="formTableRight">
           <grouper:combobox 
             filterOperation="SimpleAttributeUpdateFilter.filterStems" 
             id="attributeAssignStem" 
             width="700"/>
        </td>
      </tr>
    </c:when>
    <c:when test="${attributeUpdateRequestContainer.attributeAssignType.name == 'member'}">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="member">
            <grouper:message key="simpleAttributeAssign.assignMember" />
          </label>
        </td>
        <td class="formTableRight">
           <grouper:combobox 
             filterOperation="SimpleAttributeUpdateFilter.filterSubjects" 
             id="attributeAssignMember" 
             width="700"/>
        </td>
      </tr>
  </c:when>
  <c:when test="${attributeUpdateRequestContainer.attributeAssignType.name == 'imm_mem' || attributeUpdateRequestContainer.attributeAssignType.name == 'any_mem'}">
    <tr class="formTableRow">
      <td class="formTableLeft" style="vertical-align: middle">
        <label for="membershipGroup">
          <grouper:message key="simpleAttributeAssign.assignMembershipGroup" />
        </label>
      </td>
      <td class="formTableRight">
         <grouper:combobox 
           filterOperation="SimpleAttributeUpdateFilter.filterGroups" 
           id="attributeAssignMembershipGroup" 
           width="700"/>
      </td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft" style="vertical-align: middle">
        <label for="membershipSubject">
          <grouper:message key="simpleAttributeAssign.assignMembershipSubject" />
        </label>
      </td>
      <td class="formTableRight">
         <grouper:combobox 
           filterOperation="SimpleAttributeUpdateFilter.filterSubjects" 
           id="attributeAssignMembershipSubject" 
           width="700"/>
      </td>
    </tr>
  </c:when>
  <c:when test="${attributeUpdateRequestContainer.attributeAssignType.name == 'attr_def'}">
    <tr class="formTableRow">
      <td class="formTableLeft" style="vertical-align: middle">
        <label for="attributeDef">
          <grouper:message key="simpleAttributeAssign.assignOwnerAttributeDef" />
        </label>
      </td>
      <td class="formTableRight">
         <grouper:combobox 
           filterOperation="SimpleAttributeUpdateFilter.filterAttributeDefs" 
           id="attributeAssignOwnerAttributeDef" 
           width="700"/>
      </td>
    </tr>
  </c:when>
</c:choose>  
  <tr class="formTableRow">
    <td class="formTableLeft" style="vertical-align: middle">
      <label for="enabledDisabled">
        <grouper:message key="simpleAttributeAssign.assignFilterEnabledDisabled" />
      </label>
    </td>
    <td class="formTableRight">
      <select name="enabledDisabled">
        <option value="enabledOnly"><grouper:message key="simpleAttributeAssign.assignFilterEnabledDisabledValueEnabled" /></option>
        <option value="disabledOnly"><grouper:message key="simpleAttributeAssign.assignFilterEnabledDisabledValueDisabled" /></option>
        <option value="all"><grouper:message key="simpleAttributeAssign.assignFilterEnabledDisabledValueAll" /></option>
      </select>
    </td>
  </tr>

  <tr>
   <td colspan="2">

     <input class="blueButton" type="submit" 
      onclick="ajax('../app/SimpleAttributeUpdate.assignFilter', {formIds: 'simpleAttributeFilterForm'}); return false;" 
      value="${grouper:message('simpleAttributeAssign.assignFilterButton', true, false) }" style="margin-top: 2px" />
   
     <input class="blueButton" type="submit" 
      onclick="ajax('../app/SimpleAttributeUpdate.assignAttribute', {formIds: 'simpleAttributeFilterForm'}); return false;" 
      value="${grouper:message('simpleAttributeAssign.assignAttributeButton', true, false) }" style="margin-top: 2px" />
   
   </td>
  </tr>
</table>

<!-- End: simpleAttributeUpdate/simpleAttributeAssignFilter.jsp: main page -->
