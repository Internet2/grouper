---
key: GRP-81
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-81
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2008-01-16T14:14:18.094+0000
updated: 2008-04-14T08:16:38.532+0000
resolved: 2008-04-14T08:14:50.150+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-81  XmlImporter fails if a null attribute value is set

Colin Hudler reported to grouper-users that he got ' Import Error unable to import from xml: null' when importing:

<groupTypes>
>                 <groupType name='Provisioner'>
>                   <attribute
> name='Destinations'>ou=groups,dc=uchicago,dc=edu</attribute>
>                   <attribute name='Send To'></attribute>
>                 </groupType>


## Comments

### Gary Brown - 2008-01-16T14:20:34.880+0000

The current code has:

NotNullOrEmptyValidator vVal  = NotNullOrEmptyValidator.validate(val);

I would think that in this context setting an empty value should be treated as deleting the attribute - unless it is required.

We could use an import.property:

    import.attribute.action-if-empty=fail|skip|delete|skip-if-required|fail-if-required

### Gary Brown - 2008-04-14T08:14:50.052+0000

Actually the issue wasn't quite what I thought. The code didn't get as far as the validator - a NullPointerException was thrown first. For

  val                           = ( (Text) elAttr.getFirstChild() ).getData();

elAttr.getFirstChild()  returns null if the attribute is empty. I now catch the NullPointerException and the subsequent code works fine - though we may still have to look at the ability to unset an attribute value
