---
title: "Newcastle University Introduction"
space: Grouper
pageId: 28543399
version: 46
lastUpdated: 2026-07-12T15:26:07.178Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543399/Newcastle+University+Introduction
---

## **Newcastle University Welcome Page**

Newcastle University has now been using Grouper since 2007, with one of the first use cases being to use Grouper to manage access to the University's wiki service. The use of Grouper has grown significantly over the years, with the loading in of HR and student data vastly improving the level of delegation and access control we can now provide. Recent developments have seen us making use of Grouper's LDAPPC connector to provision groups from Grouper into the Active Directory.

This page provides details of how we are using Grouper at Newcastle University, providing examples of configurations for certain the uses of some of the components of Grouper. It will also provide links to outputs from projects that Newcastle University have run which are focused around the use of Grouper.

**Grouper UI 2.2 Testing April 2014:** Grouper UI 2.2

**Contribution from Newcastle in May 2013**: [Grouper InfoGraphic](http://www.ncl.ac.uk/itservice/group-management/infographic/grouperinfographic.jpg)

### **Update: the service in 2016**

The last 4 years have seen steady growth in the service and grouper is embedded as the default choice for group management and access control in the university.

The Grouper service is used to provide scale-able manageable access control to 23 major systems in use across the university. The Diagram below describes the integration. (FIM = Forefront identity management, SAP SLCM = student life cycle management, IDFS = Institutional data feed service).

The service was recently upgraded to version 2.2. The new functionality of real time provisioning is a welcome improvement, in particular access to team file-stores is much easier to setup and manage.

**Usage summary**

In order to give an idea of the scale grouper operates at in the university and the value it delivers, we have shared some service metrics.****

**Grouper size:**

| **memberships:** | 795,961 |
| --- | --- |
| **groups:** | 13,402 |
| **members:** | 59,671 |
| **folders:** | ****760 |

**Grouper use:**

Average number of monthly **users** of the service management interface: **80**

Average number of monthly **logins** to service management interface: **550**

Average number of monthly **manual changes** (i.e. done by a person) to groups: **570**

Average number of monthly **changes to access control groups** (i.e. the result of the combination manual intervention and data driven groups): **14,000**

The key benefit of grouper service is that is allows the university to leverage its' institutional data to deliver a manageable access control platform. 80 users being able to deliver **14,000** access control changes a month while only having to make**570** manual changes is the crux of this value. The web based interface means that many of those 80 users are non technical and could control access to their resources easily. Prior to using grouper providing access control to 23 systems would have resulted in much time consuming work for administrators, grouper has removed much of that work.

**Example of business benefit "Dreamspark"**

An example of how being able to easily management complex access control groups delivers real benefit is providing student access to Microsoft Dreamspark. Microsoft provide the Dreamspark offer which enables STEM students to use most of their software for free and for Arts students to access some of Microsoft's development software for free. Staff teaching or supporting IT for those students also get access. While these rules are easy to write in English they are not traditionally easy to implement in access control systems. Grouper enabled the university to setup a simple STEM access group with science students, teaching staff and IT support officers in it. We then granted access to the STEM offer based on that group. The setup involved manually adding 39 memberships to the group, this then resulted in 16,000 STEM users being granted access. Prior to using grouper manual registration had meant only around 300 users had access, post grouper 16,000 has access. As a result of the changes grouper enabled Students have downloaded over **£4 million** worth of software for educational use (~$6 million in USD) . Simple, manageable, scale-able access control delivers real benefits to a university.

### The story of 2012

2012 brought a number of new developments with the use of Grouper at Newcastle University.

The main development was the provisioning of groups from Grouper into our Active Directory. Prior to 2012, we only provisioned a select number of groups into the AD on a case by case basis. In April 2012 the decision was made to provision all groups that reside in our Application stem within Grouper into the AD. There were a number of reasons for doing this, first of all to improve the resilience of Shibboleth querying group memberships from Grouper (previously Shibboleth queried the Grouper database directly). The second reason was to extend the use of groups past controlling just web resources, so now a group could be set-up which controls access to a wiki, blog, filestore and so on.

We now provision over 6000 groups into the AD, made up of over 150,000 memberships, and these numbers are continually increasing as new use cases are identified.

One of the main projects in 2012 at the University has been the restructuring of the University's central services filestore service and how access to the filestores is controlled. Previously administration for filestores involved administrators manually updating access groups membership lists, which often meant that as staff moved departments or left the University, their access was not updated. With the use of Grouper this has now changed, access to filestores is now based on departmental Grouper groups, with membership of these groups being automated based on the University's corporate data. This means as staff join/leave or move around the University they are automatically granted the correct access to filestores, dramatically decreasing the amount of administration required. The delegation of administration for these groups has now been passed on to the University's IT service desk, desktop support teams and in some instances individuals outside of the IT department, all through the use of the Lite Ui. This allows the end users to take control of who should have access to the resources, and allows IT resources to be channelled into development of new services rather than having to worry about maintaining group memberships. Access to over 400 network shares are now managed by Grouper groups.

Another project that has incorporated the use of Grouper is work around "hot desking" and ensuring that staff members have access to the applications they require wherever they work. Our application support team have created over 40 groups representing different applications such as Skype, Filezilla, with departments/individuals assigned membership to these groups. These groups are provisioned into the Active Directory so that they can be used with the deployment of Microsoft's App-v and RDS so that applications follow the user.

One final recent development is that Grouper is now being used to manage access to Microsoft Dreamspark premium. Previously a manual administration process was required to allow 700 members to access Dreamspark. Now with the use of Grouper, 14,000 users will be able to access Dreamspark with minimal administration required.

In 2013, we hope to upgrade Grouper to a more recent version (currently using 1.6), and with this we hope to take advantage of PSP to allow for real time provisioning to our AD. We are also keeping a keen eye on the development of a new Grouper Ui!

Any progress over 2013 will be added on this page!

### **Configuration**

This page will provide details of how we are using Grouper at Newcastle University, providing examples of configurations for certain the uses of some of the components of Grouper. and providing details of use cases for Grouper at Newcastle.

Rampart with the Grouper web services - details of the configuration involved in enabling rampart with Grouper.

Protecting Grouper Ui's with Shibboleth - configuration details to protect the main UI and Lite UI with shibboleth.

LDAPPCNG Provisioning to the Active Directory - configuration details for the provisioning of groups and memberships to the Active directory, including group filtering.

### Use cases

As part of a JISC funded project at Newcastle University called the [GRAND](http://research.ncl.ac.uk/grand) project we have documented a number of use cases where we have used Grouper as a solution for the delegation of access control to different resources and systems.

[Reading List System](http://research.ncl.ac.uk/grand/docs/WP3-Reading%20list%20Use%20Case%2020-04-2010.pdf) - using Grouper with a newly developed system by the Library for the management of course leader reading lists.

[Syllabus Plus Room Booking Service](http://research.ncl.ac.uk/grand/docs/WP3-Reading%20list%20Use%20Case%2020-04-2010.pdf) - an example of using Grouper with a 3rd party application to provide a role based access solution for Newcastle University's room booking system.

[Newcastle University Wireless Access](http://research.ncl.ac.uk/grand/docs/Use%20Case%20Wireless%20Access.pdf) - using Grouper and the Active directory to manage access to the University's wireless network.

### Videos

As part of project work over recent years we have produced a number of screencast videos to demonstrate the use of Grouper and also the use of the data integration tool Talend to further extend the capabilities of Grouper.

New - [Structuring groups in Grouper](http://research.ncl.ac.uk/grand/resources/GrouperStructureJan12/GrouperStructureJan12.html) - This video discusses how we structure groups within Grouper to improve and demonstrates how the structure works and improves the delegation of access control.

[Provision access control groups from Grouper](http://research.ncl.ac.uk/idmaps/resources/DemoVideos/ExportDataFromGrouper2/ExportDataFromGrouper2.html) - This video discusses how we made use of the Open source data integration tool, Talend, to provision access control groups from Grouper into the Syllabus plus room booking system. This makes use of the outputs that were provided as part of the JISC funded IDMAPS project.

### Presentations

[Using Grouper: Newcastle University case studies - Internet2 2010 Fall Meeting presentation (.pdf)](http://research.ncl.ac.uk/grand/docs/20101102-using%20grouper-james.pdf)

Practical Experiences of IAM and Distributed Services -- CAMP in Nov. 2013 (.pdf)

### Grouper Newcastle Case Study

[Read the case study](http://www.internet2.edu/pubs/newcastle_grouper.pdf)
