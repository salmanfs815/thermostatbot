# ThermostatBot
IRC Bot for Red Hat's OpenJDK team

---

Clone repo:
```bash
git clone https://github.com/salmanfs815/thermostatbot.git
cd thermostatbot
```  

Set JDK for Maven to Java 11:
```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
```  

Package with Maven:
```bash
mvn package
```  

Configure bot properties and sender timezones:
```bash
cp bot.properties.template bot.properties
vim senderTZs.prope/home/sasiddiq/thermostatbot/target/classesrties
cp senderTZs.properties.template senderTZs.properties
vim senderTZs.properties
```  

Run IRC Bot
```bash
mvn exec:java
```  
