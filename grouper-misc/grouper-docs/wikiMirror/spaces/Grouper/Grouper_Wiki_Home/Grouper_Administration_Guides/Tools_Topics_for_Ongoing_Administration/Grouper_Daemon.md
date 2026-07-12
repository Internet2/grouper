---
title: "Grouper Daemon"
space: Grouper
pageId: 28545241
version: 49
lastUpdated: 2026-07-12T15:26:40.893Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545241/Grouper+Daemon
---

The Grouper Daemon is a process that can handle many tasks. In Grouper v2.4+, all daemon jobs are visible in the Grouper UI.

- Grouper daemon has a component called [Grouper Loader](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545200/Grouper+Loader) that can automatically provision Grouper memberships from external SQL sources
- Grouper daemon is a container that runs in tomcat
- This daemon is required for all deployments, even if you are not using it to provision Grouper memberships from external SQL sources
- There are a lot of daemons in grouper including  
  
  
  - [Disable expired memberships or to enable memberships](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544574/Grouper+enabled+and+disabled+dates) which are enabled in the future
  - Delete old audit and notification logs (configured in grouper-loader.properties)
  - Massage the notification logs so they have a sequential index number
  - Validate [Grouper Rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) and mark invalid ones as invalid. (v2.0)
- Notification consumers (callbacks) can be registered as a daemon. Grouper will keep track of which [change log](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545225/Change+log+consumers) number they have successfully processed so the daemons can maintain state across Grouper Loader restarts
- The Grouper Loader keeps database logs in the grouper_loader_log table. These are periodically cleaned out based on configuration
- There is a daily report that can be emailed out to Grouper admin which details the state of the registry and the status of all daemon jobs from the last day
- The [PSP changelog provisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543297/Provisioning+Models) can be enabled as a daemon process (v2.1)
- In v2.3+, the daemon is pre-configured to use a database to store job schedules instead of storing them locally in memory.
  
  
  
  - By default, the Grouper database is used. These are the grouper_QZ_* tables.
  - You can run the daemon on multiple machines and jobs will be spread among them all automatically. Be sure to keep your loader configuration (grouper-loader.properties) the same on all machines.
  - You can also use the daemon to schedule any custom jobs that you may have. To do so, add the following configuration in grouper-loader.properties:
    
    
    ```
    ################################
    ## Other jobs
    ##
    ## Configure other jobs.
    ## "jobName" is the name of your job.
    ## Class must implement org.quartz.Job.
    ## Priority is optional
    ################################
    
    # otherJob.jobName.class =
    # otherJob.jobName.quartzCron =
    # otherJob.jobName.priority =
    
    ```
- In Grouper v2.4+, all daemon jobs are visible in the Grouper UI. The screen below is linked from the "Miscellaneous" link in the Quick Links section.   
  You can also see their associated logs from the grouper_loader_log table by clicking on a job name.

Starting in v2.5+: run the Grouper container with the "daemon" argument or the appropriate env vars set ([see v2.5 container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation))

In v5.8+ you can unschedule a job to stop it. If the job is hung on a network call (e.g. a database lock), you cannot stop it, but if it is processing data it will stop shortly. You can do a few jstacks on the daemon process to see if it is making progress.

See this document to have a unix service which controls the loader

Note: it is also possible to run the loader in another webapp if you like

The long-term roadmap is to have the ability to run the loader in a web services instance or UI instance

**See Also**

For [Grouper v2.5+ see this page on Daemon configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547876/Daemon+configuration).  
For Grouper Daemon configuration options, see the Daemon section of the Grouper API page.  
For info on pruning Daemon logs, see [Ongoing Maintenance Tasks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549452/Grouper+data+cleanup)
