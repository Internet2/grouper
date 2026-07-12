---
title: "Grouper Swagger codegen"
space: Grouper
pageId: 28555712
version: 4
lastUpdated: 2026-07-01T05:37:36.720Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555712/Grouper+Swagger+codegen
---

You can use Swagger codegen to generate a client WS library in dozens of languages.

## Download the CLI for Swagger codegen

Download the CLI jar (latest non RC release candidate)

[https://mvnrepository.com/artifact/io.swagger/swagger-codegen-cli](https://mvnrepository.com/artifact/io.swagger/swagger-codegen-cli)

## Customize the Swagger JSON in your WS

See [instructions here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549495/Web+Services+OpenAPI+Swagger) on ENV vars to customize your Swagger JSON, or just edit the index.json Swagger file

1. The URL needs to be the endpoint at your institution
2. The context (first path after domain name), needs to match your WS context if its not the default: grouper-ws
3. Authentication needs to be handled (part of this could be added to swagger json file and/or generated code)

## Generate a Java client (could be any programming language supported by Swagger codegen

This example uses the grouperdemo Swagger, but you can use your own customized Swagger by

```
java -jar swagger-codegen-cli-2.4.41.jar generate -i /url/or/file/path/index.json -l java -o samples/client/grouperws/java
```

Now the code is generated, and you need to adjust it in your IDE for that language, this example is Java

## Edit the client in Eclipse

Create a maven project

Copy the pom.xml and src generated files and directories to the eclipse project

Change the project java version to 17 (or the default if the default is 17)

Add the annotation dependency to the pom.xml

```
--> in dependency section

    <dependency>
      <groupId>javax.annotation</groupId>
      <artifactId>javax.annotation-api</artifactId>
      <version>${javax-annotation-api.version}</version>
    </dependency>

--> in property section

    <javax-annotation-api.version>1.3.2</javax-annotation-api.version>

```

Everything should compile at this point

Make an ApiClient subclass that does the authentication. This example is basic auth with a hardcoded password, you would externalized this however you handle passwords

```
package io.swagger.client;

import java.util.List;
import java.util.Map;

import com.squareup.okhttp.Credentials;

public class MyApiClient extends ApiClient {

  @Override
  public void updateParamsForAuth(String[] authNames, List<Pair> queryParams,
      Map<String, String> headerParams) {
    
    String credential = Credentials.basic("test.subject.1", "whateverPassword");
    headerParams.put("Authorization", credential);
    
  }

}

```

## Use the generated code

Use the objects generated from Swagger to build the JSON request and read the JSON response. This example is Java but it would be similar in any language

## Get members

```
package io.swagger.client;

import java.util.ArrayList;
import java.util.List;

import io.swagger.client.api.GrouperApi;
import io.swagger.client.model.WsGetMembersResult;
import io.swagger.client.model.WsGetMembersResults;
import io.swagger.client.model.WsGroupLookup;
import io.swagger.client.model.WsRestGetMembersRequest;
import io.swagger.client.model.WsRestGetMembersRequestWrapper;
import io.swagger.client.model.WsSubject;

public class Test {

  public static void main(String[] args) throws Exception {
    WsRestGetMembersRequestWrapper wsRestGetMembersRequestWrapper = new WsRestGetMembersRequestWrapper();
    WsRestGetMembersRequest wsRestGetMembersRequest = new WsRestGetMembersRequest();
    wsRestGetMembersRequestWrapper.setWsRestGetMembersRequest(wsRestGetMembersRequest);

    List<WsGroupLookup> wsGroupLookups = new ArrayList<>();
    WsGroupLookup wsGroupLookup = new WsGroupLookup();
    
    wsGroupLookup.setGroupName("test:testGroup");
    wsGroupLookups.add(wsGroupLookup);
        
    wsRestGetMembersRequest.setWsGroupLookups(wsGroupLookups);
    
    GrouperApi grouperApi = new GrouperApi();
    ApiClient apiClient = new MyApiClient().setDebugging(true);
    grouperApi.setApiClient(apiClient);
    WsGetMembersResults members = grouperApi.getMembers(wsRestGetMembersRequestWrapper);
    
    if (!"SUCCESS".equals(members.getResultMetadata().getResultCode())) {
      throw new RuntimeException("Invalid result code: " + members.getResultMetadata().getResultCode());
    }
    
    for (WsGetMembersResult wsGetMembersResult : members.getResults()) {
      for (WsSubject wsSubject : wsGetMembersResult.getWsSubjects()) {
        System.out.println(wsSubject.getSourceId() + ": " + wsSubject.getId());
      }
    }
  }

}

```

Output

```
g:isa: GrouperSystem
grouperExternal: 0b5949edd3bf4b65a0ab7e9ce97a4cf9
grouperExternal: 237dd8909c20481eb143fa3ae32df998
jdbc: test
```

## Add Member

```
package io.swagger.client;
 
import java.util.ArrayList;
import java.util.List;
 
import io.swagger.client.api.GrouperApi;
import io.swagger.client.model.WsAddMemberResult;
import io.swagger.client.model.WsAddMemberResults;
import io.swagger.client.model.WsAddMemberResultsWrapper;
import io.swagger.client.model.WsGroupLookup;
import io.swagger.client.model.WsRestAddMemberRequest;
import io.swagger.client.model.WsRestAddMemberRequestWrapper;
import io.swagger.client.model.WsSubject;
import io.swagger.client.model.WsSubjectLookup;
 
public class Test3 {
 
  public static void main(String[] args) throws Exception {
    WsRestAddMemberRequestWrapper wsRestAddMemberRequestWrapper = new WsRestAddMemberRequestWrapper();
    WsRestAddMemberRequest wsRestAddMemberRequest = new WsRestAddMemberRequest();
    wsRestAddMemberRequestWrapper.setWsRestAddMemberRequest(wsRestAddMemberRequest);
 
    List<WsGroupLookup> wsGroupLookups = new ArrayList<>();
    WsGroupLookup wsGroupLookup = new WsGroupLookup();
     
    wsGroupLookup.setGroupName("test:testGroup");
    wsGroupLookups.add(wsGroupLookup);
         
    wsRestAddMemberRequest.setWsGroupLookup(wsGroupLookup);
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("0b5949edd3bf4b65a0ab7e9ce97a4cf9");
    wsSubjectLookup.setSubjectSourceId("grouperExternal");
    List<WsSubjectLookup> wsSubjectLookups = new ArrayList<WsSubjectLookup>();
    wsSubjectLookups.add(wsSubjectLookup);
    wsRestAddMemberRequest.setSubjectLookups(wsSubjectLookups);
    GrouperApi grouperApi = new GrouperApi();
    ApiClient apiClient = new MyApiClient().setDebugging(true);
    grouperApi.setApiClient(apiClient);
    WsAddMemberResultsWrapper addMemberResult = grouperApi.addMember(wsRestAddMemberRequestWrapper);
     
    if (!"SUCCESS".equals(addMemberResult.getWsAddMemberResults().getResultMetadata().getResultCode())) {
      throw new RuntimeException("Invalid result code: " + addMemberResult.getWsAddMemberResults().getResultMetadata().getResultCode());
    }
    for (WsAddMemberResult wsAddMemberResult : addMemberResult.getWsAddMemberResults().getResults()) {
      WsSubject wsSubject = wsAddMemberResult.getWsSubject();
      System.out.println(wsSubject.getSourceId() + ": " + wsSubject.getId()); 
    }
  }
}
```

## Delete member

```
package io.swagger.client;
 
import java.util.ArrayList;
import java.util.List;
 
import io.swagger.client.api.GrouperApi;
import io.swagger.client.model.WsDeleteMemberResult;
import io.swagger.client.model.WsDeleteMemberResults;
import io.swagger.client.model.WsDeleteMemberResultsWrapper;
import io.swagger.client.model.WsGroupLookup;
import io.swagger.client.model.WsRestDeleteMemberRequest;
import io.swagger.client.model.WsRestDeleteMemberRequestWrapper;
import io.swagger.client.model.WsSubject;
import io.swagger.client.model.WsSubjectLookup;
 
public class Test2 {
 
  public static void main(String[] args) throws Exception {
    WsRestDeleteMemberRequestWrapper wsRestDeleteMemberRequestWrapper = new WsRestDeleteMemberRequestWrapper();
    WsRestDeleteMemberRequest wsRestDeleteMemberRequest = new WsRestDeleteMemberRequest();
    wsRestDeleteMemberRequestWrapper.setWsRestDeleteMemberRequest(wsRestDeleteMemberRequest);
 
    WsGroupLookup wsGroupLookup = new WsGroupLookup();
     
    wsGroupLookup.setGroupName("test:testGroup");
    
         
    wsRestDeleteMemberRequest.setWsGroupLookup(wsGroupLookup);
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("0b5949edd3bf4b65a0ab7e9ce97a4cf9");
    wsSubjectLookup.setSubjectSourceId("grouperExternal");
    List<WsSubjectLookup> wsSubjectLookups = new ArrayList<WsSubjectLookup>();
    wsSubjectLookups.add(wsSubjectLookup);
    wsRestDeleteMemberRequest.setSubjectLookups(wsSubjectLookups);
    GrouperApi grouperApi = new GrouperApi();
    ApiClient apiClient = new MyApiClient().setDebugging(true);
    grouperApi.setApiClient(apiClient);
    WsDeleteMemberResultsWrapper DeleteMemberResult = grouperApi.deleteMember(wsRestDeleteMemberRequestWrapper);
     
    if (!"SUCCESS".equals(DeleteMemberResult.getWsDeleteMemberResults().getResultMetadata().getResultCode())) {
      throw new RuntimeException("Invalid result code: " + DeleteMemberResult.getWsDeleteMemberResults().getResultMetadata().getResultCode());
    }
    for (WsDeleteMemberResult wsDeleteMemberResult : DeleteMemberResult.getWsDeleteMemberResults().getResults()) {
      WsSubject wsSubject = wsDeleteMemberResult.getWsSubject();
      System.out.println(wsSubject.getSourceId() + ": " + wsSubject.getId()); 
    }
  }
}
```

## Notes

Should probably generate code in a specific package instead of the default

[https://stackoverflow.com/questions/49035999/change-the-package-of-generated-supporting-files-in-swagger-codegen](https://stackoverflow.com/questions/49035999/change-the-package-of-generated-supporting-files-in-swagger-codegen)
