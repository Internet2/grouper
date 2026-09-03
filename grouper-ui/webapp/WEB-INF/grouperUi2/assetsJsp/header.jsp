<header class="pull-left"><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');" aria-label="${textContainer.textEscapeXml['ariaLabelGrouperHome']}" title="${textContainer.text['institutionName']}"><img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="" style="padding-top: 5px; padding-bottom: 5px" /></a><br />
   <c:if test="${mediaMap['uiV2.disable.hide-show.side.panel']=='false'}">
      <button type="button" id="grouperHideSidePanelId" class="grouper-linkbutton" style="font-size: smaller" onclick="ajax('../app/UiV2Main.grouperHideSidePanel'); return false;">${textContainer.text['grouperHideSidePanel']}</button>
      <button type="button" id="grouperShowSidePanelId" class="grouper-linkbutton" style="display: none; font-size: smaller" onclick="ajax('../app/UiV2Main.grouperShowSidePanel'); return false;">${textContainer.text['grouperShowSidePanel']}</button>
  </c:if>
</header>