#!/bin/bash

EXT="-coverage.ec"

# Get the ec files
FILES=`find . -type f -name "*$EXT"`

# For each file
for FILE in $FILES
do
  ## use the basename to get the module name
  echo "$FILE"
  MODULE=`basename $FILE $EXT`
  echo "$MODULE"
  mkdir -p "$MODULE/build/outputs/code-coverage/connected"
  echo "$MODULE/build/outputs/code-coverage/connected"
  cp "$FILE" "$MODULE/build/outputs/code-coverage/connected/coverage.ec"
done

# firebase/matrix_0/walleye-27-en_US-portrait/artifacts/sdcard/<app>-coverage.ec
# to
# <app>/build/outputs/code-coverage/connected/coverage.ec