---
title: "Visualization UI"
space: Grouper
pageId: 28548433
version: 24
lastUpdated: 2026-07-01T05:44:32.642Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548433/Visualization+UI
---

## Grouper Visualization UI

[ABAC visualization](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555555/Visualization+for+ABAC)

Visualization in the Grouper UI uses the [Visualization API](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548424/Visualization+API) to internally construct an acyclic directed graph of edges and nodes based around the current page's stem, group, or subject. There are options to show the relationships as either a text-only version, or a graphical SVG version based on the GraphViz library. In the graphical version, arrows point from left to right, from "parent" objects to "child" objects. Parents include folders containing objects, loader jobs that populate groups, members of a group, and left and right components of a composite group. Children objects include memberships, composite groups, and subobjects of a folder.

### Invoking visualization

The More Actions→Visualization option will show up for folders, groups and subjects. For non-wheel users, it will only display objects you have read access to.

The main area of the visualization page is initially blank. To build and display the graph, click on the Generate button. Graph settings can be configured by clicking the button with the gear icon.

### Settings

The settings are contained in a hidden frame that gets expanded when the gear icon next to the Visualization title is clicked. It can be collapsed by clicking the button again. The settings are persistent, and get updated when the Generate button is clicked. They may not be saved when simply refreshing the page, or when changing pages by clicking a linked object.

Various drawing settings can be selected to alter what is selected in the graph. These settings get saved as user preferences whenever the Generate button is clicked. If another object is selected, the saved preferences will be set from the saved values.

- Visualization method: Whether to display using an SVG frame or a text-only description
- Display objects using name or path: show object labels as name extension, e.g. "Staff - Reduced Incremental Firmware", or as path "basis:dept:130000:staff". Whichever is chosen, the other choice will be in the tooltip for the object
- Number of parent levels to show: This limits the number of hops from the starting node to parent objects.
- Number of child levels to show: This limits the number of hops from the starting node to child objects.
- Number of sibling objects: For objects linked to a large number of objects, this limits the amount shown to a random sample of them
- Show folders: Whether to include folders in the output.
- Show loader jobs: Whether to include groups that are set up as a loader job. These objects show the groups loaded by them as child nodes
- Show provisioners: Whether to include PSPNG provisioning targets as a child object of the provisioned group
- Show member counts: Whether to include member counts. If there are many groups, this may increase the time taken to build the output

### Text mode

The text mode is a descriptive list of all the linked objects reachable from the starting node. Clicking on an individual node navigates to a new visualization page based around the selected object.

### Graphical mode

The graphic mode utilizes the D3 and GraphViz JavaScript libraries to convert the graph into an SVG canvas. The graph is drawn using different shapes and colors for folders, groups, loader jobs, provisioners, and composite groups. The SVG can be moved around within a certain range, and can be resized using the mouse scroll wheel.

