#!/bin/sh

SCRIPT=`basename $0`

# PDA Installation Directory
if [ "$PDA_HOME" = "" ] ; then
  CWD=`pwd`
  cd `dirname $0`/..
  PDA_HOME=`pwd`
  cd $CWD
fi

# Other Directories
PDA_LIB=$PDA_HOME/lib

# Java Heap
if [ "$PDA_HEAP" = "" ] ; then
  PDA_HEAP=1024m
fi

# Java Args and Classpath
PDA_JAVA_ARGS="-Xmx$PDA_HEAP"
PDA_CLASSPATH=
for jar in `find $PDA_LIB -name \*.jar`
do
  PDA_CLASSPATH=$PDA_CLASSPATH:$jar
done
PDA_CLASSPATH=$PDA_CLASSPATH

# Logging Info
echo "[$SCRIPT] CWD=`pwd`"
echo "[$SCRIPT] PDA_HOME=$PDA_HOME"
echo "[$SCRIPT] PDA_LIB=$PDA_LIB"
echo "[$SCRIPT] PDA_HEAP=$PDA_HEAP"
echo "[$SCRIPT] PDA_JAVA_ARGS=$PDA_JAVA_ARGS"
echo "[$SCRIPT] PDA_JAVA_OPTS=$PDA_JAVA_OPTS"
echo "[$SCRIPT] PDA_CLASSPATH=$PDA_CLASSPATH"

java $PDA_JAVA_ARGS $PDA_JAVA_OPTS -cp $PDA_CLASSPATH -Dpda.home=$PDA_HOME -Dpda.lib=$PDA_LIB de.nmichael.pda.Main $*
