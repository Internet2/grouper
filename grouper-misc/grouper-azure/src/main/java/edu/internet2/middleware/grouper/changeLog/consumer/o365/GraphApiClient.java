package edu.internet2.middleware.grouper.changeLog.consumer.o365;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.changeLog.consumer.Office365ChangeLogConsumer;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.*;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group.Visibility;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.log4j.Logger;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.io.IOException;
import java.util.*;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class GraphApiClient {
    private static final Logger logger = Logger.getLogger(GraphApiClient.class);
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final String scope;
    private final Office365GraphApiService service;
    String token = null;
    private final OkHttpClient graphApiHttpClient;
    private final OkHttpClient graphTokenHttpClient;
    private final Office365ChangeLogConsumer.AzureGroupType azureGroupType;
    private final Visibility visibility;

    public GraphApiClient(String clientId, String clientSecret, String tenantId, String scope,
                          Office365ChangeLogConsumer.AzureGroupType azureGroupType,
                          Visibility visibility,
                          String proxyType, String proxyHost, Integer proxyPort) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
        this.scope = scope;
        this.azureGroupType = azureGroupType;
        this.visibility = visibility;

        final Proxy proxy;

        if (proxyType == null) {
            proxy = null; // probably works too: Proxy.NO_PROXY
        } else if ("http".equals(proxyType)) {
            proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxyHost, proxyPort));
        } else if ("socks".equals(proxyType)) {
            proxy = new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(proxyHost, proxyPort));
        } else {
            logger.warn("Unable to determine proxy type from '" + proxyType + "'; Valid proxy types for this consumer are 'http' or 'socks'");
            proxy = null;
        }

        graphTokenHttpClient = buildBaseOkHttpClient(proxy);
        graphApiHttpClient = buildGraphOkHttpClient(graphTokenHttpClient);

        RetrofitWrapper retrofit = buildRetrofit(graphApiHttpClient);

        this.service = retrofit.create(Office365GraphApiService.class);
    }

    protected RetrofitWrapper buildRetrofit(OkHttpClient okHttpClient) {
        return new RetrofitWrapper((new Retrofit
                .Builder()
                .baseUrl("https://graph.microsoft.com/v1.0/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()));
    }

    protected RetrofitWrapper buildRetrofitAuth(OkHttpClient okHttpClient) {
        return new RetrofitWrapper((new Retrofit
                .Builder()
                .baseUrl("https://login.microsoftonline.com/" + this.tenantId + "/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()));
    }

    protected OkHttpClient buildBaseOkHttpClient(Proxy proxy) {
        logger.trace("Building OkHttpClient: proxy=" + proxy);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (proxy != null) {
            builder.proxy(proxy);
        }

        return builder.build();
    }

    /*
     * customize a shared OkHttpClient instance, which will share the same connection pool, thread pools, and
     * configuration as the parent
     */
    protected OkHttpClient buildGraphOkHttpClient(OkHttpClient okHttpClient) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor((msg) -> {
            logger.debug(msg);
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // strips out the Bearer token and replaces with U+2588 (a black square)
        loggingInterceptor.redactHeader("Authorization");

        return okHttpClient.newBuilder()
                .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
                    return chain.proceed(request);
                }
            })
                .addInterceptor(loggingInterceptor)
                .build();
    }


    public String getToken() throws IOException {
        logger.debug("Token client ID: " + this.clientId);
        logger.debug("Token tenant ID: " + this.tenantId);
        RetrofitWrapper retrofit = buildRetrofitAuth(this.graphTokenHttpClient);
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
            logTokenInfo(info);
            return info.accessToken;
        } else {
            ResponseBody errorBody = response.errorBody();
            throw new IOException("error requesting token (" + response.code() + "): " + errorBody.string());
        }
    }

    private void logTokenInfo(OAuthTokenInfo info) {
        logger.trace("Token scope: " + info.scope);
        logger.trace("Token expiresIn: " + info.expiresIn);
        logger.trace("Token expiresOn: " + info.expiresOn);
        logger.trace("Token resource: " + info.resource);
        logger.trace("Token tokenType: " + info.tokenType);
        logger.trace("Token notBefore: " + info.notBefore);
    }

    /*
     * This method invokes a retrofit API call with retry.  If the first call returns 401 (unauthorized)
     * the same is retried again after fetching a new token.
    */
    private <T> retrofit2.Response<T> invoke(retrofit2.Call<T> call) throws IOException {
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

    public retrofit2.Response addGroup(String displayName, String mailNickname, String description) {
        logger.debug("Creating group " + displayName + ", group type: " + this.azureGroupType.name());
        boolean securityEnabled;
        Collection<String> groupTypes = new ArrayList<>();

        switch (this.azureGroupType) {
            case Security:
                securityEnabled = true;
                break;
            case Unified:
                groupTypes.add("Unified");
                securityEnabled = false;
                break;
            case MailEnabled:
            case MailEnabledSecurity:
                throw new UnableToPerformException("Mail enabled Azure groups are currently not supported");
            default:
                throw new IllegalStateException("Unexpected value: " + this.azureGroupType);
        }
        try {
            return invoke(this.service.createGroup(
                    new edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group(
                            null,
                            displayName,
                            false,
                            mailNickname,
                            securityEnabled,
                            groupTypes,
                            description,
                            visibility
                    )
            ));

        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException("service.createGroup failed", e);
        }
    }

public void removeGroup(String groupId) {
        try {
            invoke(this.service.deleteGroup(groupId));
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException("service.deleteGroup failed", e);
        }
    }

    /* In Kansas State's version, this was used to look up users from multiple domains */
    public User lookupMSUser(String userPrincipalName) {
        User user = null;
        logger.debug("calling getUserFrom Office365ApiClient");
        try {
            user = invoke(this.service.getUserByUPN(userPrincipalName)).body();
            logger.debug("user = " + (user == null ? "null" : user.toString()));
            return user;
        } catch (IOException e) {
            logger.debug("user wasn't found on default domain of " + tenantId);
        }
        return null;
    }

    protected String lookupOffice365GroupId(Group group) {
        return group.getAttributeValueDelegate().retrieveValueString(Office365ChangeLogConsumer.GROUP_ID_ATTRIBUTE_NAME);
    }

    public void addMemberToMS(String groupId, String userPrincipalName) {
        try {
            invoke(this.service.addGroupMember(groupId, new OdataIdContainer("https://graph.microsoft.com/v1.0/users/" + userPrincipalName)));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (MemberAddAlreadyExistsException me) {
            logger.debug("member already exists for subject:" + userPrincipalName + " and group:" + groupId);
        }
    }

    public void removeMembership(String userPrincipalName, Group group) {
        try {
            if (group != null) {
                User user = lookupMSUser(userPrincipalName);
                if (user == null) {
                    throw new RuntimeException("Failed to locate member: " + userPrincipalName);
                }
                String groupId = lookupOffice365GroupId(group);
                if (ifUserAndGroupExistInMS(user, groupId)) {
                    removeUserFromGroupInMS(user.id, groupId);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (MemberDeleteAlreadyDeletedException me) {
            logger.debug("member already deleted for subject:" + userPrincipalName + " and group:" + group.getId());
        }
    }

    protected boolean ifUserAndGroupExistInMS(User user, String groupId) {
        return user != null && groupId != null;
    }

    public void removeUserFromGroupInMS(String groupId, String userId) throws IOException {
        invoke(this.service.removeGroupMember(groupId, userId));
    }
}
