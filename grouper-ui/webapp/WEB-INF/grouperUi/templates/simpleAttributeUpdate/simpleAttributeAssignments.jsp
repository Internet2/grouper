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

               <img src="" height="14px" border="0" 
              alt=""/>
              <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" id="assignMenuButton_${guiAttributeAssign.attributeAssign.id}"
            alt=""/></td>
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
                  <img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
            alt=""/>
               <img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
              alt=""/> Value1<br />
                  <img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
            alt=""/>
               <img src="../../grouperExternal/public/assets/images/application_edit.png" height="14px" border="0" 
              alt=""/> Value2<br />
                
                </td>
                <td>
                  <grouper:message value="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDef.name)}" 
                    valueTooltip="${grouper:escapeJavascript(guiAttributeAssign.attributeAssign.attributeDefName.displayName)}" />
                </td>
                <td>${grouper:abbreviate(guiAttributeAssign.attributeAssign.id, 8, true, true)}</td>
              </tr>
              <c:set var="row" value="${row + 1}" />
            </c:forEach>          
          </table> 
 
          <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
          <%--div class="pagingButtons">
            <grouper:message key="simpleAttributeUpdate.pagingResultPrefix" />
              <grouper:paging showSummaryOrButtons="false" pagingName="simpleAttributeUpdatePrivileges" 
              refreshOperation="SimpleAttributeUpdate.attributeEditPanelPrivileges?attributeDefToEditId=${attributeUpdateRequestContainer.attributeDefToEdit.id}&showIndirectPrivileges=${attributeUpdateRequestContainer.showIndirectPrivilegesComputed}" 
               />
          </div --%>
        
        </c:otherwise>
      </c:choose>
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeUpdate/simpleAttributeAssignments.jsp -->
