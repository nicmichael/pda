#!/bin/sh

BASEDIR=`dirname $0`
VERSION=${1:?}
WORKDIR=`pwd`

# MUST FIRST BUILD pda-core and pda-parsers
FILES="$BASEDIR/pda-core/target/pda-core.zip $BASEDIR/pda-parsers/target/pda-parsers.zip"

for f in $FILES
do
  if [ ! -e $f ] ; then
    echo "Cound not find $f, please build first."
    exit 1
  fi
done

BUILDDIR="/tmp/pda-`date +%Y%m%d%H%M%S`"
echo "BUILDDIR=$BUILDDIR"
mkdir -p $BUILDDIR || exit 1

echo "Copying $FILES ..."
for f in $FILES
do
  cp $f $BUILDDIR/ || exit 1
done

cd $BUILDDIR || exit 1

echo "Unpacking ..."
for f in *.zip
do
  unzip $f
done

ARCHIVE=pda${VERSION}.zip
echo "Creating final archive $ARCHIVE..."

zip -r $ARCHIVE pda || exit 1
mv $ARCHIVE $WORKDIR/

echo "Created Archive: $WORKDIR/$ARCHIVE"

exit 0

