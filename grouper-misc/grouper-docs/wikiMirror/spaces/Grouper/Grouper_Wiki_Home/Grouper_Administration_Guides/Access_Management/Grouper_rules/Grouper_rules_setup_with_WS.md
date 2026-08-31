---
title: "Grouper rules setup with WS"
space: Grouper
pageId: 28548044
version: 9
lastUpdated: 2026-07-01T05:45:29.208Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548044/Grouper+rules+setup+with+WS
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

 Here is an example of adding and testing a rule with WS rest (generated from grouper client). This is the [group intersection rule](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554980/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group) where you have to be in one group (e.g. employees) to be in another group (e.g. app users). If you fall out of employee, you will fall out of users. A nightly daemon cleans up inconsistencies.

 Note, the names of the attribute depends on where your grouper admin put them in your folder structure.

 Create group A request

 
```

<WsRestGroupSaveRequest>
	<wsGroupToSaves>
		<WsGroupToSave>
			<wsGroupLookup>
				<groupName>stem:a</groupName>
			</wsGroupLookup>
			<wsGroup>
				<displayExtension>a</displayExtension>
				<name>stem:a</name>
			</wsGroup>
			<createParentStemsIfNotExist>T</createParentStemsIfNotExist>
		</WsGroupToSave>
	</wsGroupToSaves>
</WsRestGroupSaveRequest>

```

 Create group A response

 
```

<WsGroupSaveResults>
	<results>
		<WsGroupSaveResult>
			<wsGroup>
				<extension>a</extension>
				<displayExtension>a</displayExtension>
				<displayName>stem:a</displayName>
				<name>stem:a</name>
				<uuid>7aa346e9d5ef42de842f490f49d08115</uuid>
			</wsGroup>
			<resultMetadata>
				<resultCode>SUCCESS_INSERTED</resultCode>
				<success>T</success>
			</resultMetadata>
		</WsGroupSaveResult>
	</results>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>Success for: clientVersion: 1.6.0, wsGroupToSaves:
			Array size: 1: [0]: WsGroupToSave[
			wsGroupLookup=WsGroupLookup[groupName=stem:a],
			wsGroup=WsGroup[displayExtension=a,name=stem:a],createParentStemsIfNotExist=T]

			, actAsSubject: null, txType: NONE, paramNames:
			, params: null</resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>8752</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
</WsGroupSaveResults>

```

 Create group B request

 
```

<WsRestGroupSaveRequest>
	<wsGroupToSaves>
		<WsGroupToSave>
			<wsGroupLookup>
				<groupName>stem:b</groupName>
			</wsGroupLookup>
			<wsGroup>
				<displayExtension>b</displayExtension>
				<name>stem:b</name>
			</wsGroup>
			<createParentStemsIfNotExist>T</createParentStemsIfNotExist>
		</WsGroupToSave>
	</wsGroupToSaves>
</WsRestGroupSaveRequest>

```

 Create group B response

 
```

 <WsGroupSaveResults>
	<results>
		<WsGroupSaveResult>
			<wsGroup>
				<extension>b</extension>
				<displayExtension>b</displayExtension>
				<displayName>stem:b</displayName>
				<name>stem:b</name>
				<uuid>826d8f94fcff4865b4a0a51c74dfbaff</uuid>
			</wsGroup>
			<resultMetadata>
				<resultCode>SUCCESS_INSERTED</resultCode>
				<success>T</success>
			</resultMetadata>
		</WsGroupSaveResult>
	</results>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>Success for: clientVersion: 1.6.0, wsGroupToSaves:
			Array size: 1: [0]: WsGroupToSave[
			wsGroupLookup=WsGroupLookup[groupName=stem:b],
			wsGroup=WsGroup[displayExtension=b,name=stem:b],createParentStemsIfNotExist=T]

			, actAsSubject: null, txType: NONE, paramNames:
			, params: null</resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>370</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
</WsGroupSaveResults>

```

 Rule assign type request

 
