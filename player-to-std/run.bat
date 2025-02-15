@echo off

mvn -q -e -f .\player-to-std\pom.xml exec:java -Dexec.mainClass=rpd.player_std.Main -Dplayer=%1