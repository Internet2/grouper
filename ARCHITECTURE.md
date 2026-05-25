# Grouper architecture

Reference material on Grouper's code layout, authentication model, and provisioning system. Module-level overview lives in `CLAUDE.md`; this file goes one level deeper.

## Key development areas

### Core APIs

Path: `grouper/src/grouper/edu/internet2/middleware/grouper/`

- Groups, Stems (folders), Members, Privileges
- Rules engine and change log
- Provisioning and external system integration

### UI classes

Path: `grouper-ui/java/src/edu/internet2/middleware/grouper/`

- Custom MVC design
- Java beans for UI objects
- HTTP, servlet, and authentication handling

### UI templates

Path: `grouper-ui/webapp/WEB-INF/grouperUi2/`

- JSP-based templates with modern JavaScript
- Template system for group/folder management

### Web Services

Path: `grouper-ws/grouper-ws/src/`

- REST and SCIM APIs
- SCIM 2.0 implementation

## Authentication and security

- Built-in authentication with `GrouperSystem` user
- External authentication via filters/valves
- Subject API for user/group resolution
- Rules-based authorization

## Provisioning system

Grouper includes extensive provisioning capabilities:

- SQL provisioner for database targets
- LDAP provisioner
- SCIM provisioner
- Custom provisioners via plugin architecture
