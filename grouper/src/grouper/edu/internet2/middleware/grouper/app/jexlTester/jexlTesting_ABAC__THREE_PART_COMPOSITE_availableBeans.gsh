import edu.internet2.middleware.grouper.abac.*;


GrouperAbacEntity grouperAbacEntity = new GrouperAbacEntity();
grouperAbacEntity.setMultiValuedGroupExtensionInFolder(new HashMap<String, Set<String>>());
grouperAbacEntity.setSingleValuedGroupExtensionInFolder(new HashMap<String, String>());

Set<String> memberOfGroupNames = new HashSet<>();
memberOfGroupNames.add("ref:employee");
memberOfGroupNames.add("app:wiki:adminsManual");
// memberOfGroupNames.add("ref:lockout");

grouperAbacEntity.setMemberOfGroupNames(memberOfGroupNames);

Map<String, Object> elVariableMap = new HashMap<String, Object>();

elVariableMap.put("entity", grouperAbacEntity);