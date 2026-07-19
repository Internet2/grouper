---
key: GRP-84
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-84
type: Improvement
status: Closed
resolution: Won't Fix
priority: Minor
reporter: blair christensen. <blair@example.com>
assignee: Tom Zeller <tzeller@example.com>
created: 2008-01-18T01:44:19.947+0000
updated: 2012-02-22T22:19:04.534+0000
resolved: 2012-02-22T22:19:04.456+0000
components: [API]
fixVersions: []
labels: []
links: []
---

# GRP-84  Add Cobertura code coverage; optionally remove Clover code coverage

I will be attaching two patches and three jar files.  The first patch (along with the three jar files) adds Cobertura [0] code coverage reports.  The second patch, if applied, will remove the existing Clover code coverage suppport.

Reasons for preferring Cobertura over Clover:
* Better licensing.  Cobertura is GPL'd and freely available for use while we had a special "open source" license for Clover that requires a license file, etc.
* Cobertura integrates a lot more cleanly.  It doesn't require an external jar file, an external license file and it will automatically instrument Grouper when running tests.  A new Ant target ("coverage.report") is added which generates HTML reports.

Reasons for preferring Clover:
* More active development.
* Marginally prettier reports.

[0]: <http://cobertura.sourceforge.net/>

## Comments

### blair christensen. - 2008-01-18T01:46:12.208+0000

Adds support for Cobertura code coverage.  Requires attached jar files.

### blair christensen. - 2008-01-18T01:46:41.553+0000

If desired, this patch will remove Clover code coverage support.

### blair christensen. - 2008-01-18T01:48:17.948+0000

For Cobertura code coverage

### blair christensen. - 2008-01-18T01:52:12.543+0000

The patch for adding Cobertura support may need a little cleaning before it will apply.  I generated it from within my Mercurial Grouper repo (which contained the jar files).

### tzeller@example.com - 2012-02-22T22:19:04.507+0000

Too old.

## Attachments
- add_cobertura_coverage.patch (9725 bytes) - by blair christensen. on 2008-01-18T01:46:12.171+0000
- asm-2.2.1.jar (34783 bytes) - by blair christensen. on 2008-01-18T01:48:17.932+0000
- asm-tree-2.2.1.jar (16274 bytes) - by blair christensen. on 2008-01-18T01:48:17.941+0000
- cobertura-1.9.jar (193907 bytes) - by blair christensen. on 2008-01-18T01:48:17.875+0000
- remove_clover.patch (3128 bytes) - by blair christensen. on 2008-01-18T01:46:41.500+0000