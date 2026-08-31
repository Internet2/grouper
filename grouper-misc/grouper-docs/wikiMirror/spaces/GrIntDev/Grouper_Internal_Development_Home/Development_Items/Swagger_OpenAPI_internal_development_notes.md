---
title: "Swagger OpenAPI internal development notes"
space: GrIntDev
pageId: 48792538
version: 4
lastUpdated: 2026-07-12T07:01:08.883Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792538/Swagger+OpenAPI+internal+development+notes
---

## Grouper developer notes: build a new swagger file

Change the WS pom.xml

```
        <skipSwaggerGeneration>false</skipSwaggerGeneration>

        <dependency>
            <groupId>com.github.kongchen</groupId>
            <artifactId>swagger-maven-plugin</artifactId>
            <version>3.1.8</version>
        </dependency>
```

Maven compile with swagger:generate the WS which will create a new webapp/docs/index.json file

```
mvn -f grouper-ws/grouper-ws clean compile swagger:generate dependency:copy-dependencies

[INFO] --- swagger-maven-plugin:3.1.8:generate (default) @ grouper-ws ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

## Grouper developer notes: setup the dependencies and pom

Import annotations into WS

```
        <dependency>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-annotations</artifactId>
            <version>1.6.3</version>
        </dependency>

```

## Edit Swagger in the source code annotations

Generate the swagger file on build

```
            <plugin>
                <groupId>com.github.kongchen</groupId>
                <artifactId>swagger-maven-plugin</artifactId>
                <version>3.1.8</version>
                <configuration>
                    <apiSources>
                      <apiSource>
                            <springmvc>false</springmvc>
                            <locations>edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest</locations>
                            <schemes>https</schemes>
                            <host>grouper.institution.edu</host>
                            <basePath></basePath>
                            <info>
                                <title>Grouper Web Services</title>
                                <version>v2.6.1</version>
                                <description>Grouper Web Service operations.  Substitute vX_Y_AAA with the client version, e.g. v2_6_001.  Note: the base URL and authentication are institution specific.</description>
                            </info>
                            <swaggerDirectory>${basedir}/webapp/docs/</swaggerDirectory>
                            <swaggerFileName>index</swaggerFileName>
                            <attachSwaggerArtifact>true</attachSwaggerArtifact>
                            <outputFormats>json</outputFormats>
                <swaggerDirectory>${basedir}/webapp/docs</swaggerDirectory>
                <attachSwaggerArtifact>true</attachSwaggerArtifact>
                        </apiSource>
                    </apiSources>
                </configuration>
                <executions>
                    <execution>
                        <phase>compile</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
          <!--  plugin>
            <groupId>com.greensopinion.swagger</groupId>
            <artifactId>jaxrs-gen</artifactId>
            <version>1.3.5</version>
            <configuration>
              <apiVersion>1.0</apiVersion>
              <apiBasePath>/api/latest</apiBasePath>
              <packageNames>
                <param>edu.internet2.middleware.grouper.ws</param>
                <param>edu.internet2.middleware.grouper.ws.coresoap</param>
              </packageNames>
              <outputFolder>${basedir}/webapp/docs</outputFolder>
            </configuration>
            <executions>
              <execution>
                <phase>compile</phase>
                <goals>
                  <goal>generate</goal>
                </goals>
              </execution>
            </executions>
          </plugin -->

```

Copy the Swagger UI to the WS

swagger-ui-3.52.3\dist → webapp/docs. Edit the index.html to take out "try now" since authn etc is an issue

```
    const DisableTryItOutPlugin = function() {
      return {
        statePlugins: {
          spec: {
            wrapSelectors: {
              allowTryItOutFor: () => () => false
            }
          }
        }
      }
    }

...

        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl, DisableTryItOutPlugin
        ],

