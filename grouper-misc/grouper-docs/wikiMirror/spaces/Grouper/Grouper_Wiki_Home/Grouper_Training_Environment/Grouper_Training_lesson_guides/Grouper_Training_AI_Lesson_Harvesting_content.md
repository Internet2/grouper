---
title: "Grouper Training - AI - Lesson: Harvesting content"
space: Grouper
pageId: 28545532
version: 5
lastUpdated: 2025-12-08T17:02:35.745Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545532/Grouper+Training+-+AI+-+Lesson+Harvesting+content
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

### SKIP THIS!

1. Install PlayWright (normally done in container with ENV var: GROUPER_PLAYWRIGHT_MOVE_JARS=true)  
    
  
  ```
  :q if you are in GSH
  
  (from linux prompt in GTE VM)
  ./gte-shell
  
  . /usr/local/bin/librarySetupFilesForComponent.sh && setupFilesForComponent_playwrightInstallOsLibsHelper
  ```
2. Make a daemon
  
  1. Miscellaneous → Daemons → Actions → New
  2. Config id: grouperWikiHarvest
  3. Daemon type: script daemon
  4. Script type: gsh
  5. File type: script
  6. Download this file, copy and paste script contents into textarea
  7. Search for daemon: grouperWikiHarvest
  8. Run daemon  
    
    
    ### START HERE!
3. Download the JSON file of markdown
4. Go to chatgpt
5. GPTs → Expore → Create
6. Create tab
7. Enter this and include the JSON file with '+'   
    
  
  ```
  Make an agent that answers Grouper questions based on the grouperWiki.json knowledge file.  
  
  The file is JSON where the website markdown is in the "data" array.  Each JSON object has a "url", and "contentMarkdown".  When responding, do not hallucinate, get all information from this knowledge file, and let the user know that you do not know the answer if it is not in the file.  For each response, make sure to include the link to the wiki at the bottom of the response.
  ```
8. Use the name:   
    
  
  ```
  Grouper GTE wiki assistant
  ```
9. Use the generated image
10. Type "finish" to finish
11. Click the configure tab
12. No capabilities
13. Model: 5.1
14. Click: Create button
15. Try the agent (note only 5 wiki pages are there). It should respond with the answer and the link to the right wiki  
    
  
  ```
  how do i know what gte containers are running
  ```
