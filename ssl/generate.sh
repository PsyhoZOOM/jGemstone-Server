echo "Generate the Client and Server Keystores" > /dev/null
keytool -genkeypair -alias plainserverkeys -keyalg RSA -dname "CN=Plain Server,OU=kl2217,O=kl2217org,L=Boston,ST=MA,C=US" -keypass jgemstone -keystore plainserver.jks -storepass jgemstone
keytool -genkeypair -alias plainclientkeys -keyalg RSA -dname "CN=Plain Client,OU=kl2217,O=kl2217org,L=Boston,ST=MA,C=US" -keypass jgemstone -keystore plainclient.jks -storepass jgemstone
echo "Export the server public certificate and create a seperate keystore">/dev/null
keytool -exportcert -alias plainserverkeys -file serverpub.cer -keystore plainserver.jks -storepass jgemstone
keytool -importcert -keystore serverpub.jks -alias serverpub -file serverpub.cer -storepass jgemstone
echo "Export the client public certificate and create a seperate keystore">/dev/null
keytool -exportcert -alias plainclientkeys -file clientpub.cer -keystore plainclient.jks -storepass jgemstone
keytool -importcert -keystore clientpub.jks -alias clientpub -file clientpub.cer -storepass jgemstone
