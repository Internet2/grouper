<header class="pull-left"><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');" title="${textContainer.text['institutionName']}"><img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" style="padding-top: 5px; padding-bottom: 5px" /></a><br />
   <c:if test="${mediaMap['uiV2.disable.hide-show.side.panel']=='false'}">
      <a id="grouperHideSidePanelId" href="#" style="font-size: smaller; color: inherit" onclick="ajax('../app/UiV2Main.grouperHideSidePanel'); return false;">${textContainer.text['grouperHideSidePanel']}</a>
      <a id="grouperShowSidePanelId" href="#" style="display: none; font-size: smaller; color: inherit" onclick="ajax('../app/UiV2Main.grouperShowSidePanel'); return false;">${textContainer.text['grouperShowSidePanel']}</a>
  </c:if>
</header>