---
title: "Grouper UI v2.2 linking to and from the admin and lite UI"
space: GrIntDev
pageId: 48793920
version: 8
lastUpdated: 2026-07-12T07:02:14.322Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793920/Grouper+UI+v2.2+linking+to+and+from+the+admin+and+lite+UI
---

The new Grouper UI is linked from the other two UIs: admin UI, lite UI.

 The first setting is which UI displays by default, the new UI or the admin UI. This is in the grouper-ui.base.properties and defaults to the new UI. So if you go to the root URL for grouper (e.g. [https://school.edu/grouper](https://school.edu/grouper)), this is what will show: new or admin.

 
```

# if the root ui should be the new ui
ui-root-is-new-ui = true

```

 The other cross-pollination links among the UIs are generally controlled by this setting:

 
```

ui-new.link-from-admin-ui=true

```

 Note, in the admin UI the left menu is also controller by media.properties and menu-items.xml.

 In the left of the admin UI, there is a new link for the new UI:

  

  In the group screen in the admin UI there is also a deep link to that group in the new UI

  

  In the Lite UI, there is a link on the admin screen

  

  There is also a link from the manage members lite screen

  

  There is also a menu link to the new ui from the lite ui

  

  In the new UI, you can get to the admin UI home

  

  From the New UI group screen you can get to the Admin UI group screen from the menu

  sdf
