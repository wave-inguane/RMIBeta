
The Client directory contains the code for the Echo client, the Server directory contains the code for the Echo server and the class implementing the Echo
service.

To compile the server, type 'javac EchoServer.java' in the Server directory.

To compile the client, type 'javac EchoClient.java' in the Client directory.

To run the Server and Client:

1) First start the rmiregistry by typing 'rmiregistry &'
You should probably use a unique port for your rmiregistry to avoid conflicts
with other students. So type 'rmiregistry <port> &' to have the RMI registry use
port number <port>

IMPORTANT: You should have set your CLASSPATH environment variable to include 
the Server directory before starting the rmiregistry.

IMPORTANT: Remember to kill the rmiregistry process before you log off.

2) Start the server in the Server directory by typing:
java -Djava.security.policy=policy.txt EchoServer &

3) Run the client in the Client directory by typing:
java EchoClient <string_you_want_echoed>


