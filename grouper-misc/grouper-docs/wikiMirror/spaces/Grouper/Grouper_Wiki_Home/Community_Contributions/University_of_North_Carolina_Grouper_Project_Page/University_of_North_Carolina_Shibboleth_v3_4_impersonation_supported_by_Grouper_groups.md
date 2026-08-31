---
title: "University of North Carolina - Shibboleth v3.4 impersonation supported by Grouper groups"
space: Grouper
pageId: 28545468
version: 6
lastUpdated: 2026-07-01T05:47:12.046Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545468/University+of+North+Carolina+-+Shibboleth+v3.4+impersonation+supported+by+Grouper+groups
---

The Shibboleth Identity Provider, starting with version 3.4, supports a post-authentication flow allowing the user to impersonate another authorized user. Below is our local customization to utilize this feature in a non-production environment.

There are two authorization gates checked before presenting the impersonation form. The first is whether the logged in user is allowed to impersonate in general, and the second is a list of users they are allowed to impersonate. We have tied both of these decisions to SAML attributes derived from Grouper groups published to LDAP.

## Configuration

### attribute-resolver

Our Shibboleth setup has the attribute isMemberOf returning a list of all the LDAP groups the user is a member of, which are all groups that originate in Grouper. The impersonationPermitted and impersonatableUsernames attributes are based on a subset of these groups. The first returns a value of "true" if the user is in the policy group unc:app:its:shib:uimp:users:authorized. The second returns a list of ids, uids, emails, or Kerberos principals (all of which can be mapped to a Shibboleth principal due to the way our LDAP query is constructed), by selecting all the groups matching the pattern unc:app:its:shib:uimp:targets:* and returning the last part of the group name. The group names in the uimp:targets folder in Grouper thus have their extension an id, uid, email, or Kerberos principal. Members of these groups will be allowed to impersonate *only*if they are also in the users:authorized group.

```xml
    <!-- Users allowed to impersonate -->
    <AttributeDefinition id="impersonationPermitted" xsi:type="Mapped">
        <InputAttributeDefinition ref="isMemberOf"/>
        <AttributeEncoder xsi:type="SAML2String" name="impersonationPermitted" friendlyName="impersonationPermitted" encodeType="false"/>
         <ValueMap>
            <ReturnValue>true</ReturnValue>
            <SourceValue>unc:app:its:shib:uimp:users:authorized</SourceValue>
         </ValueMap>
    </AttributeDefinition>

    <!-- Impersonation attribute -->
    <AttributeDefinition id="impersonatableUsernames" xsi:type="Mapped">
        <InputAttributeDefinition ref="isMemberOf"/>
        <AttributeEncoder xsi:type="SAML2String" name="impersonatableUsernames" friendlyName="impersonatableUsernames" encodeType="false"/>
         <ValueMap>
            <ReturnValue>$1</ReturnValue>
            <SourceValue>^unc:app:its:shib:uimp:targets:([^:]+)$</SourceValue>
         </ValueMap>
    </AttributeDefinition>

```

### ldap.properties

Note that in the login form, only an email or uid are valid as a username. But for impersonation, a larger set of ids can be used. This means that users can be impersonated even if they don't have a uid or an email (e.g., if they don't have an active affiliation).

```
idp.authn.LDAP.userFilter                       = (&(objectclass=uncperson)(|(uid={user})(uncEmail={user})))
...
idp.attribute.resolver.LDAP.searchFilter        = (&(objectclass=uncperson)(|(uid=$resolutionContext.principal)(uncEmail=$resolutionContext.principal)(uncKerberosPrincipal=$resolutionContext.principal)(pid=$resolutionContext.principal)))
```

### access-control.xml

Two new entries are added to the shibboleth.AccessControlPolicies mapping, that tie the attributes to the policies.

