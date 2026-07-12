---
title: "Penn email list provisioner"
space: Grouper
pageId: 28544202
version: 24
lastUpdated: 2026-07-12T15:26:13.554Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544202/Penn+email+list+provisioner
---

# **Leveraging Grouper's New Features to Improve Handling of Email Lists at University of Pennsylvania**

At Penn we use LSoft Listserv for email lists. We recently had a project to make it easier to manage automated lists and to improve deprovisioning of list owners and members. Using Grouper to solve these problems has allowed us to address the shortcomings of the previous service while using new features of the Grouper platform.

There are four email list roles that we are tracking in Grouper: owners, quiet owners, senders, and members. To make this easier to explain, we will just discuss owners and members, but the quiet owners and senders have similar characteristics as owners. Not to mention the typical goal of retiring bespoke unmaintainable perl scripts!

## **Integration**

We have two feeds for LSoft: one that sends current lists, owners, and members to Grouper, and one that sends members and owners of Grouper managed lists back to LSoft. Grouper integrates via SQL for both of these, and the LSoft team implemented scripts to integrate with LSoft.

## **User Feed from LSoft**

Data from LSoft to Grouper is needed for several reasons: we need to know if a list is new or not when configuring in Grouper, we need to know that the Grouper managed list corresponds to what was in LSoft for existing lists, and we want to report on LSoft lists that are not managed in Grouper. This diagram shows the data flow:

This feed is sent to two tables that Grouper can read: the names of the email lists, and the members and roles. One challenge is that users for the lists are email addresses, which are difficult or impossible to match to institutional users. These are matched by: seeing if the email address is associated with the user in the directory, seeing if the email address matches the EPPN (e.g. [jsmith@institution.edu](mailto:jsmith@institution.edu)), or seeing if it is our domain and the NetId matches the email prefix. This match is done in a view against all emails from LSoft, but it is too slothy for frequent use, so a Grouper SQL Sync is used to copy the results to an indexed table.

Grouper can sync from SQL (e.g. a view or table in any database), LDAP, web service, etc to a SQL table. The synchronization can be real time or batch. These ETL (extract, transform, load) tasks are very useful for integrations since they can move data to where you need it, or simplify complex operations into indexed results. Some opportunities with the new system are that users can see the overall view of their lists no matter what email address they used, and as they change their address in the directory it will be reflected in all email lists that Grouper manages!

Once the email addresses are matched to identities (if they can be), Grouper can use those in Grouper groups or template processes (discussed later). If email list owners do not want to manage a list in Grouper for whatever reason, they can still load the list roles into Grouper groups with the Grouper Loader. Then they can get notifications, use attestation, see point-in-time memberships, generate reports, and other features available to Grouper groups. Eventually they might change their mind!

## **Group Structures**

The group structures for the list ensure that all the roles are accounted for: automated memberships (usually [reference groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543159/Folder+and+Group+Design)), manual memberships, and external non-person email addresses (e.g. a list is a member of another list), but lists are not Subjects in Grouper!). More could be written just on this topic, but we will just briefly describe it here.

Do not be afraid of this large number of groups; the templates will be a nice facade for them. There are four groups for the four roles. There is an editors group for people who do not own the list, but who can manage memberships (e.g. help desk). The includes and excludes for people are for opt ins / opt outs, or a help desk person to intervene. The automatic group has reference groups inside (e.g. org groups or people who are IT staff). The external group has email addresses not represented as subjects. This is done with [Grouper Local Entities](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549387/Grouper+local+entities). These are objects that have security about who can use them. Then they can be used by provisioning, the UI, [GSH (Grouper Shell Script) templates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH), etc.

For the manual groups, Grouper Membership Requirements are used to ensure that when people no longer meet the requirement they are automatically removed. A recent Grouper enhancement is the ability to edit membership requirements from the “Edit group” screen. Easier deprovisioning leads to more frequent deprovisioning.

## **User Self Service**

If a list allows it, and the user is in the eligible population, users can opt in to a list. Or maybe they can just opt out. This process is a little complex and requires a friendly UI. An example of the complication is when a user wants to opt in to a list. If they are in the automatic population, and in the opt outs list (due to previously opting out, or for some other reason), then the correct action is to remove them from the opt outs list. But this is not intuitive for end users and would be time consuming for support staff to do on their behalf. So a [Grouper GSH template](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH) is used to make a UI to do the correct logic. A recent enhancement to GSH templates is the “[simplified UI” which removes all unneeded Grouper UI stuff and leaves only the part that the user needs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548015/Grouper+GSH+template+simplified+UI). The drop down of lists to subscribe to only has lists that the user is allowed to opt in to.

The result of the screen is adding or removing the user to or from the necessary groups. This is the result of opting in to a list when previously being in the opt outs.

There is a test and prod LSoft system and a test and prod folder in Grouper for these mailing lists. A recent enhancement to GSH templates allows the same template code to be used in two templates, and key off of the template ID to know if it is test or prod and adjust the queries or folders accordingly. This makes it easy to add enhancements and migrate code from the test to prod template.

## **Help Desk List Management**

Help desk workers or other people who manage lists need to be able to achieve a similar function but on behalf of other users. There is a GSH template for them to accomplish this, but only for the lists that they own or manage.

Since the inputs, logic, validation, and messaging are similar for the self-service vs admin interfaces, some of the same methods in the template are re-used in both templates.

## **Requesting a New List**

When a new list or migration of an existing list is requested, a GSH template allows the settings to be captured and sent to the email list team for review. The activation of this list can be delayed so the members can be verified before cutover. Do not activate the list until you are absolutely ready!

A new feature in GSH templates allows writing unit tests so the template logic can be easily verified from the UI on a change to the template or a Grouper upgrade. Have you tested your GSH template by hand with all the possible inputs? These tests will save you time!

When the create request is submitted, it will store the configuration settings in a database table, assign a request ID to it, and send an email to the email list team to review. When the team approves it, the Grouper folders, groups, memberships (based on feed from LSoft), membership requirements, and provisioning settings are automatically created.

This shows an approval for a list named “abc” submitted by me:

## **Provisioning the Lists to LSoft**

The new provisioning framework facilitates real time and nightly full sync provisioning of the email roles to LSoft. There are many supported provisioning targets, but in this case we are provisioning the SQL tables. Provisioning “metadata” is information on the group (or entity) which informs the target of characteristics about the group. Here is an example for an email list member role:

In this case the metadata is filled out automatically by the GSH template after the approval so users do not need to be bothered with it, but in other provisioning use cases, users might be setting things like this on provisionable objects.

## **Summary**

Rolling out this Grouper integration with LSoft is in its early phases and so far the experience has been positive. When we have integrated more lists and kicked the tires a little more we will post specifics on how things are configured and the template code needed to make this work.   
Stay tuned on the incommon-grouper slack channel!
