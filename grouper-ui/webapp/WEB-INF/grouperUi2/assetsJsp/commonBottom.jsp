<%-- note this wont work for token per page --%>
<input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>


<%-- note: jquery script are included in commonHead --%>

<script src="../../grouperExternal/public/assets/js/footable-0.1.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.form.js"></script>
<script src="../../grouperExternal/public/assets/js/jstree.min.js"></script>
<script src="../../grouperExternal/public/assets/js/grouper.js"></script>
<script src="../../grouperExternal/public/assets/js/native.history.js"></script>
<script src="../../grouperExternal/public/assets/js/grouperUi.js?ver=7.10.1"></script>

<%-- security patch, URL operation guard (see grouperUiUrlOperationGuard.js): a link or refresh may only load a screen,
     not run an action.  The guard always enforces; a screen missing from the allowlist is added with the
     grouper-ui config key uiV2.urlOperationGuard.additionalAllowedOperations (GRP-7503).
     Only fixed words and Class.method names (letters, digits, one dot) are written into the script. --%>
<script type="text/javascript">
<%
  {
    StringBuilder grouperUrlOpGuardExtraJs = new StringBuilder();
    try {
      edu.internet2.middleware.grouper.ui.util.GrouperUiConfig grouperUrlOpGuardConfig = 
          edu.internet2.middleware.grouper.ui.util.GrouperUiConfig.retrieveConfig();
      String grouperUrlOpGuardExtra = grouperUrlOpGuardConfig.propertyValueString("uiV2.urlOperationGuard.additionalAllowedOperations", "");
      if (grouperUrlOpGuardExtra != null) {
        for (String grouperUrlOpGuardOperation : grouperUrlOpGuardExtra.split(",")) {
          grouperUrlOpGuardOperation = grouperUrlOpGuardOperation.trim();
          // only Class.method, so nothing from config can break out of the javascript string
          if (grouperUrlOpGuardOperation.matches("[A-Za-z0-9]+\\.[A-Za-z0-9]+")) {
            grouperUrlOpGuardExtraJs.append(grouperUrlOpGuardExtraJs.length() == 0 ? "" : ", ")
                .append("'").append(grouperUrlOpGuardOperation).append("'");
          }
        }
      }
    } catch (RuntimeException grouperUrlOpGuardException) {
      // no extra operations
      grouperUrlOpGuardExtraJs.setLength(0);
    }
    out.print("  var grouperUrlOperationGuardConfig = {additionalAllowedOperations: [" + grouperUrlOpGuardExtraJs + "]};");
  }
%>
</script>
<script src="../../grouperExternal/public/assets/js/grouperUiUrlOperationGuard.js?updated=20261127"></script>

<script src="../../grouperExternal/public/assets/js/jquery.blockUI.js"></script>
<script src="../../grouperExternal/public/assets/nifty/niftycube.js"></script>

<script type="text/javascript">
  var grouperCopiedToClipboardText = "${textContainer.text['copiedToClipboardMessage']}";
  $().ajaxStop($.unblockUI);
  // a11y item 11: the blockUI spinner is the only loading indicator during ajax. Wrap it in a
  // role=status live region with visually-hidden "Loading" text so screen readers announce the
  // loading state, and make the spinner image decorative (empty alt) so it is not read as "busy".
  $.blockUI.defaults.message = "<div role='status'><img src='../../grouperExternal/public/assets/images/busy.gif' alt=''/><span class='visually-hidden'>${textContainer.textEscapeXml['guiLoading']}</span></div>";
  $.blockUI.defaults.css.border = 'none';
  $.blockUI.defaults.css.backgroundColor = 'transparent';
  $.blockUI.defaults.overlayCSS.opacity = '0.02';
  $.blockUI.defaults.fadeIn = '200';
  $.blockUI.defaults.fadeOut = '400';
  $.blockUI.defaults.timeout = '180000';
  //MCH 20132626: this used to be 1000, but the UI v2 modal window is zindex 1050
  $.blockUI.defaults.baseZ = '10000';
  
</script>