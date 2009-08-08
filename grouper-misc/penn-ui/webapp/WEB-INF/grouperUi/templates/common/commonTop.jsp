<%@ include file="../common/commonTaglib.jsp" %>
<div id="topLeftLogo">
  <img src="../public/assets/logo.gif" id="topLeftLogoImage" />
</div>
<div id="topRightLogo">
  <img src="../public/assets/grouper.gif" id="topRightLogoImage" />
</div>
<div id="navbar"> 
     <grouperGui:message key="simpleMembershipUpdate.screenWelcome"/> ${guiSettings.loggedInSubject.subject.description} 
     &nbsp; &nbsp; 
     <a href="#" onclick="return allObjects.pageHelpers.logout();"><img src="../public/assets/logout.gif" border="0" id="logoutImage" 
     alt="${grouperGui:message('simpleMembershipUpdate.logoutImageAlt', true, true) }" /></a>
     <a href="#" onclick="return allObjects.pageHelpers.logout();"><grouperGui:message key="simpleMembershipUpdate.logoutText"/></a>
</div>

