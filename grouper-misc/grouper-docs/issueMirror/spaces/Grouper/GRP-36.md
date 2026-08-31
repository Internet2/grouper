---
key: GRP-36
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-36
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Shilen Patel <shilen@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-19T13:12:26.422+0000
updated: 2007-10-16T13:26:50.797+0000
resolved: 2007-10-16T13:26:50.799+0000
components: [API]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-36  Infinite Loop on GroupFinder.findByName() while using wheel group

When using the wheel group, performing a search for any group using GroupFinder.findByName() will initialize the WheelAccessResolver class, which calls GroupFinder.findByName() to get the wheel group and results in an infinite loop.


Exception in thread "main" java.lang.StackOverflowError
        at java.nio.CharBuffer.<init>(CharBuffer.java:259)
        at java.nio.HeapCharBuffer.<init>(HeapCharBuffer.java:52)
        at java.nio.CharBuffer.wrap(CharBuffer.java:350)
        at java.lang.StringCoding$CharsetSE.encode(StringCoding.java:340)
        at java.lang.StringCoding.encode(StringCoding.java:378)
        at java.lang.String.getBytes(String.java:812)
        at java.io.UnixFileSystem.getBooleanAttributes0(Native Method)
        at java.io.UnixFileSystem.getBooleanAttributes(UnixFileSystem.java:228)
        at java.io.File.exists(File.java:702)
        at sun.misc.URLClassPath$FileLoader.getResource(URLClassPath.java:893)
        at sun.misc.URLClassPath$FileLoader.findResource(URLClassPath.java:871)
        at sun.misc.URLClassPath.findResource(URLClassPath.java:142)
        at java.net.URLClassLoader$2.run(URLClassLoader.java:362)
        at java.security.AccessController.doPrivileged(Native Method)
        at java.net.URLClassLoader.findResource(URLClassLoader.java:359)
        at java.lang.ClassLoader.getResource(ClassLoader.java:977)
        at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1159)
        at javax.xml.parsers.SecuritySupport$4.run(SecuritySupport.java:72)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.xml.parsers.SecuritySupport.getResourceAsStream(SecuritySupport.java:65)
        at javax.xml.parsers.FactoryFinder.findJarServiceProvider(FactoryFinder.java:213)
        at javax.xml.parsers.FactoryFinder.find(FactoryFinder.java:185)
        at javax.xml.parsers.SAXParserFactory.newInstance(SAXParserFactory.java:107)
        at net.sf.ehcache.config.ConfigurationFactory.parseConfiguration(ConfigurationFactory.java:143)
        at net.sf.ehcache.config.ConfigurationFactory.parseConfiguration(ConfigurationFactory.java:93)
        at net.sf.ehcache.CacheManager.parseConfiguration(CacheManager.java:255)
        at net.sf.ehcache.CacheManager.init(CacheManager.java:212)
        at net.sf.ehcache.CacheManager.<init>(CacheManager.java:179)
        at edu.internet2.middleware.grouper.cache.EhcacheController.initialize(EhcacheController.java:86)
        at edu.internet2.middleware.grouper.cache.EhcacheController.<init>(EhcacheController.java:44)
        at edu.internet2.middleware.grouper.privs.CachingAccessResolver.<init>(CachingAccessResolver.java:51)
        at edu.internet2.middleware.grouper.privs.AccessResolverFactory.getInstance(AccessResolverFactory.java:76)
        at edu.internet2.middleware.grouper.privs.AccessResolverFactory.getInstance(AccessResolverFactory.java:49)
        at edu.internet2.middleware.grouper.GrouperSession.getAccessResolver(GrouperSession.java:162)
        at edu.internet2.middleware.grouper.PrivilegeHelper.canView(PrivilegeHelper.java:151)
        at edu.internet2.middleware.grouper.GroupFinder.findByName(GroupFinder.java:107)
        at edu.internet2.middleware.grouper.privs.WheelAccessResolver.<init>(WheelAccessResolver.java:56)
        at edu.internet2.middleware.grouper.privs.AccessResolverFactory.getInstance(AccessResolverFactory.java:76)
        at edu.internet2.middleware.grouper.privs.AccessResolverFactory.getInstance(AccessResolverFactory.java:49)
        at edu.internet2.middleware.grouper.GrouperSession.getAccessResolver(GrouperSession.java:162)
        at edu.internet2.middleware.grouper.PrivilegeHelper.canView(PrivilegeHelper.java:151)
        at edu.internet2.middleware.grouper.GroupFinder.findByName(GroupFinder.java:107)
        at edu.internet2.middleware.grouper.privs.WheelAccessResolver.<init>(WheelAccessResolver.java:56)
        at edu.internet2.middleware.grouper.privs.AccessResolverFactory.getInstance(AccessResolverFactory.java:76)
        at edu.internet2.middleware.grouper.privs.AccessResolverFactory.getInstance(AccessResolverFactory.java:49)
        at edu.internet2.middleware.grouper.GrouperSession.getAccessResolver(GrouperSession.java:162)
        at edu.internet2.middleware.grouper.PrivilegeHelper.canView(PrivilegeHelper.java:151)
        at edu.internet2.middleware.grouper.GroupFinder.findByName(GroupFinder.java:107)
........ (The last 6 lines are repeated several times)


## Comments

### blair@example.com - 2007-10-04T03:51:03.434+0000

I should be able to fix this (and "privs/WheelNamingResolver") by calling the DAO directly.

### Gary Brown - 2007-10-16T13:26:50.756+0000

I added:

    if(s.getSubject().equals(SubjectFinder.findRootSubject()))
    	return g;

So that GrouperSystem does not have to do the canView check which ultimately led to recursion. The fix is pretty ugly - but trivial to remove when a more elegant solution is found