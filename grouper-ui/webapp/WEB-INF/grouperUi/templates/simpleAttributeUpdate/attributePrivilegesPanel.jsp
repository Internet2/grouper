<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->


<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.privilegesSectionHeader" />

  <div class="sectionBody">
    <form id="attributePrivilegesSubjectFormId" name="attributePrivilegesSubjectFormName" onsubmit="return false;" >
    
	    <%-- signify which attribute def we are talking about --%>
	    <input type="hidden" name="attributeDefToEditId" 
	              value="${attributeUpdateRequestContainer.attributeDefToEdit.id}" />
      
	    <table class="formTable formTableSpaced" cellspacing="2">
	      <tr class="formTableRow">
	        <td class="formTableLeft" style="vertical-align: middle">
	          <label for="folder">
	            <grouper:message key="simpleAttributeUpdate.privilegeSubject" />
	          </label>
	        </td>
	        <td class="formTableRight" style="white-space: nowrap;">
            <div class="combohint"><grouper:message key="simpleAttributeUpdate.privilegeSubjectCombohint"/></div>
            <table width="900" cellpadding="0" cellspacing="0">
				      <tr valign="top">
				        <td style="padding: 0px" width="710">
	 	             <grouper:combobox 
                 filterOperation="SimpleAttributeUpdateFilter.filterPrivilegeUsers?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}" 
                 id="simpleAttributeUpdatePrivilegeSubject" 
		               width="700"/>
				        </td>
				        <td>
				          <input class="blueButton" type="submit" 
				          onclick="ajax('../app/SimpleAttributeUpdateFilter.addPrivilegeSubject', {formIds: 'attributePrivilegesSubjectFormId'}); return false;" 
				          value="${attributeUpdateRequestContainer.text.filterAttributePrivilegeSubject}" style="margin-top: 2px" />
				        </td>
				      </tr>
				    </table>
	        </td>
	      </tr>

        <tr class="formTableRow">
          <td class="formTableLeft" style="vertical-align: middle">
            <label for="folder">
              <grouper:message key="simpleAttributeUpdate.indirectPrivileges" />
            </label>
          </td>
          <td class="formTableRight" style="white-space: nowrap;">
            <input style="margin-right: -3px" name="showIndirectPrivileges" value="true"
              onchange="ajax('../app/SimpleAttributeUpdate.attributeEditPanelPrivileges', {formIds: 'attributePrivilegesSubjectFormId'}); return true"
              type="checkbox" ${attributeUpdateRequestContainer.showIndirectPrivilegesComputed ? 'checked="checked"' : '' } 
            />
            <grouper:message key="simpleAttributeUpdate.indirectPrivilegesCheckbox" />
          </td>
        </tr>
	    </table>

      <%-- this is where additional subjects will be stored so we remember to draw them on the screen --%>
      <div id="additionalSubjects">
        <c:set var="row" value="0" />
        <c:forEach items="${attributeUpdateRequestContainer.privilegeAdditionalGuiMembers}" var="privilegeAdditionalGuiMember">
          <input  name="additionalMemberId_${row}"
            type="hidden" value="${privilegeAdditionalGuiMember.member.uuid}" />
          <c:set var="row" value="${row + 1}" />
        </c:forEach>
      
      </div>

      <%-- message if no members --%>  
      <c:choose>
        <c:when test="${fn:length(attributeUpdateRequestContainer.privilegeSubjectContainers) == 0}">
          <grouper:message key="simpleAttributeUpdate.noPrivilegesFound" />
        
        </c:when>
        <c:otherwise>
          <%-- paging summary shows which records, and page size --%>
          <div class="pagingSummary">
            <grouper:message key="simpleAttributeUpdate.pagingLabelPrefix" />
            <grouper:paging 
              refreshOperation="SimpleAttributeUpdate.attributeEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}" 
              showSummaryOrButtons="true" pagingName="simpleAttributeUpdatePrivileges"  />
            <c:if test="${fn:length(attributeUpdateRequestContainer.privilegeAdditionalGuiMembers) > 0}">
              <grouper:message key="simpleAttributeUpdate.pagingAndAdditional" />
            </c:if>
              
          </div>
    
          <table cellspacing="2" class="formTable" width="1100">
            <c:set var="row" value="0" />
            <c:forEach items="${attributeUpdateRequestContainer.privilegeSubjectContainers}" var="privilegeSubjectContainer">
              
              <c:set var="guiMember" value="${attributeUpdateRequestContainer.privilegeSubjectContainerGuiMembers[row]}" />
              <c:if test="${attributeUpdateRequestContainer.showPrivilegeHeader[row]}">
                <tr>
                  <c:forTokens var="privilegeName" items="attrAdmin attrRead attrUpdate attrOptin attrOptout attrView" delims=" ">
                    <th class="privilegeHeader"><grouper:message key="priv.${privilegeName}" /></grou></th>
                  </c:forTokens>
                  <th class="privilegeHeader" style="text-align: left">
                    &nbsp; &nbsp; <grouper:message key="simpleAttributeUpdate.entityHeader" /> 
                  </th>
                </tr>
              </c:if>
              <tr  ${row % 2 == 1 ? 'class="alternate"' : ''}>
                <c:forTokens var="privilegeName" items="attrAdmin attrRead attrUpdate attrOptin attrOptout attrView" delims=" ">
                  <c:set var="privilegeAssignType" value="${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType}" />
                  <td class="privilegeRow">
                    <%-- keep the previous state so we know what the user changed --%>
                    <input  name="previousState__${guiMember.member.uuid}__${privilegeName}"
                      type="hidden" value="${privilegeAssignType.immediate ? 'true' : 'false'}" />
                    <%-- note, too much space between elements, move it over 3px --%>
                    <input  style="margin-right: -3px" name="privilegeCheckbox__${guiMember.member.uuid}__${privilegeName}" value="true"
                      type="checkbox" ${privilegeAssignType.immediate ? 'checked="checked"' : '' } 
                    /><c:set var="confirmNavName" value="simpleAttributeUpdate.privilegeImageConfirm${privilegeAssignType.immediate ? 'Deny' : 'Allow'}" /><a href="#" 
                    onclick="if (confirm('${grouper:message(confirmNavName, true, true) }')) {ajax('../app/SimpleAttributeUpdate.privilegePanelImageClick?memberId=${guiMember.member.uuid}&privilegeName=${privilegeName}&allow=${privilegeAssignType.immediate ? 'false' : 'true'}', {formIds: 'attributePrivilegesSubjectFormId'});} return false;"
                    ><c:choose>
                      <c:when test="${privilegeAssignType != null}"
                      ><c:choose>
                        <c:when test="${privilegeAssignType.name == 'IMMEDIATE'}"
                          ><c:set var="tooltipName" value="simpleAttributeUpdate.immediateTooltip" /></c:when
                          ><c:when test="${privilegeAssignType.name == 'EFFECTIVE'}"
                          ><c:set var="tooltipName" value="simpleAttributeUpdate.effectiveTooltip" /></c:when
                          ><c:otherwise><c:set var="tooltipName" value="simpleAttributeUpdate.immediateAndEffectiveTooltip" /></c:otherwise></c:choose
                          ><img src="../../grouperExternal/public/assets/images/accept.png" height="14px" border="0" 
                        onmouseover="Tip('${grouper:escapeJavascript(navMap[tooltipName])}')" 
                        onmouseout="UnTip()"
                      /></c:when>
                      <c:otherwise><img src="../../grouperExternal/public/assets/images/cancel.png" height="14px" border="0" 
                        onmouseover="Tip('${grouper:escapeJavascript(navMap['simpleAttributeUpdate.unassignedTooltip'])}')" 
                        onmouseout="UnTip()"
                      /></c:otherwise>
                    </c:choose></a>
                  </td>
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
                onclick="ajax('../app/SimpleAttributeUpdate.privilegeCancel'); return false;" 
                value="${attributeUpdateRequestContainer.text.privilegePanelCancel}" style="margin-top: 2px" />
               
               <input class="blueButton" type="submit" 
                onclick="ajax('../app/SimpleAttributeUpdate.privilegePanelSubmit', {formIds: 'attributePrivilegesSubjectFormId'}); return false;" 
                value="${attributeUpdateRequestContainer.text.privilegePanelSubmit}" style="margin-top: 2px" />
             
             </td>
           </tr>
           
          </table> 
 
          <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
          <div class="pagingButtons">
            <grouper:message key="simpleAttributeUpdate.pagingResultPrefix" />
              <grouper:paging showSummaryOrButtons="false" pagingName="simpleAttributeUpdatePrivileges" 
              refreshOperation="SimpleAttributeUpdate.attributeEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}" 
               />
          </div>
        
        </c:otherwise>
      </c:choose>
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/attributePrivilegesPanel.jsp -->
