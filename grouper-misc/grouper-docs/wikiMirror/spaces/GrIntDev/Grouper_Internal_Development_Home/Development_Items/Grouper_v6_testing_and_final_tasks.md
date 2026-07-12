---
title: "Grouper v6 testing and final tasks"
space: GrIntDev
pageId: 48792525
version: 42
lastUpdated: 2026-07-12T07:01:08.439Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792525/Grouper+v6+testing+and+final+tasks
---

## Tests

| Test | v5 | v5 ran by | v6 | v6 ran by |
| --- | --- | --- | --- | --- |
| API harnesses | Ran | Chad |  |  |
| WS tests | Ran | Chris |  |  |
| Google | Ran | Vivek | Ran | Vivek |
| Azure | Ran | Vivek | Ran | Vivek |
| Box | Ran | Vivek | Ran | Vivek |
| Duo | Ran | Vivek | Ran | Vivek |
| Duo role | Ran | Vivek | Ran | Vivek |
| Midpoint | Ran | Vivek | Ran | Vivek |
| Okta | Ran | Vivek | Ran | Vivek |
| Remedy | Ran | Vivek | Ran | Vivek |
| Scim | Ran | Vivek | Ran | Vivek |
| LDAP | Ran | Vivek | Ran | Vivek |

## Tests to fix

| Test | Status | Assigned to | Description |
| --- | --- | --- | --- |
| testExecuteGshTemplate |  |  |  |
| testGetMembershipsPaging |  |  |  |
| testSortGroupsByVootMembershipRole |  |  |  |
| testXmlDifferentUpdateProperties | FIXED | Vivek |  |
| test_move_with_entities | FIXED | Shilen |  |
| testSubjectIdAndDeleteProcessedRows | FIXED | Shilen |  |
| testSubjectIdentifierAndKeepProcessedRows | FIXED | Shilen |  |
| testVersion1 | FIXED | Shilen |  |
| testPagingCursor | FIXED | Vivek |  |
| testFinderByName | FIXED | Shilen |  |
| testSubjectFinder |  |  |  |
| testFullSyncDuoStartWithAndDiagnostics | FIXED | Vivek |  |
| testFullSyncDuoRoleStartWithAndDiagnostics | FIXED | Vivek |  |
| testFullSyncGoogle | FIXED | Vivek |  |
| testIncrementalSyncGoogle | FIXED | Vivek |  |
| testFullSyncGoogleStartWithAndDiagnostics | FIXED | Vivek |  |
| testDoNotExistErrorCode |  |  |  |
| test_copy_entity | FIXED | Shilen |  |
| test_move_entity | FIXED | Shilen |  |
| testPlugin |  |  |  |
| Test_privs_CachingAccessResolver.test_hasPrivilege_cacheMiss | FIXED | Vivek |  |
| Test_privs_CachingAccessResolver.test_hasPrivilege_cacheSize | FIXED | Vivek |  |
| Test_privs_CachingAccessResolver.test_hasPrivilege_cacheHit | FIXED | Vivek |  |
| Test_privs_CachingNamingResolver.test_hasPrivilege_cacheMiss | FIXED | Vivek |  |
| Test_privs_CachingNamingResolver.test_hasPrivilege_cacheSize | FIXED | Vivek |  |
| Test_privs_CachingNamingResolver.test_hasPrivilege_cacheHit | FIXED | Vivek |  |
| testExportImport | FIXED | Shilen |  |
| testMembershipAssignments |  |  |  |
| testStemObliterate2AttributeDef3 | FIXED | Vivek |  |
| testGetValueOrExpressionEvaluation | FIXED | Vivek |  |
| testUsduJobWhenSubjectBecomesResolvableAgain |  | Shilen |  |
| testInsertEditDelete | FIXED | Vivek |  |
| testTextOrNull | FIXED | Vivek |  |
| test_getSession_nullSession | FIXED | Vivek |  |
| test_getSession_equalsSetSession | FIXED | Vivek |  |
| test_getSession_equalsSetSession2 | FIXED | Vivek |  |
| testEnabledDisabledDaemon | No changes made | Vivek | Passing for me individually and in junit test suite |
| GroupDoNotDeleteIfProvisionableTest.testHook | FIXED | Vivek |  |
| GroupUniqueNameCaseInsensitiveHookTest.testHook | FIXED | Shilen |  |
| StemUniqueNameCaseInsensitiveHookTest.testHook | FIXED | Shilen |  |
| testHookCaseSensitive | FIXED | Shilen |  |
| testLogging | No changes made | Vivek | Passing for me in junit test suite |
| testFindFoldersAllowed | Not Fixed | Vivek | [JIRA GRP-6513] |
| testShorthandByIdentifierAndSource | FIXED | Vivek |  |
| testShorthandById | FIXED | Vivek |  |
| testShorthandByIdAndSource | FIXED | Vivek |  |
| testShorthandByIdentifier | FIXED | Vivek |  |
| testShorthandByIdNoThreadLocal | FIXED | Vivek |  |
| testRuleVetoPermissionsDaemon | FIXED | Shilen |  |
| testRuleVetoDaemon |  |  |  |
| testRuleLonghandDaemon |  |  |  |
| testPrivilegeAddedByGroupFull | FIXED | Shilen |  |
| testIndexesGroup | FIXED | Vivek |  |
| testIndexesAttributeDef | FIXED | Vivek |  |
| testIndexesStem | FIXED | Vivek |  |
| testFixHibernateConnectionUrl | FIXED | Vivek |  |
| Test_privs_CachingNamingResolver | FIXED | Vivek |  |
| testSimpleGroupLdapPaMatchingIdMissingValidation |  |  |  |
| testSimpleGroupLdapPa | Worked on local | Vivek |  |
| testLoaderLdapLookupByDn | Worked on local | Vivek |  |
| testLoaderLdapLookupByFilter | Worked on local | Vivek |  |
| testLoaderLdapSimpleConvertDn | Worked on local | Vivek |  |
| testLoaderLdapLookupByDnCache | Worked on local | Vivek |  |
| testLoaderLdapReverse | Worked on local | Vivek |  |
| testLoaderShouldAbortWithoutChangingMembershipsSimpleIgnoreMinGroupSize | Worked on local | Vivek |  |
| testLoaderLdapBulkLookupByFilterCache | Worked on local | Vivek |  |
| testLoaderLdapLookupByFilterCache | Worked on local | Vivek |  |
| testLoaderShouldAbortListWithoutChangingMembershipsOverallMinMemberships2 | Worked on local | Vivek |  |
| testLoaderCopyLdapToSql | Worked on local | Vivek |  |
| testLdapProvisionerConfigurationInsertEditDelete | Worked on local | Vivek |  |
| testIncrementalPolicyRestriction | Worked on local | Vivek |  |
| testIncrementalRegexRestriction | Worked on local | Vivek |  |
| testIncrementalStemScopeOne | Worked on local | Vivek |  |
| testIncrementalPolicyRestrictionUsingFolder | Worked on local | Vivek |  |
| testIncrementalDirectToIndirectGroup | Worked on local | Vivek |  |
| testIncrementalStemNotProvisionable | Worked on local | Vivek |  |
| testVersion1 | Worked on local | Vivek |  |
| testRetrieveAttributes | Fixed | Vivek |  |
| testInsertEditDelete | Worked on local | Vivek |  |
| testSubjectFinder | Worked on local | Vivek |  |
| testPlugin | Worked on local | Vivek | Ensure the classpath has the jar file on it. |
| testMembershipAssignments | Worked on local | Vivek |  |
| testConvertDnToSubpath | Fixed | Shilen |  |
| testParseRealConfigFiles | Worked on local | Vivek |  |
| test_getSession_nullSession | Worked on local | Vivek |  |
| test_getSession_equalsSetSession | Worked on local | Vivek |  |
| test_getSession_equalsSetSession2 | Worked on local | Vivek |  |
| testEnabledDisabledDaemon | Worked on local | Vivek |  |
| testLogging | Worked on local | Vivek |  |
| testFindFoldersAllowed |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |

