#!/usr/bin/env bash
set -e

APP=testutil

###### Flags start ##### {~flags:
DRIVER=0
###### Flags end   ##### ~}

if [ "$DRIVER" -ne "0" ]; then
  LINK=/usr/local/bin/$APP
fi


##################################################
# Parse arguments:
#   [clean | skiptest]
##################################################

CLEAN=""
NOTEST=""
DONEARGS=0

while [ "$DONEARGS" -eq 0 ]; do
  if [ "$1" == "" ]; then
    DONEARGS=1
  elif [ "$1" == "clean" ]; then
    CLEAN="clean"
    shift 1
  elif [ "$1" == "skiptest" ]; then
    NOTEST="-DskipTests"
    shift 1
  ###### Custom options start ##### {~options:
  ###### Custom options end   ##### ~}
  else
    echo "Unrecognized argument: $1"
    exit 1
  fi
done


##################################################
# Perform clean, if requested
#
if [ "$CLEAN" != "" ]; then
  echo "...cleaning $APP"
  mvn clean
  if [ "$DRIVER" -ne "0" ]; then
    if [ -f $LINK ]; then
      unlink $LINK
    fi
  fi

###### Custom clean statements start ##### {~clean:
###### Custom clean statements end   ##### ~}
fi





##################################################
# Compile and test
#
if [ "$NOTEST" != "" ]; then
  echo "...skipping tests"
fi

###### Custom pre-compile start ##### {~precompile:
###### Custom pre-compile end   ##### ~}

mvn install $NOTEST




##################################################
# Create a symbolic link to the driver script
##################################################

if [ "$DRIVER" -ne "0" ]; then
  if [ ! -f $LINK ]; then
    DIR=$(pwd)
    ln -sf $DIR/driver.sh $LINK
  fi
fi


###### Custom post-compile start ##### {~postcompile:
###### Custom post-compile end   ##### ~}
