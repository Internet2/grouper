---
key: GRP-60
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-60
type: New Feature
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-17T16:15:16.060+0000
updated: 2007-11-17T16:47:30.194+0000
resolved: 2007-11-17T16:46:17.723+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-60  Allow greater control when searching for subjects i.e. specify subject attributes to search

See comment by Jess: https://wiki.internet2.edu/confluence/display/GrouperWG/Grouper+UI+Functionality+Requests at bottom of 'Group Search Capability' section.

The current search lets you enter a search term only. Want to add ability to have Source specific search fragments, and then the means of processing the form input to control the subsequent search.


## Comments

### Gary Brown - 2007-11-17T16:46:17.687+0000

Media properties of the form 'subject.search.form-fragment.<sourceId>' define tiles which include HTML form elements e.g. the radio buttons which determine the attribute to display in group search results has been refactored to take advantage of this mechanism:

subject.search.form-fragment.g\:gsa=subjectSearchGroupFragmentDef

In addition there is a SearchTermProcessor interface which has a method:

public String processSearchTerm(Source source, String searchTerm, HttpServletRequest request);

Media properties of the form: 'subject.search.term.process.<source_id>', e.g.

subject.search.term.process.personUOB=uk.ac.bris.is.grouper.ui.SearchTermProcessorImpl

specify implementations of the interface which are invoked if a specific Source is selected for searching.

At Bristol we use a custom source and so can take advantage of logic to build complex query strings. This approach is not directly applicable to the current JdbcSourceAdapter (not sure about the JndiSourceAdapter), however, it should be possible to subclass those implementations and allow different searches based on complex query Strings

### Gary Brown - 2007-11-17T16:47:28.640+0000

Many thanks to my colleague, Catherine Jewell, who actually did the leg work on this.