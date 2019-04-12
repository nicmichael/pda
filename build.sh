#!/bin/sh

SCRIPT=`readlink -f $0`
cd `dirname $0`
DIR=`pwd`
echo "Working in directory $DIR ..."

echo "Building PDA Core ..."
cd $DIR/pda-core || exit 1
mvn clean install || exit 1

echo "Building PDA Parsers ..."
cd $DIR/pda-parsers || exit 1
mvn clean install || exit 1

cd $DIR/plugins || exit 1
PLUGDIR=`pwd`
echo "Searching for additional Parsers in `pwd` ..."
parsers=""
for pom in `find . -name pom.xml`
do
  pdir=`dirname $pom`
  if [ "$pdir" != "" -a "$pdir" != "." ] ; then
    cd $PLUGDIR/$pdir || exit 1
    echo "Building parsers in `pwd` ..."
    mvn clean install || exit 1
    parsers="$parsers `pwd`"
  fi
done

rm -rf $DIR/deploy
mkdir $DIR/deploy || exit 1
cd $DIR/deploy || exit 1
echo "Deploying PDA to `pwd`/pda ..."
unzip $DIR//pda-core/target/pda-core.zip || exit 1
unzip $DIR/pda-parsers/target/pda-parsers.zip || exit 1
for p in $parsers
do
  cp $p/target/*.jar pda/lib/ || exit 1
done

echo "PDA deployed to `pwd`/pda"
echo "Run PDA with: `pwd`/pda/bin/pda.sh"
