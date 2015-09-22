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
import com.google.api.services.admin.directory.model.*;

import com.google.api.services.groupssettings.Groupssettings;
import com.google.api.services.groupssettings.model.Groups;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * These tests are intended to be run sequentially. At some point they maybe set up to run independently.
 */
public class GoogleAppsSdkUtilsTest {
    private static String TEST_USER;
    private static String TEST_GROUP;

    private static String SERVICE_ACCOUNT_EMAIL;
    private static String SERVICE_ACCOUNT_PKCS_12_FILE_PATH;
    private static String SERVICE_IMPERSONATION_USER;

    /** Global instance of the HTTP transport & JSON Factory. */
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Shared Instances of the client*/
    private static Directory directoryClient = null;
    private static Groupssettings groupssettingsClient = null;


    @BeforeClass
    public static void setupClass() {
        Properties props = new Properties();

        InputStream is = ClassLoader.getSystemResourceAsStream("unit-test.properties");
        try {
            props.load(is);
            TEST_USER = props.getProperty("TEST_USER");
            TEST_GROUP = props.getProperty("TEST_GROUP");

            SERVICE_ACCOUNT_EMAIL = props.getProperty("SERVICE_ACCOUNT_EMAIL");
            SERVICE_ACCOUNT_PKCS_12_FILE_PATH = props.getProperty("SERVICE_ACCOUNT_PKCS_12_FILE_PATH");
            SERVICE_IMPERSONATION_USER = props.getProperty("SERVICE_IMPERSONATION_USER");
        }
        catch (IOException e) {
            System.out.println("unit-test.properties configuration not found. Try again! Love, Grumpy Cat");
        }
    }

    @Before
    public void setup()  throws GeneralSecurityException, IOException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential googleDirectoryCredential = GoogleAppsSdkUtils.getGoogleDirectoryCredential(SERVICE_ACCOUNT_EMAIL,
                    SERVICE_ACCOUNT_PKCS_12_FILE_PATH, SERVICE_IMPERSONATION_USER,
                    httpTransport, JSON_FACTORY);

        GoogleCredential googleGroupssettingsCredential = GoogleAppsSdkUtils.getGoogleGroupssettingsCredential(SERVICE_ACCOUNT_EMAIL,
                SERVICE_ACCOUNT_PKCS_12_FILE_PATH, SERVICE_IMPERSONATION_USER,
                httpTransport, JSON_FACTORY);


        directoryClient = new Directory.Builder(httpTransport, JSON_FACTORY, googleDirectoryCredential)
                .setApplicationName("Google Apps Grouper Provisioner")
                .build();

