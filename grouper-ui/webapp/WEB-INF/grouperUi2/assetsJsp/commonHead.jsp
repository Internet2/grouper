<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- Le styles -->
<link href="../../grouperExternal/public/assets/dojo/dijit/themes/claro/claro.css" rel="stylesheet" type="text/css" />
<link href="../../grouperExternal/public/assets/dojo/dojo/resources/dojo.css" rel="stylesheet" type="text/css" />
<link href="../../grouperExternal/public/assets/css/grouperTooltip.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/bootstrap.css?updated=02132013_2" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/responsive.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/font-awesome.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/grouperUi2.css" rel="stylesheet">
<link href="../../grouperExternal/public/assets/css/bootstrap-datepicker.min.css" rel="stylesheet">

<c:if test="${!empty mediaNullMap['css.additional']}">
  <c:forTokens var="cssRef" items="${mediaNullMap['css.additional']}" delims=" ">
    <link href="${grouper:escapeHtml(cssRef)}" rel="stylesheet" type="text/css" />
  </c:forTokens>
</c:if>

<script src="../../grouperExternal/public/assets/js/URI.js"></script>
<script src="../../grouperExternal/public/OwaspJavaScriptServlet"></script>

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<link rel="shortcut icon" href="../../grouperExternal/public/assets/images/favicon.ico">

<script>

    dojoConfig= {
        parseOnLoad: false,
        async: false
    };
    
    grouperCsrfText="${textContainer.textEscapeSingle['guiErrorCsrfAlert']}";
    
</script>
<%-- dojo is up in commonHead, everything else is in commonBottom.jsp --%>
<script src="../../grouperExternal/public/assets/dojo/dojo/dojo.js"></script>

<script type="text/javascript" >

//require(["dojo/ready", "dijit/registry", "dojo/parser", "dojo/json", 
//         "dojo/_base/config", "dijit/Dialog", "dojo/domReady!", "dijit/form/FilteringSelect", 
//         "dojox/data/QueryReadStore", "dojo/dom-attr", "dijit/Tree", "dojo/data/ItemFileReadStore", 
//         "dojo/store/JsonRest", "dojo/_base/declare", "dijit/form/ComboBox"]);

//grouper is the dojo built file so there arent as many downloads
require(["grouper/grouperDojo"]);
</script>

<script src="../../grouperExternal/public/assets/js/jquery.js"></script>
