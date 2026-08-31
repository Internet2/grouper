---
title: "Generated javadoc and site reports"
space: GrIntDev
pageId: 48792094
version: 16
lastUpdated: 2026-07-12T07:01:06.396Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792094/Generated+javadoc+and+site+reports
---

Grouper Javadoc, cross-referenced source code, and other project and module reports are at [http://software.internet2.edu/grouper/doc/](http://software.internet2.edu/grouper/doc/) . The website has subdirectories for the currently active and recent versions, and the alias [*main*](https://software.internet2.edu/grouper/doc/main/grouper-parent/index.html) links to the currently active Git branch. The active Git branch is updated on the 3rd and 18th of every month by a cron job on i2midev6 (service user grprdist) , and pulled via rsync to webprod3. The same site can also be built locally using Maven command `mvn -f grouper-parent --clean site site-deploy`. The `site` directive will build the site locally in a subdirectory of grouper-parent, and `site-deploy` will stage it in directory /tmp/groupersite.

Reports available for each release module include:

| Document | Description |
| --- | --- |
| Javadoc | Javadoc API documentation. |
| Test Javadoc | Test Javadoc API documentation. |
| Source Xref | HTML based, cross-reference version of Java source code. |
| Test Source Xref | HTML based, cross-reference version of Java test source code. |
| Dependency Updates Report | Provides details of the dependencies which have updated versions available. |
| Plugin Updates Report | Provides details of the plugins used by this project which have newer versions available. |
| Property Updates Report | Provides details of properties which control versions of dependencies and/or plugins, and indicates any newer versions which are available. |
| Tag List | Report on various tags found in the code. |
| dependency-check:aggregate | Generates an aggregate report of all child Maven projects providing details on any published vulnerabilities within project dependencies. This report is a best effort and may contain false positives and false negatives. |

## Building the public-facing site

1) On i2midev6, edit /var/grouper-docs/bin/buildGrouperDocs.sh

Change the branch it's checking out. Ideally this should be the same as what is in Git, file grouper/misc/buildGrouperDocs.sh. Once it's working on i2midev6, you can check the changes into Git.

2) Force create the new branch so pulls work

The installed git version on i2midev6 is very old; normal branch creation doesn't work well

`cd /var/grouper-docs/git/grouper sudo -u grprdist vi .git/config sudo -u grprdist git checkout GROUPER_4_BRANCH`

> [remote "origin"]  
>  url = [https://github.com/Internet2/grouper.git](https://github.com/Internet2/grouper.git)  
>  fetch = +refs/heads/GROUPER_2_6_BRANCH:refs/remotes/origin/GROUPER_2_6_BRANCH  
> + fetch = +refs/heads/GROUPER_4_BRANCH:refs/remotes/origin/GROUPER_4_BRANCH  
>  [branch "GROUPER_2_6_BRANCH"]  
>  remote = origin  
>  merge = refs/heads/GROUPER_2_6_BRANCH  
> +[branch "GROUPER_4_BRANCH"]  
> + remote = origin  
> + merge = refs/heads/GROUPER_4_BRANCH

``3) Add a new crontab line for the build job, 1 minute in the future

> [@i2midev6 ~]$ sudo -u grprdist crontab -e
> 
> 09 13 * * * /var/grouper-docs/bin/buildGrouperDocs.sh > /var/grouper-docs/log/grouper-docs.log 2>&1

``May take about 45 minutes to complete. Check the output in /tmp/groupersite, plus the log file in /var/grouper-docs/log/grouper-docs.log

Delete the crontab entry once it completes successfully. Going forward, it should only update on the 3rd and 18th.

4) Update target folders on webprod3

```bash
cd /home/htdocs/software.internet2.edu/grouper/doc/
mkdir 4.x
rm main master
ln -s 4.x main
ln -s 4.x master
nano index.html    # edit links for branches and download packages
```

``

5) Force copy the new site into the folder (rather than waiting for the rsync scheduled job to pick it up)

There is some kind of rsync job running on webprod3 as user grprdist that will pick up the site from i2midev6. The admins didn't want i2midev6 to be pushing the files since webprod3 is a production machine, so instead there is a pull from webprod3

```bash
rsync -rtzv i2midev6:/tmp/groupersite /home/cer/grouper-doc/4.x
mv 4.x/* 4.x
```

``

6) Verify public page [https://software.internet2.edu/grouper/doc/](https://software.internet2.edu/grouper/doc/)

7) Fix file permissions

We don't have root on webprod3, so the cron job may fail since we can't set the user/group permissions in a way that grprdist can update the files. Get an admin to do this

```bash
chown -R grprdist:grprdist /home/htdocs/software.internet2.edu/grouper/doc/4.x
```

``

## Legacy Javadoc

Between 2014 and 2019, the git branch `gh-pages` was updated with the javadoc on every commit, which effectively published it on [http://internet2.github.io/grouper/](http://internet2.github.io/grouper/) . That method has been decommissioned due to the size explosion in the git repository. The main page of the github.io site now redirects to the software.internet2.edu site.
