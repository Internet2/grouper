---
title: "Grouper AI example for institution-specific help"
space: GrIntDev
pageId: 48794038
version: 4
lastUpdated: 2026-07-12T06:46:27.964Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794038/Grouper+AI+example+for+institution-specific+help
---

You could train an AI tool on institution specific Grouper documentation. This is a starting point proof of concept.

See this prompt and response from AI which is correct:

Look at the training data below. Those steps are not in there. AI knows how to generically remove a user from Mathematica, and it knows the group names for SAS, and it put two and two together and generated the correct specific instructions for the situation. If the user could find the Penn documentation, they would have to follow the same process, and anyone who supports tech applications knows this is time consuming and is not always done correctly.

It is noted that the default documentation did not work well with AI until the full group names were listed (not links), and things were explained very explicitly. It is argued that testing the documentation in AI and briefly iterating will produce better documentation for users. If AI cannot figure it out, it is not a surprise if users have issues too.

In this example at Penn we documented on our Grouper documentation page how to manage Mathematica access.

If I copy the text of that page in a training file, and put a little metadata on it about which page it is coming from, it looks like this

```
###############
## doc: https://penngroups.isc.upenn.edu/apps/mathematica
###############

Mathematica
On this page:
Groups
Mathematica is integrated with PennGroups.  Several schools are using this service.  Automated groups allow access and manual groups are delegated to schools and centers to manage includes and excludes.

Groups
To exclude a user from Mathematica:
This is either done centrally, or at a school/center
Go to the include group (see below e.g. SEAS include group is: penn:seas:apps:mathematica:mathematicaSeasIncludeManual) and see if they are included, if so remove them
See if they are in the overall group: penn:evp:businessServices:apps:kivuto:mathematica:mathematicaUsers.  If so, then add them to the exclude group (see below e.g. Wharton exclude group is: penn:wharton:apps:mathematica:mathematicaWhartonExcludeManual)
Mathematica groups

Office of Software Licensing (OSL) manages the policy group: penn:evp:businessServices:apps:kivuto:mathematica:mathematicaUsers.  This is the group that is sent to WebLogin and allows users to use Mathematica.
SEAS:
penn:seas:apps:mathematica - Folder
penn:seas:apps:mathematica:mathematicaSeasExcludeManual - Manual exclude group
penn:evp:businessServices:apps:kivuto:MatlabSSO:matLabStudentsSEAS - Loaded group of students - active students in any SEAS division.  This is the same group MatLab uses
penn:seas:apps:mathematica:mathematicaSeasIncludeManual - Manual include group
OSL:
penn:evp:businessServices:apps:kivuto:mathematica - Folder
penn:evp:businessServices:apps:kivuto:mathematica:mathematicaOslExcludeManual - Manual exclude group
penn:evp:businessServices:apps:kivuto:mathematica:mathematicaOslIncludeManual - Manual include group
Wharton
penn:wharton:apps:mathematica - Folder
penn:wharton:apps:mathematica:mathematicaWhartonExcludeManual - Manual exclude group
penn:evp:businessServices:apps:kivuto:MatlabSSO:matLabCoursesWharton.  - students registered in a Wharton class in the current term.  This is the same group MatLab uses.
penn:evp:businessServices:apps:kivuto:MatlabSSO:matLabStudentsSEAS - active students in any Wharton division, or fac/staf with an active appointment in the Wharton school.  This is the same group MatLab uses
penn:wharton:apps:mathematica:mathematicaWhartonIncludeManual - Manual include group
SAS
penn:sas:service:application:mathematica - Folder
penn:sas:service:application:mathematica:mathematicaSasExcludeManual - Manual exclude group
penn:evp:businessServices:apps:kivuto:MatlabSSO:matLabStudentsSEAS - active students in any SAS division except the english language program.  This is the same group MatLab uses
penn:sas:service:application:mathematica:mathematicaSasIncludeManual - Manual include group

```

Note: you could imagine a web crawler to automatically take all the docs, and make this large text training file for all the apps documented.

Now I make a custom GPT with that training file and these instructions:

```
Always use the knowledge files when answering questions.  Always use penngroupsDocs.txt file.

Give the link to the relevant documentation page.

Take in a request of how to do something in PennGroups, search for Penn-specific tasks.

To navigate to a group (e.g. penn:folderName:group) in penngroups, tell the user to go to: https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=penn:folderName:group

To add a user to a group, navigate to the group page.  Click "Add members".  Search for the user and click "Add".

To remove a user from a group, navigate to the group page.  Filter for the user.  If they are a direct member, click "Actions -> Revoke membership".

No navigate to a folder (e.g. penn:folderName) in penngroups, tell the user to go to: https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=penn:folderName
```

You could imagine the "how to use the Grouper UI" part to be a common Grouper AI training file, or something generated from the version of Grouper you are running, and your externalized text (e.g. if you change button labels).

Here is a screenshot of the GPT configuration
