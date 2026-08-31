---
title: "Georgia Tech Grouper Page"
space: Grouper
pageId: 28543618
version: 11
lastUpdated: 2026-07-12T15:26:08.586Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543618/Georgia+Tech+Grouper+Page
---

## **2024 - Membership Lifecycles**

Note: Our current experiences are based on defining Eligibility groups for various Policy groups and intersecting the Adhoc memberships with that Eligibility.

While we've automated group memberships to a great extent, we have several problems with maintaining manual/adhoc group memberships where we don't have good data yet. Our most urgent difficulties with adhoc memberships stem from the use of Eligibility groups to automatically clean up the memberships when people leave their department or Georgia Tech overall. In particular, even when users lose memberships 'properly' based on the logic we implemented, it happens silently, unnecessarily aggressively, and sometimes based on unexpected changes. Additionally, the assemblage of features and logic that implement automated cleanup are far from transparent, require large Gsh templates to set up, and are not incorporated into the membership-adding flows, so requests for ineligible memberships are not noticed until an expert analyzes why the memberships never took effect.

We think Grouper Rules would be a better foundation for overall Policy group definition than the JEXL/GroupMath approaches we've taken so far. We do have concerns (see Triggered Changes below) about the triggered nature of Rules, but those problems seem worth it for a more transparent and coherent system.

Finally, we know some of these requirements/stories match extremely well with existing Rules or other features. The purposes of this document include learning about the Grouper Way of doing this today, but it is also important for us to define a larger vision for Membership Lifecycles and see if the Grouper product can share that vision or if we'd better implement a custom UI with the approvals/conditions/vetoes we describe below.

Quick User Stories:

**Adding** people to groups:

- Additional data collected about new membership:
  
  - Comment regarding why user is being added to group (Ticket number, etc)
  - Who requested
  - Who approved
  - Duration
- Checks/Alerts
  
  - Compatible Subject Types (Accounts vs People)
  - Necessary Subject Attributes: Some groups require Administrative Accounts (aka [PSA](https://www.reddit.com/r/AZURE/comments/16mozry/separate_account_for_privileged_access/)s) instead of personal accounts
  - Necessary supporting group memberships:
    
    - Particularly useful when a rule is in place for automatic removals
      
      - ie, user must be an employee or must be in a group in a folder that is used for cleanup
    - Perhaps an expiration date must be provided if a user is not (yet) in a cleanup-related group
  - (Someday) Separation of duties: Can't be in Group X and Y at the same time
- Approvals
  
  - Some users can add members directly/immediately
  - Other users can request their addition but they need to be approved
  - Approvers might depend on the requested member's department
    
    - (We have some applications with administrative delegates spread across departments.)
- Notification Options (for membership Additions)
  
  - To Requester (if approval was needed and was granted or denied)
  - To Approvers
  - To new member
  - To manager of new member
  - To app owner
  - To app-owner delegates, perhaps based on new member's department

**Removing** people from groups (automatically):

Our existing systems emphasize immediate cleanup of memberships. New system will instead often implement **grace periods**(especially for job/department changes) and **notifications**.

We're thinking about two levels of group cleanup scrutiny as well as three different types of employment changes:

| **High Security Groups** | Grace Period * | Notify Group Member | Notify Group Member Manager (old vs new) | Notify App Owner/Delegate |
| --- | --- | --- | --- | --- |
| Job Change (same dept) | 14 | Yes | Yes | Yes |
| Department Change | 5 | Yes | Yes | Yes |
| Leaving GT Employment | 0 | No | Yes | Yes |
|  |  |  |  |  |
| **Medium Security Groups** | Grace Period | Notify Group Member | Notify Group Member Manager (old vs new) | Notify App Owner/Delegate |
| Job Change (same dept) | 30 | Yes | Yes | Yes |
| Department Change | 30 | Yes | Yes | Yes |
| Leaving GT Employment | 0 | No | Yes | Yes |

*Grace Period - An Expiration date is added to a membership when the member's cleanup-defined reference groups change or an cleanup-defined attribute changes

**Attesting Memberships**

Owners of our large applications don't know all their users but are nonetheless tasked with reviewing who has elevated access to them. Besides using departmental delegates who can be better aware of their coworkers' job changes, a bunch of information has been requested by application owners in order to more easily/accurately attest that only the correct people have access to their applications. This is a quick list of some of the information being requested in an attestation report:

- Information for both Now and when membership was added or reapproved:
  
  - Employment status
  - Department
  - Title
- Membership status
  
  - When Added, Who Added
  - When Approved, Who Approved
  - Upcoming membership expiration
  - Membership reason: Upstream group or Reason captured in adhoc addition
- Application:
  
  - Multiple Groups, each with different access levels (End User, Elevated, Full, Administrator)
- Divide and conquer
  
  - Split users by Department or Division

**Triggered Changes?**

Theoretically, monitoring one system and processing changes there to affect another place can keep the two places in sync. In practice, we have often found discrepancies between such systems over time where events have seemingly been missed. Perhaps that is because we're not often working in a single [ACID](https://en.wikipedia.org/wiki/ACID) database where trigger and transaction commitments are strongest. We've generally settled on "stateful" update patterns and triggered acceleration. For example, instead of (only) processing changes in a tree of department-employment groups and removing membership in a Policy group somewhere based on the department change, we choose to store the department the user has when the Policy membership was created and then removing the Policy membership when the department is ever different from that. Of course, using triggers can help make that faster, but we can also go back and full-sync-check all such conditions later.

More details on Problems/Concerns/Experiences with triggered changes:

- Missed events and lack of a full-sync safety net
- Hiccups in upstream data, cascade of (non-self-restoring) side effects

OLD:

See [Georgia Tech's presentation at the July 2016 IAM Online](https://www.incommon.org/docs/iamonline/20160713_IAMOnline.pdf) on their planned Door Management System using Grouper.

See blog from May 2018: Georgia Tech Tackles Membership Intersections with Grouper
