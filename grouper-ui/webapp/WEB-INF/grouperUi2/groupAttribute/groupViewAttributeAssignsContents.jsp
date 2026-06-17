<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<c:choose>
  <c:when test="${fn:length(grouperRequestContainer.groupContainer.guiAttributeAssigns) == 0}">
    ${textContainer.text['groupViewAttributeAssignsNoAssignedAttributes']}
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
             <th>${textContainer.text['groupViewAttributeAssignmentsColumnAction']}</th>
           </tr>
         </thead>
         <tbody>
           <c:set var="i" value="0" />
           <c:forEach items="${grouperRequestContainer.groupContainer.guiAttributeAssigns}" var="guiAttributeAssign" >
             <c:set var="attributeAssign" value="${guiAttributeAssign.attributeAssign}" />
             
             <tr>
               <td style="white-space: nowrap;">${textContainer.text['simpleAttributeUpdate.assignDirect']}</td>
               <td>${guiAttributeAssign.guiAttributeDefName.shortLinkWithIcon}</td>
               <td>${textContainer.text[guiAttributeAssign.enabledDisabledKey]}</td>
               
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
                     <div class="btn-group" style="display: inline-block;">
                       <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="dropdown-toggle grouperDropdownToggleIconOnly"
                         aria-haspopup="true" aria-expanded="false" role="button"
                         onclick="$(this).next('ul').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $(this).next('ul').find('li').first().focus();return true;}); return false;">
                         <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                          alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                       </a>
                       <ul class="dropdown-menu dropdown-menu-right">
                         <li>
                           <a href="#" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignmentValueMenu', {requestParams: {menuItemId: 'editValue', menuIdOfMenuTarget: 'assignmentValueButton_${guiAttributeAssign.attributeAssign.id}_${attributeAssignValue.id}'}}); return false;">${textContainer.text['simpleAttributeUpdate.editValueAssignmentAlt']}</a>
                         </li>
                         <li>
                           <a href="#" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignmentValueMenu', {requestParams: {menuItemId: 'deleteValue', menuIdOfMenuTarget: 'assignmentValueButton_${guiAttributeAssign.attributeAssign.id}_${attributeAssignValue.id}'}}); return false;">${textContainer.text['simpleAttributeUpdate.assignDeleteValueAlt']}</a>
                         </li>
                       </ul>
                     </div>
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
                          aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                        ${textContainer.text['groupViewActionsButton'] } 
                          <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                          <li><a href="?operation=UiV2GroupAttributeAssignment.assignmentMenuAddValue&attributeAssignId=${attributeAssign.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GroupAttributeAssignment.assignmentMenuAddValue&attributeAssignId=${attributeAssign.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddValue'] }</a></li>
                          <li><a href="?operation=UiV2GroupAttributeAssignment.assignmentMenuAddMetadataAssignment&attributeAssignId=${attributeAssign.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GroupAttributeAssignment.assignmentMenuAddMetadataAssignment&attributeAssignId=${attributeAssign.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddMetadataAssignment'] }</a></li>
                          <li><a href="?operation=UiV2GroupAttributeAssignment.assignEdit&attributeAssignId=${attributeAssign.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GroupAttributeAssignment.assignEdit&attributeAssignId=${attributeAssign.id}');">${textContainer.text['simpleAttributeUpdate.editAssignmentAlt'] }</a></li>
                          <li><a href="#" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignDelete?attributeAssignId=${attributeAssign.id}'); return false;" >${textContainer.text['simpleAttributeUpdate.deleteAssignmentAlt'] }</a></li>
                        </ul>
                    </div>
                </c:if>
               </td>
             </tr>
             
             <c:forEach items="${guiAttributeAssign.guiAttributeAssigns}" var="guiAttributeAssignAssign">
               <c:set var="i" value="${i+1}" />
       
               <%-- filter out results which dont match the enabled/disabled filter --%>              
               <c:if test="${attributeUpdateRequestContainer.enabledDisabled == null || (attributeUpdateRequestContainer.enabledDisabled == guiAttributeAssignAssign.attributeAssign.enabled )}" >
               <%-- see if there are assignments on the assignment --%>
                 <tr  ${row % 2 == 1 ? 'class="alternate"' : ''} style="vertical-align: top">
                   <td style="white-space: nowrap;" align="right">
                     <span class="simpleMembershipUpdateDisabled">&nbsp;&nbsp;&nbsp;&nbsp;${textContainer.text['simpleAttributeUpdate.assignMetadata'] }</span>
                   </td>
                
                   <td>${guiAttributeAssignAssign.guiAttributeDefName.shortLinkWithIcon}</td>
                   <td>${textContainer.text[guiAttributeAssignAssign.enabledDisabledKey] }</td>
                   <td style="white-space: nowrap;">
                     <%-- loop through the values --%>
                     <c:set var="valueRow" value="0" />
                     <c:forEach items="${guiAttributeAssignAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">          
                       <%-- we need a newline before non-first rows --%>
                       <c:if test="${valueRow != 0}"><br /></c:if>
                       ${grouper:escapeHtml(attributeAssignValue.valueFriendly)}
                       <c:if test="${guiAttributeAssignAssign.canUpdateAttributeDefName}">
                         <div class="btn-group" style="display: inline-block;">
                           <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="dropdown-toggle grouperDropdownToggleIconOnly"
                             aria-haspopup="true" aria-expanded="false" role="button"
                             onclick="$(this).next('ul').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $(this).next('ul').find('li').first().focus();return true;}); return false;">
                             <img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" 
                              alt="${grouper:escapeJavascript(navMap['contextOptionsAlt'])}"/>
                           </a>
                           <ul class="dropdown-menu dropdown-menu-right">
                             <li>
                               <a href="#" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignmentValueMenu', {requestParams: {menuItemId: 'editValue', menuIdOfMenuTarget: 'assignmentValueButton_${guiAttributeAssignAssign.attributeAssign.id}_${attributeAssignValue.id}'}}); return false;">${textContainer.text['simpleAttributeUpdate.editValueAssignmentAlt']}</a>
                             </li>
                             <li>
                               <a href="#" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignmentValueMenu', {requestParams: {menuItemId: 'deleteValue', menuIdOfMenuTarget: 'assignmentValueButton_${guiAttributeAssignAssign.attributeAssign.id}_${attributeAssignValue.id}'}}); return false;">${textContainer.text['simpleAttributeUpdate.assignDeleteValueAlt']}</a>
                             </li>
                           </ul>
                         </div>
                       </c:if>
                 
                       <c:set var="valueRow" value="${valueRow + 1}" />
                 
                     </c:forEach>
             
                   </td>
                   <td>${guiAttributeAssignAssign.guiAttributeDef.shortLinkWithIcon}</td>
                   <td>
                    <c:if test="${guiAttributeAssignAssign.canUpdateAttributeDefName}">
                     <div class="btn-group">
                        <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                          aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                        ${textContainer.text['groupViewActionsButton'] }
                          <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                          <li><a href="?operation=UiV2GroupAttributeAssignment.assignmentMenuAddValue&attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GroupAttributeAssignment.assignmentMenuAddValue&attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}');">${textContainer.text['simpleAttributeAssign.assignMenuAddValue'] }</a></li>
                          <li><a href="?operation=UiV2GroupAttributeAssignment.assignEdit&attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GroupAttributeAssignment.assignEdit&attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}');">${textContainer.text['simpleAttributeUpdate.editAssignmentAlt'] }</a></li>
                          <li><a href="#" onclick="ajax('../app/UiV2GroupAttributeAssignment.assignDelete?attributeAssignId=${guiAttributeAssignAssign.attributeAssign.id}'); return false;" >${textContainer.text['simpleAttributeUpdate.deleteAssignmentAlt'] }</a></li>
                        </ul>
                      </div>
                    </c:if>
                   </td>
                 </tr>
               </c:if>
             </c:forEach>
             <c:set var="i" value="${i+1}" />
           </c:forEach>
         </tbody>
       </table>
  
  </c:otherwise>
</c:choose>