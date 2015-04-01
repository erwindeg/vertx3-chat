# Vertx3 chat application
A simple chatbox application that makes use of the distributed eventbus to communicate between nodes. Uses mongodb to store messages and AngularJS for the frontend. 

# Usage
The application needs mongodb running on the same host.
Clone the repository and use mvn install to build the application. Use java-jar target\vertx3-chat-0.0.1-SNAPSHOT-fat.jar to run in standalone mode 
and mvn -jar target\vertx3-chat-0.0.1-SNAPSHOT-fat.jar -cluster -cluster-host <ip-address> to run in clustered mode.
Point you browser to http://localhost:8080/ to start chatting.
