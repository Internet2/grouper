<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcommon.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcombo.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcalendar.js"></script>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxmenu.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/menu/ext/dhtmlxmenu_ext.js"></script>
    <link rel="stylesheet" type="text/css" href="../../grouperExternal/public/assets/dhtmlx/menu/skins/dhtmlxmenu_dhx_blue.css" />

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/ext/dhtmlxcombo_extra.js"></script>

    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.membershipGuiContainer.guiAttributeAssigns) == 0}">
        ${textContainer.text['membershipViewAttributeAssignsNoAssignedAttributes']}
      </c:when>
      <c:otherwise>
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
             <thead>        
               <tr>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAssignmentType']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeName']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderGroup']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderEntity']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderEnabled']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderValues']}</th>
                 <th>${textContainer.text['simpleAttributeUpdate.assignHeaderAttributeDef']}</th>
               </tr>
             </thead>
             <tbody>
               <c:set var="i" value="0" />
               <c:forEach items="${grouperRequestContainer.membershipGuiContainer.guiAttributeAssigns}" var="guiAttributeAssign" >
                 <c:set var="attributeAssign" value="${guiAttributeAssign.attributeAssign}" />
                 
                 <tr>
                   <td style="white-space: nowrap;">${textContainer.text['simpleAttributeUpdate.assignDirect']}</td>
                   <td>${guiAttributeAssign.guiAttributeDefName.shortLinkWithIcon}</td>
                   <td>${grouperRequestContainer.groupContainer.guiGroup.shortLinkWithIcon}</td>
                   <td>${guiAttributeAssign.guiMember.shortLink}</td>
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
                       
                       <c:set var="valueRow" value="${valueRow + 1}" />
                     </c:forEach>
                   
                   </td>
                   <td>
                     ${guiAttributeAssign.guiAttributeDef.shortLinkWithIcon}
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
                 
                           <c:set var="valueRow" value="${valueRow + 1}" />
                 
                         </c:forEach>
             
                       </td>
                       <td>${guiAttributeAssignAssign.guiAttributeDef.shortLinkWithIcon}</td>
                     </tr>
                   </c:if>
                 </c:forEach>
                 <c:set var="i" value="${i+1}" />
               </c:forEach>
             </tbody>
           </table>
         
      </c:otherwise>
    </c:choose>