```

<WsRestAssignAttributesRequest>
	<attributeAssignOperation>add_attr</attributeAssignOperation>
	<attributeAssignType>group</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:rule</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
	<wsOwnerGroupLookups>
		<WsGroupLookup>
			<groupName>stem:a</groupName>
		</WsGroupLookup>
	</wsOwnerGroupLookups>
</WsRestAssignAttributesRequest>

```

 Rule assign type response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesTypeDef</extension>
			<name>etc:attribute:rules:rulesTypeDef</name>
			<uuid>955c3c7f331942d7852796b9a24951aa</uuid>
			<attributeDefType>type</attributeDefType>
			<multiAssignable>T</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>marker</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>rule</extension>
			<displayExtension>rule</displayExtension>
			<description>is a rule</description>
			<displayName>etc:attribute:rules:rule</displayName>
			<name>etc:attribute:rules:rule</name>
			<uuid>edbfac27573049a8835144c484c31309</uuid>
			<attributeDefId>955c3c7f331942d7852796b9a24951aa</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesTypeDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>d9da714141024e658e2784299abf337f
					</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group</attributeAssignType>
					<attributeDefNameId>edbfac27573049a8835144c484c31309
					</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:rule
					</attributeDefNameName>
					<attributeDefId>955c3c7f331942d7852796b9a24951aa</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesTypeDef
					</attributeDefName>
					<createdOn>2010/09/27 02:14:46.664</createdOn>
					<enabled>T</enabled>
					<id>ac0da4c4802b43589fbcc0a888ba0d33</id>
					<lastUpdated>2010/09/27 02:14:46.664</lastUpdated>
					<ownerGroupId>7aa346e9d5ef42de842f490f49d08115</ownerGroupId>
					<ownerGroupName>stem:a</ownerGroupName>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>F</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>313</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups>
		<WsGroup>
			<extension>a</extension>
			<displayExtension>a</displayExtension>
			<displayName>stem:a</displayName>
			<name>stem:a</name>
			<uuid>7aa346e9d5ef42de842f490f49d08115</uuid>
		</WsGroup>
	</wsGroups>
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Rule act as source request

 
```

 <WsRestAssignAttributesRequest>
	<attributeAssignOperation>assign_attr</attributeAssignOperation>
	<attributeAssignValueOperation>assign_value
	</attributeAssignValueOperation>
	<wsOwnerAttributeAssignLookups>
		<WsAttributeAssignLookup>
			<uuid>ac0da4c4802b43589fbcc0a888ba0d33</uuid>
		</WsAttributeAssignLookup>
	</wsOwnerAttributeAssignLookups>
	<values>
		<WsAttributeAssignValue>
			<valueSystem>g:isa</valueSystem>
		</WsAttributeAssignValue>
	</values>
	<attributeAssignType>group_asgn</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:ruleActAsSubjectSourceId</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

```

 Rule act as source response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>ruleActAsSubjectSourceId</extension>
			<displayExtension>ruleActAsSubjectSourceId</displayExtension>
			<description>subject source id to act as</description>
			<displayName>etc:attribute:rules:ruleActAsSubjectSourceId
			</displayName>
			<name>etc:attribute:rules:ruleActAsSubjectSourceId</name>
			<uuid>0cfadaf18db743779e6b8b5143a7f4e6</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssignValueResults>
				<WsAttributeAssignValueResult>
					<changed>T</changed>
					<deleted>F</deleted>
					<wsAttributeAssignValue>
						<id>0e572f7ce7e04893afc5b649185c7027</id>
						<valueSystem>g:isa</valueSystem>
					</wsAttributeAssignValue>
				</WsAttributeAssignValueResult>
			</wsAttributeAssignValueResults>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
					</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group_asgn</attributeAssignType>
					<attributeDefNameId>0cfadaf18db743779e6b8b5143a7f4e6
					</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:ruleActAsSubjectSourceId
					</attributeDefNameName>
					<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesAttrDef
					</attributeDefName>
					<wsAttributeAssignValues>
						<WsAttributeAssignValue>
							<id>0e572f7ce7e04893afc5b649185c7027</id>
							<valueSystem>g:isa</valueSystem>
						</WsAttributeAssignValue>
					</wsAttributeAssignValues>
					<createdOn>2010/09/27 02:27:29.349</createdOn>
					<enabled>T</enabled>
					<id>cbd0b7bcdc7e4fb8be6fe51286285164</id>
					<lastUpdated>2010/09/27 02:27:29.349</lastUpdated>
					<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
					</ownerAttributeAssignId>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>T</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>914</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups />
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Rule act as subject id request

 
```

 <WsRestAssignAttributesRequest>
	<attributeAssignOperation>assign_attr</attributeAssignOperation>
	<attributeAssignValueOperation>assign_value
	</attributeAssignValueOperation>
	<wsOwnerAttributeAssignLookups>
		<WsAttributeAssignLookup>
			<uuid>ac0da4c4802b43589fbcc0a888ba0d33</uuid>
		</WsAttributeAssignLookup>
	</wsOwnerAttributeAssignLookups>
	<values>
		<WsAttributeAssignValue>
			<valueSystem>GrouperSystem</valueSystem>
		</WsAttributeAssignValue>
	</values>
	<attributeAssignType>group_asgn</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:ruleActAsSubjectId</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

```

 Rule act as subject id response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>ruleActAsSubjectId</extension>
			<displayExtension>ruleActAsSubjectId</displayExtension>
			<description>subject id to act as, mutually exclusive with identifier
			</description>
			<displayName>etc:attribute:rules:ruleActAsSubjectId</displayName>
			<name>etc:attribute:rules:ruleActAsSubjectId</name>
			<uuid>02647c65f2e04546bf53722d02cd1f0c</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssignValueResults>
				<WsAttributeAssignValueResult>
					<changed>T</changed>
					<deleted>F</deleted>
					<wsAttributeAssignValue>
						<id>84c3ccd5264748c4b61878901f98d069</id>
						<valueSystem>GrouperSystem</valueSystem>
					</wsAttributeAssignValue>
				</WsAttributeAssignValueResult>
			</wsAttributeAssignValueResults>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
					</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group_asgn</attributeAssignType>
					<attributeDefNameId>02647c65f2e04546bf53722d02cd1f0c
					</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:ruleActAsSubjectId
					</attributeDefNameName>
					<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesAttrDef
					</attributeDefName>
					<wsAttributeAssignValues>
						<WsAttributeAssignValue>
							<id>84c3ccd5264748c4b61878901f98d069</id>
							<valueSystem>GrouperSystem</valueSystem>
						</WsAttributeAssignValue>
					</wsAttributeAssignValues>
					<createdOn>2010/09/27 02:31:16.337</createdOn>
					<enabled>T</enabled>
					<id>07566868e528408e977db37fb9e3bd0b</id>
					<lastUpdated>2010/09/27 02:31:16.337</lastUpdated>
					<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
					</ownerAttributeAssignId>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>T</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>546</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups />
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Rule check owner name request

 
```

 <WsRestAssignAttributesRequest>
	<attributeAssignOperation>assign_attr</attributeAssignOperation>
	<attributeAssignValueOperation>assign_value
	</attributeAssignValueOperation>
	<wsOwnerAttributeAssignLookups>
		<WsAttributeAssignLookup>
			<uuid>ac0da4c4802b43589fbcc0a888ba0d33</uuid>
		</WsAttributeAssignLookup>
	</wsOwnerAttributeAssignLookups>
	<values>
		<WsAttributeAssignValue>
			<valueSystem>stem:b</valueSystem>
		</WsAttributeAssignValue>
	</values>
	<attributeAssignType>group_asgn</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:ruleCheckOwnerName</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

```

 Rule check owner name response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>ruleCheckOwnerName</extension>
			<displayExtension>ruleCheckOwnerName</displayExtension>
			<description>when the check should be to see if rule should fire,
				this is owner of type, mutually exclusice with id</description>
			<displayName>etc:attribute:rules:ruleCheckOwnerName</displayName>
			<name>etc:attribute:rules:ruleCheckOwnerName</name>
			<uuid>2e690ae1b3054d32b41f24adb6d35036</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssignValueResults>
				<WsAttributeAssignValueResult>
					<changed>T</changed>
					<deleted>F</deleted>
					<wsAttributeAssignValue>
						<id>c5079562c62f4730811bc257c686ce74</id>
						<valueSystem>stem:b</valueSystem>
					</wsAttributeAssignValue>
				</WsAttributeAssignValueResult>
			</wsAttributeAssignValueResults>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
					</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group_asgn</attributeAssignType>
					<attributeDefNameId>2e690ae1b3054d32b41f24adb6d35036
					</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:ruleCheckOwnerName
					</attributeDefNameName>
					<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesAttrDef
					</attributeDefName>
					<wsAttributeAssignValues>
						<WsAttributeAssignValue>
							<id>c5079562c62f4730811bc257c686ce74</id>
							<valueSystem>stem:b</valueSystem>
						</WsAttributeAssignValue>
					</wsAttributeAssignValues>
					<createdOn>2010/09/27 02:34:11.909</createdOn>
					<enabled>T</enabled>
					<id>1333885cbbf7453aa312d8472fd93268</id>
					<lastUpdated>2010/09/27 02:34:11.909</lastUpdated>
					<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
					</ownerAttributeAssignId>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>T</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>480</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups />
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Rule check type request

 
```

<WsRestAssignAttributesRequest>
	<attributeAssignOperation>assign_attr</attributeAssignOperation>
	<attributeAssignValueOperation>assign_value
	</attributeAssignValueOperation>
	<wsOwnerAttributeAssignLookups>
		<WsAttributeAssignLookup>
			<uuid>ac0da4c4802b43589fbcc0a888ba0d33</uuid>
		</WsAttributeAssignLookup>
	</wsOwnerAttributeAssignLookups>
	<values>
		<WsAttributeAssignValue>
			<valueSystem>membershipRemove</valueSystem>
		</WsAttributeAssignValue>
	</values>
	<attributeAssignType>group_asgn</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:ruleCheckType</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

```

 Rule check type response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>ruleCheckType</extension>
			<displayExtension>ruleCheckType</displayExtension>
			<description>when the check should be to see if rule should fire,
				enum: RuleCheckType</description>
			<displayName>etc:attribute:rules:ruleCheckType</displayName>
			<name>etc:attribute:rules:ruleCheckType</name>
			<uuid>48a7e42ce4d14075b5aae2d25ff86445</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssignValueResults>
				<WsAttributeAssignValueResult>
					<changed>T</changed>
					<deleted>F</deleted>
					<wsAttributeAssignValue>
						<id>1b29367e30e44a5380c1bea2670702fb</id>
						<valueSystem>membershipRemove</valueSystem>
					</wsAttributeAssignValue>
				</WsAttributeAssignValueResult>
			</wsAttributeAssignValueResults>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>385880a5f2224f8989f2d5295834af52</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group_asgn</attributeAssignType>
					<attributeDefNameId>48a7e42ce4d14075b5aae2d25ff86445</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:ruleCheckType</attributeDefNameName>
					<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
					<wsAttributeAssignValues>
						<WsAttributeAssignValue>
							<id>1b29367e30e44a5380c1bea2670702fb</id>
							<valueSystem>membershipRemove</valueSystem>
						</WsAttributeAssignValue>
					</wsAttributeAssignValues>
					<createdOn>2010/09/27 02:37:07.676</createdOn>
					<enabled>T</enabled>
					<id>0ecf19c9b08c4554a72224eeadb60279</id>
					<lastUpdated>2010/09/27 02:37:07.676</lastUpdated>
					<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33</ownerAttributeAssignId>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>T</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>466</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups />
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Rule if condition enum request

 
```

<WsRestAssignAttributesRequest>
	<attributeAssignOperation>assign_attr</attributeAssignOperation>
	<attributeAssignValueOperation>assign_value</attributeAssignValueOperation>
	<wsOwnerAttributeAssignLookups>
		<WsAttributeAssignLookup>
			<uuid>ac0da4c4802b43589fbcc0a888ba0d33</uuid>
		</WsAttributeAssignLookup>
	</wsOwnerAttributeAssignLookups>
	<values>
		<WsAttributeAssignValue>
			<valueSystem>thisGroupHasImmediateEnabledMembership</valueSystem>
		</WsAttributeAssignValue>
	</values>
	<attributeAssignType>group_asgn</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:ruleIfConditionEnum</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

```

 Rule if condition enum response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>ruleIfConditionEnum</extension>
			<displayExtension>ruleIfConditionEnum</displayExtension>
			<description>RuleConditionEnum that sees if rule should fire, or
				blank if should run always</description>
			<displayName>etc:attribute:rules:ruleIfConditionEnum</displayName>
			<name>etc:attribute:rules:ruleIfConditionEnum</name>
			<uuid>b86358d4f54b4b1da0395deadbf5623b</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssignValueResults>
				<WsAttributeAssignValueResult>
					<changed>T</changed>
					<deleted>F</deleted>
					<wsAttributeAssignValue>
						<id>0a5621c587484f7d992a3dad27d77831</id>
						<valueSystem>thisGroupHasImmediateEnabledMembership</valueSystem>
					</wsAttributeAssignValue>
				</WsAttributeAssignValueResult>
			</wsAttributeAssignValueResults>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>385880a5f2224f8989f2d5295834af52</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group_asgn</attributeAssignType>
					<attributeDefNameId>b86358d4f54b4b1da0395deadbf5623b</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:ruleIfConditionEnum</attributeDefNameName>
					<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
					<wsAttributeAssignValues>
						<WsAttributeAssignValue>
							<id>0a5621c587484f7d992a3dad27d77831</id>
							<valueSystem>thisGroupHasImmediateEnabledMembership</valueSystem>
						</WsAttributeAssignValue>
					</wsAttributeAssignValues>
					<createdOn>2010/09/27 02:38:47.685</createdOn>
					<enabled>T</enabled>
					<id>816c21a0ada040798f4ad53799effb23</id>
					<lastUpdated>2010/09/27 02:38:47.685</lastUpdated>
					<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33</ownerAttributeAssignId>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>T</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>369</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups />
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Rule then enum request

 
```

<WsRestAssignAttributesRequest>
	<attributeAssignOperation>assign_attr</attributeAssignOperation>
	<attributeAssignValueOperation>assign_value
	</attributeAssignValueOperation>
	<wsOwnerAttributeAssignLookups>
		<WsAttributeAssignLookup>
			<uuid>ac0da4c4802b43589fbcc0a888ba0d33</uuid>
		</WsAttributeAssignLookup>
	</wsOwnerAttributeAssignLookups>
	<values>
		<WsAttributeAssignValue>
			<valueSystem>removeMemberFromOwnerGroup</valueSystem>
		</WsAttributeAssignValue>
	</values>
	<attributeAssignType>group_asgn</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:ruleThenEnum</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
</WsRestAssignAttributesRequest>

```

 Rule then enum response

 
