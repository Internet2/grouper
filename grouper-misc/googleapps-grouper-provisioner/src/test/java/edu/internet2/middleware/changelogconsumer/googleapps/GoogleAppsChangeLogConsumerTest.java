/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.changelogconsumer.googleapps;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.UserName;
import edu.internet2.middleware.changelogconsumer.googleapps.cache.GoogleCacheManager;
import edu.internet2.middleware.changelogconsumer.googleapps.utils.AddressFormatter;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "org.apache.log4j.*"})
@PrepareForTest(value = { })
public class GoogleAppsChangeLogConsumerTest {

    private static final String groupName = "qsuob:testStem:test";
    private String groupDisplayName = "test";

    private static final String subjectId = "fiwi";
    private static final String sourceId = "jdbc";

    private ChangeLogProcessorMetadata metadata;
    private static AddressFormatter addressFormatter = new AddressFormatter();

    private static GoogleAppsChangeLogConsumer consumer;
    private static String googleDomain;

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static Directory directory = null;

    @BeforeClass
    public static void setupClass() {

        consumer = new GoogleAppsChangeLogConsumer();

        Properties props = new Properties();

        InputStream is = ClassLoader.getSystemResourceAsStream("unit-test.properties");
        try {
            props.load(is);

            googleDomain = props.getProperty("DOMAIN");
            addressFormatter
                    .setDomain(googleDomain)
                    .setGroupIdentifierExpression(props.getProperty("GROUP_IDENTIFIER_EXPRESSION"))
                    .setSubjectIdentifierExpression(props.getProperty("SUBJECT_IDENTIFIER_EXPRESSION"));

            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleCredential googleCredential = null;
                 googleCredential = GoogleAppsSdkUtils.getGoogleDirectoryCredential(props.getProperty("SERVICE_ACCOUNT_EMAIL"),
                         props.getProperty("SERVICE_ACCOUNT_PKCS_12_FILE_PATH"), props.getProperty("SERVICE_IMPERSONATION_USER"),
                         httpTransport, JSON_FACTORY);

            directory = new Directory.Builder(httpTransport, JSON_FACTORY, googleCredential)
                    .setApplicationName("Google Apps Grouper Provisioner")
                    .build();
            
        } catch (Exception e) {
            System.out.println("unit-test.properties configuration not found. Try again! Love, Grumpy Cat");
        }

    }

    @Before
    public void setup() throws GeneralSecurityException, IOException {
        metadata = mock(ChangeLogProcessorMetadata.class);
        when(metadata.getConsumerName()).thenReturn("google");
    }

    @After
    public void tearDown() throws GeneralSecurityException, IOException {
        try {
            GoogleAppsSdkUtils.removeGroup(directory, addressFormatter.qualifyGroupAddress(groupName));
        } catch (IOException e) {

        }

        try {
            GoogleAppsSdkUtils.removeUser(directory, addressFormatter.qualifyGroupAddress(subjectId));
        } catch (IOException e) {

        }

        GoogleCacheManager.googleGroups().clear();
        GoogleCacheManager.googleUsers().clear();

        //Give Google a second to catch up since we create and destroy the same users and groups over and over
        pause(1000L);
    }

