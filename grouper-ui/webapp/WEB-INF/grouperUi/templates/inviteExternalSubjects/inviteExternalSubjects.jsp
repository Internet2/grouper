<%@ include file="../common/commonTaglib.jsp"%>
<!-- externalSubjectSelfRegister/externalSubjectSelfRegister.jsp: main page -->

<grouper:title key="inviteExternalSubjects.inviteTitle" />

<div style="margin-left: 20px; margin-top: 20px; width: 800px; text-align: right">
  <span class="requiredIndicator">*</span> indicates a required field
</div>
<div class="section inviteExternalSubjectsDiv" style="margin-top: 0px; width:1000px">
  <grouper:subtitle key="inviteExternalSubjects.inviteSectionHeader" />
  <div class="sectionBody">
    <form action="whatever" id="inviteExternalsFormId" name="inviteExternalsFormName">

	    <%-- shows the invite externals table --%>
	    <table class="formTable " cellspacing="2" style="margin-bottom: 0">
	      <tbody>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.emailAddressesLabel" tooltipRef="inviteExternalSubjects.emailAddressesTooltip"  />
              <span class="requiredIndicator">*</span>
            </td>
            <td class="formTableRight">
              <textarea name="emailAddressesToInvite" cols="40" rows="6"></textarea>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.subjectLabel" tooltipRef="inviteExternalSubjects.subjectTooltip"  />
              <span class="requiredIndicator"></span>
            </td>
            <td class="formTableRight">
              <input type="text" name="emailSubject" style="width: 450px"/>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.messageToUsersLabel" tooltipRef="inviteExternalSubjects.messageToUsersTooltip"  />
              <span class="requiredIndicator"></span>
            </td>
            <td class="formTableRight">
              <textarea name="messageToUsers" cols="40" rows="6"></textarea>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="inviteExternalSubjects.ccEmailAddressLabel" tooltipRef="inviteExternalSubjects.ccEmailAddressTooltip"  />
            </td>
            <td class="formTableRight">
              <input type="text" name="ccEmailAddress" style="width: 450px"/>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="inviteExternalSubjects.groupsToAssignLabel" tooltipRef="inviteExternalSubjects.groupsToAssignTooltip"  />
            </td>
            <td class="formTableRight">
              <%-- 5 combos of groups to assign --%>
              <span class="combohint" ><grouper:message key="inviteExternalSubjects.groupsToAssignHelp"  /></span>
		          <grouper:combobox filterOperation="InviteExternalSubjects.groupToAssignFilter" id="groupToAssign0" width="700"/><br />
              <span class="combohint" ><grouper:message key="inviteExternalSubjects.groupsToAssignHelp"  /></span>
              <grouper:combobox filterOperation="InviteExternalSubjects.groupToAssignFilter" id="groupToAssign1" width="700"/><br />
              <span class="combohint" ><grouper:message key="inviteExternalSubjects.groupsToAssignHelp"  /></span>
              <grouper:combobox filterOperation="InviteExternalSubjects.groupToAssignFilter" id="groupToAssign2" width="700"/><br />
              <span class="combohint" ><grouper:message key="inviteExternalSubjects.groupsToAssignHelp"  /></span>
              <grouper:combobox filterOperation="InviteExternalSubjects.groupToAssignFilter" id="groupToAssign3" width="700"/><br />
              <span class="combohint" ><grouper:message key="inviteExternalSubjects.groupsToAssignHelp"  /></span>
              <grouper:combobox filterOperation="InviteExternalSubjects.groupToAssignFilter" id="groupToAssign4" width="700"/>
            </td>
          </tr>
          
	        <tr>
	          <td colspan="2" style="text-align: right">
	            <div class="buttonRow">
						    <input class="blueButton" type="submit" 
						      onclick="ajax('InviteExternalSubjects.submit', {formIds: 'inviteExternalsFormId'}); return false;" 
						      value="${navMap['inviteExternalSubjects.submitButtonText']}" 
						      onmouseover="Tip('${grouper:escapeJavascript(navMap['inviteExternalSubjects.submitButtonTooltip'])}')" 
						      onmouseout="UnTip()" />    
						  </div>
					  </td>
	        </tr>
	      </tbody>
	    </table>
	  </form>
  </div>
</div>
