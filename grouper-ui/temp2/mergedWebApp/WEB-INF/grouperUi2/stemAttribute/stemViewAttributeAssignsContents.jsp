<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcommon.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcombo.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcalendar.js"></script>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxmenu.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/menu/ext/dhtmlxmenu_ext.js"></script>
    <link rel="stylesheet" type="text/css" href="../../grouperExternal/public/assets/dhtmlx/menu/skins/dhtmlxmenu_dhx_blue.css" />

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/ext/dhtmlxcombo_extra.js"></script>

                <form id="stemQuerySortAscendingFormId">
                  <input type="hidden" name="querySortAscending" value="${grouperRequestContainer.stemContainer.guiSorting.ascending}" /> 
                </form>
                
    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.stemContainer.guiAttributeAssigns) == 0}">
        ${textContainer.text['stemViewAttributeAssignsNoAssignedAttributes']}
      </c:when>
      <c:otherwise>
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
             <thead>        
               <tr>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAssignmentType']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeName']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderEnabled']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderValues']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeDef']}</th>
                 <th>${textContainer.text['stemViewAttributeAssignmentsColumnAction']}</th>
               </tr>
             </thead>
             <tbody>
               <c:set var="i" value="0" />
               <c:forEach items="${grouperRequestContainer.stemContainer.guiAttributeAssigns}" var="guiAttributeAssign" >
                 <c:set var="attributeAssign" value="${guiAttributeAssign.attributeAssign}" />
                 
                 <tr>
                   <td style="white-space: nowrap;">${textContainer.text['simpleAttributeUpdate.assignDirect']}</td>
                  
                   <td>${guiAttributeAssign.guiAttributeDefName.shortLinkWithIcon}</td>
                   <td>
                     ${textContainer.text[guiAttributeAssign.enabledDisabledKey]}
                   </td>
                   
                   <td style="white-space: nowrap;">
                     <%-- loop through the values --%>
                     <c:set var="valueRow" value="0" />
                 
                     
                     <c:forEach items="${guiAttributeAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
                     
                       <%-- we need a newline before non-first rows --%>
                       <c:if test="${valueRow != 0}">
                         <br />
                       </c:if>
   
                       ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                       <c:if test="${guiAttributeAssign.canUpdateAttributeDefName}">
	                       <a class="assignmentValueButton" href="#">
	                         <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
	                          id="assignmentValueButton_${guiAttributeAssign.attributeAssign.id}_${attributeAssignValue.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
	                       </a>
                       </c:if>
                       
                       <c:set var="valueRow" value="${valueRow + 1}" />
                     </c:forEach>
                   
                   </td>
                   <td>
                     ${guiAttributeAssign.guiAttributeDef.shortLinkWithIcon}
                   </td>
                   <td>
                     <c:if test="${guiAttributeAssign.canUpdateAttributeDefName}">
	                     <div class="btn-group">
	                        <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
	                          aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
	                        ${textContainer.text['stemViewActionsButton'] } 
	                          <span class="caret"></span>
	                        </a>
	                        <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
	                          <li><a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.assignmentMenuAddValue&attributeAssignId=${attributeAssign.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddValue'] }</a></li>
	                          <li><a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.assignmentMenuAddMetadataAssignment&attributeAssignId=${attributeAssign.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddMetadataAssignment'] }</a></li>
	                          <li><a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.assignEdit&attributeAssignId=${attributeAssign.id}');">${textContainer.text['simpleAttributeUpdate.editAssignmentAlt'] }</a></li>
	                          <li><a href="#" onclick="ajax('../app/UiV2StemAttributeAssignment.assignDelete?attributeAssignId=${attributeAssign.id}'); return false;" >${textContainer.text['simpleAttributeUpdate.deleteAssignmentAlt'] }</a></li>
	                        </ul>
	                      </div>
                      </c:if>
                   </td>
                 </tr>
                 
                 <c:forEach items="${guiAttributeAssign.guiAttributeAssigns}" var="guiAttributeAssignAssign">
                    <c:set var="i" value="${i+1}"></c:set>
           
                   <%-- filter out results which dont match the enabled/disabled filter --%>              
                   <c:if test="${attributeUpdateRequestContainer.enabledDisabled == null || (attributeUpdateRequestContainer.enabledDisabled == guiAttributeAssignAssign.attributeAssign.enabled )}" >
                   <%-- see if there are assignments on the assignment --%>
                     <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                       <td style="white-space: nowrap;" align="right">
                         <span class="simpleMembershipUpdateDisabled">&nbsp;&nbsp;&nbsp;&nbsp;${textContainer.text['simpleAttributeUpdate.assignMetadata']}</span>
                       </td>
                    
                       <td>${guiAttributeAssignAssign.guiAttributeDefName.shortLinkWithIcon}</td>
                       <td>${textContainer.text[guiAttributeAssignAssign.enabledDisabledKey]}</td>
                       <td style="white-space: nowrap;">
                         <%-- loop through the values --%>
                         <c:set var="valueRow" value="0" />
                         <c:forEach items="${guiAttributeAssignAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">          
                           <%-- we need a newline before non-first rows --%>
                           <c:if test="${valueRow != 0}"><br /></c:if>
                           
                           ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                           <c:if test="${guiAttributeAssign.canUpdateAttributeDefName}">
	                           <a class="assignmentValueButton" href="#">
	                             <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
	                              id="assignmentValueButton_${guiAttributeAssignAssign.attributeAssign.id}_${attributeAssignValue.id}" alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
	                           </a>
                           </c:if>
                 
                           <c:set var="valueRow" value="${valueRow + 1}" />
                 
                         </c:forEach>
             
                       </td>
                       <td>${guiAttributeAssignAssign.guiAttributeDef.shortLinkWithIcon}</td>
                       <td>
                       <c:if test="${guiAttributeAssign.canUpdateAttributeDefName}">
                         <div class="btn-group">
	                         <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
	                           aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
	                           ${textContainer.text['stemViewActionsButton'] } 
	                           <span class="caret"></span>
	                         </a>
	                         <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
	                           <li><a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.assignmentMenuAddValue&attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddValue'] }</a></li>
	                           <li><a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.assignEdit&attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}');">${textContainer.text['simpleAttributeUpdate.editAssignmentAlt'] }</a></li>
	                           <li><a href="#" onclick="ajax('../app/UiV2StemAttributeAssignment.assignDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}'); return false;" >${textContainer.text['simpleAttributeUpdate.deleteAssignmentAlt'] }</a></li>
	                         </ul>
                         </div>
                       </c:if>
                       </td>
                     </tr>
                   </c:if>
                 </c:forEach>
               </c:forEach>
             </tbody>
           </table>
         
         <%-- attach a menu for each limit value --%>
         <grouper:menu menuId="assignmentValueMenu"
           operation="UiV2StemAttributeAssignment.assignmentValueMenu"
           structureOperation="UiV2StemAttributeAssignment.assignmentValueMenuStructure" 
           contextZoneJqueryHandle=".assignmentValueButton" contextMenu="true" />
      
      </c:otherwise>
    </c:choose>
