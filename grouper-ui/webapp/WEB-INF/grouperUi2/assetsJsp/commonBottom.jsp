
<%-- note this wont work for token per page --%>
<input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>


<%-- note: dojo and jquery script are included in commonHead so we can call dojo things in the screen --%>

<script src="../../grouperExternal/public/assets/js/tree.jquery.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.dataTables.min.js"></script>
<script src="../../grouperExternal/public/assets/js/footable-0.1.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.cookie.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.form.js"></script>
<script src="../../grouperExternal/public/assets/js/grouper.js"></script>
<script src="../../grouperExternal/public/assets/js/native.history.js"></script>
<script src="../../grouperExternal/public/assets/js/grouperUi.js?ver=4.27.0"></script>

<%-- security patch, URL operation guard (see grouperUiUrlOperationGuard.js): a link or refresh may only load a screen,
     not run an action.  Same grouper-ui config keys as GRP-7503 (uiV2.urlOperationGuard.mode and
     uiV2.urlOperationGuard.additionalAllowedOperations) so settings carry over when you upgrade.
     Only fixed words and Class.method names (letters, digits, one dot) are written into the script. --%>
<script type="text/javascript">
<%
  {
    // fail closed: enforce unless the config clearly says logOnly or off
    String grouperUrlOpGuardModeJs = "enforce";
    StringBuilder grouperUrlOpGuardExtraJs = new StringBuilder();
    try {
      edu.internet2.middleware.grouper.ui.util.GrouperUiConfig grouperUrlOpGuardConfig = 
          edu.internet2.middleware.grouper.ui.util.GrouperUiConfig.retrieveConfig();
      String grouperUrlOpGuardMode = grouperUrlOpGuardConfig.propertyValueString("uiV2.urlOperationGuard.mode", "enforce");
      grouperUrlOpGuardMode = grouperUrlOpGuardMode == null ? "" : grouperUrlOpGuardMode.trim();
      if ("logOnly".equalsIgnoreCase(grouperUrlOpGuardMode)) {
        grouperUrlOpGuardModeJs = "logOnly";
      } else if ("off".equalsIgnoreCase(grouperUrlOpGuardMode)) {
        grouperUrlOpGuardModeJs = "off";
      }
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
      // keep enforce with no extra operations
      grouperUrlOpGuardExtraJs.setLength(0);
    }
    out.print("  var grouperUrlOperationGuardConfig = {mode: '" + grouperUrlOpGuardModeJs 
        + "', additionalAllowedOperations: [" + grouperUrlOpGuardExtraJs + "]};");
  }
%>
</script>
<script src="../../grouperExternal/public/assets/js/grouperUiUrlOperationGuard.js?updated=20261111"></script>

<script src="../../grouperExternal/public/assets/js/wz_tooltip.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.blockUI.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.simplemodal.js"></script>
<script src="../../grouperExternal/public/assets/nifty/niftycube.js"></script>
<script src="../../grouperExternal/public/assets/js/bootstrap-datepicker.min.js"></script>

<script type="text/javascript">
  $().ajaxStop($.unblockUI); 
  $.blockUI.defaults.message = "<img src='../../grouperExternal/public/assets/images/busy.gif' alt='busy'/>";
  $.blockUI.defaults.css.border = 'none';
  $.blockUI.defaults.css.backgroundColor = 'transparent';
  $.blockUI.defaults.overlayCSS.opacity = '0.02';
  $.blockUI.defaults.fadeIn = '200';
  $.blockUI.defaults.fadeOut = '400';
  $.blockUI.defaults.timeout = '180000';
  //MCH 20132626: this used to be 1000, but the UI v2 modal window is zindex 1050
  $.blockUI.defaults.baseZ = '10000';
  
</script>

<script>
  $( document ).ready(function() {
    dojo.parser.parse();
  });
 
</script>