```

<WsAssignAttributesResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>ruleThenEnum</extension>
			<displayExtension>ruleThenEnum</displayExtension>
			<description>RuleThenEnum to run when the rule fires</description>
			<displayName>etc:attribute:rules:ruleThenEnum</displayName>
			<name>etc:attribute:rules:ruleThenEnum</name>
			<uuid>fd84ef842cf04039a55b169ba3de9c68</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssignResults>
		<WsAssignAttributeResult>
			<wsAttributeAssignValueResults>
				<WsAttributeAssignValueResult>
					<changed>T</changed>
					<deleted>F</deleted>
					<wsAttributeAssignValue>
						<id>67b2097fd446499ba8348fc15b595ee5</id>
						<valueSystem>removeMemberFromOwnerGroup</valueSystem>
					</wsAttributeAssignValue>
				</WsAttributeAssignValueResult>
			</wsAttributeAssignValueResults>
			<wsAttributeAssigns>
				<WsAttributeAssign>
					<attributeAssignActionType>immediate</attributeAssignActionType>
					<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
					<attributeAssignActionId>385880a5f2224f8989f2d5295834af52</attributeAssignActionId>
					<attributeAssignActionName>assign</attributeAssignActionName>
					<attributeAssignType>group_asgn</attributeAssignType>
					<attributeDefNameId>fd84ef842cf04039a55b169ba3de9c68</attributeDefNameId>
					<attributeDefNameName>etc:attribute:rules:ruleThenEnum</attributeDefNameName>
					<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
					<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
					<wsAttributeAssignValues>
						<WsAttributeAssignValue>
							<id>67b2097fd446499ba8348fc15b595ee5</id>
							<valueSystem>removeMemberFromOwnerGroup</valueSystem>
						</WsAttributeAssignValue>
					</wsAttributeAssignValues>
					<createdOn>2010/09/27 02:40:38.134</createdOn>
					<enabled>T</enabled>
					<id>63331ddaddd54f22b7fbe33338473f8c</id>
					<lastUpdated>2010/09/27 02:40:38.134</lastUpdated>
					<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33</ownerAttributeAssignId>
				</WsAttributeAssign>
			</wsAttributeAssigns>
			<changed>T</changed>
			<valuesChanged>T</valuesChanged>
			<deleted>F</deleted>
		</WsAssignAttributeResult>
	</wsAttributeAssignResults>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 1 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>402</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups />
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsAssignAttributesResults>

```

 Make sure valid request

 
