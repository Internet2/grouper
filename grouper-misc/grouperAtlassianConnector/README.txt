How to turn on logging:

put log4j.properties in edit-webapp

log4j.logger.edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider = DEBUG, console, filelog


Change osuser.xml (put in edit-webapp)

        <!-- provider class="com.atlassian.jira.user.osuser.JiraOFBizAccessProvider">
                <property name="exclusive-access">true</property>
        </provider -->

        <provider class="edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider">
                <property name="exclusive-access">true</property>
        </provider>

        put the two jars in lib, and grouper.client.properties in classes in edit-webapp
        
Document jiraClasses.jar...