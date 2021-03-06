# ThermostatBot
IRC Bot for Red Hat's Thermostat/JMC team

[![Build Status](https://travis-ci.org/salmanfs815/thermostatbot.svg?branch=master)](https://travis-ci.org/salmanfs815/thermostatbot)
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
vim bot.properties
cp senderTZs.properties.template senderTZs.properties
vim senderTZs.properties
```  

Run IRC Bot
```bash
mvn exec:java
```  
