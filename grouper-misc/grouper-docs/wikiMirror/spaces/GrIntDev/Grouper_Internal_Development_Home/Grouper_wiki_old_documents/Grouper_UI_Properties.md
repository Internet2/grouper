---
title: "Grouper UI Properties"
space: GrIntDev
pageId: 48793144
version: 21
lastUpdated: 2026-07-12T17:02:45.975Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793144/Grouper+UI+Properties
---

As of Grouper 2.2, most of the Grouper UI configuration (for the Grouper Admin (legacy) UI, LITE UI and new Grouper 2.2 UI) is now in grouper-ui.base.properties, and you can override it in grouper-ui.properties. See the [Configuration Overlay page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549156/Grouper+configuration+files+and+overlays) for details. Some configuration that you generally will not need to change in upgrading to Grouper 2.2 is in media.properties. You can see what is in each here: (click download on the top file)

[https://github.com/Internet2/grouper/blob/master/grouper-ui/conf/grouper-ui.base.properties](https://github.com/Internet2/grouper/blob/master/grouper-ui/conf/grouper-ui.base.properties)

[https://github.com/Internet2/grouper/blob/master/grouper-ui/conf/resources/grouper/media.properties](https://github.com/Internet2/grouper/blob/master/grouper-ui/conf/resources/grouper/media.properties)

## grouper-ui.properties

The file grouper-ui/conf/grouper-ui.properties controls many aspects of the Grouper UI appearance and behavior. The settings in media.properties apply to the Grouper Admin UI, LITE UI's, and new Grouper 2.2 UI.

- #Simple Look and feel
- #Menu Configuration
- #Miscellaneous UI configuration
- #Membership Import and export
- #Displaying lists
- #Searching
- #Sorting
- #Audit query
- #Plugin browse / search
- #Dynamic tiles
- #ObjectAsMap Implementations
- #Infodots
- #Lite UI settings

### Simple Look and feel

How to change logos and CSS

#You may specify a logo for your organisation and for Grouper. Off-the-shelf  
 #your organisation logo appears on the left of the header and the Grouper logo  
 #appears on the right. Typically you would make the logos the same height.

image.organisation-logo =grouper/images/organisation-logo.gifimage.grouper-logo =grouper/images/grouper.gif#A space separated list of one or more .css files which are inserted into the  
 #HEAD of all Grouper pages. The .css files are referenced in order and after  
 #any Grouper CSS files. This means that your CSS files can override any  
 #Grouper style definition

css.additional =#You can omit the Grouper CSS files completely by setting grouper-css.hide=true

grouper-css.hide =false#Include link to new prototype of a Lite ui on login page

login.ui-lite.show-link =@login.ui-lite.show-link@login.ui-lite.link =/grouperUi/appHtml/grouper.html#operation=SimpleMembershipUpdate.index#Enable links (new window) to lite UI i.e. from GroupSummary page

ui-lite.link-from-admin-ui =@ui-lite.link-from-admin-ui@ui.lite.group-link =/grouperUi/appHtml/grouper.html#operation=SimpleMembershipUpdate.init&groupId=

### Menu Configuration

Out-of-the-box Grouper defines a standard set of menu items. It is possible to add  
 additional menu items and to change the order in which they appear

#space separated list of files - see default for format - which define menu items

menu.resource.files =resources/grouper/menu-items.xml#space separated list of menu item names (which must exist in 'menu.resource.files'

menu.order =MyGroups ManageGroups CreateGroups JoinGroups AllGroups SearchSubjects SavedStems SavedGroups SavedSubjects GroupTypes Help#space separated list of MenuFilters - in the order they are tested

menu.filters =edu.internet2.middleware.grouper.ui.RootMenuFilter edu.internet2.middleware.grouper.ui.GroupMembershipMenuFilter#Determines if the menu is processed once at the start of a user session or whether  
 #it is processed with each request. Use 'true' for production and 'false' if you  
 #are actively developing the menu and want to see changes immediately

menu.cache =true

### Miscellaneous UI configuration

#Specifies whether the error page should include a ticket. The ticket appears  
 #in the errror log and can be used to filter relevant messages

error.ticket =@error.ticket@#Specifies whether Grouper should display a logout link. Not all authentication  
 #schemes allow logout, including Basic authentication.  
 #This value can be set in the Grouper UI build.properties file

logout.link.show =@logout.link.show@##Set this to 'all' to remove all cookies, or set to a comma or space separated list of  
 ##cookie names to delete. Java code will do a Cookie.getName().equals or .matches  
 ##so valid regular expressions may be used

logout.cookies-to-delete =none#If you have admin priviliges this is where you go initially

admin.browse.path =/populateAllGroups.do#When creating a group, which access privs will be granted to GrouperAll?  
 #groups.create.grant.all allows the UI to override the defaults in grouper.properties

#If not set, the defaults from the grouper.properties file will be used  
 #NB in the QuickStart, no privs are automatically assigned - grouper.properties was  
 #modified so that all 'groups.create.grant.all.<priv>' are false,

groups.create.grant.all =#If true, on the 'Subject Search' page there will be a link to your 'Subject Summary'

allow.self-subject-summary =true#Unless otherwise configured, the UI starts browsing at the ROOTstem. set default.browse.stem  
 #to start browsing from a different stem

default.browse.stem =@default.browse.stem@#Grouper has no formal notion of 'personal' stems vs 'institutional' stems, however, setting  
 #personal.browse.stem, will trim this portio of the hierarchy when a user is browsing in 'All' mode  
 #TODO members of Wheel group / GrouperSystem should be able to browse regardless.

###personal.browse.stem =uob:personal#The UI has a 'Saved Groups' feature intended to make it easier to find groups of interest  
 #and used when ceating comosite groups. This property, if true, causes any new or updated group  
 #to be automatically added to your list of saved groups

put.in.session.updated.groups =true#The UI has a 'Saved Stems' feature intended to make it easier to find stems of interest  
 #This property, if true, causes any new or updated stem  
 #to be automatically added to your list of saved stems

put.in.session.updated.stems =true#Turn off the debug functionality (true/false)  
 #Can be set in the Grouper UI build.properties file

browser.debug.enable =@browser.debug.enable@#If debug is on then restrict to named group. Disable if group does not exist  
 #Can be set in the Grouper UI build.properties file

browser.debug.group =@browser.debug.group@#Enables user to specify arbitrary exsecutable as HTML editor on the server  
 #Do not enable unless you absolutely trust users  
 #Can be set in the Grouper UI build.properties file

browser.debug.group.enable-html-editor =@browser.debug.group.enable-html-editor@#The directory where preferences are saved

debug.prefs.dir =@debug.prefs.dir@

### Membership Import and export

As of V1.2 the UI provides a framework for allowing users to export membership lists  
 to flat files i.e. comma separated files, including in Excel compatible format. It is also  
 possible to import simple delimited files.  
 Both import and export require configuration and appropriate configuration will vary from  
 site to site. For this reason, the UI does not come pre-configued for import/export, however,  
 sample files are provided (see [Enabling import / export of group memberships](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545413/Customizing+the+Grouper+UI))

membership-export.config =resources/grouper/membership-export.xmlmembership-import.config =resources/grouper/membership-import.xmlIf the user does not select a file to import allow text to be typed / pasted into textarea  
 Since version 1.2.1

membership-import.allow-textarea =true##for large files, give a temp dir so they arent stored in memory or the system temp dir

file.upload.temp.dir =file.upload.max.bytes = ##users must be in this group to be able to login to the UI

require.group.for.logins =

### Displaying lists

#When browsing or searching the UI will present lists of various objects. The following settings  
 #allow sites to control default page sizes and a list of user-selectable page sizes

pager.pagesize.default =50pager.pagesize.selection =10 25 50 100#Typically, when browsing it is sufficient to show the extension/displayExtension for a group/stem  
 #as the parent stems are aleady shown and are common. When searching, however, this context is lost  
 #so sites can configure which field to display in the context of a search where results may come from  
 #different locations

search.group.result-field =displayNamesearch.stem.result-field =displayName#By setting the 'result-field-choice' properties, sites can alow users to select which  
 #field to use for displaying serach resuts

search.group.result-field-choice =displayName displayExtension namesearch.stem.result-field-choice =displayName displayExtension name#Prior to V1.2 sites could do little to control how subjects, groups or stems were displayed  
 #in the UI, beyond the display of stem/group search results, unless they created dynamic tiles  
 #It is now possible to control the display of stems, groups and subjects in different contexts  
 #In the case of subjects, an attribute can be configured based on the subject's SourceAdapter

#Provides backwards compatability - it was assumed that all Subjects woud have a 'description' attribute

subject.display.default =description#Used for groups when displayed as a subject i.e. when displayed as member of another group

subject.display.g\:gsa =displayExtension#Used for internal Grouper subjects i.e. GrouperAll and GrouperSystem

subject.display.g\:isa =name#Default attribute to display for groups

group.display =displayExtension#Attribute to use when browsing and the user has selected to hide the hierarchy -  
 #thus losing context

group.display.flat =displayName#Default attribute for stems

stem.display =displayExtension

### Searching

#Configuration affecting how simple default group/stem searches are carried out

#Determines if the name or extension field (or neither) are searched

search.default.search-in-name-or-extension =#Determines if the display name or display extension (or neither) is searched

search.default.search-in-display-name-or-extension =name#On the advanced groups search screen determines how many search fields are displayed

search.max-fields =5#On the advanced groups search screen determines how many group type select lists are displayed

search.max-group-types =3#On the advanced stems search screen determines how many search fields are displayed

search.stems.max-fields =4#Control whether default search can search any attribute. Valid values=only or true or false

search.default.any =false#Control default search option

search.default =name#Allow filtering of membership lists by subject source

members.filter.by-source =truemembers.filter.limit =500#Displays source specific form elements using keys:  
 #subject.search.form-fragment.<sourceId>

subject.search.form-fragment.g\:gsa =subjectSearchGroupFragmentDef

### Sorting

As of V1.2 the Grouper UI allows sorting of various lists of objects  
 See [Sort order of lists](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545413/Customizing+the+Grouper+UI) for explanation

comparator.impl =edu.internet2.middleware.grouper.ui.DefaultComparatorImplcomparator.helper.edu.internet2.middleware.grouper.Group  
 =edu.internet2.middleware.grouper.ui.GroupComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.ui.util.GroupAsMap  
 =edu.internet2.middleware.grouper.ui.GroupComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.Stem=edu.internet2.middleware.grouper.ui.StemComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.ui.util.StemAsMap  
 =edu.internet2.middleware.grouper.ui.StemComparatorHelpercomparator.helper.edu.internet2.middleware.subject.Subject  
 =edu.internet2.middleware.grouper.ui.SubjectComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.ui.util.SubjectAsMap  
 =edu.internet2.middleware.grouper.ui.SubjectComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.Member  
 =edu.internet2.middleware.grouper.ui.SubjectComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.Membership  
 =edu.internet2.middleware.grouper.ui.SubjectComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.ui.util.MembershipAsMap  
 =edu.internet2.middleware.grouper.ui.SubjectComparatorHelpercomparator.helper.edu.internet2.middleware.grouper.ui.util.SubjectPrivilegeAsMap  
 =edu.internet2.middleware.grouper.ui.GroupOrStemComparatorHelper#Sorting large lists can be computationally expensive - and slow. Use this property to turn off sorting for  
 #large lists

comparator.sort.limit =200To control the order in which subject attributes are listed on the Subject Summary page:#subject.attributes.order.<SOURCE_ID>=comma separated list of case sensitive attribute names

subject.attributes.order.g\:gsa =displayExtension,displayName,name,alternateName,extension,createTime,createSubjectId,createSubjectType,modifySubjectId,modifySubjectType,modifyTime,subjectType,idsubject.attributes.order.qsuob =LFNAME,LOGINID,subjectType,id

### Audit query

Date format likely to be locale dependent + may need to turn on/off

audit.query.enabled =true##SimpleDateFormat format strings

audit.query.date-format =MM/dd/yyyyaudit.query.display-date-format =dd MMM yyyy HH:mm:ss ##If no date specified show results for audit.query.default-since days

audit.query.default-since =7

### Plugin browse / search

The UI has a pluggable interface for browsing and searching. See [Customising Browsing and Searching](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545413/Customizing+the+Grouper+UI) for explanation

repository.browser.my.class =edu.internet2.middleware.grouper.ui.MyMembershipsRepositoryBrowserrepository.browser.my.flat-capable =truerepository.browser.my.root-node =repository.browser.my.hide-pre-root-node =truerepository.browser.my.flat-privs =MEMBERrepository.browser.my.flat-type =grouprepository.browser.my.search =groupsrepository.browser.create.class =edu.internet2.middleware.grouper.ui.CreateRepositoryBrowserrepository.browser.create.flat-capable =truerepository.browser.create.root-node =repository.browser.create.hide-pre-root-node =truerepository.browser.create.flat-privs =CREATE STEMrepository.browser.create.flat-type =stemrepository.browser.create.search =stemsrepository.browser.manage.class =edu.internet2.middleware.grouper.ui.ManageRepositoryBrowserrepository.browser.manage.flat-capable =truerepository.browser.manage.root-node =repository.browser.manage.hide-pre-root-node =truerepository.browser.manage.flat-privs =ADMIN UPDATE CREATE STEMrepository.browser.manage.flat-type =grouprepository.browser.manage.search =groupsrepository.browser.join.class =edu.internet2.middleware.grouper.ui.JoinRepositoryBrowserrepository.browser.join.flat-capable =truerepository.browser.join.root-node =repository.browser.join.hide-pre-root-node =truerepository.browser.join.flat-privs =OPTINrepository.browser.join.flat-type =grouprepository.browser.join.search =groupsrepository.browser.all.class =edu.internet2.middleware.grouper.ui.AllRepositoryBrowserrepository.browser.all.flat-capable =falserepository.browser.all.root-node =repository.browser.all.hide-pre-root-node =truerepository.browser.all.flat-privs =repository.browser.all.search =groupsrepository.browser.subjectsearch.class =edu.internet2.middleware.grouper.ui.AllRepositoryBrowserrepository.browser.subjectsearch.flat-capable =truerepository.browser.subjectsearch.root-node =repository.browser.subjectsearch.hide-pre-root-node =truerepository.browser.subjectsearch.flat-privs =repository.browser.subjectsearch.search =groupsrepository.browser.savedgroups.class =edu.internet2.middleware.grouper.ui.AllRepositoryBrowserrepository.browser.savedgroups.flat-capable =truerepository.browser.savedgroups.root-node =repository.browser.savedgroups.hide-pre-root-node =truerepository.browser.savedgroups.flat-privs =repository.browser.savedgroups.search =groupsrepository.browser.savedsubjects.class =edu.internet2.middleware.grouper.ui.AllRepositoryBrowserrepository.browser.savedsubjects.flat-capable =truerepository.browser.savedsubjects.root-node =repository.browser.savedsubjects.hide-pre-root-node =truerepository.browser.savedsubjects.flat-privs =repository.browser.savedsubjects.search =groups

### Dynamic tiles

The UI uses dynamic tiles to determine, at run time, how to display various objects  
 See [Defining Custom Dynamic Templates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545413/Customizing+the+Grouper+UI) for more details

composite.view.default =/WEB-INF/jsp/compositeView.jspcomposite.view.asFactor =/WEB-INF/jsp/compositeAsFactorView.jspcomposite.view.chainPath =/WEB-INF/jsp/compositeChainPathView.jspcomposite.view.chain =/WEB-INF/jsp/compositeChainView.jspsubject.view.default =/WEB-INF/jsp/subjectView.jspsubject.view.memberLink =/WEB-INF/jsp/memberLinkView.jspsubject.view.subjectSearchResultLink =/WEB-INF/jsp/subjectSearchResultLinkView.jsp =subject.view.subjectInfo =/WEB-INF/jsp/subjectInfo.jspsubject.view.groupSearchResultLink =/WEB-INF/jsp/groupSearchResultLinkView.jspsubject.view.stemSearchResultLink =/WEB-INF/jsp/stemSearchResultLinkView.jspsubject.view.assignFoundMember =/WEB-INF/jsp/assignFoundMemberView.jspsubject.view.subjectAccessPriv =/WEB-INF/jsp/subjectAccessPrivView.jspsubject.view.subjectNamingPriv =/WEB-INF/jsp/subjectNamingPrivView.jspsubject.view.subjectSummaryLink =/WEB-INF/jsp/subjectSummaryLinkView.jspsubject.view.current =/WEB-INF/jsp/currentSubjectView.jspsubject.view.isMemberOf =/WEB-INF/jsp/subjectIsMemberOfView.jspsubject.view.isIndirectMemberOf =/WEB-INF/jsp/subjectIsIndirectMemberOfView.jspsubject.view.hasPrivilege =/WEB-INF/jsp/subjectHasPrivilegeView.jspsubject.view.savedSubject =/WEB-INF/jsp/subjectSearchResultLinkView.jspgroup.view.hasPrivilege =/WEB-INF/jsp/subjectHasPrivilegeView.jspstem.view.browseHierarchy =/WEB-INF/jsp/browseChildStem.jspstem.view.assignFoundMember =/WEB-INF/jsp/browseChildStem.jspstem.view.stemSearchResultLink =/WEB-INF/jsp/stemSearchResultLinkView.jspstem.view.searchResultItem =/WEB-INF/jsp/stemSearchResultItemView.jspstem.view.default =/WEB-INF/jsp/stemView.jspstem.view.savedStem =/WEB-INF/jsp/stemSearchResultLinkView.jspsubjectType.group.view.assignFoundMember =/WEB-INF/jsp/browseForFindChildGroup.jspsubjectType.group.view.subjectSearchResult =/WEB-INF/jsp/groupAsSubjectSearchResultView.jsp##for subject searches which arent groups, this is the view (to put the subject image)

subject.view.subjectSearchResult =/WEB-INF/jsp/subjectSearchResultView.jspgroup.view.linkGroupMembers =/WEB-INF/jsp/groupLinkMembersView.jspgroup.view.compositeMember =/WEB-INF/jsp/groupChainPathView.jspgroup.view.compositeOwner =/WEB-INF/jsp/groupChainPathView.jspgroup.view.compositeGroupChainMember =/WEB-INF/jsp/compositeGroupChainMemberView.jspgroup.view.isMemberOf =/WEB-INF/jsp/subjectIsMemberOfView.jspgroup.view.current =/WEB-INF/jsp/currentSubjectView.jspgroup.view.browseHierarchy =/WEB-INF/jsp/browseChildGroup.jspgroup.view.assignFoundMember =/WEB-INF/jsp/browseForFindChildGroup.jspgroup.view.groupSearchResultLink =/WEB-INF/jsp/groupSearchResultLinkView.jspgroup.view.groupSearchResultWithPrivs =/WEB-INF/jsp/groupSearchResultWithPrivsView.jspgroup.view.savedGroup =/WEB-INF/jsp/groupSearchResultLinkView.jspgroup.view.groupMember =/WEB-INF/jsp/subjectView.jspgroup.view.chainPath =/WEB-INF/jsp/groupChainPathView.jspgroup.view.subjectSummaryGroupLink =/WEB-INF/jsp/groupChainPathView.jspgroup.view.searchResultItem =/WEB-INF/jsp/groupSearchResultItemView.jspgroup.view.groupChain =/WEB-INF/jsp/groupChainView.jspgroup.view.default =/WEB-INF/jsp/subjectView.jspmembership.view.subjectSummaryMemberLink =/WEB-INF/jsp/subjectSummaryMemberLinkView.jspmembership.view.subjectSummary =/WEB-INF/jsp/subjectSummaryMembershipView.jspmembership.view.memberLink =/WEB-INF/jsp/memberLinkView.jspmembership.view.memberWithoutLink =/WEB-INF/jsp/memberWithoutLinkView.jspmembership.view.default =/WEB-INF/jsp/defaultMembershipView.jspmembership.view.removableMembershipInfo =/WEB-INF/jsp/removableMembershipView.jspmembership.view.compositeMember =/WEB-INF/jsp/compositeMembershipView.jspsubjectprivilege.view.subjectSummaryPrivilege =/WEB-INF/jsp/subjectSummaryPrivilegeView.jspsubjectprivilege.view.default =/WEB-INF/jsp/defaultSubjectPrivilegeView.jspsubjectprivilege.access.view.privilegesLink =/WEB-INF/jsp/accessPrivilegesLinkView.jspsubjectprivilege.naming.view.privilegesLink =/WEB-INF/jsp/namingPrivilegesLinkView.jsplist.view.default =/WEB-INF/jsp/genericListView.jsplist.view.groupSummaryGroupTypes =/WEB-INF/jsp/genericItemsOnlyListView.jsplist.view.groupSummaryFields =/WEB-INF/jsp/genericItemsOnlyListView.jsplist.view.editGroupAttributes =/WEB-INF/jsp/genericItemsOnlyListView.jsplist.view.editAttributesFields =/WEB-INF/jsp/genericItemsOnlyListView.jsplist.view.compositesAsFactor =/WEB-INF/jsp/genericItemsOnlyListView.jsplist.view.searchAttributesFields =/WEB-INF/jsp/genericItemsOnlyListView.jsplist.view.searchForPrivAssignHeader =/WEB-INF/jsp/searchForPrivAssignmentListHeaderView.jsplist.view.searchForPrivAssignFooter =/WEB-INF/jsp/searchForPrivAssignmentListFooterView.jsplist.view.browseStemsFindHeader =/WEB-INF/jsp/browseStemsFindListHeaderView.jsplist.view.browseStemsFindFooter =/WEB-INF/jsp/browseStemsFindListFooterView.jsplist.view.removableMemberLinksHeader =/WEB-INF/jsp/removableMemberLinksHeaderView.jsplist.view.removableMemberLinksFooter =/WEB-INF/jsp/removableMemberLinksFooterView.jsplist.view.genericListHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.genericListFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.memberLinksHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.privilegeLinksHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.browseHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.findNewHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.assignHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.searchResultHeader =/WEB-INF/jsp/genericListHeaderView.jsplist.view.memberLinksFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.privilegeLinksFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.browseFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.findNewFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.assignFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.searchResultFooter =/WEB-INF/jsp/genericListFooterView.jsplist.view.chain =/WEB-INF/jsp/chainPath.jspfield.list.view.default =/WEB-INF/jsp/fieldLISTView.jspfield.list.view.withValue =/WEB-INF/jsp/fieldLISTWithValueView.jspfield.list.view.schema =/WEB-INF/jsp/fieldSchemaView.jspfield.attribute.view.withValue =/WEB-INF/jsp/fieldATTRIBUTEWithValueView.jspfield.attribute.view.editValue =/WEB-INF/jsp/fieldATTRIBUTEEditValueView.jspfield.attribute.view.search =/WEB-INF/jsp/fieldATTRIBUTESearchValueView.jspfield.attribute.view.schema =/WEB-INF/jsp/fieldSchemaView.jspgroupType.view.groupSummary =/WEB-INF/jsp/groupTypeSummaryView.jspgroupType.view.editGroupAttributes =/WEB-INF/jsp/groupTypeEditAttributesView.jspgroupType.view.schema-summary =/WEB-INF/jsp/groupTypeSchemaSummaryView.jspgroupType.view.audit-link =/WEB-INF/jsp/groupTypeAuditLinkView.jspauditEntry.view.summary.type.import-importExport =/WEB-INF/jsp/audit/import-importExport.jspauditEntry.view.summary.type.deleteGroupType-groupType =/WEB-INF/jsp/audit/deleteGroupType-groupType.jspauditEntry.view.summary.type.move-stem =/WEB-INF/jsp/audit/move-stem.jspauditEntry.view.summary.type.copy-stem =/WEB-INF/jsp/audit/copy-stem.jspauditEntry.view.summary.type.updateGroupPrivilege-privilege  
 =/WEB-INF/jsp/audit/updateGroupPrivilege-privilege.jspauditEntry.view.summary.type.addGroupField-groupField =/WEB-INF/jsp/audit/addGroupField-groupField.jspauditEntry.view.summary.type.updateGroupType-groupType =/WEB-INF/jsp/audit/updateGroupType-groupType.jspauditEntry.view.summary.type.addAttributeDefName-attributeDefName  
 =/WEB-INF/jsp/audit/addAttributeDefName-attributeDefName.jspauditEntry.view.summary.type.updateStem-stem =/WEB-INF/jsp/audit/updateStem-stem.jspauditEntry.view.summary.type.addGroupPrivilege-privilege  
 =/WEB-INF/jsp/audit/addGroupPrivilege-privilege.jspauditEntry.view.summary.type.deleteGroupField-groupField  
 =/WEB-INF/jsp/audit/deleteGroupField-groupField.jspauditEntry.view.summary.type.addGroup-group =/WEB-INF/jsp/audit/addGroup-group.jspauditEntry.view.summary.type.deleteGroupMembership-membership  
 =/WEB-INF/jsp/audit/deleteGroupMembership-membership.jspauditEntry.view.summary.type.updateGroup-group =/WEB-INF/jsp/audit/updateGroup-group.jspauditEntry.view.summary.type.deleteGroupComposite-groupComposite  
 =/WEB-INF/jsp/audit/deleteGroupComposite-groupComposite.jspauditEntry.view.summary.type.deleteGroup-group =/WEB-INF/jsp/audit/deleteGroup-group.jspauditEntry.view.summary.type.updateGroupField-groupField  
 =/WEB-INF/jsp/audit/updateGroupField-groupField.jspauditEntry.view.summary.type.deleteGroupAttribute-groupAttribute  
 =/WEB-INF/jsp/audit/deleteGroupAttribute-groupAttribute.jspauditEntry.view.summary.type.copy-group =/WEB-INF/jsp/audit/copy-group.jspauditEntry.view.summary.type.addGroupComposite-groupComposite  
 =/WEB-INF/jsp/audit/addGroupComposite-groupComposite.jspauditEntry.view.summary.type.addAttributeDef-attributeDef  
 =/WEB-INF/jsp/audit/addAttributeDef-attributeDef.jspauditEntry.view.summary.type.unassignGroupType-groupTypeAssignment  
 =/WEB-INF/jsp/audit/unassignGroupType-groupTypeAssignment.jspauditEntry.view.summary.type.addGroupType-groupType =/WEB-INF/jsp/audit/addGroupType-groupType.jspauditEntry.view.summary.type.addStemPrivilege-privilege=/WEB-INF/jsp/audit/addStemPrivilege-privilege.jspauditEntry.view.summary.type.addGroupAttribute-groupAttribute  
 =/WEB-INF/jsp/audit/addGroupAttribute-groupAttribute.jspauditEntry.view.summary.type.updateGroupMembership-membership  
 =/WEB-INF/jsp/audit/updateGroupMembership-membership.jspauditEntry.view.summary.type.deleteStemPrivilege-privilege  
 =/WEB-INF/jsp/audit/deleteStemPrivilege-privilege.jspauditEntry.view.summary.type.updateGroupComposite-groupComposite  
 =/WEB-INF/jsp/audit/updateGroupComposite-groupComposite.jspauditEntry.view.summary.type.changeSubject-member =/WEB-INF/jsp/audit/changeSubject-member.jspauditEntry.view.summary.type.addStem-stem =/WEB-INF/jsp/audit/addStem-stem.jspauditEntry.view.summary.type.updateStemPrivilege-privilege  
 =/WEB-INF/jsp/audit/updateStemPrivilege-privilege.jspauditEntry.view.summary.type.deleteStem-stem =/WEB-INF/jsp/audit/deleteStem-stem.jspauditEntry.view.summary.type.addGroupMembership-membership  
 =/WEB-INF/jsp/audit/addGroupMembership-membership.jspauditEntry.view.summary.type.assignGroupType-groupTypeAssignment  
 =/WEB-INF/jsp/audit/assignGroupType-groupTypeAssignment.jspauditEntry.view.summary.type.deleteGroupPrivilege-privilege  
 =/WEB-INF/jsp/audit/deleteGroupPrivilege-privilege.jspauditEntry.view.summary.type.move-group =/WEB-INF/jsp/audit/move-group.jspauditEntry.view.summary.type.updateGroupAttribute-groupAttribute  
 =/WEB-INF/jsp/audit/updateGroupAttribute-groupAttribute.jspauditEntry.view.summary =/WEB-INF/jsp/audit/summary.jspauditEntry.view.queryResult =/WEB-INF/jsp/audit/queryResult.jspauditEntry.view.default =/WEB-INF/jsp/audit/summary.jsp

### ObjectAsMap Implementations

Allow sites to provide local implementations of Map wrappers of Grouper objects

objectasmap.StemAsMap.impl =edu.internet2.middleware.grouper.ui.util.StemAsMapobjectasmap.GroupAsMap.impl =edu.internet2.middleware.grouper.ui.util.GroupAsMapobjectasmap.FieldAsMap.impl =edu.internet2.middleware.grouper.ui.util.FieldAsMapobjectasmap.MembershipAsMap.impl =edu.internet2.middleware.grouper.ui.util.MembershipAsMapobjectasmap.SubjectAsMap.impl =edu.internet2.middleware.grouper.ui.util.SubjectAsMapobjectasmap.SubjectPrivilegeAsMap.impl =edu.internet2.middleware.grouper.ui.util.SubjectPrivilegeAsMapobjectasmap.AuditEntryAsMap.impl =edu.internet2.middleware.grouper.ui.util.AuditEntryAsMap

### Infodots

##if the little yellow "i" images that show help should be enabled

infodot.enable =true

### Lite UI settings

```
 #### Lite UI settings
#cant be subjectId, sourceId, name, description, screenLabel, memberId, or attribute name which is single valued
#comma separated.  will be sorted by sourceId, then the sort field (recommended to be screenName)
simpleMembershipUpdate.exportAllSubjectFields=sourceId, screenLabel, entityId, name, description
simpleMembershipUpdate.exportAllSortField=screenLabel
simpleMembershipUpdate.groupComboboxResultSize=200
simpleMembershipUpdate.filterComboMinChars=3
simpleMembershipUpdate.filterMaxSearchSubjects=1000
#max subjects in drop down
simpleMembershipUpdate.subjectComboboxResultSize=50

# customizer class to customize common things (extend GrouperUiCustomizer)
grouperUiCustomizerClassname =

# kill all cookies with these prefixes on logout (comma separated)
grouperUi.logout.cookie.prefix =

# images (must be in assets/images dir) for subject source id
grouperUi.subjectImg.sourceId.0 = jdbc
grouperUi.subjectImg.image.0 = user.png
#e.g. Chris Hyzer, mchyzer, IT Services
# grouperUi.subjectImg.screenEl.0 = ${subject.name}, $subject.getAttributeValue("netId"), $subject.getAttributeValue("dept")
grouperUi.subjectImg.screenEl.0 = ${subject.description}

grouperUi.subjectImg.sourceId.1 = g:gsa
grouperUi.subjectImg.image.1 = group.png
grouperUi.subjectImg.screenEl.1 = ${grouperUiUtils.convertSubjectToLabel(subject)}

grouperUi.subjectImg.sourceId.2 = g:isa
grouperUi.subjectImg.image.2 = application.png
grouperUi.subjectImg.screenEl.2 = ${grouperUiUtils.convertSubjectToLabel(subject)}

grouperUi.logHtmlDir =
grouperUi.logHtml = false

#if comma separated email addresses are here, then errors will trigger emails (ui-lite)
errorMailAddresses =

```

#### See Also

[Customizing the Grouper UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545413/Customizing+the+Grouper+UI)