```xml
        <!-- Limits who can impersonate based on entitlement. -->
        <entry key="GeneralImpersonationPolicy">
          <bean parent="shibboleth.PredicateAccessControl">
            <constructor-arg>
              <bean parent="shibboleth.Conditions.AND">
                <constructor-arg>
                  <list>
                    <bean class="net.shibboleth.idp.profile.logic.SimpleAttributePredicate">
                      <property name="attributeValueMap">
                        <map>
                          <entry key="impersonationPermitted">
                            <list>
                              <value>true</value>
                            </list>
                          </entry>
                        </map>
                      </property>
                    </bean>
                    <bean class="net.shibboleth.idp.profile.logic.SimpleAttributePredicate">
                      <property name="attributeValueMap">
                        <map>
                          <entry key="impersonatableUsernames">
                            <list>
                              <value>*</value>
                            </list>
                          </entry>
                        </map>
                      </property>
                    </bean>
                  </list>
                </constructor-arg>
              </bean>
            </constructor-arg>
          </bean>
        </entry>

        <!-- Controls the impersonation scenarios to allow. -->
        <!-- NOTE: the matcher for DynamicAttributePredicate allows '*' as a valid match, which is why we need to exclude it here -->
        <entry key="SpecificImpersonationPolicy">
            <bean parent="shibboleth.PredicateAccessControl">
                <constructor-arg>
                    <bean parent="shibboleth.Conditions.AND">
                        <constructor-arg>
                            <list>
                                <bean class="net.shibboleth.idp.profile.logic.SpringExpressionPredicate"
                                    c:expression="#input.getSubcontext(T(org.opensaml.profile.context.AccessControlContext)).getResource() != '*'"/>
                                <bean class="net.shibboleth.idp.profile.logic.DynamicAttributePredicate">
                                    <property name="attributeFunctionMap">
                                        <map>
                                            <entry key="impersonatableUsernames">
                                                <list>
                                                    <bean parent="shibboleth.ContextFunctions.Expression"
                                                        c:expression="#input.getSubcontext(T(org.opensaml.profile.context.AccessControlContext)).getResource()" />
                                                </list>
                                            </entry>
                                        </map>
                                    </property>
                                </bean>
                            </list>
                        </constructor-arg>
                    </bean>
                </constructor-arg>
            </bean>
        </entry>
```

### relying-party.xml

The impersonation flow also needs to be enabled per relying party (or optionally all parties by default). Note that this example also requires multi-factor authentication, which must be passed before the impersonation form is shown.

```xml
        <bean parent="RelyingPartyByName" c:relyingPartyIds="#{{'test-entity'}}">
            <property name="profileConfigurations">
                <list>
                    <bean parent="SAML2.SSO.custom"
                        p:postAuthenticationFlows="#{{'impersonate'}}"
                        p:nameIDFormatPrecedence="#{{
                           'urn:oasis:names:tc:SAML:2.0:nameid-format:persistent',
                           'urn:oasis:names:tc:SAML:2.0:nameid-format:transient'}}">
                        <property name="defaultAuthenticationMethods">
                            <list>
                                <ref bean="MfaPrincipal" />
                            </list>
                        </property>
                    </bean>
                </list>
            </property>
        </bean>

```

### views/intercept/impersonate.vm

The default Velocity template for the impersonation form has a text field for the username. There is a commented section replacing this with a select list, and we simply uncommented it.

```
                #set ($attributeContext = $rpContext.getSubcontext('net.shibboleth.idp.attribute.context.AttributeContext'))
                #set ($usernamesAttribute = $attributeContext.getUnfilteredIdPAttributes().get("impersonatableUsernames"))
                <select class="form-element form-field" id="impersonation" name="principal">
                #if ($usernamesAttribute)
                  #foreach ($username in $usernamesAttribute.getValues())
                    <option value="$encoder.encodeForHTML($username.getValue())">$encoder.encodeForHTML($username.getValue())</option>
                  #end
                #end
                </select>
```

## Result

Impersonation form, showing buttons

Impersonation form, showing select options. Note the different types of IDs that can be used.
