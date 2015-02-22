Add "mavenCentral()" repository in the project build.gradle to be able to compile the facebook sdk from maven

Run the following command in your terminal to generate the keyhash for using facebook api's
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
