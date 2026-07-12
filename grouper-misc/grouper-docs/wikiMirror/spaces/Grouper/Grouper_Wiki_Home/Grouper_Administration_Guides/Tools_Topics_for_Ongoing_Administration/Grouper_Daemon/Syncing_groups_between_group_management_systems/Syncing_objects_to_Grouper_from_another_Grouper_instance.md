---
title: "Syncing objects to Grouper from another Grouper instance"
space: Grouper
pageId: 28555092
version: 10
lastUpdated: 2026-07-01T05:39:00.769Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555092/Syncing+objects+to+Grouper+from+another+Grouper+instance
---

This feature is available in v2.5.43+. If you are running large syncs make sure your daemon has a lot of memory and that large jobs do not run at the same time

You can provide your own SQL or you can sync between another grouper and the SQL will be automatic

If you are doing the automatic sync the source Grouper needs to be v1.6+

## Example from training environment

1. Get the env up and running, identify some folders to sync. Lets get the app:vpn and the ref folders
2. We need a database connection to that database from our database
3. Note: you need a consistent subject source
  
  
  
  
  ```
  subjectApi.source.ldap.id = ldap
  subjectApi.source.ldap.name = EDU Ldap 
  subjectApi.source.ldap.types = person
  subjectApi.source.ldap.adapterClass = edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter
  subjectApi.source.ldap.param.ldapServerId.value = grouperTrainingLdap
  subjectApi.source.ldap.param.SubjectID_AttributeType.value = uid
  subjectApi.source.ldap.param.SubjectID_formatToLowerCase.value = false
  subjectApi.source.ldap.param.Name_AttributeType.value = cn
  subjectApi.source.ldap.param.Description_AttributeType.value = cn
  subjectApi.source.ldap.param.VTLDAP_VALIDATOR.value = ConnectLdapValidator
  
  subjectApi.source.ldap.param.SubjectID_AttributeType.value = uid
  subjectApi.source.ldap.param.SubjectID_formatToLowerCase.value = false
  subjectApi.source.ldap.param.Name_AttributeType.value = cn
  subjectApi.source.ldap.param.Description_AttributeType.value = cn
  subjectApi.source.ldap.param.subjectVirtualAttribute_0_searchAttribute0.value = ${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('uid'), "")},${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('cn'), "")},${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('exampleEduRegId'), "")}
  subjectApi.source.ldap.param.sortAttribute0.value = cn
  subjectApi.source.ldap.param.searchAttribute0.value = searchAttribute0
  
  subjectApi.source.ldap.param.subjectIdentifierAttribute0.value = employeeNumber
  
  subjectApi.source.ldap.search.searchSubject.param.filter.value = (&(uid=%TERM%)(objectclass=person))
  subjectApi.source.ldap.search.searchSubject.param.scope.value = SUBTREE_SCOPE
  subjectApi.source.ldap.search.searchSubject.param.base.value = ou=people,dc=internet2,dc=edu
  
  subjectApi.source.ldap.search.searchSubjectByIdentifier.param.filter.value = (&(employeeNumber=%TERM%)(objectclass=person))
  subjectApi.source.ldap.search.searchSubjectByIdentifier.param.scope.value = SUBTREE_SCOPE
  subjectApi.source.ldap.search.searchSubjectByIdentifier.param.base.value = ou=people,dc=internet2,dc=edu
  
  subjectApi.source.ldap.search.search.param.filter.value = (&(|(|(uid=%TERM%)(cn=*%TERM%*))(uid=%TERM%*))(objectclass=person))
  subjectApi.source.ldap.search.search.param.scope.value = SUBTREE_SCOPE
  subjectApi.source.ldap.search.search.param.base.value = ou=people,dc=internet2,dc=edu
  
  subjectApi.source.ldap.internalAttributes = searchAttribute0
  
  
  ```