    @Test
    public void testProcessGroupAdd() throws GeneralSecurityException, IOException {
        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("group", "addGroup", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name)).thenReturn(groupName);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);
        Group group = GoogleAppsSdkUtils.retrieveGroup(directory, addressFormatter.qualifyGroupAddress(groupName));
        assertNotNull(group);
        assertTrue(group.getName().equalsIgnoreCase(groupDisplayName));
    }

    @Test
    public void testProcessGroupUpdate() throws GeneralSecurityException, IOException {
        final String NEW_TEST = "newTest";

        createTestGroup(groupDisplayName, groupName);

        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("group", "updateGroup", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name)).thenReturn(groupName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged)).thenReturn("displayExtension");
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue)).thenReturn(groupDisplayName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue)).thenReturn(NEW_TEST);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);
        pause(1000L);
        Group group = GoogleAppsSdkUtils.retrieveGroup(directory, addressFormatter.qualifyGroupAddress(groupName));

        assertNotNull(group);
        assertEquals(NEW_TEST, group.getName());

        //TODO: ID Change
        //TODO: Description Change
        //TODO: Privilege Change
    }


    @Test
    public void testProcessGroupMemberAddExistingUser() throws GeneralSecurityException, IOException {
        //User already exists in Google
        createTestGroup(groupDisplayName, groupName);
        createTestUser(buildSubjectAddress(subjectId), "Fiona", "Windsor");

        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("membership", "addMembership", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)).thenReturn(groupName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId)).thenReturn(subjectId);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId)).thenReturn(sourceId);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);

        List<Member> members = GoogleAppsSdkUtils.retrieveGroupMembers(directory, addressFormatter.qualifyGroupAddress(groupName));
        assertNotNull(members);
        assertTrue(members.size() == 1);
        assertTrue(members.get(0).getEmail().equalsIgnoreCase(buildSubjectAddress(subjectId)));
    }

/* This only works if the grouper-load.properties is marked with .provisionUsers=false
    @Test
    public void testProcessGroupMemberAddNewUserNoProvisioning() throws GeneralSecurityException, IOException {
        //User doesn't exists in Google
        createTestGroup(groupDisplayName, groupName);

        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("membership", "addMembership", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)).thenReturn(groupName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId)).thenReturn(subjectId);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId)).thenReturn(sourceId);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);

        List<Member> members = GoogleAppsSdkUtils.retrieveGroupMembers(directory, addressFormatter.qualifyGroupAddress(groupName));
        assertNotNull(members);
        assertTrue(members.size() == 0);
    }
*/

    //This only works if the grouper-load.properties is marked with .provisionUsers=true
    @Test
    public void testProcessGroupMemberAddNewUserWithProvisioning() throws GeneralSecurityException, IOException {
        //User doesn't exists in Google
        createTestGroup(groupDisplayName, groupName);

        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("membership", "addMembership", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)).thenReturn(groupName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId)).thenReturn(subjectId);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId)).thenReturn(sourceId);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);

        List<Member> members = GoogleAppsSdkUtils.retrieveGroupMembers(directory, addressFormatter.qualifyGroupAddress(groupName));
        assertNotNull(members);
        assertTrue(members.size() == 1);
        assertTrue(members.get(0).getEmail().equalsIgnoreCase(buildSubjectAddress(subjectId)));
    }

    @Test
    public void testProcessGroupMemberRemove() throws GeneralSecurityException, IOException {
        //User already exists in Google
        Group group = createTestGroup(groupDisplayName, groupName);
        createTestUser(buildSubjectAddress(subjectId), "Fiona", "Windsor");

        Member member = new Member()
                .setEmail(buildSubjectAddress(subjectId))
                .setRole("MEMBER");
        GoogleAppsSdkUtils.addGroupMember(directory, group.getEmail(), member);

        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("membership", "deleteMembership", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName)).thenReturn(groupName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId)).thenReturn(subjectId);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId)).thenReturn(sourceId);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);

        List<Member> members = GoogleAppsSdkUtils.retrieveGroupMembers(directory, addressFormatter.qualifyGroupAddress(groupName));
        assertNotNull(members);
        assertTrue(members.size() == 0);
    }

    @Test
    public void testProcessGroupsStemChange() throws GeneralSecurityException, IOException {
        try {
            createTestGroup(groupDisplayName, groupName + "Change");
        } catch (Exception ex) {}

        ChangeLogEntry addEntry = mock(ChangeLogEntry.class);
        when(addEntry.getChangeLogType()).thenReturn(new ChangeLogType("group", "updateGroup", ""));
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name)).thenReturn(groupName);
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged)).thenReturn("name");
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue)).thenReturn(groupName+"Change");
        when(addEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue)).thenReturn(groupName);
        when(addEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(addEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);
        pause(2000L);
        Group group = GoogleAppsSdkUtils.retrieveGroup(directory, addressFormatter.qualifyGroupAddress(groupName));

        assertNotNull(group);
        assertEquals(addressFormatter.qualifyGroupAddress(groupName).toLowerCase(), group.getEmail());
        assertTrue(group.getAliases().contains(addressFormatter.qualifyGroupAddress(groupName+"Change")));
    }

    @Test
    public void testProcessGroupDelete() throws GeneralSecurityException, IOException {
        createTestGroup(groupDisplayName, groupName);

        ChangeLogEntry deleteEntry = mock(ChangeLogEntry.class);
        when(deleteEntry.getChangeLogType()).thenReturn(new ChangeLogType("group", "deleteGroup", ""));
        when(deleteEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name)).thenReturn(groupName);
        when(deleteEntry.getContextId()).thenReturn("123456789");

        ArrayList<ChangeLogEntry> changeLogEntryList = new ArrayList<ChangeLogEntry>(Arrays.asList(deleteEntry));

        consumer.processChangeLogEntries(changeLogEntryList, metadata);
        assertTrue(GoogleAppsSdkUtils.retrieveGroup(directory, addressFormatter.qualifyGroupAddress(groupName)) == null);
    }

