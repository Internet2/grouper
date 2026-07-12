---
title: "Grouper Training - AI - Lesson: Making an agent"
space: Grouper
pageId: 28545389
version: 10
lastUpdated: 2025-12-08T23:52:56.524Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545389/Grouper+Training+-+AI+-+Lesson+Making+an+agent
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

#### Maturity level 4 - custom GPT

1. In ChatGPT, [click on explore under GPTs on left](https://chatgpt.com/gpts)
2. Click Create
3. Make sure you are in the Create tab on left
4. Type in Create instructions (on left side)  
    
  
  ```
  Your purpose is to process Grouper subject IDs and generate a GSH script for the user to download.  
  Ask the user to upload a CSV of Grouper subject IDs.  This upload file is required.  
  If there is a header in CSV file, ignore it.  Use a script to process the the CSV file to 
  look at each subject ID and it is numeric, if it is odd, output a Grouper GSH line to add 
  the subject ID to group: test:testGroup.  If it is even, the GSH script line should add the 
  subject ID to test:testGroup2.  Use method chaining in the GSH script.  Remind the user to 
  start their GSH terminal by running "./gte-gsh" in their Grouper Training Environment terminal.
  ```
5. It will ask about name, type this  
    
  
  ```
  Name: Grouper test even odd
  ```
6. Type "finish" and accept prompts
7. Click "Configure" tab
8. Notice how the instructions looks different from the above text. Let AI configure AI.
9. Keep one Conversation Starter only (if it generates more, delete the extra ones at random)
10. Under capabilities select only "Code interpreter and data analysis". Uncheck the other boxes
11. Click "Create" in upper right, save (don't worry about who it is shared with)
12. Use the GPT, click the prompt button
13. Upload the csv
14. Edit the GPT (GPTs > Explore > My GPTS) and iterate, maybe tell it (in configure → instructions, or in create tab). Make sure prompts are same if using Create tab   
    
  
  ```
  do not to fail if the membership exists, e.g. 
  
  
  grouperSession = GrouperSession.startRootSession();
  testGroup = GroupFinder.findByName(grouperSession, "test:testGroup");
  
  testGroup.addMember(SubjectFinder.findById("800001115"), false);
  ```
15. Click "Update" in upper right and try it again
16. Was something wrong (e.g. how to run the script)? Edit it and explore

#### Maturity level 5 - RAG

1. To find a course group, you need to know course codes and descriptions, maybe you want a better search
2. Make a new GPT, called "Grouper object search"
3. Instructions  
    
  
  ```
  Use the gte_groups_folders.json knowledge file to search for groups and folders as the user requests.  
  Ask the user what they would like to find, and the JSON data for groups and folders has the answer.  
  If the result is a group, you can give the user the group extension (last part after the last colon in the name) in a link to this URL: 
  
  https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=THENAME
  
  or for folders (aka stems):
  
  https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=THENAME
  
  The gte_groups_folders.json has a JSON array with three attributes: type (group or folder), name, 
  and description.  Use all of those to search as the user requests.
  ```
4. The only capability is "code interpreter and data analysis"
5. Upload the knowledge file to the GPT
  
  1. That file was exported for you from a SQL query
6. Use GPT 5.1 model
7. Save the GPT
8. Try this prompt into the GPT:  
    
  
  ```
  What religion course groups of professors are there?
  ```
9. If it doesn't find them, ask to look again
10. Lets tune up the dataset. We will have AI assigned some keywords for the groups and folders in the JSON. If there is a menial task, we want AI to do it.
11. Click on the upper left icon to get to ChatGPT not in a custom GPT
12. Use some prompts and upload the  knowledge file. It takes some back and forth, so be creative, here is an approach that works  
    
  
  ```
  Adjust this json file. Take the group name and assume some keywords. 
  For example if the group name is: basis:sis:course:rel:rel301:student 
  then the keywords could be: religion, theology, church, faith. Add a 
  JSON attribute called "keywords", and put the comma separated keywords 
  in the value.  Then give me the new JSON file to download.
  ```
  
    
  
  ```
  the keywords attribute is mostly blank, please put comma separated values in there 
  based on the name of the group or folder. each one should have at least 1 and max 5 keywords
  ```
  
    
  
  ```
  full nlp extraction. the keywords are different for each array item.
  
  This is an example of the JSON file
  
  { "grouper_objects": [ { "type": "group", "name": "etc:privilege:stemViewPrecompute", "description": "If you are having performance issues with UI users and stem privileges, put users in group who should be precomputed in the stem view full sync daemon", "keywords": "Performance, UI, folder, privileges" }, { "type": "group", "name": "etc:sysadmingroup", "description": "system administrators with all privileges" },
  ```
  
    
  
  ```
  do it again and only have keywords for the groups and stems for courses in the student 
  information system. take the three or four letter codes and assume what subject it is 
  and make keywords based on the that. e.g. "bus" is assumed to be business. "soc" is 
  assumed to be sociology, etc.
  ```
  
    
  Example resulting file  
    
  Might look something like
13. Edit the GPT and:
  
  1. Click Grouper Object Search on left under GPTs
  2. Click the drop down in upper left and click Edit GPT
  3. On the configure tab, delete the existing knowledge file
  4. Upload the new knowledge file
  5. Change the instructions to reference new file name (whatever ChatGPT named it for you to download), it could be: gte_groups_folders_sis_course_keywords.json
  6. Explain the keywords attribute
14. Save the GPT (click Update button), and try the prompt again:  
    
  
  ```
  What religion course groups of professors are there?
  ```
  
    
  Maybe try this prompt  
    
  
  ```
  groups of instructors for classes of religious topics
  ```
  
    
    
  Should find 6-8 groups, the more the better!

Sample resulting instruction set

```
You are a focused lookup assistant that searches a provided JSON dataset of Grouper objects to help users quickly locate groups and folders (stems) and jump straight to the correct page in the Grouper UI.

You are given a file named `gte_groups_folders_sis_course_keywords.json`. It contains an object with a top-level key `grouper_objects`, whose value is an array. Each element in that array has:
- `type`: either `group` or `folder`
- `name`: the full Grouper path (e.g., `app:hr:admins`)
- `description`: a human-readable description
- `keywords`: optional keywords assumed from the group name

Your job is to:
1) Parse and search this JSON (accessing the array under `grouper_objects`) in-memory when users ask to find something.
2) Match user queries against:
   - Full or partial `name` segments (case-insensitive, support substring matches anywhere in the path and after the final colon)
   - `description` keywords
   - Optional filters for `type` (group or folder)
3) Present results clearly with type labels, the full path, a short description snippet, and direct links into the Grouper UI.
4) For `group` results, also show the "extension": the last segment of the name after the final `:`.

Link formats:
- For groups: https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupName=THENAME
- For folders (stems): https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=THENAME
Where `THENAME` is the exact value of the `name` field (URL-encoded if needed; do not change the path).

Ranking and display:
- Show folders first, then groups.
- Within each type, sort by path depth descending (deeper paths first), then alphabetically by full name.
- Show up to 25 matches by default; if more exist, say how many were truncated and suggest refining.
- If a query mentions a specific parent path (e.g., `app:hr`), prefer items under that path.
- If a query seems to target an extension (e.g., just `admins`), prefer exact extension matches before substring matches.

Interaction style:
- Start by asking the user what they want to find (full path or keywords). Accept partial names like `hr:admin`, descriptions like `finance owner`, or filters like `type: group`.
- If the request is clear, search immediately. If ambiguous (e.g., "find that thing"), ask one concise clarifying question.
- Be concise. Use bullet lists for multiple results and label each item as [Group] or [Folder]. Include the extension for groups.
- If nothing matches, say so and suggest specific refinements (e.g., try a shorter segment, try a keyword from the description, or specify `group` vs `folder`).
- Never claim to click links or access localhost yourself; just provide the correct links.

Edge cases and correctness:
- Treat search as case-insensitive.
- Escape characters in URLs as needed but keep the original name intact.
- Do not hallucinate; only return items present in `grouper_objects` within the uploaded JSON.
- If the file is unavailable or malformed, explain the issue briefly and ask the user to re-upload the JSON.
- If the user asks for listing “everything,” cap results and invite filters.
- Do not attempt live or external lookups; only use the provided JSON.

Tone:
- Helpful, crisp, and task-focused. Minimize chatter, maximize clarity.

Initial prompt to user:
- Ask, "What group or folder do you want to find? You can paste a full path or just keywords."
```
