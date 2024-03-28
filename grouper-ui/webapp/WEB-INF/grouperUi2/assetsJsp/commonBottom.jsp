
<%-- note this wont work for token per page --%>
<input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>


<%-- note: dojo and jquery script are included in commonHead so we can call dojo things in the screen --%>

<script src="../../grouperExternal/public/assets/js/footable-0.1.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.cookie.jsc"></script>
<script src="../../grouperExternal/public/assets/js/jquery.form.js?ver=4.12.1a"></script>
<script src="../../grouperExternal/public/assets/js/grouper.js?ver=4.12.1d"></script>
<script src="../../grouperExternal/public/assets/js/native.history.js"></script>
<script src="../../grouperExternal/public/assets/js/grouperUi.js?ver=4.12.1"></script>
<script src="../../grouperExternal/public/assets/js/wz_tooltip.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.blockUI.js?ver=4.12.1"></script>
<script src="../../grouperExternal/public/assets/js/jquery.simplemodal.js?ver=4.12.1"></script>
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
