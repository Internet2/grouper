---
key: GRP-52
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-52
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Tom Barton  (internet2.edu) <tbarton@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-10-17T20:50:12.835+0000
updated: 2007-10-31T13:46:26.408+0000
resolved: 2007-10-31T13:46:26.429+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-52  Interface to conditionally veto UI menu items

I'll have a go at creating a pluggable interface for vetoing menu items and produce an implementation which can veto based on Group membership.

Gary

--On 17 October 2007 14:48 -0500 Tom Barton <tbarton@example.com> wrote:

> On the call today I took an AI to dig out Gary's suggested approach for a
> work around to deploy at Duke to prevent the UI from letting TAadmins
> choose a path through the UI that is frustratingly slow, ie, to
> selectively disable the Manage Groups task. Hre's the gist of what Gary
> wrote.
>
> This idea will need to be elaborated into an actual code change so that
> it can be evaluated for suitability. Shilen, is that up your alley, or is
> that more yours, Gary?
>
> Tom
>
> on 7/14/2007 Gary wrote:
>  > [AI] {Gary} will share an email with the list giving an idea of how to
>  > modify menu items based on who you are and which groups you belong to.
>
> The UI customisation guide on the Wiki gives an overview:
> <https://wiki.internet2.edu/confluence/display/GrouperWG/Customizing+the+
> Grouper+UI+v1.0#CustomizingtheGrouperUIv1.0-menu> however, the url will
> change with the official release of Version 1.2.
>
> Basically, subclass PrepareMenuAction and override the method:
>
> protected boolean isValidMenuItem(Map item,GrouperSession
> grouperSession,HttpServletRequest request)
>
> - put your logic first but if not returning false, delegate to the super
> class.
>
> In order to have the UI use the PrepareMenuAction subclass you must
> override / replace the prepareMenu.do action config in struts-config.xml.



## Comments

### Gary Brown - 2007-10-31T13:46:26.343+0000

I've made code changes which allow an arbitrary chain of MenuFilters to potentially veto a menu item. I have re-implemented the previous logic for GrouperSystem / wheel group members as a MenuFilter and created a GroupMembershipMenuFilter which uses a new UiPermissions class (configured through XML) to determine access to menu items based on Group membership.

Still need to update documentation on Wiki