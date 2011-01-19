How to turn on logging:

put log4j.properties in edit-webapp

log4j.logger.edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider = DEBUG, console, filelog
log4j.logger.edu.internet2.middleware.grouperAtlassianConnector.GrouperProfileProvider = DEBUG, console, filelog
log4j.logger.edu.internet2.middleware.grouperAtlassianConnector.GrouperLoggingAccessProviderWrapper = DEBUG, console, filelog
log4j.logger.edu.internet2.middleware.grouperAtlassianConnector.GrouperLoggingProfileProviderWrapper = DEBUG, console, filelog

Change osuser.xml (put in edit-webapp)

CredentialsProvider
        <!-- provider class="com.atlassian.jira.user.osuser.JiraOFBizCredentialsProvider">
                <property name="exclusive-access">true</property>
        </provider -->

        <provider class="edu.internet2.middleware.grouperAtlassianConnector.GrouperCredentialsProvider">
                <property name="exclusive-access">true</property>
        </provider>

AccessProvider
        <!-- provider class="com.atlassian.jira.user.osuser.JiraOFBizAccessProvider">
                <property name="exclusive-access">true</property>
        </provider -->

        <provider class="edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider">
                <property name="exclusive-access">true</property>
        </provider>

ProfileProvider
        <!--provider class="com.atlassian.jira.user.osuser.JiraOFBizProfileProvider">
                <property name="exclusive-access">true</property>
        </provider -->

        <provider class="edu.internet2.middleware.grouperAtlassianConnector.GrouperLoggingProfileProviderWrapper">
                <property name="exclusive-access">true</property>
        </provider>


        put the two jars in lib, and grouper.client.properties in classes in edit-webapp
        
Document jiraClasses.jar...