```

 <WsRestGetAttributeAssignmentsRequest>
	<attributeAssignType>group</attributeAssignType>
	<wsAttributeDefNameLookups>
		<WsAttributeDefNameLookup>
			<name>etc:attribute:rules:rule</name>
		</WsAttributeDefNameLookup>
	</wsAttributeDefNameLookups>
	<wsOwnerGroupLookups>
		<WsGroupLookup>
			<groupName>stem:a</groupName>
		</WsGroupLookup>
	</wsOwnerGroupLookups>
	<includeAssignmentsOnAssignments>T</includeAssignmentsOnAssignments>
</WsRestGetAttributeAssignmentsRequest>

```

 Make sure valid response

 
```

<WsGetAttributeAssignmentsResults>
	<wsAttributeDefs>
		<WsAttributeDef>
			<extension>rulesAttrDef</extension>
			<name>etc:attribute:rules:rulesAttrDef</name>
			<uuid>fce499cda6254a80a5a5dc5904e721d5</uuid>
			<attributeDefType>attr</attributeDefType>
			<multiAssignable>F</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>string</valueType>
		</WsAttributeDef>
		<WsAttributeDef>
			<extension>rulesTypeDef</extension>
			<name>etc:attribute:rules:rulesTypeDef</name>
			<uuid>955c3c7f331942d7852796b9a24951aa</uuid>
			<attributeDefType>type</attributeDefType>
			<multiAssignable>T</multiAssignable>
			<multiValued>F</multiValued>
			<valueType>marker</valueType>
		</WsAttributeDef>
	</wsAttributeDefs>
	<wsAttributeDefNames>
		<WsAttributeDefName>
			<extension>rule</extension>
			<displayExtension>rule</displayExtension>
			<description>is a rule</description>
			<displayName>etc:attribute:rules:rule</displayName>
			<name>etc:attribute:rules:rule</name>
			<uuid>edbfac27573049a8835144c484c31309</uuid>
			<attributeDefId>955c3c7f331942d7852796b9a24951aa</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesTypeDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleActAsSubjectId</extension>
			<displayExtension>ruleActAsSubjectId</displayExtension>
			<description>subject id to act as, mutually exclusive with identifier
			</description>
			<displayName>etc:attribute:rules:ruleActAsSubjectId</displayName>
			<name>etc:attribute:rules:ruleActAsSubjectId</name>
			<uuid>02647c65f2e04546bf53722d02cd1f0c</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleActAsSubjectSourceId</extension>
			<displayExtension>ruleActAsSubjectSourceId</displayExtension>
			<description>subject source id to act as</description>
			<displayName>etc:attribute:rules:ruleActAsSubjectSourceId
			</displayName>
			<name>etc:attribute:rules:ruleActAsSubjectSourceId</name>
			<uuid>0cfadaf18db743779e6b8b5143a7f4e6</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleCheckOwnerName</extension>
			<displayExtension>ruleCheckOwnerName</displayExtension>
			<description>when the check should be to see if rule should fire,
				this is owner of type, mutually exclusice with id</description>
			<displayName>etc:attribute:rules:ruleCheckOwnerName</displayName>
			<name>etc:attribute:rules:ruleCheckOwnerName</name>
			<uuid>2e690ae1b3054d32b41f24adb6d35036</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleCheckType</extension>
			<displayExtension>ruleCheckType</displayExtension>
			<description>when the check should be to see if rule should fire,
				enum: RuleCheckType</description>
			<displayName>etc:attribute:rules:ruleCheckType</displayName>
			<name>etc:attribute:rules:ruleCheckType</name>
			<uuid>48a7e42ce4d14075b5aae2d25ff86445</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleIfConditionEnum</extension>
			<displayExtension>ruleIfConditionEnum</displayExtension>
			<description>RuleConditionEnum that sees if rule should fire, or
				blank if should run always</description>
			<displayName>etc:attribute:rules:ruleIfConditionEnum</displayName>
			<name>etc:attribute:rules:ruleIfConditionEnum</name>
			<uuid>b86358d4f54b4b1da0395deadbf5623b</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleThenEnum</extension>
			<displayExtension>ruleThenEnum</displayExtension>
			<description>RuleThenEnum to run when the rule fires</description>
			<displayName>etc:attribute:rules:ruleThenEnum</displayName>
			<name>etc:attribute:rules:ruleThenEnum</name>
			<uuid>fd84ef842cf04039a55b169ba3de9c68</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
		<WsAttributeDefName>
			<extension>ruleValid</extension>
			<displayExtension>ruleValid</displayExtension>
			<description>T|F for if this rule is valid, or the reason, managed by
				hook automatically</description>
			<displayName>etc:attribute:rules:ruleValid</displayName>
			<name>etc:attribute:rules:ruleValid</name>
			<uuid>e7da4d50b57c4227b265796575fc919d</uuid>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
		</WsAttributeDefName>
	</wsAttributeDefNames>
	<wsAttributeAssigns>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>d9da714141024e658e2784299abf337f
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group</attributeAssignType>
			<attributeDefNameId>edbfac27573049a8835144c484c31309
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:rule</attributeDefNameName>
			<attributeDefId>955c3c7f331942d7852796b9a24951aa</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesTypeDef</attributeDefName>
			<createdOn>2010/09/27 02:14:46.664</createdOn>
			<enabled>T</enabled>
			<id>ac0da4c4802b43589fbcc0a888ba0d33</id>
			<lastUpdated>2010/09/27 02:14:46.664</lastUpdated>
			<ownerGroupId>7aa346e9d5ef42de842f490f49d08115</ownerGroupId>
			<ownerGroupName>stem:a</ownerGroupName>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>02647c65f2e04546bf53722d02cd1f0c
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleActAsSubjectId
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>84c3ccd5264748c4b61878901f98d069</id>
					<valueSystem>GrouperSystem</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:31:16.337</createdOn>
			<enabled>T</enabled>
			<id>07566868e528408e977db37fb9e3bd0b</id>
			<lastUpdated>2010/09/27 02:31:16.337</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>0cfadaf18db743779e6b8b5143a7f4e6
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleActAsSubjectSourceId
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>0e572f7ce7e04893afc5b649185c7027</id>
					<valueSystem>g:isa</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:27:29.349</createdOn>
			<enabled>T</enabled>
			<id>cbd0b7bcdc7e4fb8be6fe51286285164</id>
			<lastUpdated>2010/09/27 02:27:29.349</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>2e690ae1b3054d32b41f24adb6d35036
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleCheckOwnerName
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>c5079562c62f4730811bc257c686ce74</id>
					<valueSystem>stem:a</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:34:11.909</createdOn>
			<enabled>T</enabled>
			<id>1333885cbbf7453aa312d8472fd93268</id>
			<lastUpdated>2010/09/27 02:34:11.909</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>48a7e42ce4d14075b5aae2d25ff86445
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleCheckType
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>1b29367e30e44a5380c1bea2670702fb</id>
					<valueSystem>membershipRemove</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:37:07.676</createdOn>
			<enabled>T</enabled>
			<id>0ecf19c9b08c4554a72224eeadb60279</id>
			<lastUpdated>2010/09/27 02:37:07.676</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>b86358d4f54b4b1da0395deadbf5623b
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleIfConditionEnum
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>0a5621c587484f7d992a3dad27d77831</id>
					<valueSystem>thisGroupHasImmediateEnabledMembership</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:38:47.685</createdOn>
			<enabled>T</enabled>
			<id>816c21a0ada040798f4ad53799effb23</id>
			<lastUpdated>2010/09/27 02:38:47.685</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>fd84ef842cf04039a55b169ba3de9c68
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleThenEnum
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>67b2097fd446499ba8348fc15b595ee5</id>
					<valueSystem>removeMemberFromOwnerGroup</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:40:38.134</createdOn>
			<enabled>T</enabled>
			<id>63331ddaddd54f22b7fbe33338473f8c</id>
			<lastUpdated>2010/09/27 02:40:38.134</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
		<WsAttributeAssign>
			<attributeAssignActionType>immediate</attributeAssignActionType>
			<attributeAssignDelegatable>FALSE</attributeAssignDelegatable>
			<attributeAssignActionId>385880a5f2224f8989f2d5295834af52
			</attributeAssignActionId>
			<attributeAssignActionName>assign</attributeAssignActionName>
			<attributeAssignType>group_asgn</attributeAssignType>
			<attributeDefNameId>e7da4d50b57c4227b265796575fc919d
			</attributeDefNameId>
			<attributeDefNameName>etc:attribute:rules:ruleValid
			</attributeDefNameName>
			<attributeDefId>fce499cda6254a80a5a5dc5904e721d5</attributeDefId>
			<attributeDefName>etc:attribute:rules:rulesAttrDef</attributeDefName>
			<wsAttributeAssignValues>
				<WsAttributeAssignValue>
					<id>a945a14dd9c44c228836ccf2a5444b0d</id>
					<valueSystem>T</valueSystem>
				</WsAttributeAssignValue>
			</wsAttributeAssignValues>
			<createdOn>2010/09/27 02:27:29.755</createdOn>
			<enabled>T</enabled>
			<id>1137697e77b649789f80ec1c806cf0c3</id>
			<lastUpdated>2010/09/27 02:27:29.755</lastUpdated>
			<ownerAttributeAssignId>ac0da4c4802b43589fbcc0a888ba0d33
			</ownerAttributeAssignId>
		</WsAttributeAssign>
	</wsAttributeAssigns>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>, Found 8 results.  </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>349</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
	<wsGroups>
		<WsGroup>
			<extension>a</extension>
			<displayExtension>a</displayExtension>
			<displayName>stem:a</displayName>
			<name>stem:a</name>
			<uuid>7aa346e9d5ef42de842f490f49d08115</uuid>
		</WsGroup>
	</wsGroups>
	<wsStems />
	<wsMemberships />
	<wsSubjects />
</WsGetAttributeAssignmentsResults>

```

 Add to A request

 
