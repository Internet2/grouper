#!/bin/bash

# Counterpart to ../buildGrouperDocs.sh.  Run this on the web server.
#
# buildGrouperDocs.sh builds the site in /tmp/groupersite on the build host and then
# writes a signal file, grouper-docs.WRI.  This script logs into the build host, and if
# the signal file is there, pulls the built site into $TARGET_DIR and marks the signal
# file as consumed by renaming it to grouper-docs.WRI-copied.
#
# Usage:
#   TARGET_DIR=/home/htdocs/software.internet2.edu/grouper/doc ./publishGrouperDocs.sh
#
# Optional environment:
#   BUILD_USER         user to log into the build host as (default grprdist)
#   RSYNC_EXTRA_OPTS   extra rsync options, e.g. "--delete" or "--dry-run"
#
# Prerequisite: passwordless ssh (key based) from this host to $BUILD_USER@$BUILD_HOST.

BUILD_HOST=i2midev6
BUILD_USER=${BUILD_USER:-grprdist}
SIGNAL_DIR=/var/grouper-docs/var
SIGNAL_FILE=$SIGNAL_DIR/grouper-docs.WRI
SITE=/tmp/groupersite

if [ -z "$TARGET_DIR" ]; then
  echo "TARGET_DIR must be set to the destination directory" >&2
  exit 2
fi

if [ ! -d "$TARGET_DIR" ]; then
  echo "TARGET_DIR '$TARGET_DIR' is not a directory" >&2
  exit 2
fi

echo $(date) "Checking $BUILD_HOST for $SIGNAL_FILE"

ssh -o BatchMode=yes $BUILD_USER@$BUILD_HOST "test -f '$SIGNAL_FILE'"
status=$?

if [ $status -eq 1 ]; then
  echo $(date) "No $SIGNAL_FILE on $BUILD_HOST, nothing to do"
  exit 0
elif [ $status -ne 0 ]; then
  echo $(date) "Could not check for $SIGNAL_FILE on $BUILD_HOST (exit $status)" >&2
  exit $status
fi

echo $(date) "Found $SIGNAL_FILE, copying $BUILD_HOST:$SITE/ to $TARGET_DIR"

# Trailing slash on the source copies the contents of the site directory
rsync -rtz --exclude=.git $RSYNC_EXTRA_OPTS "$BUILD_USER@$BUILD_HOST:$SITE/" "$TARGET_DIR/"
status=$?

if [ $status -ne 0 ]; then
  echo $(date) "rsync failed (exit $status), leaving $SIGNAL_FILE in place" >&2
  exit $status
fi

echo $(date) "Completed rsync"

ssh -o BatchMode=yes $BUILD_USER@$BUILD_HOST "mv '$SIGNAL_FILE' '$SIGNAL_FILE-copied'"
status=$?

if [ $status -ne 0 ]; then
  echo $(date) "Could not rename $SIGNAL_FILE to $SIGNAL_FILE-copied (exit $status)" >&2
  exit $status
fi

echo $(date) "Renamed $SIGNAL_FILE to $SIGNAL_FILE-copied"

exit 0
