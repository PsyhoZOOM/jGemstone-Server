#!/bin/bash
echo "Generate the Client and Server Keystores" > /dev/null
keytool -genkeypair -alias plainserverkeys -keyalg RSA -dname "CN=Plain Server,OU=YUVideo,O=YUVideo SAT,L=Petrovac na Mlavi,ST=Serbia,C=RS" -keypass jgemstone -keystore plainserver.jks -storepass jgemstone -validity 4026 -deststoretype pkcs12
keytool -genkeypair -alias plainclientkeys -keyalg RSA -dname "CN=Plain Client,OU=YUVideo,O=YUVideo SAT,L=Petrovac na Mlavi,ST=Serbia,C=RS" -keypass jgemstone -keystore plainclient.jks -storepass jgemstone -validity 4026 -deststoretype pkcs12
echo "Generate PKCS12"
#keytool -importkeystore -srckeystore plainserver.jks -destkeystore plainserver.jks -deststoretype pkcs12
#keytool -importkeystore -srckeystore plainclient.jks -destkeystore plainclient.jks -deststoretype pkcs12

echo "Export the server public certificate and create a seperate keystore">/dev/null
keytool -exportcert -alias plainserverkeys -file serverpub.cer -keystore plainserver.jks -storepass jgemstone
keytool -importcert -keystore serverpub.jks -alias serverpub -file serverpub.cer -storepass jgemstone
echo "Export the client public certificate and create a seperate keystore">/dev/null
keytool -exportcert -alias plainclientkeys -file clientpub.cer -keystore plainclient.jks -storepass jgemstone
keytool -importcert -keystore clientpub.jks -alias clientpub -file clientpub.cer -storepass jgemstone


keytool -importcert -v -trustcacerts -file "clientpub.cer" -alias clientPub -keystore "clientpub.bks" -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath "bcprov-jdk15on-159.jar" -storetype BKS -storepass jgemstone
keytool -importcert -v -trustcacerts -file "serverpub.cer" -alias serverPubPub -keystore "serverpub.bks" -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath "bcprov-jdk15on-159.jar" -storetype BKS -storepass jgemstone