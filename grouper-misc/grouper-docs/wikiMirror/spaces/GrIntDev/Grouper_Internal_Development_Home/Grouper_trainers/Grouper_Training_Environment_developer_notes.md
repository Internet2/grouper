---
title: "Grouper Training Environment developer notes"
space: GrIntDev
pageId: 48793119
version: 121
lastUpdated: 2026-07-12T17:02:44.834Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793119/Grouper+Training+Environment+developer+notes
---

## Design

## Slack bookmarks

(include links)

Welcome to Grouper School! Here's some information to keep on hand so that you can access the class resources quickly:

1. Zoom - same Zoom for the course introduction, office hours, and live instruction
2. [Canvas](https://internet2.instructure.com/courses/288/): (check link!!!)
3. Pre-Course Survey - tell us what you hope to learn!
4. [Training wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541839/Grouper+Training+Environment)
5. [VM management](https://incommon.org/VM-manager)
6. [VM jump page](https://localhost:8443/)
7. Text to copy/paste
8. [101 corrections](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541839/Grouper+Training+Environment)
9. [Kahoot](https://www.kahoot.it)
10. Post-course survey

## Monday meeting

- intro: institution, role, experience with grouper, anything particular you want to get out of the training
- dont email me
- slack bookmarks at top
- multiple screens
- needle of too fast vs too slow
- sysadmin prework
- anything not on the agenda you want to cover let us know
- experts can help the people new to grouper
- speed of playback
- camera on
- get the most out of training by
  
  - doing the prework
    
    - note the corrections
  - being available during training
  - doing hands on

## Zoom standards

1. Make a recurring meeting starting 5am on start day for 24 hours, recurring for the number of days the training is
2. Meeting should have a 6 digit numeric password
3. Do not post links for the zoom coordinates except in private locations like meeting requests and slack pins
4. For alternative hosts list
  
  
  ```
  mchyzer@example.com,emurtha@example.com,chubing@example.com,emily@example.com
  ```
5. Video on for hosts and participants
6. Enable join before host
7. Mute participants on entry
8. Disable waiting room
9. Do not require authenticated participants
10. Record meeting automatically in the cloud
11. Slack the link with password, meeting id, and password to Erin (or whoever is managing the pins)
12. For the June training, ChrisHy is Main meeting, Bill is breakout #1, ChrisHu is breakout #2, Emily is breakout #3

Example pin of zoom (not actual links, those are in pin in slack)

Zoom Coordinates:

Dial by Phone: +1 312 626 6799 or +1 646 558 8656 or +1 301 715 8592 or +1 346 248 7799 or +1 669 900 6833 or +1 253 215 8782

MAIN TRAINING ZOOM:  
[https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbsdfsfdsdfBZaEVrRjdmdz09](https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFsdfsdfsdfsdfEVrRjdmdz09)  
Meeting ID: 920 7123 7697  
Password: 851808

BREAKOUT ZOOM 1:  
[https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFhPS3BMalBZaEVrRjabcd](https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFhPS3BMalBZaEVrRjabcd)  
Meeting ID: 920 123 3456  
Password: 174356

BREAKOUT ZOOM 2:  
[https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFabcdZaEVrRjabcd](https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFhPSabcdaEVrRjabcd)  
Meeting ID: 920 123 3456  
Password: 174356

BREAKOUT ZOOM 3  
[https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFabcdZaEVrRjabcd](https://internet2.zoom.us/j/92075407697?pwd=SjUvSUUvbFhPSabcdaEVrRjabcd)  
Meeting ID: 920 123 3456  
Password: 174356

## Kahoot standards

1. Use the Grouper Training folder
2. For courses, use a section folder (e.g. 101)
3. For quizzes, name with this convention: GTE 101.1: Grouper Basics
4. Do not list answers in slides
5. Do keep the questions in slides (in case Kahoot goes away)
6. Its not clear how to mute Kahoot. Right click on tab in browser and "mute site" e.g. in chrome
7. During the training, trainers should not take the quiz, as it will skew the results

## Hands on copy/paste standards

1. For a course, make a hands on wiki under [this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543462/Grouper+Training+-+lesson+guides)
2. Each thing to copy paste should be in a code block
3. For each slide that has something to copy/paste, link the first word to that course's copy/paste wiki

## Schedule

12:00-1:15 - sessions  
1:15-1:30 - break  
1:30-2:45 - sessions  
2:45-3:00 - break  
3:00-4:15 - sessions  
4:15-4:30 - break  
4:30-5:00 - sessions

| Module | Day | Who | Kahoot? (two per day) | Time | Notes |
| --- | --- | --- | --- | --- | --- |
| 201.1 | Tues | Chad? | Yes | 1:30-2:15 (with 10 min break) - 35min |  |
| 201.1.2 | Tues | Chad |  | 2:15-2:50 (35 min) |  |
| 201.2 | Tues | Chris | Yes | 3:00-3:27 (27 min) |  |
| 201.3 | Tues | Chad |  | 3:52-4:25 (33 min) | should redo this to use an attribute that doesn't already exist in LDAP. The student affiliation is already sourced in ldap, so it's confusing that we're not publishing at the ePA folder level, and just provisioning the member affiliation |
| 201.4 | Tues | Chad |  | 4:25-4:35 (10 min) | skipped the hands on except for highlights due to time; this might be folded into 201.3 as a conceptual discussion on ACM without the extra hands-on, since it's nearly identical procedures |
| 201.5 | Tues | Chris |  | 4:35-5:00 (25 min) |  |
| 211.0 | Wed | Chris |  | 1:30-1:53 (23 min) |  |
| 211.1 | Wed | Chad |  | 2:03-2:10 (7 min) |  |
| 211.2 |  | Chris | Yes | 2:10-2:34 (24 min) |  |
| 211.3 |  | Chad |  | 2:34-3:00 (26 min) |  |
| 211.4 |  | Chris |  | 3:00-3:13 (13 min) |  |
|  |  | break |  | 3:13-3:23 |  |
| 211.5 |  | Chad |  | 3:23-3:34 (11 minutes) |  |
| 211.6 | Wed | Chris | Yes | 3:34-3:50 (16 minutes) |  |
| 301.1 | Wed | Chris |  | 3:50-4:04 (14 minutes) |  |
| 301.2 |  | Chad |  | 4:04-4:16 (12 minutes) |  |
|  |  | break |  | 4:16-4:21 |  |
| 301.3 |  | Chris | Yes | 4:21-4:40 (19 minutes) |  |
| 301.4 | Wed | Chad |  | 4:40-5:00 (20 minutes) |  |
| 301.4 (cont) | Thur | Chad |  | 1:15-1:55 (40 minutes) |  |
| 311.0 | Thur | Chris | Yes | 1:55-2:15 (20 minutes) |  |
| 311.1 |  | Chris |  | 2:25:2:35 (10 minutes) |  |
| 311.2 |  | Chris |  | 2:35-3:10 (35 minutes) |  |
| 311.3 |  | Chris |  | 3:20-3:45 (25 minutes) |  |
| 311.4 |  | Chris |  | 3:45-4:10 (25 minutes) |  |
| 311.5 |  | Chris |  | 4:20-4:27 (7 minutes) |  |
| 311.6 | Thur | Chris |  | 4:27-4:35 (8 minutes) |  |
| 401.1 | Fri | Chad |  | 1:00-2:05 |  |
| 401.2 |  | Chad |  | 2:15-3:05 |  |
| 401.3 |  | Chris |  |  |  |
| 401.4 |  | Chad |  |  |  |
| 401.5 |  | Chad |  |  |  |
| 401.6 |  | Chris |  |  |  |
| 401.7 | Fri | Chris |  |  |  |

## Example of overriding a file

```
GET FILE (one time task)
wget https://raw.githubusercontent.com/Internet2/grouper/GROUPER_2_6_BRANCH/grouper-ui/webapp/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesGroupMoreActionsButtonContents.jsp

EXISTING CONTAINER (retains data, note you only need to restart if you have received the error already)
docker cp grouperObjectTypesGroupMoreActionsButtonContents.jsp 101.1.1:/opt/grouper/grouperWebapp/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesGroupMoreActionsButtonContents.jsp
docker restart 101.1.1

NEW CONTAINER (deletes data)
./gte --mount="type=bind,source=/home/student/grouperObjectTypesGroupMoreActionsButtonContents.jsp,target=/opt/grouper/grouperWebapp/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesGroupMoreActionsButtonContents.jsp" 101.1.1
```

## Upgrade environment

```
docker pull tier/gte:base-202403
docker pull tier/gte:401.end-202403
docker pull tier/gte:201.end-202403
docker pull tier/gte:101.1.1-202403
```

## GTE development

Google doc with to do's: [https://docs.google.com/document/d/1JLCuGm9pHWOirfdZmkrVsY8OlQSdaju8ldGzlUY4bnc/edit](https://docs.google.com/document/d/1JLCuGm9pHWOirfdZmkrVsY8OlQSdaju8ldGzlUY4bnc/edit)

The current working branch for November 2019 training is "201911". [https://github.internet2.edu/docker/grouper_training/tree/201911](https://github.internet2.edu/docker/grouper_training/tree/201911).

This branch is automatically built on every commit and pushed to dockerhub, with the branch name appended to the image tags. [https://hub.docker.com/r/tier/gte/tags](https://hub.docker.com/r/tier/gte/tags).

To work on updates:

1. create a feature branch off of 201911 (eg. 201911-201-updates).
2. update ./gte line 12 with your branch name (eg. replace "201911" with "201911-201-updates").
3. make local chances to exercises and content
4. run ./manualBuild.sh in whatever ex### directory you are working in. this will create docker images based on your branch name
5. run ./gte 201.1.1 (this will start the correct local version based on your branch name and link to rabbitmq)
6. test/review changes at [https://localhost:8443/grouper](https://localhost:8443/grouper)
7. push local commits/branch to github
8. Merge changes into needed branch when completed.
  
  1. Log into github and go to "Branches"
  2. Choose "New pull request" on your feature branch
  3. **Make sure to update base: master to base: 201911**
  4. Add comment and choose "Create pull request" underneath comment section
  5. Choose "Merge pull request"
  6. Choose "Confirm Merge"
  7. Choose "Delete Branch" if done with feature branch.

Developer flow looks like this:

1. make local changes
2. ./manualBuild.sh - build local docker images with tags based on branch name
3. docker ps - check to see what's running.
4. docker stop 201.1.1 - stop an image
5. ./gte 201.1.1 - start an image
6. test/review changes at [https://localhost:8443/grouper](https://localhost:8443/grouper)
7. repeat.

```
docker restart rabbitmq
./gte 101.1.1
```

```
$ docker logs 101.1.1 to bide the time.
$ ./gte-shell 101.1.1
```

stop

| `docker ps`   `docker stop imageId` |
| --- |

list of sections:

https://docs.google.com/spreadsheets/d/1Emf79h–2lmfSxaNgjVu5lRcboJ1IWkwSmh_6E5T2lo/edit#gid=0

Final date to get new patches from Grouper: June 3rd. (maybe 7th). Babb clone VMs on 10th.

Run locally or from docker hub.

```
docker exec -it {container name} /bin/bash; cd bin; gsh;
```

## Links

Admin notes for 2021: [https://docs.google.com/document/d/1ao9isNiUij4L_5oPudukUchYClu1lH6SnaAKsOJpIlM/edit#heading=h.vpz3cm3s553o](https://docs.google.com/document/d/1ao9isNiUij4L_5oPudukUchYClu1lH6SnaAKsOJpIlM/edit#heading=h.vpz3cm3s553o)

[https://github.internet2.edu/docker/grouper_training/tree/](https://github.internet2.edu/docker/grouper_training/tree/201906)[202006](https://github.internet2.edu/docker/grouper_training/tree/202006)

[Grouper training VM documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543607/Grouper+training+VM+documentation)

Grouper email list: grouper-2019-06 at internet2 dot edu

```
gte $ git clone https://github.internet2.edu/docker/grouper_training.git
grouper_training $ git checkout 201906
grouper_training $ ./manualBuild.sh 
```

[https://github.internet2.edu/docker/grouper_training/blob/202006/README.md](https://github.internet2.edu/docker/grouper_training/blob/202006/README.md)

Google drive for slides: [https://drive.google.com/drive/u/0/folders/1irHccRqkfrwkeXpA2ayQPEt2PlI67Cx5](https://drive.google.com/drive/u/0/folders/1irHccRqkfrwkeXpA2ayQPEt2PlI67Cx5)

Google drive for training revamp: [https://drive.google.com/drive/folders/1nNqrzQW2bXGvoyCB88SevYVk-ctlA4mb?dmr=1&ec=wgc-drive-hero-goto](https://drive.google.com/drive/folders/1nNqrzQW2bXGvoyCB88SevYVk-ctlA4mb?dmr=1&ec=wgc-drive-hero-goto)

Course outline: [https://docs.google.com/document/d/1o8siMrRxZm1M_DgDQ5qu6jZEE4mb4MODxfQVvIxG508/edit](https://docs.google.com/document/d/1o8siMrRxZm1M_DgDQ5qu6jZEE4mb4MODxfQVvIxG508/edit)

Tracking sheet: [https://docs.google.com/spreadsheets/d/1A5Eok-GWBJS4zMwsAvS7k0YEkR4OnZz-74B_yRLeRPg/edit#gid=0](https://docs.google.com/spreadsheets/d/1A5Eok-GWBJS4zMwsAvS7k0YEkR4OnZz-74B_yRLeRPg/edit#gid=0)

Folder for pres: [https://drive.google.com/drive/u/0/folders/1irHccRqkfrwkeXpA2ayQPEt2PlI67Cx5](https://drive.google.com/drive/u/0/folders/1irHccRqkfrwkeXpA2ayQPEt2PlI67Cx5)

Dec 2019 Temple training notes: [https://docs.google.com/document/d/1nfkt8tgv05F_fQwdQc5Ru_A4tv79FFl7JdeAq3tO3rI/edit#heading=h.2zrn2o90s27q](https://docs.google.com/document/d/1nfkt8tgv05F_fQwdQc5Ru_A4tv79FFl7JdeAq3tO3rI/edit#heading=h.2zrn2o90s27q)

Canvas: [https://internet2.instructure.com/](https://internet2.instructure.com/)

Module Guide: [https://docs.google.com/spreadsheets/d/1kfM1QkqQZ9oNFsFm6X5FEye6dMXHPl0Has5HAszj1A0/edit#gid=0](https://docs.google.com/spreadsheets/d/1kfM1QkqQZ9oNFsFm6X5FEye6dMXHPl0Has5HAszj1A0/edit#gid=0)

Drive Folder: [https://drive.google.com/drive/u/0/folders/1lsXmAfE5bVgfbRXowRFHELkHzl553LHw](https://drive.google.com/drive/u/0/folders/1lsXmAfE5bVgfbRXowRFHELkHzl553LHw)

LMS Needs for Grouper: [https://docs.google.com/document/d/1c_hqXvKIfuUFmiUoGVMyAaKyuJSPZF1nbOmddkgvbtA/edit#heading=h.xq1bphtw3tjm](https://docs.google.com/document/d/1c_hqXvKIfuUFmiUoGVMyAaKyuJSPZF1nbOmddkgvbtA/edit#heading=h.xq1bphtw3tjm)

Training notes: (feb 2021) [https://docs.google.com/document/d/1pftf1qkkviWuU9O1eTghIehXjeTdeFfpBzAaBbAsThk/edit](https://docs.google.com/document/d/1pftf1qkkviWuU9O1eTghIehXjeTdeFfpBzAaBbAsThk/edit)  
(jun 2021) [https://docs.google.com/document/d/12WoWcOCG5uXB52CwM_2xWfK9djsuxrF0bN17J8rTGkU/edit](https://docs.google.com/document/d/12WoWcOCG5uXB52CwM_2xWfK9djsuxrF0bN17J8rTGkU/edit)  
(sept 2021) [https://docs.google.com/document/d/1ClKB-SAkoFQvZb4naEK9IwDHXeD68_w21rRYRnVlsnI/edit](https://docs.google.com/document/d/1ClKB-SAkoFQvZb4naEK9IwDHXeD68_w21rRYRnVlsnI/edit)  
(feb 2022) [https://docs.google.com/document/d/10lA1MxSgQjZAoYXpRN79tODaaGKbOdCQFAe-znT7WIg/edit](https://docs.google.com/document/d/10lA1MxSgQjZAoYXpRN79tODaaGKbOdCQFAe-znT7WIg/edit)  
(may 2022) [https://docs.google.com/document/d/1ctUAgEm41SmmORRv3lJfzeUIJZZFYWx1lNOShVO0dKs/edit](https://docs.google.com/document/d/1ctUAgEm41SmmORRv3lJfzeUIJZZFYWx1lNOShVO0dKs/edit)

New folder for training on demand: [https://drive.google.com/drive/folders/1bRgLnks03JoOt2IknOoSWHy8BJX1RmQG?usp=drive_link](https://drive.google.com/drive/folders/1bRgLnks03JoOt2IknOoSWHy8BJX1RmQG?usp=drive_link)

New training materials: [https://drive.google.com/drive/folders/1HTgP7F7RtJgvi_3Ebrnc8jKLJXaRvsz0](https://drive.google.com/drive/folders/1HTgP7F7RtJgvi_3Ebrnc8jKLJXaRvsz0)

Add hours to training vms gte (search for subject ID by email then use that, e.g. chris.hyzer.3@example.com: [https://grouper.at.internet2.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Template.newTemplate&stemId=3684c051fc2a4b6d870f2d798a75b7ba&templateType=GTERegisterCourse](https://grouper.at.internet2.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Template.newTemplate&stemId=3684c051fc2a4b6d870f2d798a75b7ba&templateType=GTERegisterCourse)

## TODO for spring 2022

- installer, remove?
- postman client
- smtp external system

## Notes for later

SSH command easier to access

```
Wil Cooley  1:37 AM
FYI, for those on Linux/Mac (or otherwise using a command-line OpenSSH) if you want to not have to remember the IP address of your VM or type the extra options for remote username and port forward, you can throw something like the following into ~/.ssh/config:
Host grouper-training
  Hostname 18.218.158.77
  User student
  LocalForward 8443 localhost:8443
  PermitLocalCommand true
  LocalCommand python -m webbrowser -t https://localhost:8443
1:42
You need to have session multiplexing (ControlMaster et al) setup elsewhere though, or you’ll get errors trying to make more than one connection (not to mention spawn a bunch of open tabs).
And if you have SSH keys, ssh-copy-id grouper-training so you can forget the password too… (edited) 

```

## Patch the container

Example for patching the container

```
[student@ip-172-31-26-62 ~]$ ./gte-shell
[root@3d5dbd76ec9a WEB-INF]# sudo -u tomcat bash
[tomcat@3d5dbd76ec9a WEB-INF]$ cd classes
[tomcat@3d5dbd76ec9a classes]$ mkdir -p edu/internet2/middleware/grouper/cfg/text
[tomcat@3d5dbd76ec9a classes]$ cd edu/internet2/middleware/grouper/cfg/text
[tomcat@3d5dbd76ec9a text]$ wget https://raw.githubusercontent.com/Internet2/grouper/refs/heads/GROUPER_5_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/cfg/text/GrouperTextContainer.java
[tomcat@3d5dbd76ec9a text]$ javac -cp /opt/grouper/grouperWebapp/WEB-INF/classes:/opt/grouper/grouperWebapp/WEB-INF/lib/*:/opt/tomcat/lib/* GrouperTextContainer.java
[tomcat@3d5dbd76ec9a text]$ exit
exit
[root@3d5dbd76ec9a WEB-INF]# exit
exit
[student@ip-172-31-26-62 ~]$ docker restart 101.1.1
101.1.1
```
