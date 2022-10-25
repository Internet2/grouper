# SCIM 2 Server - Integration Testing

The TierXXXProviderIntegrationTest classes in this directory are meant to be tested against a running SCIM server. The
server should be running at localhost:8080, with web context `/grouper-ws`, and with the default jdbc source used
by the RegistrySubjects. The container needs Basic authentication as GrouperSystem:pass.

The registry subjects don't get deleted at the end of the tests (I don't see an API method to
do that), but the groups and memberships for those subjects will be cleaned up.

The easiest way to stand up a service is to run it with docker-compose (replace ${PATH_TO_GROUPER} with the base of the local repo clone):

docker-compose.yml
```
version: '3'
services:
  postgres:
    image: "postgres:14"
    restart: always
    ports:
      - '5432:5432'
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=pass

  grouper:
    image: "i2incommon/grouper:2.6.16.3"
    restart: always
    ports:
      - 8080:8080
      # Optionally debug
      #- '8000:5005'
    command:
      - scim
    environment:
      - GROUPER_SCIM=true
      - GROUPER_SCIM_ONLY=true
      - GROUPER_SCIM_GROUPER_AUTH=true
      - GROUPER_RUN_APACHE=false
      - GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true
      - GROUPER_RUN_SHIB_SP=false
      - GROUPERSYSTEM_QUICKSTART_PASS=pass
      - GROUPER_MORPHSTRING_ENCRYPT_KEY=abcd1234
      - GROUPER_DATABASE_USERNAME=postgres
      - GROUPER_DATABASE_PASSWORD=pass
      - GROUPER_DATABASE_URL=jdbc:postgresql://postgres:5432/postgres
      - GROUPER_AUTO_DDL_UPTOVERSION=v2.6.*
      # Optionally debug
      #- GROUPER_EXTRA_CATALINA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
    volumes:
      - ./grouper.hibernate.properties:/opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties
      - ${PATH_TO_GROUPER}/grouper/misc/ci-test/confForTestPGSQL/subject.properties:/opt/grouper/slashRoot/opt/grouper/grouperWebapp/WEB-INF/classes/subject.properties
```

grouper.hibernate.properties
```
grouperPasswordConfigOverride_WS_GrouperSystem_pass.elConfig = ${elUtils.processEnvVarOrFile('GROUPERSYSTEM_QUICKSTART_PASS')}
```