```

<WsRestAddMemberRequest>
	<wsGroupLookup>
		<groupName>stem:a</groupName>
	</wsGroupLookup>
	<subjectLookups>
		<WsSubjectLookup>
			<subjectId>test.subject.0</subjectId>
		</WsSubjectLookup>
	</subjectLookups>
</WsRestAddMemberRequest>

```

 Add to A response

 
```

<WsAddMemberResults>
	<results>
		<WsAddMemberResult>
			<wsSubject>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
				<id>test.subject.0</id>
				<name>my name is test.subject.0</name>
				<sourceId>jdbc</sourceId>
			</wsSubject>
			<resultMetadata>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
			</resultMetadata>
		</WsAddMemberResult>
	</results>
	<wsGroupAssigned>
		<extension>a</extension>
		<displayExtension>a</displayExtension>
		<displayName>stem:a</displayName>
		<name>stem:a</name>
		<uuid>7aa346e9d5ef42de842f490f49d08115</uuid>
	</wsGroupAssigned>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>Success for: clientVersion: 1.6.0, wsGroupLookup:
			WsGroupLookup[groupName=stem:a], subjectLookups: Array size: 1: [0]:
			WsSubjectLookup[subjectId=test.subject.0]

			, replaceAllExisting: false, actAsSubject: null, fieldName: null,
			txType: NONE, includeGroupDetail: false, includeSubjectDetail: false,
			subjectAttributeNames: null
			, params: null
			, disabledDate: null, enabledDate: null</resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>209</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
</WsAddMemberResults>

```

 Add to B request

 
