#!/bin/sh

SCRIPT=`basename $0`

# GC Log File
GCLOG=javagc.log
DEMOLOG=demo.csv

# Runtime
if [ "$1" != "" ] ; then
  RUNTIME=$1
else
  RUNTIME=5
fi


# PDA Installation Directory
if [ "$PDA_HOME" = "" ] ; then
  PWD=`pwd`
  cd `dirname $0`/..
  PDA_HOME=`pwd`
  cd $PWD
fi

# Other Directories
PDA_LIB=$PDA_HOME/lib

# Java Args and Classpath
PDA_JAVA_ARGS="-Xms50M -Xmx150M -XX:NewSize=20M -XX:MaxNewSize=20M -XX:SurvivorRatio=2 -XX:TargetSurvivorRatio=90 -XX:+UseSerialGC -XX:MaxTenuringThreshold=15 -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:${GCLOG:?}"
PDA_CLASSPATH=
for jar in `find $PDA_LIB -name \*.jar`
do
  PDA_CLASSPATH=$PDA_CLASSPATH:$jar
done
PDA_CLASSPATH=$PDA_CLASSPATH

# Logging Info
echo "[$SCRIPT] Creating a GC Logfile ..."
echo "[$SCRIPT] GC Log File          : $GCLOG"
echo "[$SCRIPT] Demo CSV File        : $DEMOLOG"
echo "[$SCRIPT] Runtime              : $RUNTIME min"
echo "[$SCRIPT] Running              : java $PDA_JAVA_ARGS -cp $PDA_CLASSPATH de.nmichael.pda.demo.WriteJavaGC ${RUNTIME:?}"

java $PDA_JAVA_ARGS -cp $PDA_CLASSPATH de.nmichael.pda.demo.WriteJavaGC ${RUNTIME:?} ${DEMOLOG:?}

echo "[$SCRIPT] Your GC Log File is  : $GCLOG"
echo "[$SCRIPT] Your Demo CSV File is: $DEMOLOG"
echo "[$SCRIPT] Done."