4. Configure the sync to grouper from the training env
5. Run readonly and see report. There is an abbreviated report in the daemon job message in the UI. If you configure it above to log full report, you will see it in the daemon logs.
  
  
  
  
  ```
  differences: 2236
  changeCount: 0
  errors: 0
  stemInserts: 9
  groupInserts: 27
  compositeInserts: 1
  membershipInserts: 2197
  groupPrivInserts: 2
  stemInsertNames: TreeSet size: 9: [0]: app:vpn
  [1]: app:vpn:security
  [2]: app:vpn:service
  [3]: app:vpn:service:policy
  [4]: app:vpn:service:ref
  [5]: ref
  [6]: ref:dept
  [7]: ref:employee
  [8]: ref:iam
  
  groupInsertNames: TreeSet size: 27: [0]: app:vpn:security:vpn_ajohnson409_mgr
  [1]: app:vpn:service:policy:vpn_authorized
  [2]: app:vpn:service:policy:vpn_authorized_allow
  [3]: app:vpn:service:policy:vpn_authorized_deny
  [4]: app:vpn:service:ref:vpn_adhoc
  [5]: app:vpn:service:ref:vpn_ajohnson409
  [6]: app:vpn:service:ref:vpn_consultants
  [7]: ref:community
  [8]: ref:dept:Accounting
  [9]: ref:dept:Accounts Payable
  [10]: ref:dept:Advising
  [11]: ref:dept:Business
  [12]: ref:dept:Computer Science
  [13]: ref:dept:Engineering
  [14]: ref:dept:Financial Aid
  [15]: ref:dept:Information Technology
  [16]: ref:dept:Language Arts
  [17]: ref:dept:Law
  [18]: ref:dept:Physical Education
  [19]: ref:dept:Purchasing
  [20]: ref:employee:fac_staff
  [21]: ref:fac_staff_student
  [22]: ref:faculty
  [23]: ref:iam:active
  [24]: ref:iam:global_deny
  [25]: ref:staff
  [26]: ref:student
  
  compositeInsertNames: TreeSet size: 1: [0]: app:vpn:service:policy:vpn_authorized
  
  membershipInsertNames: TreeSet size: 10: [0]: ref:dept:Business - sbutler930
  [1]: ref:dept:Engineering - clopez383
  [2]: ref:dept:Engineering - mnielson343
  [3]: ref:dept:Financial Aid - kdavis686
  [4]: ref:dept:Physical Education - mgrady967
  [5]: ref:faculty - kthompson169
  [6]: ref:faculty - thenderson914
  [7]: ref:staff - ehenderson862
  [8]: ref:staff - mlewis390
  [9]: ref:student - awhite318
  
  groupPrivInsertNames: TreeSet size: 2: [0]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - readers
  [1]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - updaters
  
  
  
  
  
  ```
