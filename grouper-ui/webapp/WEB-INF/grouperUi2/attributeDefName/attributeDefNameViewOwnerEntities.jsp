<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcommon.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcombo.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcalendar.js"></script>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxmenu.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/menu/ext/dhtmlxmenu_ext.js"></script>
    <link rel="stylesheet" type="text/css" href="../../grouperExternal/public/assets/dhtmlx/menu/skins/dhtmlxmenu_dhx_blue.css" />

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/ext/dhtmlxcombo_extra.js"></script>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />


    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.attributeDefNameContainer.guiAttributeAssignFinderResults.guiAttributeAssignFinderResults) == 0}">
        ${textContainer.text['groupViewAttributeAssignsNoAssignedAttributes']}
      </c:when>
      <c:otherwise>
      
      <form class="form-inline form-small form-filter" id="attributeDefNameOwnersFilterFormId">
      <div class="row-fluid">
         <div class="span3" id="folderFilterForDiv">
           <label id="folderFilterLabel" for="folder-filter">${textContainer.text['simpleAttributeUpdate.folderFilterLabel'] }</label>
         </div>
         <div class="span5">
           <grouper:combobox2 idBase="folderCombo" style="width: 30em" 
             filterOperation="../app/UiV2Stem.createAttributeDefParentFolderFilter"
             value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.objectStemId)}"
           />
         </div>
         
         <div class="span3" id="groupFilterSubmitDiv"><input type="submit" class="btn" aria-controls="groupFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
             onclick="ajax('../app/UiV2AttributeDefName.viewAttributeDefNameAssignedOwners?attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}', {formIds: 'attributeDefNameOwnersFilterFormId, groupPagingFormId'}); return false;"> 
           <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#custom-composite-filter').val('nothing'); $('#enabled-disabled-filter').val('nothing'); $('#pit-filter').val('no'); $('#pit-filter').trigger('change'); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
         </div>
         
       </div>
       </form>
      
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
             <thead>        
               <tr>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderOwnerEntity']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAssignmentType']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeName']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderEnabled']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderValues']}</th>
                 <th>${textContainer.text['groupViewAttributeAssignmentsColumnAction']}</th>
               </tr>
             </thead>
             <tbody>
               <c:set var="i" value="0" />
               <c:forEach items="${grouperRequestContainer.attributeDefNameContainer.guiAttributeAssignFinderResults.guiAttributeAssignFinderResults}" var="guiAttributeAssignFinderResult" >
                 
                 <c:set var="attributeAssign" value="${guiAttributeAssignFinderResult.guiAttributeAssign.attributeAssign}" />
                 <c:set var="guiAttributeAssign" value="${guiAttributeAssignFinderResult.guiAttributeAssign}" />
                 
                 <tr>
                    
                    <c:choose>
                     <c:when test="${not empty guiAttributeAssignFinderResult.ownerGuiGroup}">
                       <td>${guiAttributeAssignFinderResult.ownerGuiGroup.shortLinkWithIcon}</td>
                     </c:when>
                     <c:when test="${not empty guiAttributeAssignFinderResult.ownerGuiStem}">
                       <td>${guiAttributeAssignFinderResult.ownerGuiStem.shortLinkWithIcon}</td>
                     </c:when>
                     <c:when test="${not empty guiAttributeAssignFinderResult.ownerGuiAttributeDef}">
                       <td>${guiAttributeAssignFinderResult.ownerGuiAttributeDef.shortLinkWithIcon}</td>
                     </c:when>
                     <c:when test="${not empty guiAttributeAssignFinderResult.ownerGuiMember}">
                       <td>${guiAttributeAssignFinderResult.ownerGuiMember.shortLinkWithIcon}</td>
                     </c:when>
                     <c:otherwise>
                      <td></td>
                     </c:otherwise>
                   </c:choose>
                    
                    <c:if test="${empty guiAttributeAssignFinderResult.attributeAssignFinderResult.ownerAttributeAssign}">
                      <td style="white-space: nowrap;">${textContainer.text['simpleAttributeUpdate.assignDirect']}</td>
                    </c:if>
                    <c:if test="${not empty guiAttributeAssignFinderResult.attributeAssignFinderResult.ownerAttributeAssign}">
                      <td style="white-space: nowrap;">&nbsp;&nbsp;&nbsp;&nbsp;${textContainer.text['simpleAttributeUpdate.assignMetadata']}</td>
                    </c:if>
                   
                   <td>${guiAttributeAssign.guiAttributeDefName.shortLinkWithIcon}</td>

                   <td>${textContainer.text[guiAttributeAssign.enabledDisabledKey]}</td>
                   
                   <td style="white-space: nowrap;">
                     <%-- loop through the values --%>
                     <c:set var="valueRow" value="0" />
                 
                     
                     <c:forEach items="${guiAttributeAssignFinderResult.attributeAssignFinderResult.attributeAssignValues}" var="attributeAssignValue">
                     
                       <%-- we need a newline before non-first rows --%>
                       <c:if test="${valueRow != 0}">
                         <br />
                       </c:if>
   
                       ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                       <c:if test="${guiAttributeAssign.canUpdateAttributeDefName}">
                         <a class="assignmentValueButton" href="#">
                           <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                            id="assignmentValueButton_${guiAttributeAssign.attributeAssign.id}_${attributeAssignValue.id}_${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                         </a>
                       </c:if>
                       
                       <c:set var="valueRow" value="${valueRow + 1}" />
                     </c:forEach>
                   
                   </td>
                   <%-- <td>
                     ${guiAttributeAssign.guiAttributeDef.shortLinkWithIcon}
                   </td> --%>
                   <td>
                    <c:if test="${guiAttributeAssign.canUpdateAttributeDefName}">
                      <div class="btn-group">
                          <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                            aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                          ${textContainer.text['groupViewActionsButton'] } 
                            <span class="caret"></span>
                          </a>
                          <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                            <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDefName.assignmentMenuAddValue&attributeAssignId=${attributeAssign.id}&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddValue'] }</a></li>
                            <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDefName.assignmentMenuAddMetadataAssignment&attributeAssignId=${attributeAssign.id}&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddMetadataAssignment'] }</a></li>
                            <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDefName.assignEdit&attributeAssignId=${attributeAssign.id}&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');">${textContainer.text['simpleAttributeUpdate.editAssignmentAlt'] }</a></li>
                            <li><a href="#" onclick="ajax('../app/UiV2AttributeDefName.assignDelete?attributeAssignId=${attributeAssign.id}&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;" >${textContainer.text['simpleAttributeUpdate.deleteAssignmentAlt'] }</a></li>
                          </ul>
                      </div>
                    </c:if>
                   </td>
                 </tr>
                 <c:set var="i" value="${i+1}" />
               </c:forEach>
             </tbody>
           </table>
         
         <%-- attach a menu for each limit value --%>
         <grouper:menu menuId="assignmentValueMenu"
           operation="UiV2AttributeDefName.assignmentValueMenu"
           structureOperation="UiV2AttributeDefName.assignmentValueMenuStructure" 
           contextZoneJqueryHandle=".assignmentValueButton" contextMenu="true" />
         
         <div class="data-table-bottom gradient-background">
           <grouper:paging2 guiPaging="${grouperRequestContainer.attributeDefNameContainer.guiPaging}" formName="attributeDefNameOwnersPagingForm"
             refreshOperation="../app/UiV2AttributeDefName.viewAttributeDefNameAssignedOwners?attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}" />
         </div>
      
      </c:otherwise>
    </c:choose>
