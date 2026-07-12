---
title: "Grouper AI ideas"
space: GrIntDev
pageId: 48792554
version: 18
lastUpdated: 2026-07-12T17:02:37.758Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792554/Grouper+AI+ideas
---

This is the home base for ideas around Grouper and AI.

### Finding specific answers within the Grouper documentation can take time, due to Grouper’s many capabilities and integrations.

Grouper enthusiasts should consider consulting AI before spending their own investigative time or bothering others on the support list. Grouper has the advantage that it is open source with public documentation so AI models already have this information or can search for it and interpret the results. Having perfect documentation is always the goal, but if it is not perfect, AI can improve the experience.

People need to know how to use the AI tool: e.g. in ChatGPT selecting the “web search” option might help. Ask common recent incommon-grouper Slack channel questions into an untrained AI and see that there is decent advice. “How do I upgrade from Internet2 Grouper v4 to v5?” I tried this in an untrained Copilot and it gave decent advice and included links to mostly relevant wiki pages.

We used to say “let me google that for you”. Now we can say “let me vibe with AI and remove its hallucinations for you”. Will people in Slack think it is snarky to see a response pasted from AI? Perhaps we will see…

### End users and admins need to know how to generically use the Grouper UI

There has been a lot of progress with the [communal documentation working group](https://internet2.edu/community-collaborates-to-enhance-grouper-documentation/) to update the [wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541827/Grouper+Administration+Guides) with everything Grouper. The wiki documents how to use the UI. It is overwhelming since there are so many options and bells and whistles, and features in Grouper are available in various versions and evolve rapidly.

A common training file could be established to briefly explain to AI how to do various tasks, what options are available (and the version when each option became available). Then AI could take some requirements and advise the user how to use the Grouper UI to accomplish their task in the version they are using.

The Grouper team will do a proof of concept and see if this is viable. Users are confused when reading the complete documentation page for a Grouper feature. Imagine if AI could remove the unnecessary bells and whistles that are not in the requirements and give the user what they need.

### Grouper admins need to know how to write GSH scripts

There is a starting point for this training file and have a video showing how amazing it is to have AI write a decent script that only needs to be tweaked to be correct. Of course this is for common use cases. Writing training files is tedious and output should be consistent. Hearing that should ring a bell so we all know how to finish the task: use AI to generate the training file that is used by the AI tool. This is demonstrated in the example video.

### Data analysts need to be able to write ABAC scripts

Anyone at an Internet2 conference over the past couple years in an ABAC (Attribute Based Access Control) session has heard this question: “How will people know how to write these scripts?” We have done a lot of work in this area showing how to make an institution-specific training file to address this issue. There is a presentation on this from a conference at Penn(include link). Grouper could have a function to programmatically generate a starter training file based on ABAC data field configuration. I am interested to see if a Grouper UI integration is viable without any UI changes. The GSH template for an ABAC pattern is custom code which could consult our AI vendor based on this specific training file to translate natural language into digital policy.

### Custom access reports are difficult to interpret

At Penn we have reports to help identify which reference groups should be used for “front door authorization”. For more information on this topic, please attend the June IAM online on this topic presented by Harvard, Alaska, and Penn. (link). We have thorough reports that show recent authentications to an application with analysis about if the user was in certain reference groups at authentication time. It is time consuming to interpret the results and agree on a low risk but high security configuration. This is specific to Penn, so it is an example of an institution integrating its own training file to help people save time and improve accuracy of centralized authorization. You could imagine many other similar examples.

### Grouper developers and contributors need to know the API’s and produce consistent code

I am using GitHub CoPilot in Eclipse and it is helpful once you learn how to vibe with it. It enforces best practices like writing comments before you write your code, and more often than not it writes helpful code for you before you start typing. Things that are tedious and repetitive (like duplicating Javadoc on getters and setters to match the property) are now automatic and consistent. If it is worth it we could agree on an AI vendor for our development environments and encourage developers to participate.

### A user wondering if there are tasks to do.

AI could suggest attesting four groups, following up with a direct report who has not done their attestation, and looking at a daemon which is failing.

### A user could ask for a group design for an app with certain requirements.

AI could generate a suggestion with the [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) to automatically create the group design structure, and could securely run that from inside Grouper.

### A user could want 30 new groups created with certain characteristics, a manual task that could take an hour (or write the [GSH script](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) in 20 minutes).

AI could generate a GSH script in seconds and Grouper could securely run that.

### An admin could ask for help troubleshooting a [provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework)

AI could suggest to change the delete setting on a particular attribute.
