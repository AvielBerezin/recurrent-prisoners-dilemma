@echo on


echo java -cp "%%~dp0\jars" rpd.player_std.Main > "%~dp0%1\run.bat"
