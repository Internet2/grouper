<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/simpleAttributeAssignments.jsp -->


<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeUpdate.assignAssignments" />

  <div class="sectionBody">
      
      <%-- message if no members --%>  
      <c:choose>
        <c:when test="${fn:length(attributeUpdateRequestContainer.guiAttributeAssigns) == 0}">
          <grouper:message key="simpleAttributeUpdate.assignNoResultsFound" />
        
        </c:when>
        <c:otherwise>
          <%-- paging summary shows which records, and page size --%>
          <%-- div class="pagingSummary">
            <grouper:message key="simpleAttributeUpdate.pagingLabelPrefix" />
            <grouper:paging 
              refreshOperation="SimpleAttributeUpdate.attributeEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&showIndirectPrivileges=${attributeUpdateRequestContainer.showIndirectPrivilegesComputed}" 
              showSummaryOrButtons="true" pagingName="simpleAttributeUpdatePrivileges"  />
              
          </div --%>
    
          <table cellspacing="2" class="formTable" width="700">
            <c:set var="row" value="0" />
            <c:forEach items="${attributeUpdateRequestContainer.guiAttributeAssigns}" var="guiAttributeAssign">
              
              <c:if test="${attributeUpdateRequestContainer.showPrivilegeHeader[row]}">
                <tr>
                  <th></th>
                  
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    <grouper:message key="${attributeUpdateRequestContainer.attributeAssignTypeLabelKey}" />
                  </th>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeName" />
                  </th>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    <grouper:message key="simpleAttributeUpdate.assignHeaderEnabled" />
                  </th>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    <grouper:message key="simpleAttributeUpdate.assignHeaderValues" />
                  </th>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    <grouper:message key="simpleAttributeUpdate.assignHeaderAttributeDef" />
                  </th>
                  <th class="privilegeHeader" style="text-align: left; white-space: nowrap;">
                    <grouper:message key="simpleAttributeUpdate.assignHeaderUuid" /> 
                  </th>
                </tr>
              </c:if>
              <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                <td style="white-space: nowrap;">
                
                  <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignDelete?attributeAssignId=${guiAttributeAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                    ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                    alt="${grouper:message('simpleAttributeUpdate.deleteAssignmentAlt', true, false)}"
                    onmouseover="Tip('${grouper:message('simpleAttributeUpdate.deleteAssignmentAlt', true, true)}')" 
                    onmouseout="UnTip()"/></a>

                  <a href="#" onclick="ajax('SimpleAttributeUpdate.assignEdit?attributeAssignId=${guiAttributeAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                    ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                    alt="${grouper:message('simpleAttributeUpdate.editAssignmentAlt', true, false)}"
                    onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editAssignmentAlt', true, true)}')" 
                    onmouseout="UnTip()"/></a>
                  <a class="assignmentMenuButton" href="#"
                    ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                    id="assignmentMenuButton_${guiAttributeAssign.attributeAssign.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/></a>
                </td>
                <td style="white-space: nowrap;">
                  <grouper:message valueTooltip="${grouper:escapeHtml(guiAttributeAssign.screenLabelLongIfDifferent)}" 
                     value="${grouper:escapeHtml(guiAttributeAssign.screenLabelShort)}"  />
                </td>
                <td>
                  <grouper:message value="${grouper:escapeHtml(guiAttributeAssign.attributeAssign.attributeDefName.displayExtension)}" 
                    valueTooltip="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDefName.displayName)}" />
                </td>
                <td>
                  <grouper:message key="${guiAttributeAssign.enabledDisabledKey}"  />
                </td>
                <td style="white-space: nowrap;">
                  <%-- loop through the values --%>
                  <c:set var="valueRow" value="0" />
              
                  
                  <c:forEach items="${guiAttributeAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
                  
                    <%-- we need a newline before non-first rows --%>
                    <c:if test="${valueRow != 0}">
                      <br />
                    </c:if>

                    <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignValueDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignValueDelete?attributeAssignId=${guiAttributeAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                      ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                      alt="${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, false)}"
                      onmouseover="Tip('${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, true)}')" 
                      onmouseout="UnTip()"/></a>

                    <a href="#" onclick="ajax('SimpleAttributeUpdate.assignValueEdit?attributeAssignId=${guiAttributeAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                      ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                      alt="${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, false)}"
                      onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, true)}')" 
                      onmouseout="UnTip()"/></a>
                    
                    ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                    
                    <c:set var="valueRow" value="${valueRow + 1}" />
                  </c:forEach>
                
                </td>
                <td>
                  <grouper:message value="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDef.extension)}" 
                    valueTooltip="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDef.name)}" />
                </td>
                <td>${grouper:abbreviate(guiAttributeAssign.attributeAssign.id, 8, true, true)}</td>
              </tr>
              
              <c:forEach items="${guiAttributeAssign.guiAttributeAssigns}" var="guiAttributeAssignAssign">
                
                <%-- filter out results which dont match the enabled/disabled filter --%>              
                <c:if test="${attributeUpdateRequestContainer.enabledDisabled == null || (attributeUpdateRequestContainer.enabledDisabled == guiAttributeAssignAssign.attributeAssign.enabled )}" >
                <%-- see if there are assignments on the assignment --%>
                <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                  <td style="white-space: nowrap;" align="right">
                    <span class="simpleMembershipUpdateDisabled"><grouper:message key="simpleAttributeUpdate.assignMetadata" /></span>
                    <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                      ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                      alt="${grouper:message('simpleAttributeUpdate.deleteAssignmentAlt', true, false)}"
                      onmouseover="Tip('${grouper:message('simpleAttributeUpdate.deleteAssignmentAlt', true, true)}')" 
                      onmouseout="UnTip()"/></a>
  
                    <a href="#" onclick="ajax('SimpleAttributeUpdate.assignEdit?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                      ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                      alt="${grouper:message('simpleAttributeUpdate.editAssignmentAlt', true, false)}"
                      onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editAssignmentAlt', true, true)}')" 
                      onmouseout="UnTip()"/></a>
                    <a class="assignmentMenuButton" href="#"
                      ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                      id="assignmentMenuButton_${guiAttributeAssignAssign.attributeAssign.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/></a>
                  </td>
                  <td style="white-space: nowrap;">
                  </td>
                  <td>
                    <grouper:message value="${grouper:escapeHtml(guiAttributeAssignAssign.attributeAssign.attributeDefName.displayExtension)}" 
                      valueTooltip="${grouper:escapeJavascript(guiAttributeAssignAssign.attributeAssign.attributeDefName.displayName)}" />
                  </td>
                  <td>
                    <grouper:message key="${guiAttributeAssignAssign.enabledDisabledKey}"  />
                  </td>
                  <td style="white-space: nowrap;">
                    <%-- loop through the values --%>
                    <c:set var="valueRow" value="0" />
                
                    
                    <c:forEach items="${guiAttributeAssignAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
                    
                      <%-- we need a newline before non-first rows --%>
                      <c:if test="${valueRow != 0}">
                        <br />
                      </c:if>
  
                      <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeUpdate.assignValueDeleteConfirm', true, true)}')) {ajax('SimpleAttributeUpdate.assignValueDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'});} return false;" 
                        ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
                        alt="${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simpleAttributeUpdate.assignDeleteValueAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>
  
                      <a href="#" onclick="ajax('SimpleAttributeUpdate.assignValueEdit?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}&attributeAssignValueId=${attributeAssignValue.id}', {formIds: 'simpleAttributeFilterForm'}); return false;" 
                        ><img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
                        alt="${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, false)}"
                        onmouseover="Tip('${grouper:message('simpleAttributeUpdate.editValueAssignmentAlt', true, true)}')" 
                        onmouseout="UnTip()"/></a>
                      
                      ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                      
                      <c:set var="valueRow" value="${valueRow + 1}" />
                      
                    </c:forEach>
                  
                  </td>
                  <td>
                    <grouper:message value="${grouper:escapeJavascript(guiAttributeAssignAssign.attributeAssign.attributeDef.extension)}" 
                      valueTooltip="${grouper:escapeJavascript(guiAttributeAssignAssign.attributeAssign.attributeDef.name)}" />
                  </td>
                  <td>${grouper:abbreviate(guiAttributeAssignAssign.attributeAssign.id, 8, true, true)}</td>
                </tr>
                
                
                </c:if>
              
              </c:forEach>              
              <c:set var="row" value="${row + 1}" />
            </c:forEach>          
          </table> 
 
          <%-- attach a menu for each row --%>
          <grouper:menu menuId="assignmentMenu"
            operation="SimpleAttributeUpdateMenu.assignmentMenu" 
            structureOperation="SimpleAttributeUpdateMenu.assignmentMenuStructure" 
            contextZoneJqueryHandle=".assignmentMenuButton" contextMenu="true" />
       
          <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
          <%--div class="pagingButtons">
            <grouper:message key="simpleAttributeUpdate.pagingResultPrefix" />
              <grouper:paging showSummaryOrButtons="false" pagingName="simpleAttributeUpdatePrivileges" 
              refreshOperation="SimpleAttributeUpdate.attributeEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&showIndirectPrivileges=${attributeUpdateRequestContainer.showIndirectPrivilegesComputed}" 
               />
          </div --%>
        
        </c:otherwise>
      </c:choose>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/simpleAttributeAssignments.jsp -->
