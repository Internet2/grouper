# Testing Grouper

How to build, set up the test database, and run unit tests for Grouper.

## Build prerequisites

Before running the unit tests, get the latest snapshot and build the Java libraries, and incorporate dependencies:

```bash
git checkout GROUPER_7_BRANCH
git pull

mvn -f grouper-parent clean package
mvn -f grouper dependency:copy-dependencies
```

## Test database

Unit testing requires an associated database. Use the following Docker command to start with a blank PostgreSQL one:

```bash
docker run --name grouper-ci-pgsql -d \
  -e "POSTGRES_USER=grouper" \
  -e "POSTGRES_PASSWORD=test" \
  -e "POSTGRES_DB=grouper" \
  -p 15432:5432 \
  postgres -N 1000
```

After the test run completes, delete the running `grouper-ci-pgsql` Docker container.

## Running the full suite

```bash
CP=grouper/misc/ci-test/confForTestPGSQL
CP=$CP:$(compgen -G "grouper/target/grouper-[0-9].[0-9].[0-9]*.jar" | grep -v -- '-sources.jar' | tr '\n' ':' | sed -e 's/::/:/;s/:$//')
CP=$CP:"grouper/target/dependency/*"
CP=$CP:grouper/conf

java -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=./ \
  -Xms80m -Xmx640m \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.sql/java.sql=ALL-UNNAMED \
  edu.internet2.middleware.grouper.AllTests \
  -all -noprompt > test.log 2>&1
```

## Running a specific suite

Add the suite name as a suffix after the `AllTests` class on the `java` command line. Available named suites:

- `MembershipFinderTest`
- `MembershipSaveTest`
- `CompositeSaveTest`
- `TestStemFinder`
- `TestComposite`
- `TestGroupType`
- `TestSession`
- `TestGrouperVersion`
- `TestStem`
- `TestField`
- `TestCompositeU`
- `TestStemApi`
- `TestStemIntegration`
- `TestGroupTypeIncludeExclude`
- `TestCompositeI`
- `TestCompositeModel`
- `TestGrouperSession`
- `TestRegistrySubject`
- `PrivilegeGroupInheritanceSaveTest`
- `(not normally run) edu.internet2.middleware.grouperClient.AllClientConfigTests`
- `abac.AllAbacTests`
- `app.AllAppTests`
- `attr.AllAttributeTests`
- `audit.AllAuditTests`
- `changeLog.AllChangeLogTests`
- `client.AllClientTests`
- `cfg.AllConfigTests`
- `entity.AllEntityTests`
- `(not normally run) ddl.AllDdlTests`
- `app.duo.AllDuoProvisionerTests`
- `app.duo.role.AllDuoRoleProvisionerTests`
- `externalSubjects.AllExternalSubjectTests`
- `filter.AllFilterTests`
- `app.google.AllGoogleProvisionerTests`
- `group.AllGroupTests`
- `cache.AllGrouperCacheTests`
- `ui.AllGrouperUiTests`
- `hibernate.AllHibernateTests`
- `hooks.AllHooksTests`
- `internal.dao.AllInternalDaoTests`
- `log.AllLogTests`
- `member.AllMemberTests`
- `membership.AllMembershipTests`
- `messaging.AllMessagingTests`
- `misc.AllMiscTests`
- `permissions.AllPermissionsTests`
- `plugins.AllPluginsTests`
- `pit.AllPITTests`
- `privs.AllPrivsTests`
- `rules.AllRulesTests`
- `service.AllServiceTests`
- `sqlCache.AllSqlCacheTests`
- `stem.AllStemTests`
- `subj.AllSubjectTests`
- `tableIndex.AllTableIndexTests`
- `userData.AllUserDataTests`
- `util.AllUtilTests`
- `validator.AllValidatorTests`
- `xml.AllXmlTests`
- `xmpp.AllXmppTests`
