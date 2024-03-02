@echo off

set JAVA_HOME=C:\Users\aviel\.jdks\openjdk-20.0.1\
mvn -q -f .\player-to-std\pom.xml exec:java -Dexec.mainClass=rpd.player_std.Main -Dplayer=%1