package edu.internet2.middleware.grouper.app.ccure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mchange.v2.collection.MapEntry;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureUser;
import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureGroup;
import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureMembership;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CCureApiCommands {

    private static final Log LOG = GrouperUtil.getLog(CCureApiCommands.class);

    private static int INFINITELOOP_MAX = 10000000;
    private static Map<String, CCureExternalSystem> externalSystems = new HashMap<>();

    public static CCureExternalSystem retrieveExternalSystem(String configId) {
        if (externalSystems.containsKey(configId)) {
            return externalSystems.get(configId);
        } else {
            CCureExternalSystem externalSystem = new CCureExternalSystem();
            externalSystem.setConfigId(configId);

            Map<String, Object> debugMap = new LinkedHashMap<>();

            debugMap.put("method", "retrieveExternalSystem");

            try {
                externalSystem.authenticate();
                externalSystems.put(configId, externalSystem);
                return externalSystem;
            } catch (RuntimeException re) {
                debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
                try {
                    externalSystem.logout();
                } catch (Exception e) {
                    // no error, just be nice and clean up
                    throw re;
                }
            }
        }
        return null;
    }

    public static List<CCureGroup> retrieveGroups(CCureExternalSystem externalSystem) {
        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveGroups");

        long startTime = System.nanoTime();

        List<CCureGroup> result = new ArrayList<>();

        try {
            String urlSuffix = "/api/Objects/GetAll/Clearance";

            int[] returnCode = new int[]{-1};

            JsonNode jsonNode = executeMethod(debugMap, "GET", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200), returnCode, null, null, "application/json");
            if (jsonNode != null && jsonNode.isArray()) {
                for (JsonNode groupNode : jsonNode) {
                    CCureGroup group = CCureGroup.fromJson(groupNode);
                    result.add(group);
                    // sync back: capture the raw node so fields the CCureGroup record does not model
                    // are still available. GetAll returns whole objects, so no widening is needed here.
                    CCureProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(groupNode);
                }
            } else {
                throw new RuntimeException("Did not receive success for result field instead received: " + jsonNode);
            }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }

        return result;
    }

    public static CCureGroup retrieveGroupByName(CCureExternalSystem externalSystem, String searchTerm) {
        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveGroupByName");

        long startTime = System.nanoTime();

        try {
            String urlSuffix = "/api/Objects/GetAllWithCriteria";

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.Clearance");
            bodyNode.put("WhereClause", "Name = '" + searchTerm + "'");
            bodyNode.put("pagesize", 1);
            bodyNode.put("pagenumber", 1);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("GUID");
            displayFields.add("Name");
            displayFields.add("PartitionID");
            // sync back: GetAllWithCriteria only returns the projected fields, so any operator
            // configured native attribute has to be asked for here or it comes back missing
            CCureProvisioningTargetNativeSync.widenDisplayPropertiesForGroupsFromCurrentProvisioner(displayFields);
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200), returnCode, bodyNode.toString(), null, "application/json");

            if (jsonNode != null && jsonNode.isArray()) {
              // Retrieve the first user object from the array
              CCureProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(jsonNode.get(0));
              return CCureGroup.fromJson(jsonNode.get(0));
            } else {
              throw new RuntimeException("Could not retrieve CCureGroup from returned json");
            }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }
    }

    public static CCureGroup retrieveGroupByObjectId(CCureExternalSystem externalSystem, String objectId) {

        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveGroupByObjectId");

        long startTime = System.nanoTime();

        try {
            String urlSuffix = "/api/Objects/Get/Clearance/" + objectId;

            int[] returnCode = new int[]{-1};

            JsonNode jsonNode = executeMethod(debugMap, "GET", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200), returnCode, null, null, "application/json");

            if (jsonNode == null) {
                return null;
            }
            if (!jsonNode.isArray()) {
                CCureProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(jsonNode);
                return CCureGroup.fromJson(jsonNode);
            }
            if (jsonNode.size() == 1) {
                CCureProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(jsonNode.get(0));
                return CCureGroup.fromJson(jsonNode.get(0));
            }
            LOG.error("retrieveGroupByObjectId: expected 1 result for objectId=" + objectId + " but got " + jsonNode.size());
            return null;
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }
    }

    public static List<CCureUser> retrieveUsers(CCureExternalSystem externalSystem) {

        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveEntities");

        long startTime = System.nanoTime();

        List<CCureUser> result = new ArrayList<>();

        try {
          Integer nextPageNumber = 1;
          int pageSize = externalSystem.getPersonnelPageSize();

          String urlSuffix = "/api/Objects/GetAllWithCriteria";

          int maxCalls = Math.max(INFINITELOOP_MAX/pageSize, 1);
          int numberOfCalls = 0;

          while (nextPageNumber > 0) {
            if (maxCalls-- < 0) {
              throw new RuntimeException("Endless loop detected! total results so far: " + result.size()
                      + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
            }

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel");
            //bodyNode.put("WhereClause", "?");
            bodyNode.put("pagesize", pageSize);
            bodyNode.put("pagenumber", nextPageNumber);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("GUID");
            displayFields.add("Name");
            displayFields.add("Int1");
            // sync back: GetAllWithCriteria only returns the projected fields (see retrieveGroupByName)
            CCureProvisioningTargetNativeSync.widenDisplayPropertiesForEntitiesFromCurrentProvisioner(displayFields);
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            ++nextPageNumber;
            ++numberOfCalls;
            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200, 404), returnCode, bodyNode.toString(), null, "application/json");

            if (returnCode[0] == 404) {
              ; // there are no values, so return an empty list
              nextPageNumber = -1;
            } else if (jsonNode != null && jsonNode.isArray()) {
              boolean hasData = false;
              for (JsonNode userNode : jsonNode) {
                CCureUser user = CCureUser.fromJson(userNode);
                result.add(user);
                hasData = true;
                // sync back: capture the raw node, which may carry more than the CCureUser record models
                CCureProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(userNode);
              }
              // live progress: pages over many slow WS calls with no total available, so report
              // count-so-far and page number.  Uses the thread-scoped current provisioner; null off a run.
              GrouperProvisioner currentProvisionerForUsers = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
              if (currentProvisionerForUsers != null) {
                currentProvisionerForUsers.assignProgressLabelTarget("retrieving users from target: " + result.size()
                        + " so far (page " + numberOfCalls + ")");
              }

              if (!hasData) {
                nextPageNumber = -1;
              }
            } else {
              throw new RuntimeException("Did not receive success for result field instead received: " + jsonNode);
            }
          }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }

        return result;
    }

    public static CCureUser retrieveEntityByObjectId(CCureExternalSystem externalSystem, String objectId) {

        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveEntityByObjectId");

        long startTime = System.nanoTime();

        try {
            String urlSuffix = "/api/Objects/Get/Personnel/" + objectId;

            int[] returnCode = new int[]{-1};

            JsonNode jsonNode = executeMethod(debugMap, "GET", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200), returnCode, null, null, "application/json");

            if (jsonNode == null) {
                return null;
            }
            if (!jsonNode.isArray()) {
                CCureProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(jsonNode);
                return CCureUser.fromJson(jsonNode);
            }
            if (jsonNode.size() == 1) {
                CCureProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(jsonNode.get(0));
                return CCureUser.fromJson(jsonNode.get(0));
            }
            LOG.error("retrieveEntityByObjectId: expected 1 result for objectId=" + objectId + " but got " + jsonNode.size());
            return null;
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }
    }

    public static CCureUser retrieveEntityByMatchField(CCureExternalSystem externalSystem, String matchField, String searchTerm) {

        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveEntityByMatchField");

        long startTime = System.nanoTime();

        try {
            String urlSuffix = "/api/Objects/GetAllWithCriteria";

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel");
            bodyNode.put("WhereClause", matchField + " = " + searchTerm);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("GUID");
            displayFields.add("Name");
            displayFields.add("Int1");
            // sync back: GetAllWithCriteria only returns the projected fields (see retrieveGroupByName)
            CCureProvisioningTargetNativeSync.widenDisplayPropertiesForEntitiesFromCurrentProvisioner(displayFields);
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200), returnCode, bodyNode.toString(), null, "application/json");

            if (jsonNode != null && jsonNode.isArray()) {
                // Retrieve the first user object from the array
              CCureProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(jsonNode.get(0));
              return CCureUser.fromJson(jsonNode.get(0));
            } else {
                throw new RuntimeException("Could not retrieve CCUser from returned json");
            }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }
    }

    public static List<CCureMembership> retrieveMemberships(CCureExternalSystem externalSystem) {
        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveMemberships");

        long startTime = System.nanoTime();

        List<CCureMembership> result = new ArrayList<>();

        try {
          Integer nextPageNumber = 1;
          int pageSize = externalSystem.getClearancePairPageSize();

          String urlSuffix = "/api/Objects/GetAllWithCriteria";

          int maxCalls = Math.max(INFINITELOOP_MAX/pageSize, 1);
          int numberOfCalls = 0;

          while (nextPageNumber > 0) {
            if (maxCalls-- < 0) {
              throw new RuntimeException("Endless loop detected! total results so far: " + result.size()
                      + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
            }

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair");
            //bodyNode.put("WhereClause", "?");
            bodyNode.put("pagesize", pageSize);
            bodyNode.put("pagenumber", nextPageNumber);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("PersonnelID");
            displayFields.add("ClearanceID");
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            ++nextPageNumber;
            ++numberOfCalls;
            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200, 404), returnCode, bodyNode.toString(), null, "application/json");

            if (returnCode[0] == 404) {
              ; // there are no values, so return an empty list
              nextPageNumber = -1;
            } else if (jsonNode != null && jsonNode.isArray()) {
              boolean hasData = false;
              for (JsonNode mshipNode : jsonNode) {
                CCureMembership mship = CCureMembership.fromJson(mshipNode);
                result.add(mship);
                hasData = true;
                // sync back: the pair's ClearanceID/PersonnelID are already the native group/user ids
                CCureProvisioningTargetNativeSync.captureMembershipFromCurrentProvisioner(mship);
              }
              // live progress: pages over many slow WS calls with no total available, so report
              // count-so-far and page number.  Uses the thread-scoped current provisioner; null off a run.
              GrouperProvisioner currentProvisionerForMemberships = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
              if (currentProvisionerForMemberships != null) {
                currentProvisionerForMemberships.assignProgressLabelTarget("retrieving memberships from target: " + result.size()
                        + " so far (page " + numberOfCalls + ")");
              }

              if (!hasData) {
                nextPageNumber = -1;
              }
            } else {
              throw new RuntimeException("Did not receive success for result field instead received: " + jsonNode);
            }
          }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }

        return result;
    }


    public static List<CCureMembership> retrieveMembershipsForUser(CCureExternalSystem externalSystem, String targetEntityId) {
        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveMembershipsForUser");

        long startTime = System.nanoTime();

        List<CCureMembership> result = new ArrayList<>();

        try {
          Integer nextPageNumber = 1;
          int pageSize = externalSystem.getClearancePairPageSize();

          String urlSuffix = "/api/Objects/GetAllWithCriteria";

          int maxCalls = Math.max(INFINITELOOP_MAX/pageSize, 1);
          int numberOfCalls = 0;

          while (nextPageNumber > 0) {
            if (maxCalls-- < 0) {
              throw new RuntimeException("Endless loop detected! total results so far: " + result.size()
                      + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
            }

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair");
            bodyNode.put("WhereClause", "PersonnelID = " + targetEntityId);
            bodyNode.put("pagesize", pageSize);
            bodyNode.put("pagenumber", nextPageNumber);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("PersonnelID");
            displayFields.add("ClearanceID");
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            ++nextPageNumber;
            ++numberOfCalls;
            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200, 404), returnCode, bodyNode.toString(), null, "application/json");

            if (returnCode[0] == 404) {
              ; // there are no values, so return an empty list
              nextPageNumber = -1;
            } else if (jsonNode != null && jsonNode.isArray()) {
              boolean hasData = false;
              for (JsonNode mshipNode : jsonNode) {
                CCureMembership mship = CCureMembership.fromJson(mshipNode);
                result.add(mship);
                hasData = true;
                // sync back: the pair's ClearanceID/PersonnelID are already the native group/user ids
                CCureProvisioningTargetNativeSync.captureMembershipFromCurrentProvisioner(mship);
              }
              // live progress: pages over many slow WS calls with no total available, so report
              // count-so-far and page number.  Uses the thread-scoped current provisioner; null off a run.
              GrouperProvisioner currentProvisionerForMembershipsByUser = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
              if (currentProvisionerForMembershipsByUser != null) {
                currentProvisionerForMembershipsByUser.assignProgressLabelTarget("retrieving memberships from target: " + result.size()
                        + " so far (page " + numberOfCalls + ")");
              }

              if (!hasData) {
                nextPageNumber = -1;
              }
            } else {
              throw new RuntimeException("Did not receive success for result field instead received: " + jsonNode);
            }
          }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }

        return result;
    }

    public static List<CCureMembership> retrieveMembershipsForGroup(CCureExternalSystem externalSystem, String targetGroupId) {
        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveMembershipsForGroup");

        long startTime = System.nanoTime();

        List<CCureMembership> result = new ArrayList<>();

        try {
          Integer nextPageNumber = 1;
          int pageSize = externalSystem.getClearancePairPageSize();

          String urlSuffix = "/api/Objects/GetAllWithCriteria";

          int maxCalls = Math.max(INFINITELOOP_MAX/pageSize, 1);
          int numberOfCalls = 0;

          while (nextPageNumber > 0) {
            if (maxCalls-- < 0) {
              throw new RuntimeException("Endless loop detected! total results so far: " + result.size()
                      + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
            }

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair");
            bodyNode.put("WhereClause", "ClearanceID = " + targetGroupId);
            bodyNode.put("pagesize", pageSize);
            bodyNode.put("pagenumber", nextPageNumber);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("PersonnelID");
            displayFields.add("ClearanceID");
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            ++nextPageNumber;
            ++numberOfCalls;
            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200, 404), returnCode, bodyNode.toString(), null, "application/json");

            if (returnCode[0] == 404) {
              ; // there are no values, so return an empty list
              nextPageNumber = -1;
            } else if (jsonNode != null && jsonNode.isArray()) {
              boolean hasData = false;
              for (JsonNode mshipNode : jsonNode) {
                CCureMembership mship = CCureMembership.fromJson(mshipNode);
                result.add(mship);
                hasData = true;
                // sync back: the pair's ClearanceID/PersonnelID are already the native group/user ids
                CCureProvisioningTargetNativeSync.captureMembershipFromCurrentProvisioner(mship);
              }
              // live progress: pages over many slow WS calls with no total available, so report
              // count-so-far and page number.  Uses the thread-scoped current provisioner; null off a run.
              GrouperProvisioner currentProvisionerForMembershipsByGroup = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
              if (currentProvisionerForMembershipsByGroup != null) {
                currentProvisionerForMembershipsByGroup.assignProgressLabelTarget("retrieving memberships from target: " + result.size()
                        + " so far (page " + numberOfCalls + ")");
              }

              if (!hasData) {
                nextPageNumber = -1;
              }
            } else {
              throw new RuntimeException("Did not receive success for result field instead received: " + jsonNode);
            }
          }
        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }

        return result;
    }


    public static Exception insertMembershipsForUser(CCureExternalSystem externalSystem, Map<String, Object> debugMap, String personnelId, List<String> clearanceIds) {
        // length(), not isBlank(): GrouperUtil.isBlank(Object) is true only for null or a blank
        // String, so an EMPTY list is not blank and would fall through to a call with no children,
        // which CCure rejects with 400 "Missing 'Children' field"
        if (GrouperUtil.length(clearanceIds) == 0) {
            return null;
        }

        String urlSuffix = "/api/Objects/PersistToContainer";
        Set<Integer> allowedReturnCodes = GrouperUtil.toSet(200, 201);

        try {

            int[] returnCode = new int[]{-1};

            //Content-Type: application/x-www-form-urlencoded
            List<Map.Entry<String, String>> formFields = new ArrayList<>();
            formFields.add(new MapEntry("Type", "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel"));
            formFields.add(new MapEntry("ID", personnelId));

            int idx = 0;
            for (String clearanceId: clearanceIds) {
                formFields.add(new MapEntry("Children[" + idx + "][Type]", "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair"));

                formFields.add(new MapEntry("Children[" + idx + "][PropertyNames][0]", "PersonnelID"));
                formFields.add(new MapEntry("Children[" + idx + "][PropertyNames][1]", "ClearanceID"));

                formFields.add(new MapEntry("Children[" + idx + "][PropertyValues][0]", personnelId));
                formFields.add(new MapEntry("Children[" + idx + "][PropertyValues][1]", clearanceId));

                ++idx;
            }

            String form = formFields.stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            // json - not used
//            ObjectMapper objectMapper = new ObjectMapper();
//            ObjectNode bodyNode = objectMapper.createObjectNode();
//            bodyNode.put("Type", "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel");
//            bodyNode.put("ID", personnelId);
//            ArrayNode childrenNode = objectMapper.createArrayNode();
//            bodyNode.putIfAbsent("Children", childrenNode);
//
//            for (String clearanceId : clearanceIds) {
//                ObjectNode childNode = objectMapper.createObjectNode();
//                childNode.put("Type", "SoftwareHouse.NextGen.Common.SecurityObjects.Credential.PersonnelClearancePair");
//                ArrayNode propertyNames = objectMapper.createArrayNode();
//                ArrayNode propertyValues = objectMapper.createArrayNode();
//                propertyNames.add("ClearanceID");
//                propertyValues.add(clearanceId);
//                childNode.putIfAbsent("PropertyNames", propertyNames);
//                childNode.putIfAbsent("Propertyvalues", propertyValues);
//
//                childrenNode.add(childNode);
//            }


            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    allowedReturnCodes, returnCode, form, null, "application/x-www-form-urlencoded");

            return null;
        } catch (RuntimeException e) {
            return e;
        }
    }

    public static Exception deleteMembershipsForUser(CCureExternalSystem externalSystem, Map<String, Object> debugMap, String personnelId, List<String> objectIds) {
        // see insertMembershipsForUser: an empty list is not "blank", so guard on length
        if (GrouperUtil.length(objectIds) == 0) {
            return null;
        }

        String urlSuffix = "/api/Objects/RemoveFromContainer";
        Set<Integer> allowedReturnCodes = GrouperUtil.toSet(200);

        try {

            int[] returnCode = new int[]{-1};

            //Content-Type: application/x-www-form-urlencoded
            List<Map.Entry<String, String>> formFields = new ArrayList<>();
            formFields.add(new MapEntry("Type", "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel"));
            formFields.add(new MapEntry("ID", personnelId));

            int idx = 0;
            for (String objectId: objectIds) {
                if (GrouperUtil.isBlank(objectId)) {
                    throw new RuntimeException("Blank ObjectID for personnelID " + personnelId);
                }
                formFields.add(new MapEntry("Children[" + idx + "][Type]", "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair"));
                formFields.add(new MapEntry("Children[" + idx + "][ID]", objectId));

                ++idx;

//                formFields.add(new MapEntry("Children[" + idx + "][PropertyNames][0]", "PersonnelID"));
//                formFields.add(new MapEntry("Children[" + idx + "][PropertyNames][1]", "ClearanceID"));
//                formFields.add(new MapEntry("Children[" + idx + "][PropertyNames][0]", "ObjectID"));

//                formFields.add(new MapEntry("Children[" + idx + "][PropertyValues][0]", personnelId));
//                formFields.add(new MapEntry("Children[" + idx + "][PropertyValues][1]", clearandId));
//                formFields.add(new MapEntry("Children[" + idx + "][PropertyValues][0]", objectId));
            }

            String form = formFields.stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            // json - not used
//            ObjectMapper objectMapper = new ObjectMapper();
//            ObjectNode bodyNode = objectMapper.createObjectNode();
//            bodyNode.put("Type", "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel");
//            bodyNode.put("ID", personnelId);
//            ArrayNode childrenNode = objectMapper.createArrayNode();
//            bodyNode.putIfAbsent("Children", childrenNode);
//
//            for (String clearanceId : clearanceIds) {
//                ObjectNode childNode = objectMapper.createObjectNode();
//                childNode.put("Type", "SoftwareHouse.NextGen.Common.SecurityObjects.Credential.PersonnelClearancePair");
//                ArrayNode propertyNames = objectMapper.createArrayNode();
//                ArrayNode propertyValues = objectMapper.createArrayNode();
//                propertyNames.add("ClearanceID");
//                propertyValues.add(clearanceId);
//                childNode.putIfAbsent("PropertyNames", propertyNames);
//                childNode.putIfAbsent("Propertyvalues", propertyValues);
//
//                childrenNode.add(childNode);
//            }


            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    allowedReturnCodes, returnCode, form, null, "application/x-www-form-urlencoded");

            return null;
        } catch (RuntimeException e) {
            return e;
        }
    }

    /**
     * Executes an HTTP method and returns the response as a JsonNode.
     *
     * @param debugMap           a map to store debug information
     * @param httpMethodName     the HTTP method name (e.g., GET, POST)
     * @param externalSystem           the configuration ID for authentication
     * @param urlSuffix          the URL suffix for the request
     * @param allowedReturnCodes a set of allowed HTTP return codes
     * @param returnCode         an array to store the return code of the HTTP request
     * @param body          the body parameter for the HTTP request
     * @param returnBody         an array to store the response body
     * @param contentType        the content type header value
     * @return the response as a JsonNode
     */
    private static JsonNode executeMethod(Map<String, Object> debugMap,
                                          String httpMethodName, CCureExternalSystem externalSystem,
                                          String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body, String[] returnBody, String contentType) {

        String url = externalSystem.constructUrl(urlSuffix);

        GrouperHttpClient grouperHttpCall = new GrouperHttpClient();

        externalSystem.attachAuthenticationToHttpClient(grouperHttpCall);

        debugMap.put("url", url);
        debugMap.put("session_id", externalSystem.getSessionId());
        debugMap.put("token", externalSystem.getAccessToken());

        grouperHttpCall.assignUrl(url);
        grouperHttpCall.assignGrouperHttpMethod(httpMethodName);

        if ("POST".equals(httpMethodName)) {
            grouperHttpCall.addHeader("Content-Type", contentType != null ? contentType : "application/json");
        }

        if (body != null) {
            grouperHttpCall.assignBody(body);
        }

        grouperHttpCall.setRetryForThrottlingOrNetworkIssuesBackOffMillis(2 * 60 * 1000);

        grouperHttpCall.setRetryForThrottlingOrNetworkIssuesSleepMillis(2 * 60 * 1000);

        grouperHttpCall.assignRetryForThrottlingIsMinutes(true);

        grouperHttpCall.assignRetryForThrottlingUseRetryAfter(false);

        grouperHttpCall.setRetryForThrottlingOrNetworkIssues(30);


        grouperHttpCall.setThrottlingCallback(new GrouperHttpThrottlingCallback() {

            @Override
            public boolean setupThrottlingCallback(GrouperHttpClient httpClient) {
                String throttlingBody = StringUtils.trim(httpClient.getResponseBody());
                try {
                    if (StringUtils.isNotBlank(throttlingBody) && throttlingBody.contains("error_code") && throttlingBody.contains("\"429")) {
                        GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
                        return true;
                    }
                } catch (Exception e) {
                    LOG.error("Error: " + debugMap.get("url") + ", " + grouperHttpCall.getResponseCode() + ", " + throttlingBody, e);
                }

                boolean isThrottle = grouperHttpCall.getResponseCode() == 429;
                if (isThrottle) {
                    GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
                }
                return isThrottle;
            }
        });
        grouperHttpCall.executeRequest();

        int code;
        String json;

        try {
            code = grouperHttpCall.getResponseCode();
            returnCode[0] = code;
            json = grouperHttpCall.getResponseBody();
            if (returnBody != null && returnBody.length > 0) {
                returnBody[0] = json;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
        }

        if (!allowedReturnCodes.contains(code)) {
            throw new RuntimeException(
                    "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
                            + ". '" + debugMap.get("url") + "' " + json);
        }

        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return GrouperUtil.jsonJacksonNode(json);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: '" + json + "'", e);
        }

    }

    public static CCureMembership retrieveMembershipByGroupAndUser(CCureExternalSystem externalSystem, String clearanceID, String personnelID) {
        Map<String, Object> debugMap = new LinkedHashMap<>();

        debugMap.put("method", "retrieveMembershipsByGroupAndUser");

        long startTime = System.nanoTime();

        try {
            String urlSuffix = "/api/Objects/GetAllWithCriteria";

            int[] returnCode = new int[]{-1};

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode bodyNode = objectMapper.createObjectNode();
            bodyNode.put("TypeFullName", "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair");
            bodyNode.put("WhereClause", "ClearanceID = " + clearanceID + " and PersonnelID = " + personnelID);
            bodyNode.put("pagesize", 1);
            bodyNode.put("pagenumber", 1);

            ArrayNode displayFields = objectMapper.createArrayNode();
            displayFields.add("ObjectID");
            displayFields.add("PersonnelID");
            displayFields.add("ClearanceID");
            bodyNode.putIfAbsent("DisplayProperties", displayFields);

            JsonNode jsonNode = executeMethod(debugMap, "POST", externalSystem, urlSuffix,
                    GrouperUtil.toSet(200), returnCode, bodyNode.toString(), null, "application/json");

            if (jsonNode.isArray()) {
                return CCureMembership.fromJson(jsonNode.get(0));
            }

            throw new RuntimeException("Could not retrieve ClearancePair for PersonnelID=" + personnelID + " and ClearanceID=" + clearanceID);

        } catch (RuntimeException re) {
            debugMap.put("exception", re.getMessage());
            throw re;
        } finally {
            CCureLog.log(debugMap, startTime);
        }
    }
}
