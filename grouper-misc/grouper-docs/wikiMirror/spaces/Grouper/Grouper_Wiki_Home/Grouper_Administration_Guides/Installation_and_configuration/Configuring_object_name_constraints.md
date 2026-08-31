---
title: "Configuring object name constraints"
space: Grouper
pageId: 28544862
version: 5
lastUpdated: 2025-09-02T20:39:50.857Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544862/Configuring+object+name+constraints
---

Grouper has the ability to enforce constraints on the format of the name (or display name, description, etc.) for objects you can create: groups, folders, attribute definitions, and attribute def names. The checks are implemented using [hooks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347/Grouper+Hooks), which means that the validation happens before the object is created or updated, and the action is vetoed if the field not match the regular expression pattern defined for it. Enforcement applies not only to objects created from the UI, but also through loader jobs, web services, or GSH. Even though users carefully name objects from the UI, a loader job may at some point fail if incoming data contains unexpected characters.

The default configuration in Grouper has a built-in check on the extension for groups, folders, attribute definitions, and attribute def names, to only allow letters, numbers, underscore, or dash. This is configured in grouper.properties with settings for each object type. Note that that the properties are set to true by default. To disable a default check completely, override any of these values with false.

```
group.validateExtensionByDefault = true
stem.validateExtensionByDefault = true
attributeDef.validateExtensionByDefault = true
attributeDefName.validateExtensionByDefault = true
```

To change a pattern to allow a different naming convention (e.g. to allow periods or spaces), you can set custom regular expressions targeting a certain object type and field. A set of related entries are defined per field, with settings for the field type, the pattern, and the message that will appear in the UI and the log on error. For the veto message, the value `$attributeValue$` will be replaced with the value that was tested against the pattern. In this example, a custom pattern on group extension and another on group display extension is configured thus:

```
group.attribute.validator.attributeName.0 = extension
group.attribute.validator.regex.0 = ^[a-z0-9_.-]+$
group.attribute.validator.vetoMessage.0 = Group extension '$attributeValue$' only allows lower case letters, numbers, underscore, dash, and periods

group.attribute.validator.attributeName.1 = displayExtension
group.attribute.validator.regex.1 = ^[A-Za-z0-9_. -]+$
group.attribute.validator.vetoMessage.1 = Group display extension '$attributeValue$' only allows letters, numbers, underscore, dash, spaces, and periods

stem.attribute.validator.attributeName.0 = extension
stem.attribute.validator.regex.0 = ^[a-z0-9_-]+$
stem.attribute.validator.vetoMessage.0 = Folder extension '$attributeValue$' only allows lower case letters, numbers, underscore, and dash
```

Properties for validators are indexed with an incrementing number starting at zero for each object type. Note that if any custom validation is set up, it overrides the default check on that object type for extension matching alphanumeric, underscore, and dash.

## Example error messages

Default error messages, when extension does not match alphanumeric, underscore, or dash. If you see these error messages in the UI or log output, consider modifying the default behavior by disabling the default checks, or setting custom validation (preferred).

- Groups: *The group ID can only contain letters, numbers, underscore, or dash*
- Folders: *The folder ID can only contain letters, numbers, underscore, or dash*
- Attribute definitions: *The attribute definition ID can only contain letters, numbers, underscore, or dash*
- Attribute def names: *The attribute definition name ID can only contain letters, numbers, underscore, or dash*
