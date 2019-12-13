#!/bin/bash

HASHCMD="md5deep"

if ! which fakeroot ; then
    echo fakeroot not found
    exit 2
fi

if ! which md5deep ; then
    if which hashdeep ; then
        HASHCMD="hashdeep"
    else
        echo md5deep or hashdeep not found
        exit 2
    fi
fi

# move to the project root dir
unset CDPATH
cd "$(dirname "${BASH_SOURCE[0]}")"/..

# detect version
NAME="naumen-server-access"
VERSION=`git describe | cut -d- -f2,3`
if [[ -z $VERSION ]]
then
    echo "Cannot detect git version."
    exit 1;
fi;

tmpdir="naumen-server-access_"$VERSION"_all"

mkdir -p $tmpdir/DEBIAN

echo creating directories...

TARGET=$tmpdir/opt/naumen/server-access
mkdir -p $TARGET/lib
mkdir -p $tmpdir/usr/share/applications
mkdir -p $tmpdir/usr/share/pixmaps/naumen-server-access

echo copying files

cp ./lin64/build/libs/ServerAccess-all.jar $TARGET/
cp ./lin64/resources/server-access $TARGET/
cp ./lin64/resources/naumen-server-access.desktop $tmpdir/usr/share/applications

cp -R ./base/src/main/resources/icons/ $tmpdir/usr/share/pixmaps/naumen-server-access
cp ./resource/naumen-server-access.png $tmpdir/usr/share/pixmaps/
cp LICENSE.GPL $TARGET/
touch $TARGET/serveraccess.log
chmod a+rw $TARGET/serveraccess.log

$HASHCMD -rl $tmpdir/ > $tmpdir/DEBIAN/md5sums

touch $tmpdir/conffiles

echo creating control file...

let SIZE=0
for l in 'opt' 'usr'
do
    let SIZE=$SIZE+`du -s "$tmpdir/$l" | awk '{print $1}'`
done;

cat <<-EOF > $tmpdir/DEBIAN/control
Package: $NAME
Version: $VERSION
Provides: $NAME
Maintainer: Andrey Hitrin <ahitrin@naumen.ru>
Architecture: all
Section: net
Source: https://github.com/nauphone/ServerAccess
Description: An SSH,FTP and HTTP client.
 A tool for getting servers info from db and connecting to selected servers by ssh
 or others tools (uses ssh port mapping to local ports)
Depends: openjdk-8-jre-headless | java-runtime-headless (>= 1.8) | java8-runtime-headless
Recommends: gftp
Installed-Size: $SIZE
Priority: optional
Essential: no
EOF

fakeroot dpkg-deb --build $tmpdir

echo cleaning everything...
rm -rf $tmpdir
echo success
