<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleGroupUpdate/groupPrivilegesPanel.jsp -->


<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleGroupUpdate.privilegesSectionHeader" />

  <div class="sectionBody">
    <form id="groupPrivilegesSubjectFormId" name="groupPrivilegesSubjectFormName" onsubmit="return false;" >
    
	    <%-- signify which group we are talking about --%>
	    <input type="hidden" name="groupToEditId" 
	              value="${groupUpdateRequestContainer.groupToEdit.uuid}" />
      
	    <table class="formTable formTableSpaced" cellspacing="2">
	      <tr class="formTableRow">
	        <td class="formTableLeft" style="vertical-align: middle">
	          <label for="folder">
	            <grouper:message key="simpleGroupUpdate.privilegeSubject" />
	          </label>
	        </td>
	        <td class="formTableRight" style="white-space: nowrap;">
            <div class="combohint"><grouper:message key="simpleGroupUpdate.privilegeSubjectCombohint"/></div>
            <table width="900" cellpadding="0" cellspacing="0">
				      <tr valign="top">
				        <td style="padding: 0px" width="710">
	 	             <grouper:combobox 
                 filterOperation="SimpleGroupUpdateFilter.filterPrivilegeUsers?groupToEditId=${groupUpdateRequestContainer.groupToEdit.id}" 
                 id="simpleGroupUpdatePrivilegeSubject" 
		               width="700"/>
				        </td>
				        <td>
				          <input class="blueButton" type="submit" 
				          onclick="ajax('../app/SimpleGroupUpdateFilter.addPrivilegeSubject', {formIds: 'groupPrivilegesSubjectFormId'}); return false;" 
				          value="${grouper:message('simpleGroupUpdate.filterGroupPrivilegeSubject', true, false) }" style="margin-top: 2px" />
				        </td>
				      </tr>
				    </table>
	        </td>
	      </tr>

        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="folder">
              <grouper:message key="simpleGroupUpdate.indirectPrivileges" />
            </label>
          </td>
          <td class="formTableRight" style="white-space: nowrap;">
            <input style="margin-right: -3px" name="showIndirectPrivileges" value="true"
              onchange="ajax('../app/SimpleGroupUpdate.groupEditPanelPrivilegesClearPaging', {formIds: 'groupPrivilegesSubjectFormId'}); return true"
              type="checkbox" ${groupUpdateRequestContainer.showIndirectPrivilegesComputed ? 'checked="checked"' : '' } 
            />
            <grouper:message key="simpleGroupUpdate.indirectPrivilegesCheckbox" />
          </td>
        </tr>
	    </table>

      <%-- this is where additional subjects will be stored so we remember to draw them on the screen --%>
      <div id="additionalSubjects">
        <c:set var="row" value="0" />
        <c:forEach items="${groupUpdateRequestContainer.privilegeAdditionalGuiMembers}" var="privilegeAdditionalGuiMember">
          <input  name="additionalMemberId_${row}"
            type="hidden" value="${privilegeAdditionalGuiMember.member.uuid}" />
          <c:set var="row" value="${row + 1}" />
        </c:forEach>
      
      </div>

      <%-- message if no members --%>  
      <c:choose>
        <c:when test="${fn:length(groupUpdateRequestContainer.privilegeSubjectContainers) == 0}">
          <grouper:message key="simpleGroupUpdate.noPrivilegesFound" />
        
        </c:when>
        <c:otherwise>
          <%-- paging summary shows which records, and page size --%>
          <div class="pagingSummary">
            <grouper:message key="simpleGroupUpdate.pagingLabelPrefix" />
            <grouper:paging 
              refreshOperation="SimpleGroupUpdate.groupEditPanelPrivileges?groupToEditId=${groupUpdateRequestContainer.groupToEdit.id}&showIndirectPrivileges=${groupUpdateRequestContainer.showIndirectPrivilegesComputed}" 
              showSummaryOrButtons="true" pagingName="simpleGroupUpdatePrivileges"  />
            <c:if test="${fn:length(groupUpdateRequestContainer.privilegeAdditionalGuiMembers) > 0}">
              <grouper:message key="simpleGroupUpdate.pagingAndAdditional" />
            </c:if>
              
          </div>
    
          <table cellspacing="2" class="formTable" width="1100">
            <c:set var="row" value="0" />
            <c:forEach items="${groupUpdateRequestContainer.privilegeSubjectContainers}" var="privilegeSubjectContainer">
              
              <c:set var="guiMember" value="${groupUpdateRequestContainer.privilegeSubjectContainerGuiMembers[row]}" />
              <c:if test="${groupUpdateRequestContainer.showPrivilegeHeader[row]}">
                <tr>
                  <c:forTokens var="privilegeName" items="admin read update optin optout view" delims=" ">
                    <c:if test="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb != 'entity' || privilegeName == 'admin' || privilegeName == 'view'}" >
                      <th class="privilegeHeader"><grouper:message key="priv.${privilegeName}" /></grou></th>
                    </c:if>
                  </c:forTokens>
                  <th class="privilegeHeader" style="text-align: left">
                    &nbsp; &nbsp; <grouper:message key="simpleGroupUpdate.entityHeader" /> 
                  </th>
                </tr>
              </c:if>
              <tr  ${row % 2 == 1 ? 'class="alternate"' : ''}>
                <c:forTokens var="privilegeName" items="admin read update optin optout view" delims=" ">
                
                  <c:if test="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb != 'entity' || privilegeName == 'admin' || privilegeName == 'view'}" >
                  
                    <c:set var="privilegeAssignType" value="${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType}" />
                    <td class="privilegeRow">
                      <%-- keep the previous state so we know what the user changed --%>
                      <input  name="previousState__${guiMember.member.uuid}__${privilegeName}"
                        type="hidden" value="${privilegeAssignType.immediate ? 'true' : 'false'}" />
                      <%-- note, too much space between elements, move it over 3px --%>
                      <input  style="margin-right: -3px" name="privilegeCheckbox__${guiMember.member.uuid}__${privilegeName}" value="true"
                        type="checkbox" ${privilegeAssignType.immediate ? 'checked="checked"' : '' } 
                      /><c:set var="confirmNavName" value="simpleGroupUpdate.privilegeImageConfirm${privilegeAssignType.immediate ? 'Deny' : 'Allow'}" /><a href="#" 
                      onclick="if (confirm('${grouper:message(confirmNavName, true, true) }')) {ajax('../app/SimpleGroupUpdate.privilegePanelImageClick?memberId=${guiMember.member.uuid}&privilegeName=${privilegeName}&allow=${privilegeAssignType.immediate ? 'false' : 'true'}', {formIds: 'groupPrivilegesSubjectFormId'});} return false;"
                      ><c:choose>
                        <c:when test="${privilegeAssignType != null}"
                        ><c:choose>
                          <c:when test="${privilegeAssignType.name == 'IMMEDIATE'}"
                            ><c:set var="tooltipName" value="simpleGroupUpdate.immediateTooltip" /></c:when
                            ><c:when test="${privilegeAssignType.name == 'EFFECTIVE'}"
                            ><c:set var="tooltipName" value="simpleGroupUpdate.effectiveTooltip" /></c:when
                            ><c:otherwise><c:set var="tooltipName" value="simpleGroupUpdate.immediateAndEffectiveTooltip" /></c:otherwise></c:choose
                            ><img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                          onmouseover="Tip('${grouper:escapeJavascript(navMap[tooltipName])}')" 
                          onmouseout="UnTip()"
                        /></c:when>
                        <c:otherwise><img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                          onmouseover="Tip('${grouper:escapeJavascript(navMap['simpleGroupUpdate.unassignedTooltip'])}')" 
                          onmouseout="UnTip()"
                        /></c:otherwise>
                      </c:choose></a>
                    </td>
                  </c:if>
                </c:forTokens>
                <td width="1000">
                  <%-- show an icon for the subject --%>
                  <grouper:subjectIcon guiSubject="${guiMember.guiSubject}" /> 
                  ${fn:escapeXml(guiMember.guiSubject.screenLabel)}
                </td>
              </tr>
              <c:set var="row" value="${row + 1}" />
            </c:forEach>          
           <tr>
             <td colspan="7">
      
               <input class="redButton" type="submit" 
                onclick="ajax('../app/SimpleGroupUpdate.privilegeCancel'); return false;" 
                value="${grouper:message('simpleGroupUpdate.privilegePanelCancel', true, false) }" style="margin-top: 2px" />
               
               <input class="blueButton" type="submit" 
                onclick="ajax('../app/SimpleGroupUpdate.privilegePanelSubmit', {formIds: 'groupPrivilegesSubjectFormId'}); return false;" 
                value="${grouper:message('simpleGroupUpdate.privilegePanelSubmit', true, false) }" style="margin-top: 2px" />
             
             </td>
           </tr>
           
          </table> 
 
          <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
          <div class="pagingButtons">
            <grouper:message key="simpleGroupUpdate.pagingResultPrefix" />
              <grouper:paging showSummaryOrButtons="false" pagingName="simpleGroupUpdatePrivileges" 
              refreshOperation="SimpleGroupUpdate.groupEditPanelPrivileges?groupToEditId=${groupUpdateRequestContainer.groupToEdit.id}&showIndirectPrivileges=${groupUpdateRequestContainer.showIndirectPrivilegesComputed}" 
               />
          </div>
        
        </c:otherwise>
      </c:choose>
    </form>
    <br />
  </div>
</div>

<!-- End: simpleGroupUpdate/groupPrivilegesPanel.jsp -->
