---
title: "Grouper managing cloud licenses to reduce costs"
space: Grouper
pageId: 28544367
version: 4
lastUpdated: 2026-07-01T05:48:33.570Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544367/Grouper+managing+cloud+licenses+to+reduce+costs
---

We are trying to managing Atlassian licenses so we do not need to license users who are not using the product. This is different than when we had Atlassian "on-prem" since licenses were more inexpensive on-prem.

- Automatically license some users so they do not need to be bothered
- Users who claim licenses (an easy to use opt-in), keep their license unless they have not used the product for 60 days
- When the Grouper Atlassian SCIM provisioner adds a user to the "has a license" group in the cloud, they are a paid licensed user. When the provisioner removed them from that group, they are no longer a paid licensed users
  
  - Note, in Jira, sometimes users need licenses so they can be assigned Jiras, even if they have not logged in for a while

Our analysis has determined

- We had a 2k user max on-prem
- For Jira and Confluence we were using around 700 seats each
- Only 200-300 users have used Jira / Confluence in last 60 days
- Reducing the number of licenses that we pay for in the cloud will save us $20k (combined for Jira/Confluence)

Users in groups sent to Atlassian do not necessarily have licenses "Atlassian groups". If you have a Grouper group which has logs of who has logged in to an SP recently, you can have inactivity based license granting. A "custom UI" can allow users who are eligible to have a license to claim a license until they stop using Atlassian cloud for a certain amount of time. Note if you have SAML connected to your domain, you do not know based on IdP logs if a user is connected to your Jira/Confluence, but rather if they are using any product in Atlassian cloud. This can be ignored and considered an edge case.

You can set a group which is provisioned to allow those users to have a license in Atlassian "Has a license".

1. Atlassian groups have full populations, but users might not have licenses
2. Automatic population gets a license (groups of people who use Jira often and should not be bothered by claiming)
3. Allowed to claim license
  
  1. IT dept can claim (if not automatic)
  2. Certain client groups (i.e. Atlassian groups) can claim license
4. Allowed to keep license are people who use the Custom UI to claim their license (in claimed group), and either have claimed within last 60 days, or have logged in to Atlassian cloud in the last 60 days)
5. Has a license group gets licenses (if they are active at Penn) (see visualization above)

This is built with:

1. Have a static HTML "jump page" so users can easily know where to go since the error message at Atlassian cannot be customized  
  
  
  1. This has links to the cloud Atlassian product, and the Grouper Custom UI so users can troubleshoot their access and claim a license
2. [Custom UI to allow people to easily claim license](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555220/Grouper+Custom+UI+example+to+analyze+Jira+cloud+account) and analyze/troubleshoot their access state
3. [Splunk loader](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560387/Grouper+daemon+other+job+GSH+script+to+manage+group+memberships+from+Splunk+WS) to know who has logged in in last 60 days
4. [Loader to manage who is allowed to keep license](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555121/Grouper+Loader+example+of+users+who+have+been+recently+added+or+in+another+group)
5. [Rule to remove people from claimed license](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554956/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group+2) when their inactivity reaches 60 days
6. [Grouper → Atlassian SCIM provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28565516/Grouper+provisioning+SCIM+for+Atlassian+example+with+local+entities)
