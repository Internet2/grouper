---
title: "Grouper rules pattern - Send email after new membership"
space: Grouper
pageId: 28554964
version: 12
lastUpdated: 2026-07-01T05:39:15.436Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554964/Grouper+rules+pattern+-+Send+email+after+new+membership
---

Assign this rule to the group that has the membership to be added.

# Configure rule for v5+

# Configure rule for v4 and previous

## Welcome emails

By changing the above configuration slightly, one can send out welcome emails to someone who is added to a group. In our implementation, we added the rules notification to the policy group that is provisioned, so as to take into consideration any deny groups.

| **Attribute** | **Value** |
| --- | --- |
| etc:attribute:rules:ruleActAsSubjectId | GrouperSystem |
| etc:attribute:rules:ruleActAsSubjectIdentifier | g:isa |
| etc:attribute:rules:ruleCheckType | flattenedMembershipAdd |
| etc:attribute:rules:ruleThenEnum | sendEmail |
| etc:attribute:rules:ruleThenEnumArg0 | ${ (safeSubject.emailAddress?safeSubject.emailAddress:'fallback-email-address@[example.com](http://example.com)') } |
| etc:attribute:rules:ruleThenEnumArg1 | Email Subject…. |
| etc:attribute:rules:ruleThenEnumArg2 | Hello ${safeSubject.name}, \n\n Some Welcome Text |

For our use case, we wanted to configure a welcome email to go out when someone opted into a group, in this case for Adobe Creative Cloud, assuming they are in scope to opt into the group. Below is a visualization of our configuration.

The welcome email is configured on the provisionable Grouper group called "creative_cloud_users." The "adobe_creative_cloud_opt_in" Grouper group is a group that anyone can opt-in if they are a member of the the "adobe_creative_in_scope" group. As you can see in the image above, there are 16k eligible people who could opt-in to the Adobe Creative Cloud Grouper group, but only 676 have, of which only 652 are eligible; I pre-loaded people into the "adobe_creative_cloud_opt_in" group which is why there are 676 members but only 652 are eligible.

**Note:** when you first create a Grouper group that has a rule on it, a daemon will be created in a few minutes called "CHANGE_LOG_consumer_grouperRules." To troubleshoot grouperRules, review the logs from this daemon.

## HTML welcome emails

To send out HTML welcome emails, one needs to perform the above but make the following edits:

1. Ensure that etc:attribute:rules:ruleThenEnumArg2 has valid HTML including doctype; see below for an example

Example valid HTML for etc:attribute:rules:ruleThenEnumArg2

> <!DOCTYPE html>  
> <html lang="en">  
> <head>  
> <title>Welcome to the X group</title>  
> </head>
> 
> <body>  
> <p>Hello ${[safeSubject.name](http://safeSubject.name)},</p>
> 
> <p>Welcome to the X group</p>
> 
> </body>
> 
> </html>
