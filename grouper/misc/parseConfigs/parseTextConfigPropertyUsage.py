"""
Reports on what config properties are in the grouper text config (grouper/conf/grouperText/grouper.textNg.en.us.base.properties),
which aren't in any quoted string in Java or jsp. These are likely to be unused properties that are safe to remove.
"""

import argparse
import glob
import os.path
import re
import sys
import datetime
from dataclasses import dataclass

parser = argparse.ArgumentParser(description='Reports on what grouperText config properties are referenced in Java that don\'t exist in the config base file, and vice versa.')
parser.add_argument('--directory', '-d',
                    default='../../..',
                    help='Grouper base directory (default: ../../..)')
parser.add_argument('--add-placeholders', action='store_true',
                    help='Create a modified properties file with unused keys replaced by **key**')
parser.add_argument('--show-matched', action='store_true',
                    help='Show properties matched by known patterns separately from unmatched')

args = parser.parse_args()
baseDir = args.directory

if not os.path.isdir(baseDir):
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
    'grouper-ws/grouper-ws-java-manual-client',
    'grouper-ws/grouper-ws-test',
    'grouper-misc/googleapps-grouper-provisioner',
    'grouper-misc/grouper-azure',
    'grouper-misc/grouper-box',
    'grouper-misc/grouper-duo',
    'grouper-misc/grouper-messaging-activemq',
    'grouper-misc/grouper-messaging-aws',
    'grouper-misc/grouper-messaging-rabbitmq',
    'grouper-misc/grouperActivemq',
]

# Keys that should never be replaced with placeholders (retain production values)
PLACEHOLDER_EXCLUDE_KEYS = {
    'text.config.hierarchy',
    'text.config.secondsBetweenUpdateChecks',
}


@dataclass
class KnownPattern:
    tag: str
    pattern: str
    values: list[str]


