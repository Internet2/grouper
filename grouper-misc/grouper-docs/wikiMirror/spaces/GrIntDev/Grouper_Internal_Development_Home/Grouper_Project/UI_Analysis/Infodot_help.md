---
title: "Infodot help"
space: GrIntDev
pageId: 48824689
version: 4
lastUpdated: 2026-07-12T07:02:46.974Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824689/Infodot+help
---

An infodot is help text which is hidden by default, and can be shown by clicking the infodot image (currently a yellow circle with an "i" inside). Here is an example:

  

  Using the infodot

 

1. First think of a html id which will be unique on the page.
2. Then use the infodot custom tag where the button should be:

 
```

<grouper:infodot hideShowHtmlId="firstHideShow" />

```

 

1. Code the HTML for help text, put this in an HTML element (e.g. div or tr), and put the hideShowTarget tag in it so it gets the right style and id. Note: make sure you have useNewTermContext set to true so that terms are displayed on the screen when the infodot is hidden

 
```

<div class="helpText" <grouper:hideShowTarget hideShowHtmlId="firstHideShow"  /> >
  <grouper:message bundle="${nav}" key="groups.infodot.example" useNewTermContext="true" />
</div>

```

 Note: you can have multiple things which are hidden or shown for one infodot button.

 

#### Settings

 

- You can disable the infodots globally (or for certain locales) with the media.properties setting "infodot.enable", set to false
- You can have a link to enable/disable the infodot (via the cookie "grouperInfodots" with value true or false) with this source

 
```

<a href="https://wiki.internet2.edu/confluence/pages/editpage.action#"
    onclick="return toggleInfodots(event, '<grouper:message bundle="${nav}" key="infodot.disableText" tooltipDisable="true"/>',
'<grouper:message bundle="${nav}" key="infodot.enableText" tooltipDisable="true"/>');" id="infodotToggleLink"></a>
<script type="text/javascript">
  writeInfodotText('<grouper:message bundle="${nav}" key="infodot.disableText" tooltipDisable="true"/>',
  '<grouper:message bundle="${nav}" key="infodot.enableText" tooltipDisable="true"/>');
</script>

```

 

#### Page level infodot

 The title is displayed in the tile: title.jsp, so I make up a name for an infodot based on title nav.properties id, and if there is a subtitle, concatenate. Then if there is an infodot based on that name, display the infodot, else don't. Either way, in the HTML comments, display the name of the infodot so it is easy to configure. Except for some pages which are different, but have the same titles, it works fine. Here are some screenshots...

 Here is an example comment:

 
```

<!-- trying title infodot with key: infodot.title.groups.all -->

```

 Here is the entry in the nav.properties:

 
```

infodot.title.groups.all=Find a group, click on the group name and act on the group (edit properties, show members, etc) using the buttons at the bottom of the screen

```

  

   

  

#### Section level infodot

 You can put section level infodot (as long as section has an infodottable h2 tag (one that doesnt have a subtile)). Note: not every single subtitle is available for this

 First, view source to see what to name the nav.properties entry:

 
```

<!-- subtitle infodot from nav.properties key: infodot.subtitle.groups.heading.browse -->

```

 Then add that entry in nav.properties, and you will see the infodot to the right of that section heading

 

 (technical info) If you want to put infodot on a section of a page (something that has a subtitle), first of all the subtitle needs to use the grouper:subtitle tag, which I converted all h2's to use (so it should be available everywhere that doesnt have a complicated (e.g. sub-tiled) h2. The substitle tag can take two params for substitution into the message value

 Here is the old code:

 
```

<h2 class="actionheader">
    <grouper:message bundle="${nav}" key="groups.heading.browse"/>
</h2>

```

 Here is the new code:

 
```

<grouper:subtitle key="groups.heading.browse" />

```
