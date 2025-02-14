@echo off
javac -encoding UTF-8 *.java
if %errorlevel% equ 0 (
    echo Compilation successful! Starting game...
    java PathfindingGame
) else (
    echo Compilation failed!
    pause
)
pause