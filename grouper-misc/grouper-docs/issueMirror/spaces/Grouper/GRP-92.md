---
key: GRP-92
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-92
type: Improvement
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2008-03-03T13:42:40.761+0000
updated: 2008-03-03T13:48:22.494+0000
resolved: 2008-03-03T13:48:22.496+0000
components: [UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-92  Allow alternative implementation classes to be configured for ObjectAsMaps

Most Grouper objects that are rendered as HTML in the UI are wrapped in ObjectAsMap subclasses. Currently it is not possible to change the implementation class. At Bristol we have been working on  'special' stems that act like Unix symbolic links for which we have a UoBStemAsMap implementation 

## Comments

### Gary Brown - 2008-03-03T13:48:22.486+0000

The following media properties define the default implementations. If using this feature, should subclass the default implementation and call super.init()

objectasmap.StemAsMap.impl=edu.internet2.middleware.grouper.ui.util.StemAsMap
objectasmap.GroupAsMap.impl=edu.internet2.middleware.grouper.ui.util.GroupAsMap
objectasmap.FieldAsMap.impl=edu.internet2.middleware.grouper.ui.util.FieldAsMap
objectasmap.MembershipAsMap.impl=edu.internet2.middleware.grouper.ui.util.MembershipAsMap
objectasmap.SubjectAsMap.impl=edu.internet2.middleware.grouper.ui.util.SubjectAsMap
objectasmap.SubjectPrivilegeAsMap.impl=edu.internet2.middleware.grouper.ui.util.SubjectPrivilegeAsMap

can have knock on effect for sorting:

objectasmap.StemAsMap.impl=uk.ac.bris.is.grouper.ui.util.UOBStemAsMap
required 
comparator.helper.uk.ac.bris.is.grouper.ui.util.UOBStemAsMap=edu.internet2.middleware.grouper.ui.StemComparatorHelper