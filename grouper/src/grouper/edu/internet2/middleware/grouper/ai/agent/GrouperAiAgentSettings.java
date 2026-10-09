/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.mcp.GrouperMcpGroupMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.subject.Subject;

/**
 * how the agent in the UI is set up, from grouper.properties (grouper.ai.agent.*).  read fresh for
 * each user message, so a config change takes effect on the next message
 */
public class GrouperAiAgentSettings {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAiAgentSettings.class);

  /** if the agent is on */
  private boolean enabled = false;

  /** anthropic or openai */
  private String provider = GrouperAiAnthropicClient.PROVIDER;

  /** the bearer token external system with the endpoint and key */
  private String externalSystemConfigId;

  /**
   * the model, required for every provider.  there is no built in default: models are released and
   * retired every few months, so a default would soon be out of date, and an install which never
   * set one would stay on it until the provider retired it.  it also leaves the cost and quality
   * trade off with the admin, who pays for it
   */
  private String model;

  /** most tokens the model may write in one call */
  private int maxOutputTokens = 16000;

  /** most model calls for one user message, so a confused model cannot loop forever */
  private int maxStepsPerMessage = 10;

  /** a tool result longer than this is cut off before the model sees it */
  private int maxToolResultChars = 20000;

  /** most look-ups run in one step, so what one message can pull in is bounded by config.  changes
   * are not counted */
  private int maxLookupsPerStep = 10;

  /** most seconds to wait, for one model call, for the provider's rate limit to clear */
  private int rateLimitMaxWaitSeconds = 90;

  /** most characters in one message from the user */
  private int maxUserMessageChars = 8000;

  /** how many of the latest exchanges are kept word for word when older ones are summarized */
  private int verbatimExchanges = 6;

  /** how long to wait for the provider */
  private int httpTimeoutMillis = 300000;

  /** most turns running at once on one UI node */
  private int maxConcurrentTurnsPerNode = 10;

  /** most tokens one user may use a day, null for no limit */
  private Long maxTokensPerUserPerDay = null;

  /** most tokens one conversation may use, null for no limit */
  private Long maxTokensPerConversation = null;

  /** most messages one user may send a day, null for no limit */
  private Long maxQuestionsPerUserPerDay = null;

  /**
   * if grouper.ai.agent.maxQuestionsPerUserPerDay was set at all.  it and maxTokensPerConversation
   * are required when the agent is on, so that what a person and a conversation can cost is a
   * decision: blank is not set up, and no limit has to be asked for with -1
   */
  private boolean maxQuestionsPerUserPerDayConfigured = false;

  /** if grouper.ai.agent.maxTokensPerConversation was set at all, see maxQuestionsPerUserPerDayConfigured */
  private boolean maxTokensPerConversationConfigured = false;

  /**
   * @return most messages one user may send a day, null for no limit.  approving or declining
   * changes does not count
   */
  public Long getMaxQuestionsPerUserPerDay() {
    return this.maxQuestionsPerUserPerDay;
  }

  /**
   * @param maxQuestionsPerUserPerDay1 most messages one user may send a day, null for no limit
   */
  public void setMaxQuestionsPerUserPerDay(Long maxQuestionsPerUserPerDay1) {
    this.maxQuestionsPerUserPerDay = maxQuestionsPerUserPerDay1;
    this.maxQuestionsPerUserPerDayConfigured = true;
  }

  /**
   * @return most tokens one conversation may use, counted as in {@link GrouperAiAgentUsage}, null
   * for no limit
   */
  public Long getMaxTokensPerConversation() {
    return this.maxTokensPerConversation;
  }

  /**
   * @param maxTokensPerConversation1 most tokens one conversation may use, null for no limit
   */
  public void setMaxTokensPerConversation(Long maxTokensPerConversation1) {
    this.maxTokensPerConversation = maxTokensPerConversation1;
    this.maxTokensPerConversationConfigured = true;
  }

  /** the config property naming the group whose members may use the agent */
  public static final String CONFIG_USERS_GROUP = "grouper.ai.agent.users";

  /** if Anthropic should rerun a declined request on a fallback model */
  private boolean anthropicServerSideFallback = true;

  /**
   * if OpenAI requests ask for reasoning items to come back with their encrypted content, which a
   * reasoning model needs to carry on after a tool call.  off for a model without reasoning, which
   * may refuse the request
   */
  private boolean openAiIncludeReasoningContent = true;

  /**
   * if the provider endpoint may be plain http.  for development only: every call carries the API
   * key and the conversation, which holds Grouper data
   */
  private boolean allowInsecureEndpoint = false;

  /** the system prompt, blank for the built in one */
  private String systemPrompt;

  /**
   * read the settings from grouper.properties
   * @return the settings
   */
  public static GrouperAiAgentSettings fromConfig() {

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperAiAgentSettings settings = new GrouperAiAgentSettings();

    settings.enabled = grouperConfig.propertyValueBoolean("grouper.ai.agent.enabled", false);
    settings.provider = StringUtils.defaultIfBlank(
        grouperConfig.propertyValueString("grouper.ai.agent.provider"), GrouperAiAnthropicClient.PROVIDER);
    settings.externalSystemConfigId = grouperConfig.propertyValueString("grouper.ai.agent.externalSystemConfigId");

    // required, see createLlmClient
    settings.model = StringUtils.trimToNull(grouperConfig.propertyValueString("grouper.ai.agent.model"));

    settings.maxOutputTokens = grouperConfig.propertyValueInt("grouper.ai.agent.maxOutputTokens", 16000);
    settings.maxStepsPerMessage = grouperConfig.propertyValueInt("grouper.ai.agent.maxStepsPerMessage", 10);
    settings.maxToolResultChars = grouperConfig.propertyValueInt("grouper.ai.agent.maxToolResultChars", 20000);
    settings.maxLookupsPerStep = grouperConfig.propertyValueInt("grouper.ai.agent.maxLookupsPerStep", 10);
    settings.rateLimitMaxWaitSeconds = grouperConfig.propertyValueInt("grouper.ai.agent.rateLimitMaxWaitSeconds", 90);
    settings.maxUserMessageChars = grouperConfig.propertyValueInt("grouper.ai.agent.maxUserMessageChars", 8000);
    settings.verbatimExchanges = grouperConfig.propertyValueInt("grouper.ai.agent.verbatimExchanges", 6);
    settings.httpTimeoutMillis = grouperConfig.propertyValueInt("grouper.ai.agent.httpTimeoutMillis", 300000);
    settings.maxConcurrentTurnsPerNode = grouperConfig.propertyValueInt("grouper.ai.agent.maxConcurrentTurnsPerNode", 10);
    settings.maxTokensPerUserPerDay = limit(grouperConfig, "grouper.ai.agent.maxTokensPerUserPerDay");
    settings.maxTokensPerConversation = limit(grouperConfig, "grouper.ai.agent.maxTokensPerConversation");
    settings.maxTokensPerConversationConfigured = !StringUtils.isBlank(
        grouperConfig.propertyValueString("grouper.ai.agent.maxTokensPerConversation"));
    settings.maxQuestionsPerUserPerDay = limit(grouperConfig, "grouper.ai.agent.maxQuestionsPerUserPerDay");
    settings.maxQuestionsPerUserPerDayConfigured = !StringUtils.isBlank(
        grouperConfig.propertyValueString("grouper.ai.agent.maxQuestionsPerUserPerDay"));
    settings.anthropicServerSideFallback = grouperConfig.propertyValueBoolean(
        "grouper.ai.agent.anthropic.serverSideFallback", true);
    settings.allowInsecureEndpoint = grouperConfig.propertyValueBoolean(
        "grouper.ai.agent.allowInsecureEndpoint", false);
    settings.openAiIncludeReasoningContent = grouperConfig.propertyValueBoolean(
        "grouper.ai.agent.openai.includeReasoningContent", true);
    settings.systemPrompt = grouperConfig.propertyValueString("grouper.ai.agent.systemPrompt");

    return settings;
  }

  /** config ids of the group limit overrides, grouper.ai.agent.limitOverride.configId.groupName */
  public static final Pattern LIMIT_OVERRIDE_PATTERN =
      Pattern.compile("^grouper\\.ai\\.agent\\.limitOverride\\.([^.]+)\\.groupName$");

  /**
   * raise the limits for a user from the overrides for groups they are in
   * (grouper.ai.agent.limitOverride.configId.*).  for each limit the highest wins, and an override
   * can only raise a limit, never lower it.  membership is checked against Grouper, cached for a
   * minute on each node
   * @param subject the user
   * @return this, for chaining
   */
  public GrouperAiAgentSettings applyLimitOverrides(Subject subject) {

    if (subject == null) {
      return this;
    }

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);

    for (String configId : GrouperUtil.nonNull(grouperConfig.propertyConfigIds(LIMIT_OVERRIDE_PATTERN))) {

      String prefix = "grouper.ai.agent.limitOverride." + configId + ".";

      // blank group names are refused by the membership check, which fails closed
      if (!GrouperMcpGroupMembership.isSubjectInGroup(authUser, prefix + "groupName")) {
        continue;
      }

      this.maxQuestionsPerUserPerDay = raiseLimit(this.maxQuestionsPerUserPerDay,
          grouperConfig.propertyValueString(prefix + "maxQuestionsPerUserPerDay"), prefix + "maxQuestionsPerUserPerDay");
      this.maxTokensPerUserPerDay = raiseLimit(this.maxTokensPerUserPerDay,
          grouperConfig.propertyValueString(prefix + "maxTokensPerUserPerDay"), prefix + "maxTokensPerUserPerDay");
      this.maxTokensPerConversation = raiseLimit(this.maxTokensPerConversation,
          grouperConfig.propertyValueString(prefix + "maxTokensPerConversation"), prefix + "maxTokensPerConversation");
    }

    return this;
  }

  /**
   * apply one group override to a limit
   * @param current the limit so far, null for no limit
   * @param overrideValue the override: blank leaves the limit as it is, negative removes it, a
   * number raises it to that if higher.  a value which is not a number is logged and ignored, so a
   * typo can never raise a limit
   * @param key the property, for the log
   * @return the new limit, null for no limit
   */
  static Long raiseLimit(Long current, String overrideValue, String key) {
    if (StringUtils.isBlank(overrideValue)) {
      return current;
    }
    long overrideLimit = 0;
    try {
      overrideLimit = Long.parseLong(overrideValue.trim());
    } catch (NumberFormatException nfe) {
      LOG.error(key + " is not a number: '" + overrideValue + "'.  Ignoring it");
      return current;
    }
    // no limit already, or this group removes it
    if (current == null || overrideLimit < 0) {
      return null;
    }
    return Math.max(current, overrideLimit);
  }

  /**
   * read a usage limit.  blank or negative is no limit, the same as -1 for other Grouper limits.  a
   * value which is not a number is a limit of 0, so a typo stops use until it is fixed rather than
   * silently removing the limit
   * @param grouperConfig the config
   * @param key the property
   * @return the limit, or null for no limit
   */
  private static Long limit(GrouperConfig grouperConfig, String key) {
    String value = grouperConfig.propertyValueString(key);
    if (StringUtils.isBlank(value)) {
      return null;
    }
    long limit = 0;
    try {
      limit = Long.parseLong(value.trim());
    } catch (NumberFormatException nfe) {
      LOG.error(key + " is not a number: '" + value
          + "'.  Treating it as 0, so the AI agent cannot be used until it is fixed");
    }
    return limit < 0 ? null : Long.valueOf(limit);
  }

  /**
   * the client for the configured provider
   * @return the client
   * @throws RuntimeException if the provider, external system or model is not set up
   */
  public GrouperAiLlmClient createLlmClient() {

    if (StringUtils.isBlank(this.externalSystemConfigId)) {
      throw new RuntimeException("grouper.ai.agent.externalSystemConfigId is required");
    }
    // a typo or a renamed external system would otherwise only show on the first call, as a general
    // failure users would retry, rather than as the assistant not being set up
    if (StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.wsBearerToken." + this.externalSystemConfigId + ".endpoint"))) {
      throw new RuntimeException("grouper.ai.agent.externalSystemConfigId is '" + this.externalSystemConfigId
          + "', but there is no web service bearer token external system with that id: "
          + "grouper.wsBearerToken." + this.externalSystemConfigId + ".endpoint is not set in grouper-loader.properties");
    }
    // every call carries the API key and the conversation, which holds Grouper data
    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.wsBearerToken." + this.externalSystemConfigId + ".endpoint");
    if (!StringUtils.startsWithIgnoreCase(StringUtils.trim(endpoint), "https://") && !this.allowInsecureEndpoint) {
      throw new RuntimeException("grouper.wsBearerToken." + this.externalSystemConfigId + ".endpoint must be "
          + "https for the AI agent: every call carries the API key and Grouper data.  "
          + "grouper.ai.agent.allowInsecureEndpoint = true allows http, for development only");
    }
    if (StringUtils.isBlank(this.model)) {
      throw new RuntimeException("grouper.ai.agent.model is required: the provider's id for the model to "
          + "use, e.g. claude-sonnet-5 for anthropic.  See the provider's list of current models");
    }
    // what one person and one conversation can cost must be decided, not left to a default: the
    // design's cost bound only holds if conversations are capped.  -1 is no limit, chosen on purpose
    if (!this.maxQuestionsPerUserPerDayConfigured) {
      throw new RuntimeException("grouper.ai.agent.maxQuestionsPerUserPerDay is required when the AI agent is on: "
          + "the most messages one person may send a day, or -1 for no limit");
    }
    if (!this.maxTokensPerConversationConfigured) {
      throw new RuntimeException("grouper.ai.agent.maxTokensPerConversation is required when the AI agent is on: "
          + "the most tokens one conversation may use, or -1 for no limit");
    }

    if (StringUtils.equals(GrouperAiAnthropicClient.PROVIDER, this.provider)) {
      return new GrouperAiAnthropicClient(this.externalSystemConfigId, this.anthropicServerSideFallback,
          this.httpTimeoutMillis);
    }
    if (StringUtils.equals(GrouperAiOpenAiClient.PROVIDER, this.provider)) {
      return new GrouperAiOpenAiClient(this.externalSystemConfigId, this.openAiIncludeReasoningContent,
          this.httpTimeoutMillis);
    }
    throw new RuntimeException("grouper.ai.agent.provider must be '" + GrouperAiAnthropicClient.PROVIDER
        + "' or '" + GrouperAiOpenAiClient.PROVIDER + "': '" + this.provider + "'");
  }

  /**
   * @return if the agent is on
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * @param enabled1 if the agent is on
   */
  public void setEnabled(boolean enabled1) {
    this.enabled = enabled1;
  }

  /**
   * @return anthropic or openai
   */
  public String getProvider() {
    return this.provider;
  }

  /**
   * @param provider1 anthropic or openai
   */
  public void setProvider(String provider1) {
    this.provider = provider1;
  }

  /**
   * @return the bearer token external system with the endpoint and key
   */
  public String getExternalSystemConfigId() {
    return this.externalSystemConfigId;
  }

  /**
   * @param externalSystemConfigId1 the bearer token external system with the endpoint and key
   */
  public void setExternalSystemConfigId(String externalSystemConfigId1) {
    this.externalSystemConfigId = externalSystemConfigId1;
  }

  /**
   * @return the model
   */
  public String getModel() {
    return this.model;
  }

  /**
   * @param model1 the model
   */
  public void setModel(String model1) {
    this.model = model1;
  }

  /**
   * @return most tokens the model may write in one call
   */
  public int getMaxOutputTokens() {
    return this.maxOutputTokens;
  }

  /**
   * @param maxOutputTokens1 most tokens the model may write in one call
   */
  public void setMaxOutputTokens(int maxOutputTokens1) {
    this.maxOutputTokens = maxOutputTokens1;
  }

  /**
   * @return most model calls for one user message
   */
  public int getMaxStepsPerMessage() {
    return this.maxStepsPerMessage;
  }

  /**
   * @param maxStepsPerMessage1 most model calls for one user message
   */
  public void setMaxStepsPerMessage(int maxStepsPerMessage1) {
    this.maxStepsPerMessage = maxStepsPerMessage1;
  }

  /**
   * @return a tool result longer than this is cut off
   */
  public int getMaxToolResultChars() {
    return this.maxToolResultChars;
  }

  /**
   * @param maxToolResultChars1 a tool result longer than this is cut off
   */
  public void setMaxToolResultChars(int maxToolResultChars1) {
    this.maxToolResultChars = maxToolResultChars1;
  }

  /**
   * @return most look-ups run in one step, 0 or less for no limit.  changes are not counted
   */
  public int getMaxLookupsPerStep() {
    return this.maxLookupsPerStep;
  }

  /**
   * @param maxLookupsPerStep1 most look-ups run in one step, 0 or less for no limit
   */
  public void setMaxLookupsPerStep(int maxLookupsPerStep1) {
    this.maxLookupsPerStep = maxLookupsPerStep1;
  }

  /**
   * @return most seconds to wait, for one model call, for the provider's rate limit to clear.  0
   * or less means do not wait
   */
  public int getRateLimitMaxWaitSeconds() {
    return this.rateLimitMaxWaitSeconds;
  }

  /**
   * @param rateLimitMaxWaitSeconds1 most seconds to wait, for one model call, for the provider's
   * rate limit to clear.  0 or less means do not wait
   */
  public void setRateLimitMaxWaitSeconds(int rateLimitMaxWaitSeconds1) {
    this.rateLimitMaxWaitSeconds = rateLimitMaxWaitSeconds1;
  }

  /**
   * @return most characters in one message from the user
   */
  public int getMaxUserMessageChars() {
    return this.maxUserMessageChars;
  }

  /**
   * @param maxUserMessageChars1 most characters in one message from the user
   */
  public void setMaxUserMessageChars(int maxUserMessageChars1) {
    this.maxUserMessageChars = maxUserMessageChars1;
  }

  /**
   * @return how many of the latest exchanges are kept word for word
   */
  public int getVerbatimExchanges() {
    return this.verbatimExchanges;
  }

  /**
   * @param verbatimExchanges1 how many of the latest exchanges are kept word for word
   */
  public void setVerbatimExchanges(int verbatimExchanges1) {
    this.verbatimExchanges = verbatimExchanges1;
  }

  /**
   * @return how long to wait for the provider
   */
  public int getHttpTimeoutMillis() {
    return this.httpTimeoutMillis;
  }

  /**
   * @param httpTimeoutMillis1 how long to wait for the provider
   */
  public void setHttpTimeoutMillis(int httpTimeoutMillis1) {
    this.httpTimeoutMillis = httpTimeoutMillis1;
  }

  /**
   * @return most tokens one user may use a day, counted as in {@link GrouperAiAgentUsage}, null for
   * no limit
   */
  public Long getMaxTokensPerUserPerDay() {
    return this.maxTokensPerUserPerDay;
  }

  /**
   * @param maxTokensPerUserPerDay1 most tokens one user may use a day, null for no limit
   */
  public void setMaxTokensPerUserPerDay(Long maxTokensPerUserPerDay1) {
    this.maxTokensPerUserPerDay = maxTokensPerUserPerDay1;
  }

  /**
   * @return most turns running at once on one UI node
   */
  public int getMaxConcurrentTurnsPerNode() {
    return this.maxConcurrentTurnsPerNode;
  }

  /**
   * @param maxConcurrentTurnsPerNode1 most turns running at once on one UI node
   */
  public void setMaxConcurrentTurnsPerNode(int maxConcurrentTurnsPerNode1) {
    this.maxConcurrentTurnsPerNode = maxConcurrentTurnsPerNode1;
  }

  /**
   * @return if Anthropic should rerun a declined request on a fallback model
   */
  public boolean isAnthropicServerSideFallback() {
    return this.anthropicServerSideFallback;
  }

  /**
   * @param anthropicServerSideFallback1 if Anthropic should rerun a declined request on a fallback model
   */
  public void setAnthropicServerSideFallback(boolean anthropicServerSideFallback1) {
    this.anthropicServerSideFallback = anthropicServerSideFallback1;
  }

  /**
   * @return if OpenAI requests ask for reasoning items to come back with their encrypted content
   */
  public boolean isOpenAiIncludeReasoningContent() {
    return this.openAiIncludeReasoningContent;
  }

  /**
   * @param openAiIncludeReasoningContent1 if OpenAI requests ask for reasoning items to come back
   * with their encrypted content
   */
  public void setOpenAiIncludeReasoningContent(boolean openAiIncludeReasoningContent1) {
    this.openAiIncludeReasoningContent = openAiIncludeReasoningContent1;
  }

  /**
   * @return the system prompt, blank for the built in one
   */
  public String getSystemPrompt() {
    return this.systemPrompt;
  }

  /**
   * @param systemPrompt1 the system prompt, blank for the built in one
   */
  public void setSystemPrompt(String systemPrompt1) {
    this.systemPrompt = systemPrompt1;
  }

}
