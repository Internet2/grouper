<%@ include file="../common/commonTaglib.jsp" %>
<div id="topLeftLogo">
  <img src="../../${mediaMap['image.organisation-logo']}" id="topLeftLogoImage" />
</div>
<div id="topRightLogo">
  <img src="../../${mediaMap['image.grouper-logo']}" id="topRightLogoImage" />
</div>
<div id="navbar"><table border="0" cellpadding="0" cellspacing="0"><tr><td style="white-space: nowrap;"> 
     <grouper:message key="simpleMembershipUpdate.screenWelcome"/> ${grouper:abbreviate(grouperLoginId, 125, true, true)} 
     <c:if test="${mediaMap['logout.link.show']=='true'}">
       &nbsp; &nbsp; 
       <a href="#" onclick="if (confirm('${grouper:message('simpleMembershipUpdate.confirmLogout', true, true) }')) {location.href = 'grouper.html?operation=ExternalSubjectSelfRegister.logout'; } return false;"
       ><img src="../../grouperExternal/public/assets/images/logout.gif" border="0" id="logoutImage" 
       alt="${grouper:message('simpleMembershipUpdate.logoutImageAlt', true, true) }" /></a>
       
       <a href="#" 
       onclick="if (confirm('${grouper:message('simpleMembershipUpdate.confirmLogout', true, true) }')) {location.href = 'grouper.html?operation=ExternalSubjectSelfRegister.logout'; } return false;"><grouper:message key="simpleMembershipUpdate.logoutText"/></a>
     </c:if>
     </td></tr></table></div>

