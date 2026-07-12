---
title: "Grouper minor upgrade"
space: GrIntDev
pageId: 48793441
version: 7
lastUpdated: 2012-08-16T00:45:57.171Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793441/Grouper+minor+upgrade
---

> [http://www.youtube.com/watch?v=dE67V38Jes0](http://www.youtube.com/watch?v=dE67V38Jes0) This topic is discussed in the ["Grouper Minor Upgrade" training video](http://www.youtube.com/watch?v=dE67V38Jes0).

 There are two ways to do a minor upgrade.

 

1. Take parts of the new download and put into an existing deployment. e.g. take the new grouper.jar and put it in the grouper UI and WS. This is not recommended since it is difficult to know which files exactly need to be upgraded
2. (recommended) Create a new UI/WS etc and merge your config changes into it.

 Here is the second option fleshed out

 

1. Read the upgrade instructions (e.g. [2.1](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793429/v2.1+Upgrade+Instructions+from+v1.6)) and changes for version (e.g. [2.1](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793439/Grouper+changes+v2.1))
2. [Download the required components](http://www.internet2.edu/grouper/software.html) (note you can edit the download URL to get previous versions)
3. Copy your config files into the downloaded unzipped file structures (might want to keep track of config changes you make to make this easier)
4. Compare and merge the new config files into your config files. i.e. put in the new options with default values where applicable
5. Run and test the components
6. To migrate, turn off your production components
7. Run the change log job one last time:  
   gsh 1% grouperSession = GrouperSession.startRootSession();  
   gsh 2% loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog");
8. Turn on the new services
9. Test the new deployment