Clicking on the individual nodes navigates to a new visualization page based around the selected object. The GraphViz .dot output can be obtained by clicking on the "Copy raw .dot data" button to expand a text box. This text can be copied and then used in GraphViz offline, or used in online sites such as [GraphVizFiddle](https://stamm-wilbrandt.de/GraphvizFiddle/). The raw SVG markup can be viewed by clicking on the "Copy SVG" button to expand the text box. This text can be copied and saved as a *.svg file (you must save as UTF-8 format!), which can then be opened directly in a web browser. The text box can be closed by clicking the Copy... button again.

The tooltip for the graph will include statistical information such as the number of objects, total memberships, number of folders and groups skipped, etc. The tooltip for objects will be either the display name or full path, plus description if there is one. The tooltip for arrows will describe the type of relationship between parent and child object.

## Open issues

Note: trying to make a release of this by 2/19/19 in a patch…

Prioritized list of AIs (lower number = higher priority)

1. [AI Chad] carefully review Chris's escaping commit/patch
2. [AI Chad] Discuss with chris moving grouperVisualization.js to commonHead.jsp... easier to debug in browser I think?
3. [AI Chad] I think the whole graph shouldnt have a tooltip, lets do a hide/show link in HTML for that perhaps... or maybe show the first couple lines with ellipses? lets discuss. Its difficult to focus on a line tooltip when the whole graph has one...
4. [AI Chris/Chad] Wordsmith. Chad, are things committed and I can run it and edit the text file (and if you see my commit and disagree we can discuss)? Or do you want to meet and discuss things first / team program it out? (Chad - this is branch GRP-1966-ui-visualization ; major changes are done, multiple editors no problem)
5. Is there a max graph size? e.g. maybe a configured limit of 2k objects? Lets discuss (Chad- can be added; right now can limit sibling objects, which can help with a lot of runaway queries)
6. Is there a max time to display? e.g. 3 minutes? lets discuss
7. Are the calls paged? Maybe size of 100 of each type of query? Lets discuss
8. (future revision) Color review (good for first release)
9. (future revision) New window option (and clicks in new window would control the parent window somehow)
10. (future revision) Resize option (like youtube full screen), or new tab/window
11. I think having the legend key be part of the graph ( not just hover/help button) is a better idea. Especially if people are capturing the image to embed in (shutter) documentation/paper/powerpoint presentations. Unless the key is “very big/long” I would just include it or not in the graph. ( checkbox to display it ? )
  
  
  
  Having the hover stuff is nice too when you are interacting with it live. So I would not rule that design out. But I would personally prefer the static legend before the hover feature.
  
  
  
  
  
  I don’t know how common it is to have multiple subject sources, but it might be useful to know memberships per source for groups too.
  
  ( Which also gives you a “grouper objects” vs “subjects’ counts too. )
  
  
  
  It may also be useful, in some cases to deal with “grouper Entities” counts as well.
  
  
  
  Having a “short cut” way to include the “object type” would be a very good thing.
  
  However, an attribute (AKA: meta data) selection that is more generic would be awesome too.
  
  I do have custom attributes that I would want reported as well. ( on folders and/or groups )
  
  And yea, you might need to deal with attributes assigned to attributes. J

## Closed issues

1. [AI Chad] export sample data as GSH and put in a test case somewhere (test doesnt have to run, but want to save the state for future manual testing) (DONE - in edu.internet2.middleware.grouper.app.graph.GraphSampleData.loadSampleData())
2. [AI Chad] solve graphviz bug where loses ability to resize svg when the graph is drawn a second time (DONE - remove call to d3.empty to fix issue)
3. [AI Chris] send Chad how form would look (DONE- implemented similar to the attestation edit form)
4. [AI Chad] confirm all calls honor Grouper security. e.g. dont show names of groups the user cant view. Dont show members the user cant read. Lets discuss (DONE - objects need READ not VIEW to appear in the graph)
5. [AI Chad] D3 should be listed first and be the default (DONE)
6. [AI Chad] persist preferences (DONE- using userData attribute)
7. [AI Chad] take out option to make it fit a div (sort of DONE - extending to the left covered up the folder navigation which was problematic; next version maybe can have a full screen button?)
8. [AI Chad] sentence case on everything except acronyms and proper nouns (DONE)
9. [AI Chad] “stem” should be “folder” in ui (DONE)
10. Shows in box:
11. Tooltip on box:
12. [AI Chad/Chris] sync up what types of arrows and what the tooltip would be (DONE)
13. [AI Chad] need to hit “Generate” button to show graph after “visualize” button (DONE)
14. [AI Chris/Chad] Decide on default for parents/children (DONE - default for first version is all parents, all children, sibling object limit 50; all can be changed in preferences)
15. [AI Chad] Put stats above the graph in HTML (Chad- real estate getting crowded on the page, need to work on improving layout) (DONE - did as a tooltip instead)
16. (future revision?) [AI Chad] Change text mode to something like example below (DONE)
17. (future revision?) Option to show display full path in box instead (DONE)
18. (future revision) Dot output available, popup a textarea (DONE - is a hidden textarea with a button to toggle it)

## Text mode example (list out all boxes as paragraphs, and lists of relationships):

**Folder**: app  
Path: app  
Description: The app folder holds apps  
Contains:

- folder: lms
- folder: boardeffect

**Folder**: boardeffect  
Path: app:boardeffect  
Description: Has boardeffect objects  
Contains:

- folder: ref
- group: all-users

**Group**: president_assistant  
Path: ref:roles:president_assistant  
Description: Those who assist the president  
Member count: 5  
Direct group members: none  
Direct member of these groups:

- board_managers
- boardeffect_mgr

**Group**: lms_authorized  
Path: app:lms:lms_authorized  
Description: users allowed to access LMS  
Member count: 25  
Provision to: pspngProd  
Composite owner of lms_authorized_allow minus lms_authorized_deny  
Direct member of these groups: none

**See Also**

[Visualization API](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548424/Visualization+API)