6. Run read/write and see changes
  
  
  
  
  ```
  differences: 2236
  changeCount: 2236
  errors: 0
  stemInserts: 9
  groupInserts: 27
  compositeInserts: 1
  membershipInserts: 2197
  groupPrivInserts: 2
  output: ArrayList size: 2236: [0]: Success inserting folder 'app:vpn
  [1]: Success inserting folder 'app:vpn:security
  [2]: Success inserting folder 'app:vpn:service
  [3]: Success inserting folder 'app:vpn:service:policy
  [4]: Success inserting folder 'app:vpn:service:ref
  [5]: Success inserting folder 'ref
  [6]: Success inserting folder 'ref:dept
  [7]: Success inserting folder 'ref:employee
  [8]: Success inserting folder 'ref:iam
  [9]: Success inserting group 'app:vpn:security:vpn_ajohnson409_mgr
  [10]: Success inserting group 'app:vpn:service:policy:vpn_authorized
  [11]: Success inserting group 'app:vpn:service:policy:vpn_authorized_allow
  [12]: Success inserting group 'app:vpn:service:policy:vpn_authorized_deny
  [13]: Success inserting group 'app:vpn:service:ref:vpn_adhoc
  [14]: Success inserting group 'app:vpn:service:ref:vpn_ajohnson409
  [15]: Success inserting group 'app:vpn:service:ref:vpn_consultants
  [16]: Success inserting group 'ref:community
  [17]: Success inserting group 'ref:dept:Accounting
  [18]: Success inserting group 'ref:dept:Accounts Payable
  [19]: Success inserting group 'ref:dept:Advising
  [20]: Success inserting group 'ref:dept:Business
  [21]: Success inserting group 'ref:dept:Computer Science
  [22]: Success inserting group 'ref:dept:Engineering
  [23]: Success inserting group 'ref:dept:Financial Aid
  [24]: Success inserting group 'ref:dept:Information Technology
  [25]: Success inserting group 'ref:dept:Language Arts
  [26]: Success inserting group 'ref:dept:Law
  [27]: Success inserting group 'ref:dept:Physical Education
  [28]: Success inserting group 'ref:dept:Purchasing
  [29]: Success inserting group 'ref:employee:fac_staff
  [30]: Success inserting group 'ref:fac_staff_student
  [31]: Success inserting group 'ref:faculty
  [32]: Success inserting group 'ref:iam:active
  [33]: Success inserting group 'ref:iam:global_deny
  [34]: Success inserting group 'ref:staff
  [35]: Success inserting group 'ref:student
  [36]: Success inserting composite 'app:vpn:service:policy:vpn_authorized
  [37]: Success inserting membership 'ref:dept:Engineering', 'ldap', 'mnielson343'
  [38]: Success inserting membership 'ref:dept:Physical Education', 'ldap', 'mgrady967'
  [39]: Success inserting membership 'ref:staff', 'ldap', 'mlewis390'
  [40]: Success inserting membership 'ref:student', 'ldap', 'awhite318'
  [41]: Success inserting membership 'ref:faculty', 'ldap', 'thenderson914'
  [42]: Success inserting membership 'ref:dept:Engineering', 'ldap', 'clopez383'
  [43]: Success inserting membership 'ref:dept:Financial Aid', 'ldap', 'kdavis686'
  [44]: Success inserting membership 'ref:staff', 'ldap', 'ehenderson862'
  [45]: Success inserting membership 'ref:faculty', 'ldap', 'kthompson169'
  [46]: Success inserting membership 'ref:dept:Business', 'ldap', 'sbutler930'
  [47]: Success inserting membership 'ref:dept:Business', 'ldap', 'pvales202'
  [48]: Success inserting membership 'ref:community', 'ldap', 'jlewis235'
  [49]: Success inserting membership 'ref:dept:Financial Aid', 'ldap', 'elewis961'
  [50]: Success inserting membership 'ref:community', 'ldap', 'lgrady119'
  [51]: Success inserting membership 'ref:dept:Advising', 'ldap', 'jclark540'
  ...
  [2227]: Success inserting membership 'app:vpn:service:ref:vpn_ajohnson409', 'ldap', 'bsmith458'
  [2228]: Success inserting membership 'ref:student', 'ldap', 'awhite522'
  [2229]: Success inserting membership 'ref:dept:Accounts Payable', 'ldap', 'lbrown571'
  [2230]: Success inserting membership 'ref:faculty', 'ldap', 'sroberts309'
  [2231]: Success inserting membership 'ref:dept:Law', 'ldap', 'egonazles92'
  [2232]: Success inserting membership 'ref:dept:Accounting', 'ldap', 'cdoe834'
  [2233]: Success inserting membership 'ref:faculty', 'ldap', 'ldoe594'
  [2234]: Success inserting privilege group 'app:vpn:service:ref:vpn_ajohnson409', 'g:gsa', 'app:vpn:security:vpn_ajohnson409_mgr', updaters
  [2235]: Success inserting privilege group 'app:vpn:service:ref:vpn_ajohnson409', 'g:gsa', 'app:vpn:security:vpn_ajohnson409_mgr', readers
  
  stemInsertNames: TreeSet size: 9: [0]: app:vpn
  [1]: app:vpn:security
  [2]: app:vpn:service
  [3]: app:vpn:service:policy
  [4]: app:vpn:service:ref
  [5]: ref
  [6]: ref:dept
  [7]: ref:employee
  [8]: ref:iam
  
  groupInsertNames: TreeSet size: 27: [0]: app:vpn:security:vpn_ajohnson409_mgr
  [1]: app:vpn:service:policy:vpn_authorized
  [2]: app:vpn:service:policy:vpn_authorized_allow
  [3]: app:vpn:service:policy:vpn_authorized_deny
  [4]: app:vpn:service:ref:vpn_adhoc
  [5]: app:vpn:service:ref:vpn_ajohnson409
  [6]: app:vpn:service:ref:vpn_consultants
  [7]: ref:community
  [8]: ref:dept:Accounting
  [9]: ref:dept:Accounts Payable
  [10]: ref:dept:Advising
  [11]: ref:dept:Business
  [12]: ref:dept:Computer Science
  [13]: ref:dept:Engineering
  [14]: ref:dept:Financial Aid
  [15]: ref:dept:Information Technology
  [16]: ref:dept:Language Arts
  [17]: ref:dept:Law
  [18]: ref:dept:Physical Education
  [19]: ref:dept:Purchasing
  [20]: ref:employee:fac_staff
  [21]: ref:fac_staff_student
  [22]: ref:faculty
  [23]: ref:iam:active
  [24]: ref:iam:global_deny
  [25]: ref:staff
  [26]: ref:student
  
  compositeInsertNames: TreeSet size: 1: [0]: app:vpn:service:policy:vpn_authorized
  
  membershipInsertNames: TreeSet size: 50: [0]: ref:community - jlewis235
  [1]: ref:community - lgrady119
  [2]: ref:community - mlewis252
  [3]: ref:community - mwilliams323
  [4]: ref:community - nhenderson756
  [5]: ref:dept:Accounting - anielson378
  [6]: ref:dept:Accounting - aprice891
  [7]: ref:dept:Accounting - hlewis924
  [8]: ref:dept:Accounting - jmorrison517
  [9]: ref:dept:Accounts Payable - jbutler593
  [10]: ref:dept:Accounts Payable - landerson294
  [11]: ref:dept:Advising - aanderson465
  [12]: ref:dept:Advising - enielson174
  [13]: ref:dept:Advising - jclark540
  [14]: ref:dept:Advising - mlewis399
  [15]: ref:dept:Advising - nsmith297
  [16]: ref:dept:Business - phenderson38
  [17]: ref:dept:Business - pvales202
  [18]: ref:dept:Business - sbutler930
  [19]: ref:dept:Computer Science - jvales729
  [20]: ref:dept:Computer Science - mlee863
  [21]: ref:dept:Engineering - abutler125
  [22]: ref:dept:Engineering - clopez383
  [23]: ref:dept:Engineering - mnielson343
  [24]: ref:dept:Financial Aid - cclark395
  [25]: ref:dept:Financial Aid - elewis961
  [26]: ref:dept:Financial Aid - jroberts67
  [27]: ref:dept:Financial Aid - kdavis686
  [28]: ref:dept:Language Arts - dsmith789
  [29]: ref:dept:Language Arts - jmorrison596
  [30]: ref:dept:Language Arts - wvales534
  [31]: ref:dept:Physical Education - mgrady967
  [32]: ref:dept:Purchasing - lpeterson773
  [33]: ref:dept:Purchasing - mgrady0
  [34]: ref:faculty - alopez160
  [35]: ref:faculty - kthompson169
  [36]: ref:faculty - mroberts854
  [37]: ref:faculty - rnielson369
  [38]: ref:faculty - svales170
  [39]: ref:faculty - thenderson914
  [40]: ref:staff - ehenderson862
  [41]: ref:staff - jbutler593
  [42]: ref:staff - jclark540
  [43]: ref:staff - lgonazles537
  [44]: ref:staff - mlewis390
  [45]: ref:staff - rdoe288
  [46]: ref:staff - wlee388
  [47]: ref:student - awhite318
  [48]: ref:student - dlee555
  [49]: ref:student - jbutler123
  
  groupPrivInsertNames: TreeSet size: 2: [0]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - readers
  [1]: app:vpn:service:ref:vpn_ajohnson409 - app:vpn:security:vpn_ajohnson409_mgr - updaters
  
  
  
  
  ```
