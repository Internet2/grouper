# Grouper configuration files

Lists of the base and user-managed property files, plus the database-backed config rules. The high-level concepts (base overridden by user-managed, DB takes precedence, externalize text for UI wizards) live in `CLAUDE.md`; this file is the file-by-file directory.

## Base property files

Grouper ships with base configuration properties named `*.base.properties`. These are the defaults; the user-managed equivalents below override them.

- `grouper-misc/grouperClient/conf/grouper.client.base.properties`
- `grouper/conf/grouper.hibernate.base.properties` — database connectivity
- `grouper/conf/grouperText/grouper.textNg.en.us.base.properties` — internationalization (i18n) strings. User-facing text should be in this file.
- `grouper/conf/grouperText/grouper.textNg.fr.fr.base.properties` — internationalization (i18n) strings
- `grouper/conf/grouper-loader.base.properties` — external system and daemon job configuration
- `grouper/conf/grouper-ws-ng.base.properties` — web services settings
- `grouper/conf/grouper-ui-ng.base.properties` — web UI settings
- `grouper/conf/subject.base.properties` — subject source definitions
- `grouper/conf/grouper.base.properties` — core Grouper configuration
- `grouper/conf/grouper.cache.base.properties`

## User-managed property files

Corresponding files without `.base` in the filename. Note that `grouperText`, `grouper-ui`, and `grouper-ws` have slightly different naming than the standard pattern. `morphString.properties` has no base properties file.

- `grouper-misc/grouperClient/conf/grouper.client.properties`
- `grouper/conf/grouper.hibernate.properties`
- `grouper/conf/grouperText/grouper.text.en.us.properties`
- `grouper/conf/grouperText/grouper.text.fr.fr.properties`
- `grouper/conf/grouper-loader.properties`
- `grouper/conf/grouper-ws.properties`
- `grouper/conf/grouper-ui.properties`
- `grouper/conf/subject.properties`
- `grouper/conf/grouper.properties`
- `grouper/conf/grouper.cache.properties`
- `morphString.properties` — string encryption configuration

## Database-backed configuration

There is also configuration in the database, which takes precedence over the non-base config files. This is edited in the UI (since history is kept in another table), so it should not be edited in the database directly — view it via the UI, or via the MCP tool for looking at configuration (preferred).
