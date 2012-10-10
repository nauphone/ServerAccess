#!/bin/bash

function require_app() {
    if [[ -z `which $1` ]]
    then
        echo "$1 not found - exiting"
        exit 2
    fi
}

require_app fakeroot
require_app md5deep

# move to the project root dir
unset CDPATH
cd "$(dirname "${BASH_SOURCE[0]}")"/..

# detect version
NAME="naumen-server-access"
VERSION=`cat ./VERSION.TXT 2>/dev/null`
if [[ -z $VERSION ]]
then
    echo "Cannot find VERSION.TXT file."
    exit 1;
fi;

tmpdir="naumen-server-access_"$VERSION"_all"

mkdir -p "$tmpdir/DEBIAN"

echo "creating directories..."

mkdir -p "$tmpdir/opt/naumen/server-access/lib"
mkdir -p "$tmpdir/usr/share/applications"
mkdir -p "$tmpdir/usr/share/pixmaps/naumen-server-access"

echo "copying files"

cp "./distr/servacc.jar" "$tmpdir/opt/naumen/server-access/"
cp ./resource/log4j.properties $tmpdir/opt/naumen/server-access/
cp "./resource/server-access" "$tmpdir/opt/naumen/server-access/"
cp -R "./resource/icons/" "$tmpdir/usr/share/pixmaps/naumen-server-access"
cp "./resource/naumen-server-access.desktop" "$tmpdir/usr/share/applications"
cp "./resource/naumen-server-access.png" "$tmpdir/usr/share/pixmaps/naumen-server-access"
cp "./lib/mindterm.jar" "$tmpdir/opt/naumen/server-access/lib"
cp "./lib/log4j-1.2.17.jar" "$tmpdir/opt/naumen/server-access/lib"
cp "LICENSE.GPL" "$tmpdir/opt/naumen/server-access/"
touch $tmpdir/opt/naumen/server-access/serveraccess.log
chmod a+rw $tmpdir/opt/naumen/server-access/serveraccess.log

md5deep -rl "$tmpdir/" > "$tmpdir/DEBIAN/md5sums"

touch "$tmpdir/conffiles"

echo "creating control file..."

let SIZE=0
for l in 'opt' 'usr'
do
    let SIZE=$SIZE+`du -s "$tmpdir/$l" | awk '{print $1}'`
done;

cat <<-EOF > "$tmpdir/DEBIAN/control"
Package: $NAME
Version: $VERSION
Provides: $NAME
Maintainer: Grigori Frolov <gfrolov@naumen.ru>
Architecture: all
Section: net
Source: https://github.com/w31rd0/ServerAccess
Description: An SSH,FTP and HTTP client.
 A tool for getting servers info from db and connecting to selected servers by ssh
 or others tools (uses ssh port mapping to local ports)
Depends: libswt-gtk-3-java, libswt-gtk-3-jni
Installed-Size: $SIZE
Priority: optional
Essential: no
EOF

fakeroot dpkg-deb --build "$tmpdir"

echo "cleaning everything..."
rm -rf "$tmpdir"
echo "Success"
