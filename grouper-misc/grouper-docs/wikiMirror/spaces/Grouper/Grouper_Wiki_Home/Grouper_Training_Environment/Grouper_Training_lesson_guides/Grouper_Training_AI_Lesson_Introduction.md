---
title: "Grouper Training - AI - Lesson: Introduction"
space: Grouper
pageId: 28544868
version: 28
lastUpdated: 2025-12-08T16:34:43.073Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544868/Grouper+Training+-+AI+-+Lesson+Introduction
---

**Getting started**

If you are doing the hands-on exercises

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM) (for 5 hours). Wait 3 minutes and SSH in and sign in.

1. Sign up for openai ChatGPT Plus, $20/month, pay monthly for one month for $20. 
  
  1. When we are done with the training, cancel this subscription so you are not charged next month.
2. Go to: [https://platform.openai.com/settings/](https://platform.openai.com/settings/), if you have to create an account there, use the same email address as chatgpt. 
  
  1. Click on "billing" from that link, add payment details, and add $5, do not enable auto-recharge.
  2. When we are done with the training you can close this account too.
3. You will be reimbursed $25. 
  
  1. Don't be weird about it.
  2. Get it at front of class.

#### Maturity level 1 - new question

Enter this into a new ChatGPT session

```
write a Grouper GSH script to create a group named 'test:testGroup'	
```

> You should get back either one or two suggestions. One will say to use helper methods like addStem and addGroup. The other method using GroupSave() is valid Java, but not valid for the GSH interactive terminal. The correct GroupSave syntax should be:
> 
> 
> ```
> new GroupSave().
>   assignName("test:testGroup").
>   assignDisplayName("Test: Test Group").
>   save()
> ```

In your terminal type:

```
[student@ip-172-31-30-141 ~]$ ./gte-gsh
```

Try out ChatGPT's response. Converse with ChatGPT if you get an error message until it creates the group (e.g. remind it to create stem first, or whatever)

See the group created in Grouper: [https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=test](https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=test)

#### Maturity level 2 - conversation

In the same ChatGPT conversation, enter this if method chaining was not previously used

```
Write it again with method chaining for test:testGroup2
```

Enter this if method chaining was previously used

```
Write it again without method chaining for test:testGroup2
```

Enter that script in your GTE, see the group in Grouper

If you need to, give this to AI as an example of method chaining

```
This is an example of method chaining

new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();

Also, this is groovy put the command on one line or have the dots at the end of the line or else it thinks the line is done
```

#### Maturity level 3 - script

Enter this in ChatGPT, and (ChatGPT plus sign) attach this file  in same submission

```
write a python script that adds odd numbered subject ids to testGroup and even numbered subject ids to testGroup2 from this CSV. 
Run it and give me the GSH script.
```

You don't need to run the python yourself. Did it use a python script? There should be all 26 subject IDs in the gsh script. If not, tell it to run again and return all 26 rows. Check the counts testGroup (15), testGroup2 (11). Ask AI what it did

```
was that based on python or not?
```

Ask for the script:

```
show the python code
```

Make sure test:testGroup and test:testGroup2 exist in grouper and you can run the script if you like in GSH