```

## Grouper developer notes: add OpenAPI documentation to a WS operation

Note the versions put the operations in alphabetical order.

The versions should be numeric and valid versions

The context (first part after domain) needs to be correct in swagger json for it to work

The wrapper property name needs to match exactly the JSON top level attribute

1. Make a wrapper for the input so the initial field (in caps) will be included
  
  
  ```
  @ApiModel(description = "Request body to find groups")
  public class WsRestFindGroupsRequestWrapper {
  
    private WsRestFindGroupsRequest wsRestFindGroupsRequest;
    
    @ApiModelProperty(name = "WsRestFindGroupsRequest", value = "Identifies the request as a findGroups request")
    public WsRestFindGroupsRequest getWsRestFindGroupsRequest() {
      return wsRestFindGroupsRequest;
    }
    public void setWsRestFindGroupsRequest(WsRestFindGroupsRequest wsRestFindGroupsRequest1) {
      wsRestFindGroupsRequest = wsRestFindGroupsRequest1;
    }
  }
  ```
2. Make a wrapper for the normal response so the initial field (in caps) will be included
  
  
  ```
  @ApiModel(description = "Response body when a findGroups is successful")
  public class WsFindGroupsResultsWrapper {
    private WsFindGroupsResults wsFindGroupsResults;
  
    @ApiModelProperty(name = "WsFindGroupsResults", value = "Identifies the response for findGroups")
    public WsFindGroupsResults getWsFindGroupsResults() {
      return wsFindGroupsResults;
    }
    public void setWsFindGroupsResults(WsFindGroupsResults wsFindGroupsResults) {
      this.wsFindGroupsResults = wsFindGroupsResults;
    }
  }
  ```
3. Make a wrapper for the error response so the results wont be there
  
  
  ```
  @ApiModel(description = "Response body when not successful contains error message and metadata")
  public class WsFindGroupsResultsWrapperError {
    private WsResultsError wsFindGroupsResults;
  
    @ApiModelProperty(name = "WsFindGroupsResults", value = "Identifies the response for findGroups")
    public WsResultsError getWsFindGroupsResults() {
      return wsFindGroupsResults;
    }
    public void setWsFindGroupsResults(WsResultsError wsFindGroupsResults1) {
      this.wsFindGroupsResults = wsFindGroupsResults1;
    }
  }
  ```
4. Convert "notes" samples to HTML, e.g. [https://www.textfixer.com/html/convert-text-html.php](https://www.textfixer.com/html/convert-text-html.php)
5. Edit GrouperServicesRest for the operation.
  
  
  ```
    @POST
    @Path("/grouper-ws/servicesRest/vX_Y_0ZA/groups")   @ApiOperation(httpMethod = "POST", value = "Find groups", nickname = "findGroups", response = WsFindGroupsResultsWrapper.class,
        notes = "<b>Sample 1</b>: Find by substring in a folder<br /><pre>POST /grouper-ws/servicesRest/v2_6_001/groups<br />"
            + "{<br>  "WsRestFindGroupsRequest":{<br>    "wsQueryFilter":{<br>      "queryFilterType":"FIND_BY_GROUP_NAME_APPROXIMATE",<br>      "stemName":"aStem",<br>      "groupName":"aGr"<br>    }<br>  }<br>}</pre>") 
    @ApiResponses({@ApiResponse(code = 200, message = "SUCCESS", response = WsFindGroupsResultsWrapper.class),
                  @ApiResponse(code = 400, message = "INVALID_QUERY", response = WsFindGroupsResultsWrapperError.class),
                  @ApiResponse(code = 404, message = "STEM_NOT_FOUND", response = WsFindGroupsResultsWrapperError.class),
                  @ApiResponse(code = 500, message = "EXCEPTION", response = WsFindGroupsResultsWrapperError.class)})
    @ApiImplicitParams({
      @ApiImplicitParam(required = true, dataType = "edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequestWrapper", paramType = "body")})
    public static WsFindGroupsResults findGroups(GrouperVersion clientVersion,
  
  
  ```
6. Edit the request and response beans at the class level (make sure to be consistent)
  
  
  ```
  @ApiModel(description = "Request body to find groups")
  public class WsRestFindGroupsRequestWrapper {
  ```
7. Edit the request and response beans at the getter level (make sure to be consistent)
  
  
  ```
    @ApiModelProperty(value = "Integer ID for object", example = "12345")
    public String getIdIndex() {
  ```
8. Add "Lite" operations to GrouperService.java
  
  
  ```
    @POST
    @Path("/grouper-ws/servicesRest/vX_Y_FGL/groups")
    @ApiOperation(httpMethod = "POST", value = "Find groups lite", nickname = "findGroupsLite", response = WsFindGroupsResultsWrapper.class,
    notes = "<b>Sample 1</b>: Find by substring in a folder<br /><pre>POST /grouper-ws/servicesRest/v2_6_001/groups<br /><b>wsLiteObjectType</b>=WsRestFindGroupsLiteRequest&<b>"
        + "groupName</b>=aGr&<b>queryFilterType</b>=FIND_BY_GROUP_NAME_APPROXIMATE&<b>stemName</b>=aStem</pre>") 
    @ApiResponses({@ApiResponse(code = 200, message = "SUCCESS", response = WsFindGroupsResultsWrapper.class),
                  @ApiResponse(code = 400, message = "INVALID_QUERY", response = WsFindGroupsResultsWrapperError.class),
                  @ApiResponse(code = 404, message = "STEM_NOT_FOUND", response = WsFindGroupsResultsWrapperError.class),
                  @ApiResponse(code = 500, message = "EXCEPTION", response = WsFindGroupsResultsWrapperError.class)})
    @ApiImplicitParams({
      @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
          value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
      @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
          value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
      @ApiImplicitParam(required = false, name = "queryFilterType", dataType = "String", paramType = "form", 
      value = "findGroupType is the WsQueryFilterType enum for which type of find is happening: "
        + "e.g. FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, FIND_BY_APPROXIMATE_ATTRIBUTE, "
        + "FIND_BY_ATTRIBUTE,  FIND_BY_GROUP_NAME_APPROXIMATE, FIND_BY_TYPE, AND, OR, MINUS", 
        example = "FIND_BY_GROUP_UUID | FIND_BY_GROUP_NAME_EXACT | FIND_BY_STEM_NAME | FIND_BY_APPROXIMATE_ATTRIBUTE |"
        + " FIND_BY_ATTRIBUTE | FIND_BY_GROUP_NAME_APPROXIMATE | FIND_BY_TYPE | AND | OR | MINUS"),
      @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
          value = "groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
      @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
          value = "Will return groups only in this stem (by name)", example = "some:parent:folder:name"),
      @ApiImplicitParam(required = false, name = "stemNameScope", dataType = "String", paramType = "form", 
          value = "if searching by stem, ONE_LEVEL is for one level, ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE", 
          example = "ONE_LEVEL | ALL_IN_SUBTREE"),
      @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
      value = "groupUuid search by group uuid (must match exactly)", example = "abc123"),
      @ApiImplicitParam(required = false, name = "groupAttributeName", dataType = "String", paramType = "form", 
      value = "This is the attribute name, or null for search all attributes.  This could be a legacy attribute or an attributeDefName of a string valued attribute", example = "some:attribute:name"),
      @ApiImplicitParam(required = false, name = "groupAttributeValue", dataType = "String", paramType = "form", 
      value = "The attribute value to filter on if querying by attribute and value", example = "someValue"),
      @ApiImplicitParam(required = false, name = "groupTypeName", dataType = "String", paramType = "form", 
      value = "not implemented", example = "NA"),
      @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
      value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
          + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
      @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
      value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
          + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
      @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
      value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
          + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
      @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
      value = "If the group detail should be returned, default to false", example = "T|F"),
      @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
      value = "Page size if paging", example = "100"),
      @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
      value = "Page number 1 indexed if paging", example = "1"),
      @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
          value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
          example = "name | displayName | extension | displayExtension"),
      @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
      value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
      @ApiImplicitParam(required = false, name = "typeOfGroups", dataType = "String", paramType = "form", 
      value = "Comma separated type of groups can be an enum of TypeOfGroup, e.g. group, role, entity", example = "group|role|entity"),
      @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
      value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
      @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
      value = "Field that will be sent back for cursor based paging", example = "abc123"),
      @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
      value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
      @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
      value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
      @ApiImplicitParam(required = false, name = "enabled", dataType = "String", paramType = "form", 
      value = "enabled is A for all, T or null for enabled only, F for disabled", example = "A|T|F")    
    })
    public WsFindGroupsResults findGroupsLite(
        final String clientVersion,
        String queryFilterType, String groupName, String stemName, String stemNameScope,
        String groupUuid, String groupAttributeName, String groupAttributeValue,
        String groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
        String actAsSubjectIdentifier, String includeGroupDetail, String paramName0,
        String paramValue0, String paramName1, String paramValue1, String pageSize, 
        String pageNumber, String sortString, String ascending, String typeOfGroups,
        String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
        String pageCursorFieldIncludesLastRetrieved,
        String enabled) {
  
  
  ```
