<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- Le styles -->
<link href="../../grouperExternal/public/assets/css/grouperTooltip.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/bootstrap.css?updated=02132013_2" rel="stylesheet">
<style>
.container-fluid {
  max-width: ${grouperRequestContainer.indexContainer.containerFluidMaxWidth};
}
</style>
<link href="../../grouperExternal/public/assets/css/responsive.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/font-awesome.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/tom-select.v2.4.3.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/jstree_themes/default/style.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/grouperUi2.css?updated=01012026" rel="stylesheet">

<link href="../../grouperExternal/public/assets/css/c3.min.css" rel="stylesheet" type="text/css">

<c:if test="${!empty mediaNullMap['css.additional']}">
  <c:forTokens var="cssRef" items="${mediaNullMap['css.additional']}" delims=" ">
    <link href="${grouper:escapeHtml(cssRef)}" rel="stylesheet" type="text/css" />
  </c:forTokens>
</c:if>

<script src="../../grouperExternal/public/OwaspJavaScriptServlet"></script>

<script src="../../grouperExternal/public/assets/js/d3.min.js" charset="utf-8"></script>
<script src="../../grouperExternal/public/assets/js/c3.min.js"></script>
<!--<script src="../../grouperExternal/public/assets/js/gantt-chart-d3v21.js" charset="utf-8"></script>-->

<script src="../../grouperExternal/public/assets/js/viz.v1.8.2.js"></script><!-- type="javascript/worker" -- errors since called by anonymous root-level blob (blob:http://localhost:8080/37783c21-4aad-4ab0-9ec4-c500040ac16e:4:21) -->
<!--<script src="../../grouperExternal/public/assets/js/viz.js.v2.0.0.full.render.js" type="application/javascript"></script>-->
<script src="../../grouperExternal/public/assets/js/d3-graphviz.v2.6.1.min.js"></script>

<script src="../../grouperExternal/public/assets/js/tom-select.complete.min.v2.4.3.js"></script>


<!-- HTML5 shim, for IE6-8 support of HTML elements -->
<!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<link rel="shortcut icon" href="../../grouperExternal/public/assets/images/favicon.ico">

<script>
    
    grouperCsrfText="${textContainer.textEscapeSingle['guiErrorCsrfAlert']}";
    
</script>

<script src="../../grouperExternal/public/assets/js/jquery.js?ver=3.7.1"></script>
<script src="../../grouperExternal/public/assets/js/jquery-migrate.js"></script>
<script src="../../grouperExternal/public/assets/js/bootstrap.js"></script>