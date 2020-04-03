package edu.internet2.middleware.grouper.changeLog.consumer;


import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.Office365AuthApiService;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.Office365GraphApiService;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.GroupsOdata;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.OAuthTokenInfo;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.OdataIdContainer;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.User;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.subject.Subject;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.log4j.Logger;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by jj on 5/30/16.
 */
public class Office365ChangeLogConsumer extends ChangeLogConsumerBaseImpl {
    private static final Logger logger = Logger.getLogger(Office365ChangeLogConsumer.class);
    private static final String CONFIG_PREFIX = "changeLog.consumer.";
    private static final String DEFAULT_ID_ATTRIBUTE = "uid";

    private String token = null;
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final String scope;
    private final String idAttribute;
    private final String domain;

    private final String groupJexl;

    private final Office365GraphApiService service;

    private final GrouperSession grouperSession;

    private final JexlEngine jexlEngine = new JexlEngine();

    public Office365ChangeLogConsumer() {
        // TODO: this.getConsumerName() isn't working for some reason. track down
        String name = this.getConsumerName() != null ? this.getConsumerName() : "o365";
        this.clientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".clientId");
        this.clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".clientSecret");
        this.tenantId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".tenantId");
        this.scope = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".scope", "https://graph.microsoft.com/.default");
        this.idAttribute = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".idAttribute", DEFAULT_ID_ATTRIBUTE);
        this.domain = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".domain", this.tenantId);

        this.groupJexl = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".groupJexl");

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(loggingInterceptor)
                .build();
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl("https://graph.microsoft.com/v1.0/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build();

        this.service = retrofit.create(Office365GraphApiService.class);

        this.grouperSession = GrouperSession.startRootSession();
    }

    private String getToken() throws IOException {
        logger.debug("Token client ID: " + this.clientId);
        logger.debug("Token tenant ID: " + this.tenantId);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://login.microsoftonline.com/" + this.tenantId + "/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        Office365AuthApiService service = retrofit.create(Office365AuthApiService.class);
        retrofit2.Response<OAuthTokenInfo> response = service.getOauth2Token(
                "client_credentials",
                this.clientId,
                this.clientSecret,
                this.scope,
                "https://graph.microsoft.com")
                .execute();
        if (response.isSuccessful()) {
            OAuthTokenInfo info = response.body();
            logger.debug("Token scope: " + info.scope);
            logger.debug("Token expiresIn: " + info.expiresIn);
            logger.debug("Token expiresOn: " + info.expiresOn);
            logger.debug("Token resource: " + info.resource);
            logger.debug("Token tokenType: " + info.tokenType);
            logger.debug("Token notBefore: " + info.notBefore);
            return info.accessToken;
        } else {
            ResponseBody errorBody = response.errorBody();
            throw new IOException("error requesting token (" + response.code() + "): " + errorBody.string());
        }
    }


    /*
    This method invokes a retrofit API call with retry.  If the first call returns 401 (unauthorized)
    the same is retried again after fetching a new token.
     */
    private <T> retrofit2.Response<T> invoke(Call<T> call) throws IOException {
        for (int retryMax = 2; retryMax > 0; retryMax--) {
            if (token == null) {
                token = getToken();
            }
            retrofit2.Response<T> r = call.execute();
            if (r.isSuccessful()) {
                return r;
            } else if (r.code() == 401) {
                logger.debug("auth fail, retry: " + call.request().url());
                // Call objects cannot be reused, so docs say to use clone() to create a new one with the
                // same specs for retry purposes
                call = call.clone();
                // null out existing token so we'll fetch a new one on next loop pass
                token = null;
            } else {
                throw new IOException("Unhandled invoke response (" + r.code() + ") " + r.errorBody().string());
            }
        }
        throw new IOException("Retry failed for: " + call.request().url());
    }

    private String getJexlGroupName(Object group) {
        String finalName;
        if (this.groupJexl == null) {
            if (group instanceof PITGroup) {
                finalName = ((PITGroup) group).getName();
            } else {
                // assume a Group
                finalName = ((Group) group).getName();
            }
        } else {
            Expression expression = this.jexlEngine.createExpression(this.groupJexl);
            MapContext context = new MapContext();
            context.set("group", group);
            finalName = (String)expression.evaluate(context);
        }
        return finalName;
    }

    @Override
    protected void addGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("Creating group " + group);
        try {
            logger.debug("**** ");
            // TODO: currently uses the mailNickname as an ID. need to fix this
            /*
            {
            "id": "faccfbe2-3270-4db6-9d61-cf95feae9faf",
            "createdDateTime": "2016-06-03T01:09:51Z",
            "description": null,
            "displayName": "test",
            "groupTypes": [],
            "mail": null,
            "mailEnabled": false,
            "mailNickname": "test",
            "onPremisesLastSyncDateTime": null,
            "onPremisesSecurityIdentifier": null,
            "onPremisesSyncEnabled": null,
            "proxyAddresses": [],
            "renewedDateTime": "2016-06-03T01:09:51Z",
            "securityEnabled": true,
            "visibility": null
        }
             */
            retrofit2.Response response = invoke(this.service.createGroup(
                    new edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group(
                            null,
                            this.getJexlGroupName(group),
                            false,
                            group.getUuid(),
                            true,
                            new ArrayList<String>(),
                            group.getId()
                    )
            ));

            AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:office365:o365Id", false);
            group.getAttributeDelegate().assignAttribute(attributeDefName);
            group.getAttributeValueDelegate().assignValue("etc:attribute:office365:o365Id", ((edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group) response.body()).id);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    // TODO: find out how to induce and implement (if necessary)
    @Override
    protected void removeGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("removing group " + group);
        String id = group.getAttributeValueDelegate().retrieveValuesString("etc:attribute:office365:o365Id").get(0);
        logger.debug("removing id: " + id);
    }

    @Override
    protected void removeDeletedGroup(PITGroup pitGroup, ChangeLogEntry changeLogEntry) {
        logger.debug("removing group " + pitGroup + ": " + pitGroup.getId());
        try {
            String finalName = this.getJexlGroupName(pitGroup);
            Map options = new TreeMap<>();
            //TODO: fix this
            options.put("$filter", "displayName eq '" + finalName + "'");
            edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group group = ((GroupsOdata) invoke(this.service.getGroups(options)).body()).groups.get(0);
            invoke(this.service.deleteGroup(group.id));
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @Override
    protected void addMembership(Subject subject, Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("adding " + subject + " to " + group);
        logger.debug("attributes: " + subject.getAttributes());

        String groupId = group.getAttributeValueDelegate().retrieveValueString("etc:attribute:office365:o365Id");
        logger.debug("groupId: " + groupId);

        try {
            invoke(this.service.addGroupMember(groupId, new OdataIdContainer("https://graph.microsoft.com/v1.0/users/" + subject.getAttributeValue(this.idAttribute) + "@" + this.domain)));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void removeMembership(Subject subject, Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("removing " + subject + " from " + group);
        try {
            User user = invoke(this.service.getUserByUPN(subject.getAttributeValue(this.idAttribute) + "@" + this.domain)).body();
            String groupId = group.getAttributeValueDelegate().retrieveValueString("etc:attribute:office365:o365Id");
            invoke(this.service.removeGroupMember(groupId, user.id));
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
