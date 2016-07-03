# Remove grouperLoaderLdapErrorUnresolvable attribute
System.out.println("\n\n##########################################\n# Grouper 2.3.0 Upgrade Step 1/1: Remove grouperLoaderLdapErrorUnresolvable attribute\n##########################################");
grouperSession = GrouperSession.startRootSession();
attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(LoaderLdapUtils.attributeLoaderLdapStemName() + ":grouperLoaderLdapErrorUnresolvable", false, null);
if (attributeDefName == null) { System.out.println("Attribute doesn't exist so nothing to remove."); } else { attributeDefName.delete(); System.out.println("Successfully removed attribute."); }