```

<WsRestAddMemberRequest>
	<wsGroupLookup>
		<groupName>stem:b</groupName>
	</wsGroupLookup>
	<subjectLookups>
		<WsSubjectLookup>
			<subjectId>test.subject.0</subjectId>
		</WsSubjectLookup>
	</subjectLookups>
</WsRestAddMemberRequest>

```

 Add to B response:

 
```

<WsAddMemberResults>
	<results>
		<WsAddMemberResult>
			<wsSubject>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
				<id>test.subject.0</id>
				<name>my name is test.subject.0</name>
				<sourceId>jdbc</sourceId>
			</wsSubject>
			<resultMetadata>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
			</resultMetadata>
		</WsAddMemberResult>
	</results>
	<wsGroupAssigned>
		<extension>b</extension>
		<displayExtension>b</displayExtension>
		<displayName>stem:b</displayName>
		<name>stem:b</name>
		<uuid>826d8f94fcff4865b4a0a51c74dfbaff</uuid>
	</wsGroupAssigned>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>Success for: clientVersion: 1.6.0, wsGroupLookup:
			WsGroupLookup[groupName=stem:b], subjectLookups: Array size: 1: [0]:
			WsSubjectLookup[subjectId=test.subject.0]

			, replaceAllExisting: false, actAsSubject: null, fieldName: null,
			txType: NONE, includeGroupDetail: false, includeSubjectDetail: false,
			subjectAttributeNames: null
			, params: null
			, disabledDate: null, enabledDate: null</resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>102</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
</WsAddMemberResults>

```

 Delete from B request

 