/*
    @Test
    public void testProcessSyncAttributeAddedDirectly() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }

    @Test
    public void testProcessSyncAttributeAddedToParent() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }

    @Test
    public void testProcessSyncAttributeRemovedDirectly() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }

    @Test
    public void testProcessSyncAttributeRemovedFromParent() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }
*/
/*
    @Test
    public void testProcessPrivilegeAdded() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }

    @Test
    public void testProcessPrivilegeRemoved() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }

    @Test
    public void testProcessPrivilegeChange() throws GeneralSecurityException, IOException {
        fail("Not Implemented");
    }
*/


    private Group createTestGroup(String name, String mailbox) throws IOException {
        Group group = new Group();
        group.setName(name);
        group.setEmail(addressFormatter.qualifyGroupAddress(mailbox));
        return GoogleAppsSdkUtils.addGroup(directory, group);
    }

    private User createTestUser(String email, String givenName, String surname) throws IOException {
        User user = new User();
        user.setPrimaryEmail(email);
        user.setName(new UserName());
        user.getName().setFamilyName(surname);
        user.getName().setGivenName(givenName);
        user.setPassword(new BigInteger(130, new SecureRandom()).toString(32));
        return GoogleAppsSdkUtils.addUser(directory, user);
    }

    private Subject getTestUserSubject() {
        Subject subject = mock(Subject.class);
        when(subject.getAttributeValue("givenName")).thenReturn("testgn2");
        when(subject.getAttributeValue("sn")).thenReturn("testfn2");
        when(subject.getAttributeValue("displayName")).thenReturn("testgn2, testfn2");
        when(subject.getAttributeValue("mail")).thenReturn(buildSubjectAddress(subjectId));
        when(subject.getType()).thenReturn(SubjectTypeEnum.PERSON);
        return subject;
    }

    private Subject getTestGroupSubject() {
        Subject subject = mock(Subject.class);
        when(subject.getAttributeValue("givenName")).thenReturn("testgn2");
        when(subject.getAttributeValue("sn")).thenReturn("testfn2");
        when(subject.getAttributeValue("displayName")).thenReturn("testgn2, testfn2");
        when(subject.getAttributeValue("mail")).thenReturn(buildSubjectAddress(subjectId));
        when(subject.getType()).thenReturn(SubjectTypeEnum.GROUP);
        return subject;
    }

    private void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String buildSubjectAddress(String subjectId) {
        return String.format("%s@%s", subjectId, googleDomain);
    }

}