        groupssettingsClient = new Groupssettings.Builder(httpTransport, JSON_FACTORY, googleGroupssettingsCredential)
                .setApplicationName("Google Apps Grouper Provisioner")
                .build();
    }

    @After
    public void teardown() throws GeneralSecurityException, IOException {
        //Give Google a half a second to catch up since we create and destroy the same users and groups over and over
        pause(500L);
    }

    private void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void teardownClass() throws IOException {
        GoogleAppsSdkUtils.removeGroup(directoryClient, TEST_GROUP);
        GoogleAppsSdkUtils.removeUser(directoryClient, TEST_USER);
    }

    @Test
    public void testGetGoogleCredential() throws Exception {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential googleCredential = GoogleAppsSdkUtils.getGoogleDirectoryCredential(SERVICE_ACCOUNT_EMAIL,
                SERVICE_ACCOUNT_PKCS_12_FILE_PATH, SERVICE_IMPERSONATION_USER,
                httpTransport, JSON_FACTORY);

        Directory service = new Directory.Builder(httpTransport, JSON_FACTORY, googleCredential)
                .setApplicationName("Google Apps Grouper Provisioner")
                .build();

        Directory.Users.List request = service.users().list().setCustomer("my_customer");
        request.execute();
    }

    @Test
    public void testCreateUser() throws GeneralSecurityException, IOException {
        User user = new User();
        user.setName(new UserName().setFamilyName("Gasper").setGivenName("Test"))
                .setPrimaryEmail(TEST_USER)
                .setPassword(new BigInteger(130, new SecureRandom()).toString(32));

        User currentUser = GoogleAppsSdkUtils.addUser(directoryClient, user);
        assertEquals("Boom", currentUser.getName().getGivenName(), user.getName().getGivenName());

    }

    @Test
    public void testCreateGroup() throws GeneralSecurityException, IOException {
        Group group = new Group();
        group.setName("Test Group")
            .setEmail(TEST_GROUP);

        Group currentGroup = GoogleAppsSdkUtils.addGroup(directoryClient, group);
        assertEquals("Boom", currentGroup.getName(), group.getName());

    }

    @Test
    public void testRetrieveAllUsers() throws GeneralSecurityException, IOException {
        List<User> allUsers = GoogleAppsSdkUtils.retrieveAllUsers(directoryClient);
        assertTrue(allUsers.size() > 0);
    }

    @Test
    public void testRetrieveUser() throws GeneralSecurityException, IOException {
        User user = GoogleAppsSdkUtils.retrieveUser(directoryClient, TEST_USER);
        assertEquals("Test", user.getName().getGivenName());
    }

    @Test
    public void testRetrieveMissingUser() throws GeneralSecurityException, IOException {
        User user = GoogleAppsSdkUtils.retrieveUser(directoryClient, "missing-" + TEST_USER);
        assertTrue(user == null);
    }

    @Test
    public void testRetrieveAllGroups() throws GeneralSecurityException, IOException {
        List<Group> allGroups = GoogleAppsSdkUtils.retrieveAllGroups(directoryClient);
        assertTrue(allGroups.size() > 0);
    }

    @Test
    public void testRetrieveGroup() throws GeneralSecurityException, IOException {
        Group group = GoogleAppsSdkUtils.retrieveGroup(directoryClient, TEST_GROUP);
        assertEquals("Test Group", group.getName());
    }

    @Test
    public void testRetrieveGroupSettings() throws GeneralSecurityException, IOException {
        com.google.api.services.groupssettings.model.Groups groupsSettings = GoogleAppsSdkUtils.retrieveGroupSettings(groupssettingsClient, TEST_GROUP);
        assertEquals("Test Group", groupsSettings.getName());
    }


    @Test
    public void testRetrieveMissingGroup() throws GeneralSecurityException, IOException {
        Group group = GoogleAppsSdkUtils.retrieveGroup(directoryClient, "missing-" + TEST_GROUP);
        assertTrue(group == null);
    }

    @Test
    public void testAddMember() throws GeneralSecurityException, IOException {
        Member member = new Member();
        member.setRole("MEMBER")
            .setEmail(TEST_USER);

        Member currentMember = GoogleAppsSdkUtils.addGroupMember(directoryClient, TEST_GROUP, member);
        assertEquals("Boom", currentMember.getEmail(), member.getEmail());
    }

    @Test
    public void testRetrieveGroupMembers() throws GeneralSecurityException, IOException {
        List<Member> members = GoogleAppsSdkUtils.retrieveGroupMembers(directoryClient, TEST_GROUP);
        assertTrue(members.size() > 0);
    }

    @Test
    public void testRemoveMember() throws GeneralSecurityException, IOException {
        GoogleAppsSdkUtils.removeGroupMember(directoryClient, TEST_GROUP, TEST_USER);
        assertTrue(GoogleAppsSdkUtils.retrieveGroupMembers(directoryClient, TEST_GROUP).size() == 0);
    }

    @Test
    public void testUpdateGroup() throws GeneralSecurityException, IOException {
        Group group = GoogleAppsSdkUtils.retrieveGroup(directoryClient, TEST_GROUP);
        group.setName("test");

        Group result = GoogleAppsSdkUtils.updateGroup(directoryClient, TEST_GROUP, group);
        assertEquals("test", result.getName());
        assertEquals("test", GoogleAppsSdkUtils.retrieveGroup(directoryClient, TEST_GROUP).getName());
    }

    @Test
    public void testUpdateGroupSettings() throws GeneralSecurityException, IOException {
        Groups groupSettings = GoogleAppsSdkUtils.retrieveGroupSettings(groupssettingsClient, TEST_GROUP);
        groupSettings.setShowInGroupDirectory("false");

        Groups result = GoogleAppsSdkUtils.updateGroupSettings(groupssettingsClient, TEST_GROUP, groupSettings);
        assertEquals("false", result.getShowInGroupDirectory());
        assertEquals("false", GoogleAppsSdkUtils.retrieveGroupSettings(groupssettingsClient, TEST_GROUP).getShowInGroupDirectory());
    }

    @Test
    public void testRemoveGroup() throws GeneralSecurityException, IOException {
        GoogleAppsSdkUtils.removeGroup(directoryClient, TEST_GROUP);
        assertTrue(GoogleAppsSdkUtils.retrieveGroup(directoryClient, TEST_GROUP) == null);
    }

    @Test
    public void testRemoveUser() throws GeneralSecurityException, IOException {
        GoogleAppsSdkUtils.removeUser(directoryClient, TEST_USER);
        assertTrue(GoogleAppsSdkUtils.retrieveUser(directoryClient, TEST_USER) == null);
    }
}
