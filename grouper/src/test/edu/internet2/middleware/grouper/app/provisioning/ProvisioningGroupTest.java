package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class ProvisioningGroupTest extends GrouperTest {

  public ProvisioningGroupTest() {
  }

  public ProvisioningGroupTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    
    TestRunner.run(new ProvisioningGroupTest("testConvertToJsonForCacheLong"));
    
    
  }

  public void testConvertToJsonForCache() {
    
    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    provisioningGroup.assignAttributeValue("name", "someName");
    provisioningGroup.assignAttributeValue("id", "abc123");
    provisioningGroup.addAttributeValue("member", "jsmith");
    provisioningGroup.addAttributeValue("member", "ajackson");
    provisioningGroup.addAttributeValue("member", "tjohnson");
    provisioningGroup.addAttributeValue("objectClass", "groupOfNames");
    provisioningGroup.addAttributeValue("objectClass", "top");
    provisioningGroup.addAttributeValue("objectClass", "memberGroup");
    provisioningGroup.assignAttributeValue("description", "This is the description of the group");
    provisioningGroup.assignAttributeValue("displayName", "Some name");
    provisioningGroup.assignAttributeValue("uuid", "abc123xyz456");
    String json = provisioningGroup.toJsonForCache("member");

    assertEquals("{\"description\":\"This is the description of the group\",\"displayName\":\"Some name\",\"id\":\"abc123\","
        + "\"name\":\"someName\",\"objectClass\":[\"groupOfNames\",\"memberGroup\",\"top\"],\"uuid\":\"abc123xyz456\"}", json);
    //{"description":"This is the description of the group","displayName":"Some name","id":"abc123","name":"someName","objectClass":["groupOfNames","memberGroup","top"],"uuid":"abc123xyz456"}
    
    ProvisioningGroup provisioningGroup2 = new ProvisioningGroup();
    
    provisioningGroup2.fromJsonForCache(json);
    assertEquals("someName" , provisioningGroup2.retrieveAttributeValueString("name"));
    assertEquals("abc123" , provisioningGroup2.retrieveAttributeValueString("id"));
    Collection<?> objectClass = (Collection<?>)provisioningGroup2.retrieveAttributeValue("objectClass");
    assertEquals(3, GrouperUtil.length(objectClass));
    assertTrue(objectClass.contains("groupOfNames"));
    assertTrue(objectClass.contains("top"));
    assertTrue(objectClass.contains("memberGroup"));
    assertEquals(0, GrouperUtil.length(provisioningGroup2.getTruncatedAttributeNames()));
  }

  public void testConvertToJsonForCacheLong() {
    
    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    provisioningGroup.assignAttributeValue("name", "someName");
    provisioningGroup.assignAttributeValue("id", "abc123");
    provisioningGroup.addAttributeValue("member", "jsmith");
    provisioningGroup.addAttributeValue("member", "ajackson");
    provisioningGroup.addAttributeValue("member", "tjohnson");
    provisioningGroup.addAttributeValue("objectClass", "groupOfNames");
    provisioningGroup.addAttributeValue("objectClass", "top");
    provisioningGroup.addAttributeValue("objectClass", "memberGroup");
    provisioningGroup.assignAttributeValue("description", "This is the description of the group");
    provisioningGroup.assignAttributeValue("displayName", "Some name");
    provisioningGroup.assignAttributeValue("uuid", "abc123xyz456");

    // 1426
    provisioningGroup.assignAttributeValue("someLong1", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Vitae auctor eu augue ut lectus arcu bibendum. Egestas erat imperdiet sed euismod nisi porta lorem mollis aliquam. In metus vulputate eu scelerisque felis imperdiet proin fermentum leo. Scelerisque fermentum dui faucibus in ornare quam. Massa eget egestas purus viverra accumsan in nisl. Dapibus ultrices in iaculis nunc sed augue lacus viverra vitae. Aliquam malesuada bibendum arcu vitae. Quam adipiscing vitae proin sagittis nisl rhoncus mattis. Vivamus at augue eget arcu dictum. Blandit aliquam etiam erat velit scelerisque in dictum. Egestas pretium aenean pharetra magna ac placerat vestibulum lectus. Egestas erat imperdiet sed euismod nisi porta lorem mollis aliquam. Egestas fringilla phasellus faucibus scelerisque eleifend. Erat velit scelerisque in dictum non consectetur a.  Fusce ut placerat orci nulla pellentesque dignissim enim sit. Vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt. Aliquet nibh praesent tristique magna sit amet purus. Sit amet commodo nulla facilisi. Fermentum dui faucibus in ornare quam viverra. Bibendum at varius vel pharetra vel turpis. Volutpat est velit egestas dui id. Elementum eu facilisis sed odio morbi quis commodo odio aenean. Integer malesuada nunc vel risus commodo viverra maecenas. Tempus quam pellentesque nec nam aliquam sem.");
    
    // 982
    provisioningGroup.assignAttributeValue("someLong2", "Vestibulum lorem sed risus ultricies tristique nulla. Purus viverra accumsan in nisl nisi scelerisque eu. Mattis pellentesque id nibh tortor. Amet dictum sit amet justo donec enim. Justo nec ultrices dui sapien eget mi proin. Faucibus scelerisque eleifend donec pretium. Dignissim sodales ut eu sem integer vitae justo eget magna. Consequat ac felis donec et odio pellentesque diam volutpat commodo. Porttitor eget dolor morbi non arcu risus. Pharetra et ultrices neque ornare aenean euismod elementum nisi quis. Erat nam at lectus urna duis convallis convallis tellus. Nec nam aliquam sem et tortor consequat id porta. Sit amet tellus cras adipiscing enim. Tortor aliquam nulla facilisi cras fermentum odio eu. Proin fermentum leo vel orci. Et netus et malesuada fames ac turpis egestas sed. Proin libero nunc consequat interdum. Ridiculus mus mauris vitae ultricies leo integer malesuada. Sapien eget mi proin sed libero enim sed. Diam maecenas ultricies mi eget mauris pharetra et.");
    
    // 765
    provisioningGroup.assignAttributeValue("someLong3", "Viverra maecenas accumsan lacus vel facilisis volutpat est velit. Quam lacus suspendisse faucibus interdum posuere lorem ipsum dolor sit. Et malesuada fames ac turpis egestas. Sit amet dictum sit amet justo. Sit amet consectetur adipiscing elit pellentesque habitant morbi tristique. Ullamcorper velit sed ullamcorper morbi tincidunt. Justo eget magna fermentum iaculis eu non diam. Dolor sit amet consectetur adipiscing elit ut aliquam purus. Mattis aliquam faucibus purus in massa. Felis eget velit aliquet sagittis id consectetur purus ut. Hendrerit dolor magna eget est lorem. Tortor id aliquet lectus proin. Velit euismod in pellentesque massa placerat duis. Eu scelerisque felis imperdiet proin fermentum. Porttitor leo a diam sollicitudin tempor id eu nisl.");
    
    // 537
    provisioningGroup.assignAttributeValue("someLong4", "Facilisi morbi tempus iaculis urna id. Sollicitudin tempor id eu nisl nunc. Ultrices eros in cursus turpis massa tincidunt dui. Ultrices vitae auctor eu augue ut lectus arcu bibendum at. Nunc consequat interdum varius sit amet. Consectetur libero id faucibus nisl. Imperdiet sed euismod nisi porta lorem mollis. Cras pulvinar mattis nunc sed blandit libero volutpat sed cras. A diam sollicitudin tempor id eu nisl nunc mi. Non blandit massa enim nec dui nunc. Sapien eget mi proin sed libero. Pulvinar neque laoreet suspendisse interdum");
    
    // 1617
    provisioningGroup.assignAttributeValue("someLong5", "Odio tempor orci dapibus ultrices in iaculis nunc. Nibh venenatis cras sed felis eget velit aliquet sagittis id. Sed risus ultricies tristique nulla aliquet enim tortor at. Vitae aliquet nec ullamcorper sit. Arcu non odio euismod lacinia at quis. Faucibus vitae aliquet nec ullamcorper sit amet. Praesent tristique magna sit amet purus gravida quis blandit. Velit euismod in pellentesque massa placerat duis. A diam sollicitudin tempor id eu. Adipiscing at in tellus integer. Placerat duis ultricies lacus sed turpis tincidunt id. Faucibus a pellentesque sit amet porttitor eget dolor morbi non. Cras sed felis eget velit aliquet sagittis id. Risus nec feugiat in fermentum posuere. Elit duis tristique sollicitudin nibh. Mattis rhoncus urna neque viverra justo nec ultrices dui sapien. Nisl pretium fusce id velit ut tortor. Tincidunt augue interdum velit euismod in pellentesque.  Felis bibendum ut tristique et egestas quis ipsum suspendisse ultrices. Enim diam vulputate ut pharetra. Justo eget magna fermentum iaculis eu non. Molestie nunc non blandit massa enim nec dui nunc mattis. Ut tortor pretium viverra suspendisse potenti nullam ac. Ultricies mi quis hendrerit dolor magna eget est lorem ipsum. Nibh cras pulvinar mattis nunc sed blandit. Adipiscing bibendum est ultricies integer quis auctor elit. Sed cras ornare arcu dui vivamus arcu. Odio ut enim blandit volutpat maecenas volutpat blandit aliquam etiam. Lorem donec massa sapien faucibus. Scelerisque eleifend donec pretium vulputate. Lacus suspendisse faucibus interdum posuere lorem ipsum dolor. Ut faucibus pulvinar elementum integer enim neque.");
    
    String json = provisioningGroup.toJsonForCache("member");

    //System.out.println(json);
    assertEquals("{\"description\":\"This is the description of the group\",\"displayName\":\"Some name\",\"id\":\"abc123\",\"name\":\"someName\",\"objectClass\":[\"groupOfNames\",\"memberGroup\",\"top\"],\"someLong1\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Vitae auctor eu augue ut lectus arcu bibendum. Egestas erat imperdiet sed euismod nisi porta lorem mollis aliquam. In metus vulputate eu scelerisque felis imperdiet proin fermentum leo. Scelerisque fermentum dui faucibus in ornare quam. Massa eget egestas purus viverra accumsan in nisl. Dapibus ultrices in iaculis nunc sed augue lacus viverra vitae. Aliquam malesuada bib...\",\"someLong2\":\"Vestibulum lorem sed risus ultricies tristique nulla. Purus viverra accumsan in nisl nisi scelerisque eu. Mattis pellentesque id nibh tortor. Amet dictum sit amet justo donec enim. Justo nec ultrices dui sapien eget mi proin. Faucibus scelerisque eleifend donec pretium. Dignissim sodales ut eu sem integer vitae justo eget magna. Consequat ac felis donec et odio pellentesque diam volutpat commodo. Porttitor eget dolor morbi non arcu risus. Pharetra et ultrices neque ornare aenean euismod eleme...\",\"someLong3\":\"Viverra maecenas accumsan lacus vel facilisis volutpat est velit. Quam lacus suspendisse faucibus interdum posuere lorem ipsum dolor sit. Et malesuada fames ac turpis egestas. Sit amet dictum sit amet justo. Sit amet consectetur adipiscing elit pellentesque habitant morbi tristique. Ullamcorper velit sed ullamcorper morbi tincidunt. Justo eget magna fermentum iaculis eu non diam. Dolor sit amet consectetur adipiscing elit ut aliquam purus. Mattis aliquam faucibus purus in massa. Felis eget velit aliquet sagittis id consectetur purus ut. Hendrerit dolor magna eget est lorem. Tortor id aliquet lectus proin. Velit euismod in pellentesque massa placerat duis. Eu scelerisque felis imperdiet proin fermentum. Porttitor leo a diam sollicitudin tempor id eu nisl.\",\"someLong4\":\"Facilisi morbi tempus iaculis urna id. Sollicitudin tempor id eu nisl nunc. Ultrices eros in cursus turpis massa tincidunt dui. Ultrices vitae auctor eu augue ut lectus arcu bibendum at. Nunc consequat interdum varius sit amet. Consectetur libero id faucibus nisl. Imperdiet sed euismod nisi porta lorem mollis. Cras pulvinar mattis nunc sed blandit libero volutpat sed cras. A diam sollicitudin tempor id eu nisl nunc mi. Non blandit massa enim nec dui nunc. Sapien eget mi proin sed libero. Pulvinar neque laoreet suspendisse interdum\",\"someLong5\":\"Odio tempor orci dapibus ultrices in iaculis nunc. Nibh venenatis cras sed felis eget velit aliquet sagittis id. Sed risus ultricies tristique nulla aliquet enim tortor at. Vitae aliquet nec ullamcorper sit. Arcu non odio euismod lacinia at quis. Faucibus vitae aliquet nec ullamcorper sit amet. Praesent tristique magna sit amet purus gravida quis blandit. Velit euismod in pellentesque massa placerat duis. A diam sollicitudin tempor id eu. Adipiscing at in tellus integer. Placerat duis ultricies lacus sed turpis tincidunt id. Faucibus a pellentesque sit amet porttitor eget dolor morbi non. Cras sed felis eget velit aliquet sagittis id. Risus nec feugiat in fermentum posuere. Elit duis tristique sollicitudin nibh. Mattis rhoncus urna neque viverra justo nec ultrices dui sapien. Nisl pretium fusce id velit ut tortor. Tincidunt augue interdum velit euismod in pellentesque.  Felis bibendum ut tristique et egestas quis ipsum suspendisse ultrices. Enim diam vulputate ut pharetra. Justo ege...\",\"uuid\":\"abc123xyz456\",\"trunc_attrs\":[\"someLong1\",\"someLong2\",\"someLong5\"]}", json);
    
    //{"description":"This is the description of the group","displayName":"Some name","id":"abc123","name":"someName","objectClass":["groupOfNames","memberGroup","top"],"uuid":"abc123xyz456"}

    ProvisioningGroup provisioningGroup2 = new ProvisioningGroup();
    provisioningGroup2.assignAttributeValue("name", "someName");
    provisioningGroup2.assignAttributeValue("id", "abc123");
    provisioningGroup2.addAttributeValue("member", "jsmith");
    provisioningGroup2.addAttributeValue("member", "ajackson");
    provisioningGroup2.addAttributeValue("member", "tjohnson");
    provisioningGroup2.addAttributeValue("objectClass", "groupOfNames");
    provisioningGroup2.addAttributeValue("objectClass", "top");
    provisioningGroup2.addAttributeValue("objectClass", "memberGroup");
    provisioningGroup2.assignAttributeValue("description", "This is the description of the group");
    provisioningGroup2.assignAttributeValue("displayName", "Some name");
    provisioningGroup2.assignAttributeValue("uuid", "abc123xyz456");

    // 1426
    provisioningGroup2.assignAttributeValue("someLong1", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Vitae auctor eu augue ut lectus arcu bibendum. Egestas erat imperdiet sed euismod nisi porta lorem mollis aliquam. In metus vulputate eu scelerisque felis imperdiet proin fermentum leo. Scelerisque fermentum dui faucibus in ornare quam. Massa eget egestas purus viverra accumsan in nisl. Dapibus ultrices in iaculis nunc sed augue lacus viverra vitae. Aliquam malesuada bibendum arcu vitae. Quam adipiscing vitae proin sagittis nisl rhoncus mattis. Vivamus at augue eget arcu dictum. Blandit aliquam etiam erat velit scelerisque in dictum. Egestas pretium aenean pharetra magna ac placerat vestibulum lectus. Egestas erat imperdiet sed euismod nisi porta lorem mollis aliquam. Egestas fringilla phasellus faucibus scelerisque eleifend. Erat velit scelerisque in dictum non consectetur a.  Fusce ut placerat orci nulla pellentesque dignissim enim sit. Vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt. Aliquet nibh praesent tristique magna sit amet purus. Sit amet commodo nulla facilisi. Fermentum dui faucibus in ornare quam viverra. Bibendum at varius vel pharetra vel turpis. Volutpat est velit egestas dui id. Elementum eu facilisis sed odio morbi quis commodo odio aenean. Integer malesuada nunc vel risus commodo viverra maecenas. Tempus quam pellentesque nec nam aliquam sem.");
    
    // 982
    provisioningGroup2.assignAttributeValue("someLong2", "Vestibulum lorem sed risus ultricies tristique nulla. Purus viverra accumsan in nisl nisi scelerisque eu. Mattis pellentesque id nibh tortor. Amet dictum sit amet justo donec enim. Justo nec ultrices dui sapien eget mi proin. Faucibus scelerisque eleifend donec pretium. Dignissim sodales ut eu sem integer vitae justo eget magna. Consequat ac felis donec et odio pellentesque diam volutpat commodo. Porttitor eget dolor morbi non arcu risus. Pharetra et ultrices neque ornare aenean euismod elementum nisi quis. Erat nam at lectus urna duis convallis convallis tellus. Nec nam aliquam sem et tortor consequat id porta. Sit amet tellus cras adipiscing enim. Tortor aliquam nulla facilisi cras fermentum odio eu. Proin fermentum leo vel orci. Et netus et malesuada fames ac turpis egestas sed. Proin libero nunc consequat interdum. Ridiculus mus mauris vitae ultricies leo integer malesuada. Sapien eget mi proin sed libero enim sed. Diam maecenas ultricies mi eget mauris pharetra et.");
    
    // 765
    provisioningGroup2.assignAttributeValue("someLong3", "Viverra maecenas accumsan lacus vel facilisis volutpat est velit. Quam lacus suspendisse faucibus interdum posuere lorem ipsum dolor sit. Et malesuada fames ac turpis egestas. Sit amet dictum sit amet justo. Sit amet consectetur adipiscing elit pellentesque habitant morbi tristique. Ullamcorper velit sed ullamcorper morbi tincidunt. Justo eget magna fermentum iaculis eu non diam. Dolor sit amet consectetur adipiscing elit ut aliquam purus. Mattis aliquam faucibus purus in massa. Felis eget velit aliquet sagittis id consectetur purus ut. Hendrerit dolor magna eget est lorem. Tortor id aliquet lectus proin. Velit euismod in pellentesque massa placerat duis. Eu scelerisque felis imperdiet proin fermentum. Porttitor leo a diam sollicitudin tempor id eu nisl.");
    
    // 592
    provisioningGroup2.assignAttributeValue("someLong4", "Facilisi morbi tempus iaculis urna id. Sollicitudin tempor id eu nisl nunc. Ultrices eros in cursus turpis massa tincidunt dui. Ultrices vitae auctor eu augue ut lectus arcu bibendum at. Nunc consequat interdum varius sit amet. Consectetur libero id faucibus nisl. Imperdiet sed euismod nisi porta lorem mollis. Cras pulvinar mattis nunc sed blandit libero volutpat sed cras. A diam sollicitudin tempor id eu nisl nunc mi. Non blandit massa enim nec dui nunc. Sapien eget mi proin sed libero. Pulvinar neque laoreet suspendisse interdum");
    
    // 1617
    provisioningGroup2.assignAttributeValue("someLong5", "Odio tempor orci dapibus ultrices in iaculis nunc. Nibh venenatis cras sed felis eget velit aliquet sagittis id. Sed risus ultricies tristique nulla aliquet enim tortor at. Vitae aliquet nec ullamcorper sit. Arcu non odio euismod lacinia at quis. Faucibus vitae aliquet nec ullamcorper sit amet. Praesent tristique magna sit amet purus gravida quis blandit. Velit euismod in pellentesque massa placerat duis. A diam sollicitudin tempor id eu. Adipiscing at in tellus integer. Placerat duis ultricies lacus sed turpis tincidunt id. Faucibus a pellentesque sit amet porttitor eget dolor morbi non. Cras sed felis eget velit aliquet sagittis id. Risus nec feugiat in fermentum posuere. Elit duis tristique sollicitudin nibh. Mattis rhoncus urna neque viverra justo nec ultrices dui sapien. Nisl pretium fusce id velit ut tortor. Tincidunt augue interdum velit euismod in pellentesque.  Felis bibendum ut tristique et egestas quis ipsum suspendisse ultrices. Enim diam vulputate ut pharetra. Justo eget magna fermentum iaculis eu non. Molestie nunc non blandit massa enim nec dui nunc mattis. Ut tortor pretium viverra suspendisse potenti nullam ac. Ultricies mi quis hendrerit dolor magna eget est lorem ipsum. Nibh cras pulvinar mattis nunc sed blandit. Adipiscing bibendum est ultricies integer quis auctor elit. Sed cras ornare arcu dui vivamus arcu. Odio ut enim blandit volutpat maecenas volutpat blandit aliquam etiam. Lorem donec massa sapien faucibus. Scelerisque eleifend donec pretium vulputate. Lacus suspendisse faucibus interdum posuere lorem ipsum dolor. Ut faucibus pulvinar elementum integer enim neque.");

    Set<String> attributeNamesDifferentForCache = provisioningGroup.attributeNamesDifferentForCache(provisioningGroup2, "member");
    assertEquals(0, GrouperUtil.length(attributeNamesDifferentForCache));
  
    provisioningGroup2.assignAttributeValue("name", "someOtherName");
    provisioningGroup2.assignAttributeValue("id", "acb234");
    attributeNamesDifferentForCache = provisioningGroup.attributeNamesDifferentForCache(provisioningGroup2, "member");
    
    assertEquals(2, GrouperUtil.length(attributeNamesDifferentForCache));
    assertTrue(attributeNamesDifferentForCache.contains("name"));
    assertTrue(attributeNamesDifferentForCache.contains("id"));
    
  }

  
  
}
