---
title: "GrouperShell (gsh) Membership finder (MembershipFinder)"
space: Grouper
pageId: 28548580
version: 5
lastUpdated: 2026-07-01T05:44:07.457Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548580/GrouperShell+gsh+Membership+finder+MembershipFinder
---

Use this class to find memberships within the Groups registry

A membership is the object which represents a join of member and group. Has metadata like type and creator, and, if an effective membership, the parent membership

Sample call

> Membership membership1 = new MembershipFinder().addGroup(group1).addSubject(subject).assignEnabled(true).findMembership(true);

Sample call to find multiple memberships

> Set<object[]> members = new MembershipFinder().addMembershipId(membership1.getUuid()).addMembershipId(membership2.getUuid()).findMembershipsMembers();

Sample call to find immediate groups of a user

> Set membershipsOwnersMembers = new MembershipFinder().addSubject(subject).addField(Group.getDefaultList()).assignMembershipType(MembershipType.IMMEDIATE).assignEnabled(true).findMembershipResult().getMembershipsOwnersMembers(); for (Object membershipsOwnersMember : GrouperUtil.nonNull(membershipsOwnersMembers)) { Group group = (Group)((Object[])membershipsOwnersMember)[1]; System.out.println(group.getName()); }

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/MembershipFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/MembershipFinder.html)
