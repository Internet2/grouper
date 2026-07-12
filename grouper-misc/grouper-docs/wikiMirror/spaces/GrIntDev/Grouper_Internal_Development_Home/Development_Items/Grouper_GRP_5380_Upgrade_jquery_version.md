---
title: "Grouper GRP-5380 Upgrade jquery version"
space: GrIntDev
pageId: 48792626
version: 2
lastUpdated: 2026-07-12T06:45:34.446Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792626/Grouper+GRP-5380+Upgrade+jquery+version
---

Note, a partial fix for this is in branch GRP-5380_Upgrade_jquery_version

Add this in commonHead.jsp and commonBottom.jsp to script files that change

```
jquery.form.js?ver=4.12.1">
```

## Delete unused libraries

I believe these javascript libraries are unused:

- tree.jquery.js  
    
  Theres a bunch of POC code in grouper.js which can be removed
  
  
  ```
    var data = [
      { label: 'Root',
        id: 'root',
        children: [
          {
              label: 'Applications',
              id: 'applications',
              children: [
  
  
    // Set a max height for the explore tree based on the user's viewport size
    $('#tree1').css('max-height',function() {
      var viewportHeight = document.documentElement.clientHeight;
      var maxHeight = viewportHeight - 300;
      return maxHeight;
    });
  
   // Create Tree
  var $tree = $('#tree1');
  $tree.tree({
  data: data,
  selectable: false
  });
  ```
- jquery1.3.2.js
- jquery2.js
- jquery.dataTables.js

## Delete test folder

The webapp/test folder can be removed

## Jquery upgrade

Jquery can be upgraded to v3.7.1

Change this in gropuerUi.js

FROM:

```
$(window).unload(function() {
```

TO:

```
$( window ).on( "unload", function() {
```

Tooltips might be a problem which I believe are bootstrap

```
$('.top-container').tooltip({
```

Jquery blockui can be upgraded to 2.70.0

Jquery.cookie needs to stay the same unless we start using npm

## Bootstrap upgrade

bootstrap.js and css can be upgraded to 5.3.3, but does not work at all

Might need to move bootstrap.js script tag to commonTop.jsp
