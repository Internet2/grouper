"""
Reports on what config properties are referenced in Java that don't exist in the config base file, and vice versa.
Looks in source code for lines similar to xxxConfig.retrieveConfig().propertyValueXXX("the-property"). Then, for
all the base files corresponding to the xxxConfig classes, read in all the properties. Then, diff the lists in each.
"""

import glob
import os.path
import re
import sys
import datetime

if len(sys.argv) != 2:
    print("Syntax: python3 parseConfigPropertyUsage.py <grouper base directory>")
    sys.exit(1)

baseDir = sys.argv[1]

if (not os.path.isdir(baseDir)):
    print("Grouper base %s is invalid directory" % baseDir)
    sys.exit(2)

print("Started: %s" % datetime.datetime.now())

# subclasses of ConfigPropertiesCascadeBase. Not an exhaustive list, but the subset of configs that
# are actually referenced in Java code with retrieveConfig()
classBasePropertyMap = {
    'ConfigPropertiesOriginalHasHierarchy': 'grouper-misc/grouperClient/src/test/resources/testCascadeConfig-example2.properties',
    'ConfigPropertiesOverrideHasHierarchy': 'grouper-misc/grouperClient/src/test/resources/testCascadeConfig-example.properties',
    'GrouperActivemqConfig': 'grouper-misc/grouperActivemq/conf/grouper.activemq.base.properties',
    'GrouperClientConfig': 'grouper-misc/grouperClient/conf/grouper.client.base.properties',
    'GrouperConfig': 'grouper/conf/grouper.base.properties',
    'GrouperDbConfigTestConfig': 'grouper/src/test/edu/internet2/middleware/grouper/cfg/dbConfig/grouper.dbConfigTest.base.properties',
    'GrouperHibernateConfig': 'grouper/conf/grouper.hibernate.base.properties',
    'GrouperLoaderConfig': 'grouper/conf/grouper-loader.base.properties',
    'GrouperUiConfig': 'grouper/conf/grouper-ui-ng.base.properties',
    'GrouperWsConfig': 'grouper/conf/grouper-ws-ng.base.properties',
    'MorphStringConfig': 'grouper-misc/grouperClient/conf/morphString.base.properties',
    'SubjectConfig': 'grouper/conf/subject.base.properties',
}

# These are all the projects built from Maven
projects = [
    'grouper-misc/grouperClient',
    'grouper',
    'grouper-ui',
    'grouper-ws/grouper-ws-java-manual-client',
    'grouper-ws/grouper-ws-test',
    'grouper-ws/grouper-ws',
    'grouper-misc/grouper-pspng',
    'grouper-misc/googleapps-grouper-provisioner',
    'grouper-misc/grouper-azure',
    'grouper-misc/grouper-box',
    'grouper-misc/grouper-duo',
    'grouper-misc/grouper-installer',
    'grouper-misc/grouper-messaging-activemq',
    'grouper-misc/grouper-messaging-aws',
    'grouper-misc/grouper-messaging-rabbitmq',
    'grouper-misc/grouperActivemq',
    'grouper-misc/grouperScim',
]

class PropertyReference:
    deprecatedClassMap = {
        'GrouperClientUtils' : 'GrouperClientConfig',
    }

    def __init__(self, file, configClass, propertyType, propertyName):
        self.file = file
        self.configClass = self.deprecatedClassMap.get(configClass, configClass)
        self.propertyType = propertyType
        self.propertyName = propertyName

    @classmethod
    def mapDeprecatedClassToClass(cls, configClass):
        return cls.deprecatedClassMap.get(configClass, configClass)

# maps config class short name to a PropertyReference
# configRefs{ configClass : { propertyName : propertyReferences[] }
configRefs = dict()


# Given a directory, find all code that looks like a call to a config class retrieveConfig()
def searchDir(dirname):
    pattern = re.compile('(\w+)\.retrieveConfig\(\)\.(\w+)\("([^"]+)')
    pattern2 = re.compile('(GrouperClientUtils)\.(propertiesValue)\("([^"]+)')
    patternComment = re.compile('^\s*//')
    files = glob.glob('%s/**/*.java' % dirname, recursive=True)
    for file in files:
        for line in open(file, 'r'):
            if re.search(patternComment, line):
                continue
            match = re.search(pattern, line)
            if match == None:
                match = re.search(pattern2, line)
                if match != None:
                    print("*Reference to deprecated method: (%s) %s" % (file, line))
            if match:
                propRef = PropertyReference(file=file, configClass=match.group(1), propertyType=match.group(2), propertyName=match.group(3))
                if propRef.configClass not in configRefs:
                    configRefs[propRef.configClass] = dict()
                propertyKeys = configRefs[propRef.configClass]
                if propRef.propertyName not in propertyKeys:
                    propertyKeys[propRef.propertyName] = list()
                propertyKeys[propRef.propertyName].append(propRef)

# load properties from the specified base properties file
def importProperties(file):
    pattern = re.compile('^([^#][^\s=]+)\s*=')
    ret = list()
    for line in open(file, 'r'):
        match = re.search(pattern, line)
        if match:
            ret.append(match.group(1))
    return ret


# MAIN

# Search in any project directory, i.e. having a Maven pom file
# Load up the configRefs map with anything looking like a property lookup
#files = glob.glob('%s/**/pom.xml' % baseDir, recursive = True)
#for file in files:
#    dir = os.path.dirname(file)
#    searchDir(os.path.dirname(file))
#    print("Parsed %s" % dir)

for dir in projects:
    searchDir("%s/%s" % (baseDir, dir))
    print("Parsed %s" % dir)

print()

# For each Config class, load the properties of the base file. report on which properties
for (key, value) in configRefs.items():
    key = PropertyReference.mapDeprecatedClassToClass(key)
    if key not in classBasePropertyMap:
        print("Could not find a mapping between class %s and a base file\n\n" % key)
        continue

    props = importProperties(baseDir + os.path.sep +  classBasePropertyMap.get(key))
    extra = list(set(configRefs[key]) - set(props))
    unused = list(set(props) - set(configRefs[key]))

    extra.sort()
    unused.sort()

    print("%s\n%s" % (key, "="*len(key)))
    print("Extra (fetched properties not in base file):\n\t%s" % "\n\t".join(extra))
    print("Unused (in base file but not referenced):\n\t%s\n\n" % "\n\t".join(unused))
