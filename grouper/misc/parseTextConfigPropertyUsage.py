"""
Reports on what config properties are in the grouper text config (grouper/conf/grouperText/grouper.textNg.en.us.base.properties),
which aren't in any quoted string in Java or jsp. These are likely to be unused properties that are safe to remove.
"""

import glob
import os.path
import re
import sys
import datetime

if len(sys.argv) != 2:
    print("Syntax: python3 parseTextConfigPropertyUsage.py <grouper base directory>")
    sys.exit(1)

baseDir = sys.argv[1]

if (not os.path.isdir(baseDir)):
    print("Grouper base %s is invalid directory" % baseDir)
    sys.exit(2)

print("Started: %s" % datetime.datetime.now())

# These are all the projects built from Maven
projects = [
    'grouper',
    'grouper-ui',
    'grouper-misc/grouperClient',
    'grouper-ws/grouper-ws',
    'grouper-misc/grouper-pspng',
    'grouper-misc/grouper-installer',
    'grouper-ws/grouper-ws-java-generated-client',
    'grouper-ws/grouper-ws-java-manual-client',
    'grouper-ws/grouper-ws-scim',
    'grouper-ws/grouper-ws-test',
    'grouper-misc/googleapps-grouper-provisioner',
    'grouper-misc/grouper-azure',
    'grouper-misc/grouper-box',
    'grouper-misc/grouper-duo',
    'grouper-misc/grouper-messaging-activemq',
    'grouper-misc/grouper-messaging-aws',
    'grouper-misc/grouper-messaging-rabbitmq',
    'grouper-misc/grouperActivemq',
    'grouper-misc/grouperScim',
]

class PropertyReference:
    def __init__(self, file, linenum, propertyName):
        self.file = file
        self.linenum = linenum
        self.propertyName = propertyName

# maps config class short name to a PropertyReference
# configRefs = { propname : propertyReferences[] }
configRefs = dict()


# Given a directory, find all Java code that looks like a string
def searchJavaDir(dirname):
    pattern = re.compile('"(\w+)')
    patternComment = re.compile('^\s*//')

    files = glob.glob('%s/**/*.java' % dirname, recursive=True)

    for file in files:
        lnum = 0
        for line in open(file, 'r'):
            lnum += 1
            if re.search(patternComment, line):
                continue
            # unescape quotes
            line.replace('\"', '"')
            matches = re.findall(pattern, line)
            if len(matches) > 0:
                for match in matches:
                    propRef = PropertyReference(file=file, linenum=lnum, propertyName=match)
                    if propRef.propertyName not in configRefs:
                        configRefs[propRef.propertyName] = list()
                    configRefs[propRef.propertyName].append(propRef)


def searchJspDir(dirname):
    pattern = re.compile('["\'](\w+)')

    files = glob.glob('%s/**/*.jsp' % dirname, recursive=True)
    for file in files:
        lnum = 0
        for line in open(file, 'r'):
            lnum += 1
            # unescape quotes
            line.replace("\\\"", '"')
            line.replace("\\\'", "'")
            matches = re.findall(pattern, line)
            if len(matches) > 0:
                for match in matches:
                    propRef = PropertyReference(file=file, linenum=lnum, propertyName=match)
                    if propRef.propertyName not in configRefs:
                        configRefs[propRef.propertyName] = list()
                    configRefs[propRef.propertyName].append(propRef)

# load properties from the specified base properties file
def importProperties(file):
    pattern = re.compile('^([^\s=]+)')
    patternComment = re.compile('\s*#')
    ret = list()
    for line in open(file, 'r'):
        if re.search(patternComment, line):
            continue
        match = re.search(pattern, line)
        if match:
            propName = match.group(1)
            if propName in ret:
                print("Duplicate property '%s'" % propName)
            else:
                ret.append(match.group(1))
    return ret


# MAIN

for dir in projects:
    searchJavaDir("%s/%s" % (baseDir, dir))
    searchJspDir("%s/%s" % (baseDir, dir))
    print("Parsed %s" % dir)

print()

baseFile = 'grouper/conf/grouperText/grouper.textNg.en.us.base.properties'
print("Base file: %s" % baseFile)
props = importProperties(baseDir + os.path.sep + baseFile)
unused = list(set(props) - set(configRefs.keys()))

unused.sort()

print("\nUnused (in base file but not referenced):\n----------------\n%s\n" % "\n".join(unused))

print("Total unused properties (out of %d total): %d" % (len(props), len(unused)))