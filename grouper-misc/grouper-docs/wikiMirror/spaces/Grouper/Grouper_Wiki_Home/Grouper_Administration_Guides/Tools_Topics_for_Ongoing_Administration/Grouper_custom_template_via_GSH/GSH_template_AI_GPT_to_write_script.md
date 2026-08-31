---
title: "GSH template AI GPT to write script"
space: Grouper
pageId: 28547932
version: 4
lastUpdated: 2026-07-01T05:45:43.345Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547932/GSH+template+AI+GPT+to+write+script
---

There is a [GPT training file in git](https://github.com/Internet2/grouper/blob/GROUPER_5_BRANCH/grouper/misc/aiGsh/aiGsh.txt) that you can use to train a GPT for your institution. If you want to help edit the file please make a pull request or post a new file to the incommon-grouper slack channel.

This [video shows how to use the GPT](https://www.youtube.com/watch?v=qprJ4rOB4Ow). Watch at 1.5x and it is 12 minutes.

1. In a licensed ChatGPT account (or in another AI tool), make a GPT
  
  1. Click on "explore GPTs"
  2. Create +Create
2. Name: Grouper GSH template script
3. Description: This will write a Java GSH template for Grouper
4. Instructions
  
  
  ```
  Use the uploaded file of examples to write a GSH template for Internet2 Grouper.
  
  Write an AI GSH GrouperShell Java script to accomplish a specific task.
  
  This is a simple GSH script.  Note, use all imports whether they are needed or not.  Just in case.
  Write valid Java, but so it works in groovy.  For instance do not have java that uses multiple lines.  Do not use method chaining.
  Do not break up string concatenation across multiple lines.  You can use triple quoted strings if necessary.
  Queries should be in triple quoted strings.
  
  There are labeled blocks in this training file but you do not need blocks in the actual GSH generated script.
  ```
5. Upload training file. Save [this file from git](https://github.com/Internet2/grouper/blob/GROUPER_5_BRANCH/grouper/misc/aiGsh/aiGsh.txt) (and periodically get the new file as others crowd-source it), and upload to the GPT
6. Capabilities: Web search, canvas

## Vibing with Grouper GSH AI

1. If its a GSH template, tell AI to output to screen, if it is a GSH script (non template), tell it to use System.out.println
2. Tell it not to use Java """ on same line as first part of string
3. Tell it not to declare arrays inline, use lists with GrouperUtil.toList
4. Tell it to validate a GSH template before returning to screen
5. If it is a GSH script (not template), tell it to open a root grouper session at start
6. Tell is to not use replaceAll (if not a regex), use StringUtils.replace
7. If it makes sense, tell it to check if groups exist before using them, and print a message if not
