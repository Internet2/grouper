---
key: GRP-50
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-50
type: Task
status: Resolved
resolution: Fixed
priority: Trivial
reporter: James Cramton <jcramton@example.com>
assignee: Tom Barton  (internet2.edu) <tbarton@example.com>
created: 2007-10-05T14:33:09.246+0000
updated: 2008-01-10T13:16:04.963+0000
resolved: 2007-10-31T02:00:38.321+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-50  Update Grouper 1.2.1 schema documentatin

We've discussed updating the Grouper documentation to include an ERD of the current DB Schema. Having this would help us move forward quicker with debugging and various programming and reporting tasks using Grouper.

## Comments

### tbarton - 2007-10-31T01:57:56.840+0000

Some doc on the v1.2.1 DB schema. The jpg is referenced in the html.

### tbarton - 2007-10-31T02:00:06.959+0000

I've produced some schema doc (attached html & jpg files) using DBVisualizer. Let's try this out for v1.2.1 and learn what else deployers may want to see.

### James Cramton - 2007-10-31T12:52:40.803+0000

Thanks, Tom. The most useful thing for me would be an er diagram that includes foreign key relationships. I've been able to reverse engineer some, but there are some obscure relationships there, that I haven't deciphered yet. Importantly, since there is no enforced referential integrity in the DB schema, understanding the intended relationships between tables is what's missing. The column names and descriptions are easily read from OEM or other tools.

## Attachments
- er-1.2.1.jpg (371566 bytes) - by ? on 2007-10-31T01:57:56.815+0000
- grouper-1.2.1-schema.html (30861 bytes) - by ? on 2007-10-31T01:57:56.719+0000