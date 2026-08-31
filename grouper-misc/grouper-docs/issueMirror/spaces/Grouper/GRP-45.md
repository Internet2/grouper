---
key: GRP-45
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-45
type: Improvement
status: Resolved
resolution: Won't Fix
priority: Major
reporter: James Cramton <jcramton@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-09-20T20:10:21.806+0000
updated: 2008-01-04T05:41:22.664+0000
resolved: 2007-10-31T14:26:55.811+0000
components: [UI]
fixVersions: []
labels: []
links: []
---

# GRP-45  Need support for arbitrary attributes in import/export

This may be the result of my own poorly understood capabilities of MG 1.2.0's import/export feature. But from what I can tell, we can only import or export a limited number of attributes, spicifically, the Grouper identifier and 1 other useful field. We have 2 use cases that would be better served with a more flexible import/export framework.

1. An Instructional Technology Staff member (typically an advanced but non-technical skillset) needs to import a list of 100 students into a course. The current import format requires them to include the Grouper identifier, which is an attribute that they do not easily have access to.  Typically, they would receive a list of brownshortids, which are the login ids of the students in the course.  Since Grouper has an ability to map arbitrary attributes associated with a subject, we should be able to import a list of brownshortids or email addresses, rather than a list that must contain the Grouper identifier. I would imagine a list of attributes similar to the current configuration list that would allow us to define an arbitrary set of attributes to be used in the import file.

2. An Instructional Technology Staff member needs to deliver a list of email addresses of students in a course. Currently, all they can do is export a list that includes the Grouper identifier and one other configurable field, which we typically set to displayName. We would like to have the ability to export a list of attributes that is configurable in the export config file, so we could export attributes that are of greater utility than just the grouper identifier. For example, a list of brownshortids, displaynames, sn, givenname, and email addresses would be very useful.

The presence of the groups in the exported data is not helpful. We would like the ability to limit the membership listing to just the person or just the group members. See https://bugs.internet2.edu/jira/browse/GRP-42


Id	Name	Type
020114810	Anthony S. Jaworski	person
0f3a1e7a-5654-43e7-b8aa-6e20f7bfe911	COURSE:TEST:0001:2007-Fall:S01: Administrator 	group
177581cc-ed7c-464e-8117-51f7416545a8	COURSE:TEST:0001:2007-Fall:S01: Administrator Instructors 	group
234b0c23-7b29-4a72-88ee-818f79294916	COURSE:TEST:0001:2007-Fall:S01: Administrator Managers 	group
7ce9dbea-802e-4d22-b07d-389743c69d79	COURSE:TEST:0001:2007-Fall:S01: Administrator TAs 	group
c91c1e41-5b36-4793-a5b4-66d941f6a0bb	COURSE:TEST:0001:2007-Fall:S01: Contributor 	group
b3752ce8-37b2-4025-9e68-fd1733602bad	COURSE:TEST:0001:2007-Fall:S01: Contributor Content Developers 	group
6d914f67-0d70-4cee-9d8d-c76633ec8cdf	COURSE:TEST:0001:2007-Fall:S01: Contributor Mentors 	group
8f0c1cb4-3a66-46a0-8760-cbfea9b47dcd	COURSE:TEST:0001:2007-Fall:S01: Learner 	group
977fe7c4-b42f-46b9-a6ec-6cbfeb4d38de	COURSE:TEST:0001:2007-Fall:S01: Learner Auditors 	group
d3eae042-2314-411a-a13f-45fbef6bab83	COURSE:TEST:0001:2007-Fall:S01: Learner Students 	group
b8ec65a8-f713-493c-b344-aee432eda2ab	COURSE:TEST:0001:2007-Fall:S01: Learner Vagabonds 	group
010034671	Huiling Xu	person
010202803	James F. Cramton	person
010024657	Peter J. DiCamillo	person


## Comments

### Gary Brown - 2007-09-25T13:27:00.147+0000

The UI import / export functions have the flexibility to achieve what you want, though in the case of 1) you probably have to write some code:

1) The subject API only provides two methods designed to return a unique subject. One which matches on the actual subject ID and one which matches on an 'identifier'. If you are using the JdbcSourceAdapter you can modify the 'searchSubjectByIdentifier' query in sources.xml to match different 'identifiers'. This assumes that there is no overlap between the types of identifiers

