



@echo off

mvn -q -f .\player-to-std\pom.xml exec:java -Dexec.mainClass=rpd.player_std.Main -Dplayer=%1