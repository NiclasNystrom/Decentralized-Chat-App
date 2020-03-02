# Decentralized-Chat-App
Decentralized, except for a name server, chat application using common distributed algorithms. Includes GUI with group chat and debug functionality and ensures:

* Group management and chat functionality
* **Unreliable multicaster** communication
* **Unordered** and **Causal** ordering
* **Debug** functionality
* Graphical interface (Normal and Debug)


![System](images/system.png)


# Requirements:
Maven and Java 1.8+

# Install 
Execute build.sh
Generates NameServer.jar and ChatApp.jar

# Run

```
java -jar NameServer.jar
java -jar ChatApp.jar
```

# Usage



![Host configuration](images/0_host_config.png)


![Home](images/1_home.png)

![Create Groups](images/2_create_group.png)

![Chat Normal](images/3_chat_normal.png)

![Chat Debug](images/3_chat_debug.png)
