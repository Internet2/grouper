---
title: "Grouper Training - MCP AI - Lesson 08: Claude driving the Grouper UI"
space: Grouper
pageId: 332202005
version: 10
lastUpdated: 2026-10-26T16:23:00.609Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/332202005/Grouper+Training+-+MCP+AI+-+Lesson+08+Claude+driving+the+Grouper+UI
---

In this lesson Claude uses the browser built into the Claude app to work in the Grouper UI itself: it opens pages, clicks, types, and reads what is on the screen, while you watch.

Before you start: your GTE VM is running with its SSH tunnel open.

Stay organized: name this lesson's conversations `Lesson 08: ...` and keep them in a sidebar section named `Lesson 08 Driving the UI`, as in Lesson 01.

## Step 1: reset your GTE

An earlier lesson already did the basis and reference groups exercise, so start from a fresh container.

If you are still in GSH, type `:quit` first, so you see the `student@...` prompt.

In the SSH window on your GTE VM:

```bash
./gte 101.1.1
```

Wait a few minutes for Grouper to start, until [https://localhost:8443/grouper](https://localhost:8443/grouper) loads in your browser.

## Step 2: check the GTE certificate

Claude's browser refuses self-signed certificates, so it can only open the GTE because you trusted the GTE certificate in step 3 of [Grouper Training - MCP AI - Lesson 00: Pre-work](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314408978/Grouper+Training+-+MCP+AI+-+Lesson+00+Pre-work). If you skipped it, do it now, then quit the Claude app completely and open it again.

## Step 3: log in to the Grouper UI

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Open https://localhost:8443/grouper in your
browser and log in as banderson with the password
password.
```

The browser pane opens, Claude fills in the GTE login page, and you see the Grouper home page logged in as Bob Anderson. If the page does not load, the certificate is not trusted yet: check step 2 and that you restarted the Claude app.

If Claude asks to import cookies from your browser, click "Do not show again". The training does not need your browser cookies, and they should stay out of Claude's browser.

## Step 4: do a real lesson in the UI

Earlier lessons did [Use cases - Lesson 01: Basis and reference groups part 1](https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1) with a GSH script and through MCP. This time Claude does it the way a person would, by clicking through the Grouper UI.

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
Do the exercise in Grouper Training - Use cases -
Lesson 01: Basis and reference groups part 1 in
the Grouper UI, in your browser, the way the
lesson describes, step by step:
https://docs.grouper.internet2.edu/wiki/spaces/Grouper/pages/28544224/Grouper+Training+-+Use+cases+-+Lesson+01+Basis+and+reference+groups+part+1
Tell me each step as you go, take a screenshot at
the end, and answer the questions in the lesson.
```

Watch it create the folder [ref:student](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=ref:student) and the group [ref:student:students](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=ref:student:students), add the ref type with its data owner, add the class year groups, and set the end date on the `cm:2026` membership. Compare the student count with what you got through MCP. This takes longer than MCP, because Claude waits for each page and reads the screen.

If something does not work, copy and paste the error into Claude in the same conversation, and iterate until it works.

## Extras, if there is time

The instructor goes over these, and the class does them if there is time.

### Show me where

Use the copy button to copy this prompt into the Claude app in a new conversation:

```
I am new to Grouper.  In the UI, show me how to
see everyone who has the ADMIN privilege on
ref:student:students, and take a screenshot of
that screen.
```

Claude navigates to the group's "Privileges" tab and shows you where the information is, so you can find it yourself next time.

## Clean up

After the training, remove the GTE certificate and the GTE MCP server as described at the end of [Grouper Training - MCP AI - Lesson 00: Pre-work](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/314408978/Grouper+Training+-+MCP+AI+-+Lesson+00+Pre-work).