A similar solution would be to implement a custom SourceAdapter which can accept more than one type of identifier. The adapter can simply run through a series of lookups until it finds a match, or it can determine the type of the identifier and use the appropriate lookup.

Finally, the UI uses 'public class DefaultMembershipImporter implements MembershipImporter' to read each line of input. Depending on the configuration it will call SubjectFinder.findById(id) or SubjectFinder.findByIdentifier(id). You can implement your own class which does a lookup (or several)  to uniquely determine a subject id and then call SubjectFinder.findById(id) on that.

2) You can export any subject attribute returned in Subject.getAttributes(). Simply add additional fields to the XML configuration i.e.

		<source id="qsuob">
			<field name="id"/>
			<field name="name"/>
			<field value="person"/>
			<field name="EMAIL"/>
                        <field name="AN_ARBITRARY_ATTRIBUTE"/>
		</source>

You should add a matching header if you choose to use a header line. The fields will be output in the order they are listed in the config file. You can remove ones such as <field value="person"/> if you don't need them.

3) If you don't want to export group information remove the:

		<source id="g:gsa">
			<field name="id"/>
			<field name="displayName"/> 
			<field value="group"/>
		</source>

configuration snippet. The exporter will only export data for subjects from sources that are listed.

### Gary Brown - 2007-10-31T14:26:55.700+0000

As indicated in previous comment I think the capabilities requested are already available though some coding to appropriate interfaces may be necessary

### dede - 2008-01-04T05:41:22.559+0000

http://sexporndownload.sprayblog.se
http://downloadsex.sprayblog.se
http://sexdownload.sprayblog.se
http://sexpornsex.sprayblog.se
http://blog.ifrance.com/downloadsex
http://forum.jahmusik.net/ftopic5664.html
http://forum.jahmusik.net/ftopic5663.html
http://forum.jahmusik.net/ftopic5661.html
http://forum.jahmusik.net/ftopic5660.html
http://forum.jahmusik.net/ftopic5659.html
http://forum.jahmusik.net/ftopic5658.html
http://forum.jahmusik.net/ftopic5657.html
http://forum.jahmusik.net/ftopic5656.html
http://forum.jahmusik.net/ftopic5655.html
http://forum.jahmusik.net/ftopic5654.html
http://forum.jahmusik.net/ftopic5653.html
http://forum.jahmusik.net/ftopic5652.html
http://forum.jahmusik.net/ftopic5651.html
http://forum.jahmusik.net/ftopic5650.html
http://forum.jahmusik.net/ftopic5649.html
http://forum.jahmusik.net/ftopic5648.html
http://forum.jahmusik.net/ftopic5647.html
http://forum.jahmusik.net/ftopic5646.html
http://forum.jahmusik.net/ftopic5645.html
http://forum.jahmusik.net/ftopic5644.html
http://forum.jahmusik.net/ftopic5643.html
http://forum.jahmusik.net/ftopic5642.html
http://forum.jahmusik.net/ftopic5641.html
http://forum.jahmusik.net/ftopic5640.html
http://forum.jahmusik.net/ftopic5639.html
http://forum.jahmusik.net/ftopic5638.html
http://forum.jahmusik.net/ftopic5637.html
http://forum.jahmusik.net/ftopic5636.html
http://forum.jahmusik.net/ftopic5635.html
http://forum.jahmusik.net/ftopic5634.html
http://forum.jahmusik.net/ftopic5633.html
http://forum.jahmusik.net/ftopic5632.html
http://forum.jahmusik.net/ftopic5631.html
http://forum.jahmusik.net/ftopic5630.html
http://forum.jahmusik.net/ftopic5629.html
http://forum.jahmusik.net/ftopic5628.html
http://forum.jahmusik.net/ftopic5627.html
http://forum.jahmusik.net/ftopic5626.html
http://forum.jahmusik.net/ftopic5625.html
http://forum.jahmusik.net/ftopic5624.html
http://forum.jahmusik.net/ftopic5623.html
http://forum.jahmusik.net/ftopic5622.html
http://forum.jahmusik.net/ftopic5620.html
http://forum.jahmusik.net/ftopic5619.html
http://forum.jahmusik.net/ftopic5618.html
http://forum.jahmusik.net/ftopic5617.html
http://forum.jahmusik.net/ftopic5616.html
http://forum.jahmusik.net/ftopic5615.html
http://forum.jahmusik.net/ftopic5614.html
http://forum.jahmusik.net/ftopic5613.html