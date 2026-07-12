---
title: "Git Walkthrough"
space: GrIntDev
pageId: 48793798
version: 15
lastUpdated: 2026-07-12T07:02:05.308Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793798/Git+Walkthrough
---

The following steps below outline and demonstrate the use of git CLI to interact with the Grouper codebase now on Github. These try to show the most common operations, but don't pretend to be comprehensive in any way. For full details, please review the Git documentation.

 **Switch to a local branch:**

 
```

git checkout GROUPER_2_1_BRANCH

```

 Note: Make sure you have no modified files that are not committed. Otherwise, git will ask you to clear your changes before you can switch branches.

 **See what branches are available and which one you are on:**

 
```

git branch

```

 **Git the status of this branch, (files modified, renamed, added, etc)**

 
```

git status

```

 **Create a branch based on the branch you are on now and switch to it after creation:**

 
```

git checkout -b GRP-1234

```

 **Make changes (i.e. README.md file). Once you had made changes, review changes.**

 
```

git status

```

 **add all changes to the git staging area for this branch**

 
```

git add --all

```

 **Commit changes to this branch**

 
```

git commit -m "My changes are now committed"

```

 **Review the git log**

 
```

git log | less

```

 **Find the commit id for the latest commit (commit 93d655ef8…) and use that to git a list of all files committed under that id**

 
```

git show --pretty="format:" --name-only 93d655ef80

```

 Note: just the first 6 chars or so of the commit id should usually suffice.

 At this point you're ready to push the GRP-1234 branch to the remote.

 **See remotes**

 
```

git remote --v

```

 **Push changes from the local branch GRP-1234 to a remote tracking branch by the same name**

 
```

git push origin GRP-1234

```

 **Git a list of remotely tracked branches:**

 
```

git branch --r

```

 **Switch to the master branch**

 
```

git checkout master

```

 **Pull latest changes from master**

 
```

git pull origin master

```

 **Reset and clean any file that is uncommitted but modified**

 
```

git reset --hard
git clean --fd

```

 **Checkout the branch again**

 
```

git checkout GRP-1234

```

 **Change a few more files, and add, and commit them again**

 
```

git add --all
git commit -m "Another changeset going in"

```

 **Rebase the last 2 commit messages; allows you to pick/squash commits****via an editor and construct a new commit message for the squashed commit**

 
```

git rebase -i HEAD~2

```

 **or you can use autosquash to be not be prompted**

 
```

git rebase --autosquash HEAD~2

```

 **Switch to a local branch:**

 
```

git checkout GROUPER_2_1_BRANCH

```

 **Merge the GRP-1234 with GROUPER_2_1_BRANCH**

 
```

git merge GRP-1234

```

 **Check out master**

 
```

git checkout master

```

 **Get a list of all commits between GROUPER_2_1_BRANCH and GRP-1234**

 
```

git log GROUPER_2_1_BRANCH..GRP-1234--pretty=format:%H --abbrev-commit

```

 **Cherry pick a set of commits that are in the GRP-1234 branch and merge them into master**

 
```

git cherry-pick 71c524b36073dc33d940dfe21cd2376998b7f7de^..73c3afdaabdbb26ca4fd66e2310fe304a2b84130

```

 **When you are done, you can merge GRP-1234 with master:**

 
```

git checkout master
git merge GRP-1234

```

 **Look at the master and you'll see the commits from GRP-1234 ONLY!**

 
```

git log | less

```

 **Delete local branches that have been merged and whose upstream has been deleted**

 
```

git branch --merged | grep -v '*' | xargs -n 1 git branch -d

```

 **If there is a conflict on a pull, this might help**

 
```

first do a commit of your changes

 git add *
 git commit -a -m "auto dev server commit"

then fetch the changes and overwrite if there is a conflict

 git fetch origin master
 git merge -s recursive -X theirs origin/master

```

 or

 
```

git add .
git commit
git pull

```

 try (to say use the file on the system if there are conflicts)

 
```

git checkout --ours <file-with-conflicts>

```

 s