```

<WsRestDeleteMemberRequest>
	<wsGroupLookup>
		<groupName>stem:b</groupName>
	</wsGroupLookup>
	<subjectLookups>
		<WsSubjectLookup>
			<subjectId>test.subject.0</subjectId>
		</WsSubjectLookup>
	</subjectLookups>
</WsRestDeleteMemberRequest>

```

 Delete from B response

 
```

<WsDeleteMemberResults>
	<results>
		<WsDeleteMemberResult>
			<wsSubject>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
				<id>test.subject.0</id>
				<name>my name is test.subject.0</name>
				<sourceId>jdbc</sourceId>
			</wsSubject>
			<resultMetadata>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
			</resultMetadata>
		</WsDeleteMemberResult>
	</results>
	<wsGroup>
		<extension>b</extension>
		<displayExtension>b</displayExtension>
		<displayName>stem:b</displayName>
		<name>stem:b</name>
		<uuid>826d8f94fcff4865b4a0a51c74dfbaff</uuid>
	</wsGroup>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>Success for: clientVersion: 1.6.0, wsGroupLookup:
			WsGroupLookup[groupName=stem:b], subjectLookups: Array size: 1: [0]:
			WsSubjectLookup[subjectId=test.subject.0]

			, actAsSubject: null, fieldName: null, txType: NONE
			, params: null</resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>424</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
</WsDeleteMemberResults>

```

 See removed from A request

 
