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
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryRequest;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.*;
import com.google.api.services.groupssettings.Groupssettings;
import com.google.api.services.groupssettings.GroupssettingsRequest;
import com.google.api.services.groupssettings.GroupssettingsScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * GoogleAppsSdkUtils is a helper class that interfaces with the Google SDK Admin API and handles exponential back-off.
 * see https://developers.google.com/admin-sdk/directory/v1/guides/delegation
 *
 * @author John Gasper, Unicon
 */
public class GoogleAppsSdkUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAppsChangeLogConsumer.class);

    private static final String[] directoryScope = {DirectoryScopes.ADMIN_DIRECTORY_USER, DirectoryScopes.ADMIN_DIRECTORY_GROUP};

    private static final String[] groupssettingsScope = {GroupssettingsScopes.APPS_GROUPS_SETTINGS};

    private static final Random randomGenerator = new Random();

    /**
     * getGoogleDirectoryCredential creates a credential object that authenticates the REST API calls.
     * @param serviceAccountEmail the application's account email address provided by Google
     * @param serviceAccountPKCS12FilePath path of a private key (.p12) file provided by Google
     * @param serviceAccountUser a impersonation user account
     * @param httpTransport a httpTransport object
     * @param jsonFactory a jsonFactory object
     * @return a Google Credential
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static GoogleCredential getGoogleDirectoryCredential(String serviceAccountEmail, String serviceAccountPKCS12FilePath,
                                                                String serviceAccountUser, HttpTransport httpTransport, JsonFactory jsonFactory)
            throws GeneralSecurityException, IOException {

        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(Arrays.asList(directoryScope))
                .setServiceAccountUser(serviceAccountUser)
                .setServiceAccountPrivateKeyFromP12File(new File(serviceAccountPKCS12FilePath))
                .build();
    }

    /**
     * getGoogleDirectoryCredential creates a credential object that authenticates the REST API calls.
     * @param serviceAccountEmail the application's account email address provided by Google
     * @param serviceAccountPKCS12FilePath path of a private key (.p12) file provided by Google
     * @param serviceAccountUser a impersonation user account
     * @param httpTransport a httpTransport object
     * @param jsonFactory a jsonFactory object
     * @return a Google Credential
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static GoogleCredential getGoogleGroupssettingsCredential(String serviceAccountEmail, String serviceAccountPKCS12FilePath,
                                                                String serviceAccountUser, HttpTransport httpTransport, JsonFactory jsonFactory)
            throws GeneralSecurityException, IOException {

        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(Arrays.asList(groupssettingsScope))
                .setServiceAccountUser(serviceAccountUser)
                .setServiceAccountPrivateKeyFromP12File(new File(serviceAccountPKCS12FilePath))
                .build();
    }

    /**
     * addUser creates a user to Google.
     * @param directoryClient a Directory (service) object
     * @param user a populated User object
     * @return the new User object created/returned by Google
     * @throws IOException
     */
    public static User addUser(Directory directoryClient, User user) throws IOException {
        LOG.debug("addUser() - {}", user);

        Directory.Users.Insert request = null;

        try {
            request = directoryClient.users().insert(user);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (User) execute(request);
    }

    /**
     * removeGroup removes a group from Google.
     * @param directoryClient a Directory (service) object
     * @param userKey an identifier for a user (e-mail address is the most popular)
     * @throws IOException
     */
    public static void removeUser(Directory directoryClient, String userKey) throws IOException {
        LOG.debug("removeUser() - {}", userKey);

        Directory.Users.Delete request = null;

        try {
            request = directoryClient.users().delete(userKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        execute(request);
    }


    /**
     * addGroup adds a group to Google.
     * @param directoryClient a Directory client
     * @param group a populated Group object
     * @return the new Group object created/returned by Google
     * @throws IOException
     */
    public static Group addGroup(Directory directoryClient, Group group) throws IOException {
        LOG.debug("addGroup() - {}", group);

        Directory.Groups.Insert request = null;

        try {
            request = directoryClient.groups().insert(group);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (Group) execute(request);
    }

    /**
     * removeGroup removes a group from Google.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @throws IOException
     */
    public static void removeGroup(Directory directoryClient, String groupKey) throws IOException {
        LOG.debug("removeGroup() - {}", groupKey);

        Directory.Groups.Delete request = null;

        try {
            request = directoryClient.groups().delete(groupKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        execute(request);
    }

    /**
     * addGroup adds a group to Google.
     * @param directoryClient a Directory client
     * @param group a populated Group object
     * @return the new Group object created/returned by Google
     * @throws IOException
     */
    public static Group updateGroup(Directory directoryClient, String groupKey, Group group) throws IOException {
        LOG.debug("updateGooGroup() - {}", group);

        Directory.Groups.Update request = null;

        try {
            request = directoryClient.groups().update(groupKey, group);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (Group) execute(request);
    }

    /**
     * addGroup adds a group to Google.
     * @param groupssettingsClient a Groupssettings client
     * @param groupSettings a populated Groups (group settings) object
     * @return the new Group object created/returned by Google
     * @throws IOException
     */
    public static com.google.api.services.groupssettings.model.Groups updateGroupSettings(Groupssettings groupssettingsClient, String groupKey, com.google.api.services.groupssettings.model.Groups groupSettings) throws IOException {
        LOG.debug("updateGroupssettings() - {}", groupKey);

        Groupssettings.Groups.Update request = null;

        try {
            request = groupssettingsClient.groups().update(groupKey, groupSettings);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (com.google.api.services.groupssettings.model.Groups) execute(request);
    }

    /**
     * retrieveAllUsers returns all of the users from Google.
     * @param directoryClient a Directory client
     * @return a list of all the users in the directory
     * @throws IOException
     */
    public static List<User> retrieveAllUsers(Directory directoryClient) throws IOException {
        LOG.debug("retrieveAllUsers()");

        List<User> allUsers = new ArrayList<User>();

        Directory.Users.List request = null;
        try {
            request = directoryClient.users().list().setCustomer("my_customer").setMaxResults(500);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        do { //continue until we have all the pages read in.
            Users currentPage = (Users)execute(request);

            final List<User> users = currentPage.getUsers();
            if (users != null) {
                allUsers.addAll(users);
            }
            request.setPageToken(currentPage.getNextPageToken());

        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return allUsers;
    }

    /**
     *
     * @param directoryClient a Directory (service) object
     * @param userKey an identifier for a user (e-mail address is the most popular)
     * @return the User object returned by Google.
     * @throws IOException
     */
    public static User retrieveUser(Directory directoryClient, String userKey) throws IOException {
        LOG.debug("retrieveUser() - {}", userKey);

        Directory.Users.Get request = null;

        try {
            request = directoryClient.users().get(userKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (User) execute(request);
    }

    /**
     *
     * @param directoryClient a Directory client
     * @return a list of all the groups in the directory
     * @throws IOException
     */
    public static List<Group> retrieveAllGroups(Directory directoryClient) throws IOException {
        LOG.debug("retrieveAllGroups()");

        final List<Group> allGroups = new ArrayList<Group>();

        Directory.Groups.List request = null;
        try {
            request = directoryClient.groups().list().setCustomer("my_customer").setMaxResults(1000000);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        do { //continue until we have all the pages read in.
            final Groups currentPage = (Groups)execute(request);

            final List<Group> groups = currentPage.getGroups();
            if (groups != null) {
                allGroups.addAll(groups);
            }
            request.setPageToken(currentPage.getNextPageToken());

        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return allGroups;
    }

    /**
     * retrieveGroup returns a requested group.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @return the Group object from Google
     * @throws IOException
     */
    public static Group retrieveGroup(Directory directoryClient, String groupKey) throws IOException {
        LOG.debug("retrieveGroup() - {}", groupKey);

        Directory.Groups.Get request = null;

        try {
            request = directoryClient.groups().get(groupKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (Group) execute(request);
    }

    /**
     * retrieveGroup returns a requested group.
     * @param groupssettingClient a Groupssettings client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @return the Groups object from Google
     * @throws IOException
     */
    public static com.google.api.services.groupssettings.model.Groups retrieveGroupSettings(Groupssettings groupssettingClient, String groupKey) throws IOException {
        LOG.debug("retrieveGroupssettings() - {}", groupKey);

        Groupssettings.Groups.Get request = null;

        try {
            request = groupssettingClient.groups().get(groupKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (com.google.api.services.groupssettings.model.Groups) execute(request);
    }

    /**
     * retrieveGroupMember returns a requested group member.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @param userKey an identifier for a group (e-mail address is the most popular)
     * @return the Group object from Google
     * @throws IOException
     */
    public static Member retrieveGroupMember(Directory directoryClient, String groupKey, String userKey) throws IOException {
        LOG.debug("retrieveGroupMember() - {} in {}", userKey, groupKey);

        Directory.Members.Get request = null;

        try {
            request = directoryClient.members().get(groupKey, userKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (Member) execute(request);
    }

    /**
     * addGroup adds a group to Google.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @param userKey an identifier for a user (e-mail address is the most popular)
     * @param member a populated member object
     * @return the new Group object created/returned by Google
     * @throws IOException
     */
    public static Member updateGroupMember(Directory directoryClient, String groupKey, String userKey, Member member) throws IOException {
        LOG.debug("updateGroupMember() - {}", member);

        Directory.Members.Update request = null;

        try {
            request = directoryClient.members().update(groupKey, userKey, member);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (Member) execute(request);
    }

    /**
     * retrieveGroupMembers returns a list of members of a group.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @return a list of Members in the Group
     * @throws IOException
     */
    public static List<Member> retrieveGroupMembers(Directory directoryClient, String groupKey) throws IOException {
        LOG.debug("retrieveGroupMembers() - {}", groupKey);

        final List<Member> groupMembers = new ArrayList<Member>();

        Directory.Members.List request = null;
        try {
            request = directoryClient.members().list(groupKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        do { //continue until we have all the pages read in.
            try {
                final Members currentPage = (Members) execute(request);

                final List<Member> members = currentPage.getMembers();
                if (members != null) {
                    groupMembers.addAll(members);
                }

                request.setPageToken(currentPage.getNextPageToken());
            } catch (NullPointerException ex) {
                break;
            }

        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return groupMembers;
    }

    /**
     * addGroupMember add an additional member to a group.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a group (e-mail address is the most popular)
     * @param member a Member object
     * @return a Member object stored on Google.
     * @throws IOException
     */
    public static Member addGroupMember(Directory directoryClient, String groupKey, Member member) throws IOException {
        LOG.debug("addGroupMember() - add {} to {}", member, groupKey);

        Directory.Members.Insert request = null;

        try {
            request = directoryClient.members().insert(groupKey, member);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        return (Member) execute(request);
    }

    /**
     * removeGroupMember removes a member of a group.
     * @param directoryClient a Directory client
     * @param groupKey an identifier for a user (e-mail address is the most popular)
     * @param memberKey an identifier for a user (e-mail address is the most popular)
     * @throws GoogleJsonResponseException
     */
    public static void removeGroupMember(Directory directoryClient, String groupKey, String memberKey) throws IOException {
        LOG.debug("removeGroupMember() - remove {} from {}", memberKey, groupKey);

        Directory.Members.Delete request = null;

        try {
            request = directoryClient.members().delete(groupKey, memberKey);
        } catch (IOException e) {
            LOG.error("An unknown error occurred: " + e);
        }

        execute(request);
    }

    /**
     * handleGoogleJsonResponseException makes the handling of exponential back-off easy.
     * @param ex the GoogleJsonResponseException being handled
     * @param interval the exponential back-off interval
     * @return true=no record found, false=everything was handled properly
     * @throws GoogleJsonResponseException
     */
    private static boolean handleGoogleJsonResponseException(GoogleJsonResponseException ex, int interval)
            throws GoogleJsonResponseException {

        final GoogleJsonError e = ex.getDetails();

        switch (e.getCode()) {
            case 403:
                if (e.getErrors().get(0).getReason().equals("rateLimitExceeded")
                    || e.getErrors().get(0).getReason().equals("userRateLimitExceeded")) {

                    try {
                        LOG.warn("handleGoogleJsonResponseException() - we've exceeded a rate limit ({}) so taking a nap. (You should see if you can get the rate limit increased by Google.)", e.getErrors().get(0).getReason());
                        Thread.sleep((1 << interval) * 1000 + randomGenerator.nextInt(1001));
                    } catch (InterruptedException ie) {
                        LOG.debug("handleGoogleJsonResponseException() - {}", ie);
                    }
                } else {
                    LOG.info("handleGoogleJsonResponseException() - Unknown 403 error: {}", e);
                }

                break;

            case 404: //Not found
                LOG.warn("handleGoogleJsonResponseException() - Not found: {}", e);
                return true;

            case 503:
                if (e.getErrors().get(0).getReason().equals("backendError")) {
                    try {
                        LOG.warn("handleGoogleJsonResponseException() - service unavailable/backend error so taking a nap.");
                        Thread.sleep((1 << interval) * 1000 + randomGenerator.nextInt(1001));
                    } catch (InterruptedException ie) {
                        LOG.debug("handleGoogleJsonResponseException() - {}", ie);
                    }

                } else {
                    LOG.debug("handleGoogleJsonResponseException() - Unknown 503 error: {}", e);
                }
                break;

            default:
                // Other error, re-throw.
                throw ex;
        }
        return false;
    }

    /**
     * execute takes a DirectoryRequest and calls the execute() method and handles exponential back-off, etc.
     * @param request a populated DirectoryRequest object
     * @return an output Object that should be cast in the calling method
     * @throws IOException
     */
    private static Object execute(DirectoryRequest request) throws IOException {
        return execute(request, 1);
    }

    /**
     * execute takes a DirectoryRequest and calls the execute() method and handles exponential back-off, etc.
     * @param interval the count of attempts that this request has had.
     * @param request a populated DirectoryRequest object
     * @return an output Object that should be cast in the calling method
     * @throws IOException
     */
    private static Object execute(DirectoryRequest request, int interval) throws IOException {
        LOG.trace("execute() - {} request attempt #{}",request.getClass().getName().replace(request.getClass().getPackage().getName(), ""), interval);

        try {
            return request.execute();
        } catch (GoogleJsonResponseException ex) {
            if (interval == 7) {
                LOG.error("execute() - Retried attempt 7 times, failing request");
                throw ex;

            } else {
                if (handleGoogleJsonResponseException(ex, interval)) { //404's return true
                    return null;
                } else {
                    return execute(request, ++interval);
                }
            }
        } catch(IOException e) {
            LOG.error("execute() - An unknown IO error occurred: " + e);

            if (interval == 7) {
                LOG.error("Retried attempt 7 times, failing request");
                throw e;

            } else {
                return execute(request, ++interval);
            }
        }

    }


    /**
     * execute takes a GroupssettingsRequest and calls the execute() method and handles exponential back-off, etc.
     * @param request a populated GroupssettingsRequest object
     * @return an output Object that should be cast in the calling method
     * @throws IOException
     */
    private static Object execute(GroupssettingsRequest request) throws IOException {
        return execute(request, 1);
    }

    /**
     * execute takes a GroupssettingsRequest and calls the execute() method and handles exponential back-off, etc.
     * @param request a populated GroupsettingsRequest object
     * @param interval the count of attempts that this request has had.
     * @return an output Object that should be cast in the calling method
     * @throws IOException
     */
    private static Object execute(GroupssettingsRequest request, int interval) throws IOException {
        LOG.trace("execute() - {} request attempt #{}",request.getClass().getName().replace(request.getClass().getPackage().getName(), ""), interval);

        try {
            return request.execute();
        } catch (GoogleJsonResponseException ex) {
            if (interval == 7) {
                LOG.error("execute() - Retried attempt 7 times, failing request");
                throw ex;

            } else {
                if (handleGoogleJsonResponseException(ex, interval)) { //404's return true
                    return null;
                } else {
                    return execute(request, ++interval);
                }
            }
        } catch(IOException e) {
            LOG.error("execute() - An unknown IO error occurred: " + e);

            if (interval == 7) {
                LOG.error("Retried attempt 7 times, failing request");
                throw e;

            } else {
                return execute(request, ++interval);
            }
        }

    }

}
