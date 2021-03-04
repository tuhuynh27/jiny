# First, use graalvm
# jabba use graalvm@20.2.0

# Server
./gradlew :keva:server:shadowJar
native-image --no-server --no-fallback -H:ReflectionConfigurationFiles=native-config.json --allow-incomplete-classpath -jar keva/server/build/libs/server-1.0-SNAPSHOT-all.jar keva-server
mv keva-server keva/builds/macOS_x86/keva-server

# Client
./gradlew :keva:client:shadowJar
native-image --no-server --no-fallback -H:ReflectionConfigurationFiles=native-config.json --allow-incomplete-classpath -jar keva/client/build/libs/client-1.0-SNAPSHOT-all.jar keva-client
mv keva-client keva/builds/macOS_x86/keva-client
