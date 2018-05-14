# Disconnect Premium for Android

## Build instructions
1. The strongswan project needs to be build to generate the jni source files
* Download strongswan project to /strongswan
* Build strongswan project referring https://wiki.strongswan.org/projects/strongswan/wiki/AndroidVPNClientBuild

2. Clone git clone git://git.strongswan.org/android-ndk-boringssl.git -b ndk-static openssl into ./DisconnectPro/app/src/main/jni 

3. Load DisconnectPro project into Android studio and build