7. Make some changes in the source and run again. I believe if a folder is being deleted and it had other comparable objects inside, then the total number of differences will be different than the actual changes made
  
  
  ```
  differences: 14
  changeCount: 13
  errors: 0
  stemInserts: 1
  stemDeletes: 1
  groupInserts: 2
  compositeInserts: 1
  membershipInserts: 3
  membershipUpdates: 1
  membershipDeletes: 1
  groupPrivInserts: 3
  stemPrivInserts: 1
  output: ArrayList size: 13: [0]: Success deleting folder 'ref:iam'
  [1]: Success inserting folder 'app:vpn:service:basis
  [2]: Success inserting group 'app:vpn:service:basis:someBasis
  [3]: Success inserting group 'app:vpn:service:ref:adhocAndConsultants
  [4]: Success inserting composite 'app:vpn:service:ref:adhocAndConsultants
  [5]: Success deleting membership 'app:vpn:service:ref:vpn_adhoc', 'g:gsa', 'app:vpn:service:ref:vpn_ajohnson409'
  [6]: Success inserting membership 'app:vpn:service:ref:vpn_adhoc', 'ldap', 'ejohnson180'
  [7]: Success inserting membership 'app:vpn:service:ref:vpn_adhoc', 'ldap', 'kdavis311'
  [8]: Success inserting membership 'app:vpn:service:basis:someBasis', 'ldap', 'dsmith789'
  [9]: Success inserting privilege group 'app:vpn:service:policy:vpn_authorized', 'ldap', 'ejohnson175', optins
  [10]: Success inserting privilege group 'app:vpn:service:policy:vpn_authorized', 'ldap', 'ejohnson175', viewers
  [11]: Success inserting privilege group 'app:vpn:service:policy:vpn_authorized', 'ldap', 'bsmith458', readers
  [12]: Success inserting privilege stem 'app:vpn:service:ref', 'ldap', 'plangenberg246', creators
  
  stemInsertNames: TreeSet size: 1: [0]: app:vpn:service:basis
  
  stemDeleteNames: TreeSet size: 1: [0]: ref:iam
  
  groupInsertNames: TreeSet size: 2: [0]: app:vpn:service:basis:someBasis
  [1]: app:vpn:service:ref:adhocAndConsultants
  
  compositeInsertNames: TreeSet size: 1: [0]: app:vpn:service:ref:adhocAndConsultants
  
  membershipInsertNames: TreeSet size: 3: [0]: app:vpn:service:basis:someBasis - dsmith789
  [1]: app:vpn:service:ref:vpn_adhoc - ejohnson180
  [2]: app:vpn:service:ref:vpn_adhoc - kdavis311
  
  membershipUpdateNames: TreeSet size: 1: [0]: app:vpn:service:ref:vpn_consultants - jsmith
  
  membershipDeleteNames: TreeSet size: 1: [0]: app:vpn:service:ref:vpn_adhoc - app:vpn:service:ref:vpn_ajohnson409
  
  groupPrivInsertNames: TreeSet size: 3: [0]: app:vpn:service:policy:vpn_authorized - bsmith458 - readers
  [1]: app:vpn:service:policy:vpn_authorized - ejohnson175 - optins
  [2]: app:vpn:service:policy:vpn_authorized - ejohnson175 - viewers
  
  stemPrivInsertNames: TreeSet size: 1: [0]: app:vpn:service:ref - plangenberg246 - creators
  
  
  
  
  ```
