---
title: "Grouper UI v2.2 dojo"
space: GrIntDev
pageId: 48793257
version: 32
lastUpdated: 2026-07-12T06:46:10.847Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793257/Grouper+UI+v2.2+dojo
---

### Version

Started with Dojo Toolkit 1.9.1.43d05c6, originally combos were in 1.8.3.30226. Note: look in dojo/dojo.js.uncompressed.js to see which version it is. Note: when downloading dojo, get the binary and source tarballs.

Current dojo version: 1.9.2.f774568

When editing files, replace with the source version, commit, then change the file, and commit. So the exact changes are easily diffed in svn.

Edited files: QueryReadStore.js

### Gotchas

Dojo 1.9 conflicts with jquery datatables and simplemodal, and dojo will not parse the dom onload. I believe [this is the issue](http://dojo-toolkit.33424.n3.nabble.com/Chrome-CORS-preflight-timeout-td3998054.html#a3998628)

To fix this, make things synchronous, and parse after jquery is loaded:

```

<script>
    dojoConfig= {
        parseOnLoad: false,
        async: false
    };
</script>

<script src="../../grouperExternal/public/assets/dojo/dojo/dojo.js" ></script>

<script type="text/javascript" >

require(["dojo/ready", "dojo/json", 
         "dojo/_base/config", "dijit/Dialog", "dojo/domReady!", "dijit/form/FilteringSelect", 
         "dojox/data/QueryReadStore", "dojo/dom-attr", "dijit/Tree", "dojo/data/ItemFileReadStore", 
         "dojo/store/JsonRest", "dojo/_base/declare", "dijit/form/ComboBox"]);

</script>

<script src="../../grouperExternal/public/assets/js/jquery.js"></script>

<script src="../../grouperExternal/public/assets/js/bootstrap.js"></script>
<script src="../../grouperExternal/public/assets/js/tree.jquery.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.dataTables.min.js"></script>
<script src="../../grouperExternal/public/assets/js/footable-0.1.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.cookie.js"></script>
<script src="../../grouperExternal/public/assets/js/grouper.js"></script>
<script src="../../grouperExternal/public/assets/js/native.history.js"></script>
<script src="../../grouperExternal/public/assets/js/grouperUi.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.blockUI.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.simplemodal.js"></script>
<script src="../../grouperExternal/public/assets/nifty/niftycube.js"></script>

<script>
$( document ).ready(function() {
  dojo.parser.parse();
});

</script>

```

### Dojo combobox

Requires dojo version 1.8.3.30226 (proof of concept of menu was 1.9.1.43d05c6).

[Submit data from other form elements](http://dojo-toolkit.33424.n3.nabble.com/dojo-queryreadstore-filtering-select-send-other-form-data-to-server-td3996152.html) (QueryReadStore.js)

[Throbber patch](http://dojo-toolkit.33424.n3.nabble.com/filtering-select-throbber-busy-notification-td3995853.html#a3996150) QueryReadStore.js)

[Show errors when there are problems with communication with server](http://dojo-toolkit.33424.n3.nabble.com/help-with-database-backed-filtering-select-td3995747.html#a3995880) (QueryReadStore.js)

[Dont interrupt typing, autocomplete off](http://dojo-toolkit.33424.n3.nabble.com/combobox-or-filtering-select-interrupts-typing-td3938918.html) (no files edited, was an option)

[Send the entered label if it is not associated with a value](http://dojo-toolkit.33424.n3.nabble.com/filtering-select-send-to-server-what-was-typed-in-not-selected-td4000507.html) (so server can give validation errors or so a name or id can be entered and not selected) (QueryReadStore.js)

If you have a combobox in a div which is replaced by ajax, and another combobox appears with the same id, you will get an error. The custom tag prevents this with this script before combobox:

```

    //unregister old ones since if they are replaced in divs they will give you an error
    result.append("    <script>\n");
    result.append("      dojoUnregisterWidget('" + this.idBase + "Id');");
    result.append("      dojoUnregisterWidget('" + this.idBase + "StoreId');");
    result.append("    </script>\n");

This function is in the grouperUi.js

/**
 * unregister a widget
 * @param id
 */
function dojoUnregisterWidget(id) {
  
  var widget = dijit.byId(id);
  if (widget != null) {
    if (typeof widget.destroyRecursive != 'undefined') {
      widget.destroyRecursive();
    }
  }
}

```

If we want paging, I think we need to try switching from a QueryReadStore to a JSON store, and use HTTP headers to communicate paging information

### Tree control

didnt show pluses or minuses, here is a [forum post about it](http://dojo-toolkit.33424.n3.nabble.com/dojo-tree-not-showing-pluses-or-minuses-td3999639.html#a3999642), note, this is not a problem in 1.8.3 (note: claro.css needed to be edited originally, but not anymore)

show icons for folders, groups, attributes, attribute defs, doesnt work in 1.8.3

### Build dojo into fewer files

[Following these instructions](http://sagittech.blogspot.com/2011/08/asdadsad-qwasdace-aavvrv-place-holder.html)

Note, original files are kept in this project: [https://github.com/Internet2/grouper/tree/master/grouper-misc/grouper-ui-dojo](https://github.com/Internet2/grouper/tree/master/grouper-misc/grouper-ui-dojo)

There is a README.txt that shows which files need to be copied to the grouper UI, e.g.

```

dojo/dojo.js
dojo/resources/dojo.css
dojo/resources/blank.gif
dojo/selector/lite.js
dijit/registry.js
dijit/icons/images/commonIconsObjActEnabled.png
dijit/themes/claro/claro.css
dijit/themes/claro/images/loadingAnimation.gif
dijit/themes/claro/images/tooltip.png
dijit/themes/claro/images/treeExpandImages.png
dijit/themes/claro/form/images/error.png
dijit/nls/*
dijit/form/nls/*
grouper/*

```

Note: make sure the nls dirs are there or dojo wont work for other locales. Try changing your browser to different languages to test them.

Get the source build for dojo, unzip, and put in the changes (only QueryReadStore.js?)

Make an HTML file: dojo-release-1.9.2-src/grouper.html

```

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Tutorial: Hello Dojo!</title>

  <!-- load Dojo -->
  <script src="dojo/dojo.js"></script>

  <script>
    dojo.require("grouper.grouperDojo");

    function init() {
      alert("Dojo ready, version:" + dojo.version);
        dojo.byId("greeting").innerHTML +=
          ". Greetings from dojo " + dojo.version;
      }
      dojo.ready(init);
  </script>
</head>
<body>
  <h1 id="greeting">Hello</h1>
</body>
</html>

```

Make a javascript file

```

[appadmin@i2midev1 grouper]$ pwd
/home/appadmin/dojo/dojo-release-1.9.2-src/grouper
[appadmin@i2midev1 grouper]$ more grouperDojo.js
dojo.provide("grouper");

/*
dojo.require("dojo.ready");
dojo.require("dojo.registry");
dojo.require("dojo.parser");
dojo.require("dojo.json");
dojo.require("dojo._base.config");
dojo.require("dijit.Dialog");
dojo.require("dojo.domReady!");
dojo.require("dijit/form/FilteringSelect");
dojo.require("dojox/data/QueryReadStore");
dojo.require("dojo/dom-attr");
dojo.require("dijit/Tree");
dojo.require("dojo/data/ItemFileReadStore");
dojo.require("dojo/store/JsonRest");
dojo.require("dojo/_base/declare");
dojo.require("dijit/form/ComboBox");
*/

require(["dojo/ready", "dijit/registry", "dojo/parser", "dojo/json",
         "dojo/_base/config", "dijit/Dialog", "dojo/domReady!", "dijit/form/FilteringSelect",
         "dojox/data/QueryReadStore", "dojo/dom-attr", "dijit/Tree", "dojo/data/ItemFileReadStore",
         "dojo/store/JsonRest", "dojo/_base/declare", "dijit/form/ComboBox"]);

```

Build dojo

```

[appadmin@i2midev1 buildscripts]$ pwd
/home/appadmin/dojo/dojo-release-1.9.2-src/util/buildscripts[appadmin@i2midev1 buildscripts]$ ./build.sh action=release htmlFiles=/home/appadmin/dojo/dojo-release-1.9.2-src/grouper.html

```

Result is here, tarball it up

```

/home/appadmin/dojo/dojo-release-1.9.2-src/release/dojo/grouper

[appadmin@i2midev1 dojo]$ pwd
/home/appadmin/dojo/dojo-release-1.9.2-src/release/dojo
[appadmin@i2midev1 dojo]$ tar czvf grouper.tgz grouper
[appadmin@i2midev1 dojo]$

```

SFTP that to SVN in the dojo grouper directory

sfd
