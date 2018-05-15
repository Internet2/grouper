<%@ include file="../common/commonTaglib.jsp" %>

<%-- note this wont work for token per page --%>
<input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>


<div id="topLeftLogo">
  <img src="../../${mediaMap['image.organisation-logo']}" id="topLeftLogoImage" />
</div>
<div id="topRightLogo">
  <img src="../../${mediaMap['image.grouper-logo']}" id="topRightLogoImage" />
</div>
<div id="navbar">
  <table cellpadding="0" cellspacing="0" border="0" width="1080">
    <tr>
      <td width="200" style="padding-left: 20px; white-space: nowrap"><a id="menuLink" href="#"><grouper:message key="mainMenu.link"/></a>
        <%-- register the menu, and attach it to the link --%>
        <grouper:menu menuId="liteMenu"
        operation="MiscMenu.miscMenu" 
        structureOperation="MiscMenu.miscMenuStructure" 
        contextZoneJqueryHandle="#menuLink" contextMenu="true" />
      </td>
      <td align="right">     
        <grouper:message key="simpleMembershipUpdate.screenWelcome"/> ${grouper:abbreviate(guiSettings.loggedInSubject.subject.description, 125, true, true)} 
        <c:if test="${mediaMap['logout.link.show']=='true'}">
         &nbsp; &nbsp; 
         <a href="#" onclick="if (confirm('${grouper:message('simpleMembershipUpdate.confirmLogout', true, true) }')) {location.href = 'grouper.html?operation=Misc.logout'; } return false;"
         ><img src="../../grouperExternal/public/assets/images/logout.gif" border="0" id="logoutImage" 
         alt="${grouper:message('simpleMembershipUpdate.logoutImageAlt', true, true) }" /></a>
         
         <a href="#" 
         onclick="if (confirm('${grouper:message('simpleMembershipUpdate.confirmLogout', true, true) }')) {location.href = 'grouper.html?operation=Misc.logout'; } return false;"><grouper:message key="simpleMembershipUpdate.logoutText"/></a>
       </c:if>
      </td>
    </tr>
  
  </table>
    </div>