## Tasks

| Task | Owner | Status | Description |
| --- | --- | --- | --- |
| Run v5 tests |  |  |  |
| Upgrade jars and libraries | Chad | FIXED |  |
| Remove unused externalized texts | Chad | DONE |  |
| Reconcile duplicate configs |  |  | See Below |
| Remove unused configs | Chad | DONE |  |
| Refactor SQLs into DAOs |  |  |  |
| Fix user lifecycle issues from Shilen |  |  |  |
| Run v6 tests |  |  |  |
| Release v6 |  |  |  |
| Branch v7 |  |  |  |

### Duplicate text configs

(1)

rulesTableConditionHumanFriendlyValue_thisGroupHasImmediateEnabledMembership = ${grouperRequestContainer.rulesContainer.currentGuiRuleDefinition.ifGuiObject.shortLinkWithIcon} has immediate enabled membership

rulesTableConditionHumanFriendlyValue_thisGroupHasImmediateEnabledMembership = Entity is a direct member of ${grouperRequestContainer.rulesContainer.currentGuiRuleDefinition.ifGuiObject.shortLinkWithIcon}

(2)

config.GenericConfiguration.attribute.replaceMemberships.description = If memberships should be replaced in the target. Generally this will be 'false'. This is only applicable if your provisioner can replace memberships.

config.GenericConfiguration.attribute.replaceMemberships.description = On a full sync should memberships be replaced in target. Generally this will be 'true' except when you do not want to full sync memberships
