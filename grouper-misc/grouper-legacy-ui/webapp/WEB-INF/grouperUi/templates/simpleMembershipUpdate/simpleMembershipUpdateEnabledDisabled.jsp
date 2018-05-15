<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleMembershipUpdateEnabledDisabled.jsp -->
<div class="simpleMembershipUploadMemberDetails">

  <div class="section">
  
    <grouper:subtitle label="${simpleMembershipUpdateContainer.text.enabledDisableSubtitle}" />
  
    <div class="sectionBody">
    <form id="simpleMembershipEnableDisableForm" name="simpleMembershipEnableDisableForm">
      <table class="formTable formTableSpaced SubjectInfo">
      
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableGroupPath}" />
            </td>
            <td class="formTableRight">
              ${simpleMembershipUpdateContainer.guiGroup.group.displayName}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableEntity}" />
            </td>
            <td class="formTableRight">
              ${simpleMembershipUpdateContainer.enabledDisabledMember.guiSubject.screenLabel}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableEntityId}" />
            </td>
            <td class="formTableRight">
              ${simpleMembershipUpdateContainer.enabledDisabledMember.guiSubject.subject.id}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableEntitySource}" />
            </td>
            <td class="formTableRight">
              ${simpleMembershipUpdateContainer.enabledDisabledMember.guiSubject.subject.sourceId}
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableStartDate}" />
            </td>
            <td class="formTableRight"><input type="text" name="enabledDate"  id="enabledDate"
            value="${simpleMembershipUpdateContainer.enabledDisabledMember.enabledDate}" style="width: 8em" />
            <%-- a href="#" onclick="return guiCalendarImageClick('enabledDate');"
              ><img src="../../grouperExternal/public/assets/images/calendar.gif" border="0" /></a --%>
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableDateMask}" /></span>
              <%-- script>guiCalendarInit("enabledDate");</script --%>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableEndDate}" />
            </td>
            <td class="formTableRight"><input type="text" name="disabledDate"  id="disabledDate"
            value="${simpleMembershipUpdateContainer.enabledDisabledMember.disabledDate}" style="width: 8em" />
            <%--  a href="#" onclick="return guiCalendarImageClick('disabledDate');"
              ><img src="../../grouperExternal/public/assets/images/calendar.gif" border="0" /></a --%>
            <span class="simpleMembershipUpdateDisabled"
              ><grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableDateMask}" /></span>
              <%-- script>guiCalendarInit("disabledDate");</script --%>
            </td>
          </tr>
          <tr>
            <td colspan="2" align="right"  class="buttonRow">

              <button class="simplemodal-close blueButton"><grouper:message value="${simpleMembershipUpdateContainer.text.enabledDisableCancelButton}" /></button> 
              &nbsp;
              <%-- add member button --%>
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleMembershipUpdate.enabledDisabledSubmit', {formIds: 'simpleMembershipEnableDisableForm'}); return false;" 
              value="${simpleMembershipUpdateContainer.text.enabledDisableOkButton}" />
            </td>
          </tr>
          
      </table>
      </form>
    </div>
  </div>
</div>
<!-- End: simpleMembershipUpdateEnabledDisabled.jsp -->


