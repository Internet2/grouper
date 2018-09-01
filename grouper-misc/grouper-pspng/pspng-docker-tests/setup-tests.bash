# This script needs to be dotted into parent scripts so 
# this script can manipulate the parent script's environment
set -o errexit
set -o pipefail
set -o nounset

D=$(cd $(dirname "${BASH_SOURCE[0]}"); pwd -P)
cd "$D"

PATH="$D:$PATH"

. ./functions.bash
. ./functions-testharness.bash

API=/opt/grouper/grouper.apiBinary
GSH=$API/bin/gsh

tempdir_into_T

