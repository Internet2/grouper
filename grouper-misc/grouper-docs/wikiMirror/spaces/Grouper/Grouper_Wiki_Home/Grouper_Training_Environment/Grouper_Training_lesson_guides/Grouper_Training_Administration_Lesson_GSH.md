---
title: "Grouper Training - Administration - Lesson: GSH"
space: Grouper
pageId: 28544339
version: 29
lastUpdated: 2025-04-09T00:27:07.163Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544339/Grouper+Training+-+Administration+-+Lesson+GSH
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

> If you mistype one or more lines in GSH, type :c to clear all the lines since the last executed command.
> 
> To exit GSH, type :q

## Reference Links

- Grouper wiki command reference for GSH
- Groovy syntax: [https://groovy-lang.org/documentation.html](https://groovy-lang.org/documentation.html)
- Grouper API Javadoc: [https://software.internet2.edu/grouper/doc/](https://software.internet2.edu/grouper/doc/)
- 3rd Party site Baeldung - Groovy blog posts: [https://www.baeldung.com/tag/groovy/](https://www.baeldung.com/tag/groovy/)
- 3rd Party site MrHaki - Groovy blog posts: [https://blog.mrhaki.com/](https://blog.mrhaki.com/)

## Getting started

Look at the script of the `gte-gsh` command in your home directory.

`cat ~/gte-gsh`On the last line, you can see it's logging into your container and running the `gsh.sh` command

With a GTE container started and running, GSH can initiated by running:

`gte-gsh`After some startup information, wait until the `groovy:000>` prompt.

## Example commands

### Find a subject and save to a variable

Save the banderson subject into a variable:

```groovy
def subj = SubjectFinder.findByIdentifier("banderson", true)
```

Note we could have also used `Subject subj = ...` to enforce object type checking, but it's optional.

We can call various method on this variable. Since it is a Subject, we can get the name:

```groovy
println subj.name
```

### ``Exploring interactive terminal

1. Try pressing the up arrow and down arrow. You should see the history of previous commands that you ran.
2. If the interpreter detects a multi-line command it will wait until you finish it. You can abort this with `:c`

Enter this command:

```
def myVar = (
    1 + 3
```

Notice the number in `groovy:000` incremented on each row, while waiting for the ending parenthesis. If we enter `)`, the command will finish, and the line will go be to `000`. If we instead want to cancel the command due to a mistake, enter `:c`. Try both methods to finish the above command and get back to the start of a new command.

3 ) Auto-completion

Enter the command `subj.` and hit the tab key. Notice that the interpreter tells you what methods are publicly available. The ones with parentheses may take variables, and it's hard to know what to use without documentation. But you can call the ones with no parentheses. With the cursor still at the `subj`, try some of these, like `subj.description` or `subj.sourceId`.

### Example when an error occurs

Run the following command:

```groovy
def admins = GroupFinder.findByName("etc:sysadmin", true)
```

GSH displays a friendly version of the problem (highlighted in red, depending on your console) and we can quickly see that we just put in the wrong group name. The additional stack information is helpful if you ever need to report a problem out to [Grouper's bug reporting system](https://todos.internet2.edu/projects/GRP/issues).

## Finding a group, printing its members

Knowing the correct name of the group, run the following command:

```groovy
def admins = GroupFinder.findByName("etc:sysadmingroup", true)
```

This stores a Group object into another variable.

We can fetch the members of this group, and loop through each one and print them on a single line. Note we have the group variable, so we don't need to find the group again.

```groovy
for (Member member: admins.getMembers()) {
    println "Member: ${member}"
}
```
