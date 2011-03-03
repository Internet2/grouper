<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: inviteExternalSubjects/inviteExternalSubjects.jsp: main page -->

<table cellpadding="0" cellspacing="0" width="800">
  <tr>
    <td><grouper:title key="inviteExternalSubjects.inviteTitle" /></td>
    <td>
      <c:if test="${inviteExternalSubjectsContainer.showLinksToUi}">
        <html:link page="/grouperUi/appHtml/grouper.html?operation=SimpleMembershipUpdate.init&groupId=${inviteExternalSubjectsContainer.defaultGroup.id}" >
          <grouper:message key="ui-lite.fromInvite-link"/>
        </html:link>
        &nbsp; &nbsp;
        <html:link page="/populateGroupSummary.do?groupId=${inviteExternalSubjectsContainer.defaultGroup.id}" >
          <grouper:message key="ui-lite.fromInvite-admin-link"/>
        </html:link>
      </c:if>
    </td>
   </tr>
</table>

<div style="margin-left: 20px; margin-top: 20px; width: 800px; text-align: right">
  <span class="requiredIndicator">*</span> indicates a required field
</div>
<div class="section inviteExternalSubjectsDiv" style="margin-top: 0px; width:1200px">
  <grouper:subtitle key="inviteExternalSubjects.inviteSectionHeader" />  
  <div class="sectionBody">
    <form action="whatever" id="inviteExternalsFormId" name="inviteExternalsFormName">

	    <%-- shows the invite externals table --%>
	    <table class="formTable " cellspacing="2" style="margin-bottom: 0">
	      <tbody>

          <c:if test="${inviteExternalSubjectsContainer.allowInviteByIdentifier}">
            <tr class="formTableRow">
              <td class="formTableLeft" style="white-space: nowrap;">
                <grouper:message key="inviteExternalSubjects.inviteByIdentifierLabel" tooltipRef="inviteExternalSubjects.inviteByIdentifierTooltip"  />
                <span class="requiredIndicator">*</span>
              </td>
              <td class="formTableRight" style="white-space: nowrap;">
                <input type="radio" name="inviteBy" checked="checked" value="emailAddress" 
                    onclick="guiHideShow(event, 'inviteExternalSubjectEmails', true); return true;" /> 
                <grouper:message key="inviteExternalSubjects.inviteByEmailRadioLabel" tooltipRef="inviteExternalSubjects.inviteByEmailRadioTooltip"  />
                &nbsp;
                &nbsp;
                <input type="radio" name="inviteBy" value="identifier" 
                    onclick="guiHideShow(event, 'inviteExternalSubjectEmails', false); return true;"/> 
                <grouper:message key="inviteExternalSubjects.inviteByIdentifierRadioLabel" tooltipRef="inviteExternalSubjects.inviteByIdentifierRadioTooltip"  />
              </td>
            </tr>
            
          </c:if>

          <tr class="formTableRow hides_inviteExternalSubjectEmails" style="display:none">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.loginIdsLabel" tooltipRef="inviteExternalSubjects.loginIdsTooltip"  />
              <span class="requiredIndicator">*</span>
            </td>
            <td class="formTableRight">
              <textarea name="loginIdsToInvite" cols="40" rows="6"></textarea>
            </td>
          </tr>
        
          <tr class="formTableRow shows_inviteExternalSubjectEmails">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.emailAddressesLabel" tooltipRef="inviteExternalSubjects.emailAddressesTooltip"  />
              <span class="requiredIndicator">*</span>
            </td>
            <td class="formTableRight">
              <textarea name="emailAddressesToInvite" cols="40" rows="6"></textarea>
            </td>
          </tr>
          <tr class="formTableRow shows_inviteExternalSubjectEmails">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.subjectLabel" tooltipRef="inviteExternalSubjects.subjectTooltip"  />
              <span class="requiredIndicator"></span>
            </td>
            <td class="formTableRight">
              <input type="text" name="emailSubject" style="width: 450px" value="${grouper:escapeHtml(inviteExternalSubjectsContainer.defaultEmailSubject)}" />
            </td>
          </tr>
          <tr class="formTableRow shows_inviteExternalSubjectEmails">
            <td class="formTableLeft" style="white-space: nowrap;">
              <grouper:message key="inviteExternalSubjects.messageToUsersLabel" tooltipRef="inviteExternalSubjects.messageToUsersTooltip"  />
              <span class="requiredIndicator"></span>
            </td>
            <td class="formTableRight">
              <textarea name="messageToUsers" cols="40" rows="6">${grouper:escapeHtml(inviteExternalSubjectsContainer.defaultEmailMessage)}</textarea>
            </td>
          </tr>
          <tr class="formTableRow shows_inviteExternalSubjectEmails">
            <td class="formTableLeft" style="white-space: nowrap;">
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
		          <grouper:combobox filterOperation="InviteExternalSubjects.groupToAssignFilter" id="groupToAssign0" width="700" 
		              comboDefaultText="${inviteExternalSubjectsContainer.firstComboDefaultText}"  comboDefaultValue="${inviteExternalSubjectsContainer.firstComboDefaultValue}" /><br />
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
<!-- End: inviteExternalSubjects/inviteExternalSubjects.jsp: main page -->