KNOWN_PATTERNS = [
    # GrouperExternalSystem, ProvisioningConfiguration, GrouperDaemonConfiguration (isMultiple == true)
    KnownPattern('GrouperConfigurationModuleBase', r'^config.(\w+).title$', [
        'ActiveMqGrouperExternalSystem',
        'AzureGrouperExternalSystem',
        'BoxGrouperExternalSystem',
        'DatabaseGrouperExternalSystem',
        'DuoGrouperExternalSystem',
        'GoogleGrouperExternalSystem',
        'LdapGrouperExternalSystem',
        'OidcGrouperExternalSystem',
        'RabbitMqGrouperExternalSystem',
        'RemedyDigitalMarketplaceGrouperExternalSystem',
        'RemedyGrouperExternalSystem',
        'SftpGrouperExternalSystem',
        'SmtpGrouperExternalSystem',
        'SqsGrouperExternalSystem',
        'TeamDynamixExternalSystem',
        'WsBearerTokenExternalSystem',
        # ----
        'AdobeProvisionerConfiguration',
        'AzureProvisionerConfiguration',
        'BoxProvisionerConfiguration',
        'DigitalMarketplaceProvisionerConfiguration',
        'DuoProvisionerConfiguration',
        'DuoRoleProvisionerConfiguration',
        'GenericProvisionerConfiguration',
        'GoogleProvisionerConfiguration',
        'GrouperScim2Configuration',
        'GshTemplateProvisionerConfiguration',
        'LdapProvisionerConfiguration',
        'MessagingProvisionerConfiguration',
        'MidPointProvisionerConfiguration',
        'OktaProvisionerConfiguration',
        'RemedyProvisionerConfiguration',
        'SqlProvisionerConfiguration',
        'TeamDynamixProvisionerConfiguration',
        # ----
        'GrouperDaemonChangeLogConsumerConfiguration',
        'GrouperDaemonChangeLogEsbConfiguration',
        'GrouperDaemonChangeLogEsbToMessagingConfiguration',
        'GrouperDaemonChangeLogScriptConfiguration',
        'GrouperDaemonChangeLogToMessagingConfiguration',
        'GrouperDaemonMessagingListenerConfiguration',
        'GrouperDaemonMessagingListenerToChangeLogConfiguration',
        'GrouperDaemonOtherJobConfiguration',
        'GrouperDaemonOtherJobCsvReportConfiguration',
        'GrouperDaemonOtherJobDataProviderFullSyncConfiguration',
        'GrouperDaemonOtherJobDataProviderIncrementalSyncConfiguration',
        'GrouperDaemonOtherJobGroupSyncAnotherGrouperConfiguration',
        'GrouperDaemonOtherJobLdapToSqlConfiguration',
        'GrouperDaemonOtherJobLoaderIncrementalConfiguration',
        'GrouperDaemonOtherJobNotificationConfiguration',
        'GrouperDaemonOtherJobProvisioningFullSyncConfiguration',
        'GrouperDaemonOtherJobScriptConfiguration',
        'GrouperDaemonOtherJobSftpToSqlConfiguration',
        'GrouperDaemonOtherJobSubjectChangeConfiguration',
        'GrouperDaemonOtherJobSyncToGrouperFromSqlConfiguration',
        'GrouperDaemonOtherJobTableSyncConfiguration',
        'GrouperDaemonProvisioningIncrementalSyncConfiguration',
    ]),
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
    # pattern = re.compile('"(\w+)')
    pattern = re.compile(r'(GrouperUiUtils\.message|contentKeys\.get|getText\(\)\.get|GrouperTextContainer\.textOrNull|grouperUiApiTextConfig\.propertyValueString|TextContainer\.retrieveFromRequest\(\)\.getTextEscapeXml\(\)\.get)\("([\w.-]+)"\)')
    patternComment = re.compile(r'^\s*//')

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
                    propRef = PropertyReference(file=file, linenum=lnum, propertyName=match[1])
                    if propRef.propertyName not in configRefs:
                        configRefs[propRef.propertyName] = list()
                    configRefs[propRef.propertyName].append(propRef)


def searchJspDir(dirname):
    # pattern = re.compile('["\'](\w+)')
    pattern = re.compile(r'(?:textContainer\.(?:text|textEscapeXml|textEscapeDouble)\[|grouper:titleFromKeyAndText\()[\'"]([\w.-]+)')

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
    pattern = re.compile(r'^([^\s=]+)')
    patternComment = re.compile(r'^\s*#')
    ret = list()
    line_num = 1
    for line in open(file, 'r'):
        line_num += 1
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

def filterByKnownPatterns(keys: list[str]) -> tuple[list[str], list[str]]:
    """
    Filter out keys that match a known pattern where the captured group
    is in that pattern's values list.

    Returns a tuple of (unmatched_keys, matched_keys).
    """
    unmatched = []
    matched = []
    for key in keys:
        found = False
        for knownPattern in KNOWN_PATTERNS:
            match = re.match(knownPattern.pattern, key)
            if match and match.group(1) in knownPattern.values:
                found = True
                break
        if found:
            matched.append(key)
        else:
            unmatched.append(key)
    return unmatched, matched


def createPlaceholderFile(inputFile: str, outputFile: str, unusedKeys: set):
    """
    Create a copy of the properties file where unused keys have their values
    replaced with **key**, except for keys in PLACEHOLDER_EXCLUDE_KEYS.
    """
    patternProperty = re.compile(r'^([^\s=]+)\s*=')
    patternComment = re.compile(r'^\s*#')

    with open(inputFile, 'r') as infile, open(outputFile, 'w') as outfile:
        for line in infile:
            if re.search(patternComment, line):
                outfile.write(line)
                continue

            match = re.search(patternProperty, line)
            if match:
                propName = match.group(1)
                if propName in unusedKeys and propName not in PLACEHOLDER_EXCLUDE_KEYS:
                    outfile.write(f'{propName} = **{propName}**\n')
                else:
                    outfile.write(line)
            else:
                outfile.write(line)


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
unused, matched = filterByKnownPatterns(unused)
extra = list(set(configRefs.keys()) - set(props))

unused.sort()
matched.sort()
extra.sort()

print("\n\nMissing (fetched properties not in base file):\n\t%s" % "\n\t".join(extra))

print("Total Missing references (out of %d total): %d" % (len(props), len(extra)))

for key in extra:
    print("\nMissing key: %s" % key)
    for ref in configRefs[key]:
        print("\t%s:%d" % (ref.file, ref.linenum))


print("\n\nUnused (in base file but not referenced):\n----------------\n%s\n" % "\n".join(unused))

print("Total unused properties (out of %d total): %d" % (len(props), len(unused)))

if args.show_matched:
    unusedSet = set(unused)
    allMatched = [p for p in props if p not in unusedSet]
    allMatched.sort()
    print("\n\nMatched (referenced in code or by known patterns):\n----------------\n%s\n" % "\n".join(allMatched))
    print("Total matched properties: %d" % len(allMatched))

if args.add_placeholders:
    unusedSet = set(unused)
    inputPath = baseDir + os.path.sep + baseFile
    outputPath = baseDir + os.path.sep + baseFile.replace('.base.properties', '.placeholders.properties')
    createPlaceholderFile(inputPath, outputPath, unusedSet)
    print(f"\nCreated placeholder file: {outputPath}")

# Uncomment below to debug where a specific property is used
#for ref in configRefs["grouper"]:
#    print("%s:%d" % (ref.file, ref.linenum))
