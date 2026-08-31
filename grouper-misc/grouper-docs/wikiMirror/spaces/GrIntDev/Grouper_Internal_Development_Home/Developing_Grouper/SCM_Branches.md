---
title: "SCM Branches"
space: GrIntDev
pageId: 48793044
version: 11
lastUpdated: 2026-07-12T07:01:20.073Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793044/SCM+Branches
---

This document has information about how to manage branches in grouper. Assumes using Eclipse.

 

#### Standards

 Example branch name: GROUPER_1_6_BRANCH

 Example branch merge point: just commit the misc/merge.txt

 

#### How do I work in a branch?

 You need to checkout that branch from git, and work in it. Here is how:

 

1. You should probably close all projects of different branches since the names will conflict. Or you can rename with the branch in the name (e.g. grouper_trunk)
2. If you have the branch checked out, just import the project into eclipse, or open the project

 

#### Which branch should I work in, and when? How to merge?

 If you are making a change for the next major release (or before a branch has been made for a release), use trunk.

 **Note:** Anonymous access to Grouper's Git repository is provided via: [http://github.com/Internet2/grouper](http://github.com/Internet2/grouper)

 If you are making a fix for a previous branch, then:

 

1. Make the change in the oldest branch first (see instructions above)
2. Commit the change (note the commit-id)
3. Checkout the second-most-recent branch, then git cherry-pick (commit-id) where (commit-id) is the commit of your patch
4. Rinse, lather, repeat, submit a pull request.