```

<WsRestHasMemberRequest>
	<wsGroupLookup>
		<groupName>stem:a</groupName>
	</wsGroupLookup>
	<subjectLookups>
		<WsSubjectLookup>
			<subjectId>test.subject.0</subjectId>
		</WsSubjectLookup>
	</subjectLookups>
</WsRestHasMemberRequest>

```

 See removed from A response

 
```

<WsHasMemberResults>
	<results>
		<WsHasMemberResult>
			<wsSubject>
				<resultCode>SUCCESS</resultCode>
				<success>T</success>
				<id>test.subject.0</id>
				<name>my name is test.subject.0</name>
				<sourceId>jdbc</sourceId>
			</wsSubject>
			<resultMetadata>
				<resultCode>IS_NOT_MEMBER</resultCode>
				<success>T</success>
			</resultMetadata>
		</WsHasMemberResult>
	</results>
	<wsGroup>
		<extension>a</extension>
		<displayExtension>a</displayExtension>
		<displayName>stem:a</displayName>
		<name>stem:a</name>
		<uuid>7aa346e9d5ef42de842f490f49d08115</uuid>
	</wsGroup>
	<resultMetadata>
		<resultCode>SUCCESS</resultCode>
		<resultMessage>Success for: clientVersion: 1.6.0, wsGroupLookup:
			WsGroupLookup[groupName=stem:a], subjectLookups: Array size: 1: [0]:
			WsSubjectLookup[subjectId=test.subject.0]

			memberFilter: All, actAsSubject: null, fieldName: null, includeGroupDetail: false,
			includeSubjectDetail: false, subjectAttributeNames: null
			,params: null
                </resultMessage>
		<success>T</success>
	</resultMetadata>
	<responseMetadata>
		<resultWarnings></resultWarnings>
		<millis>38</millis>
		<serverVersion>2.0.0</serverVersion>
	</responseMetadata>
</WsHasMemberResults>

```

 sdf
