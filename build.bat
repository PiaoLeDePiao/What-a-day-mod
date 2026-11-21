@echo off
setlocal
cd /d "%~dp0"

echo ==========================================================
echo  Chasm Mod - One-click build script
echo ==========================================================

if not exist "gradle.properties" goto not_extracted
if not exist "build.gradle" goto not_extracted

echo "%~dp0" | findstr /I /B /C:"%TEMP%" >nul
if not errorlevel 1 goto not_extracted

rem -- locate or download JDK 21 --
set "JAVA_BIN="
set "JAVA_HOME="
where java >nul 2>nul
if errorlevel 1 goto try_local_jdk
java -version 2>&1 | findstr /C:"21." >nul
if not errorlevel 1 set "JAVA_BIN=java"
if defined JAVA_BIN goto jdk_ready

:try_local_jdk
if exist ".jdk\bin\java.exe" goto use_local_jdk

echo.
echo [1/3] Downloading JDK 21 - about 200 MB, please wait...
curl -L --retry 3 -o jdk21.zip "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse"
if errorlevel 1 goto dl_fail
if not exist jdk21.zip goto dl_fail
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive -Path 'jdk21.zip' -DestinationPath '.jdk_tmp' -Force"
for /d %%d in (.jdk_tmp\*) do move "%%d" ".jdk"
rd /s /q .jdk_tmp 2>nul
del jdk21.zip 2>nul

:use_local_jdk
set "JAVA_BIN=.jdk\bin\java.exe"
set "JAVA_HOME=%~dp0.jdk"

:jdk_ready
echo Using Java: %JAVA_BIN%

rem -- locate or download Gradle 8.10.2 --
if exist ".gradle-dist\gradle-8.10.2\bin\gradle.bat" goto gradle_ready

echo.
echo [2/3] Downloading Gradle 8.10.2 - about 130 MB, please wait...
curl -L --retry 3 -o gradle.zip "https://services.gradle.org/distributions/gradle-8.10.2-bin.zip"
if errorlevel 1 goto dl_fail
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive -Path 'gradle.zip' -DestinationPath '.gradle-dist' -Force"
if errorlevel 1 goto dl_fail
del gradle.zip 2>nul

:gradle_ready
rem -- pin Gradle cache into this folder so restarts keep it --
set "GRADLE_USER_HOME=%~dp0.gradle"

rem -- network pre-check: NeoForge repo must be reachable --
echo.
echo Checking network access to maven.neoforged.net ...
curl -sI -m 20 "https://maven.neoforged.net/releases/" >nul 2>&1
if errorlevel 1 goto net_fail
echo Network OK.
echo.

echo [3/3] Building - first run downloads NeoForge deps, about 300 MB.
echo       This window stays quiet while progress is written to build-log.txt.
echo       Please be patient, it can take 10-30 minutes.
echo.
call ".gradle-dist\gradle-8.10.2\bin\gradle.bat" --no-daemon --console=plain build > build-log.txt 2>&1
if errorlevel 1 goto build_fail

if not exist "build\libs\chasm-0.3.0-all.jar" goto build_missing

echo.
echo ==========================================================
echo  BUILD SUCCESS
echo  Output files in build\libs :
dir /b build\libs
echo.
echo  Copy chasm-0.3.0-all.jar into your mods folder.
echo ==========================================================
pause
exit /b 0

:not_extracted
echo.
echo  ERROR: gradle.properties / build.gradle not found next to
echo  this script. This usually means you ran build.bat from
echo  INSIDE the zip file. Extract the whole zip first, then
echo  run build.bat again.
echo.
pause
exit /b 1

:dl_fail
echo.
echo  ERROR: download failed. Check your network and try again,
echo  or install JDK 21 manually and put java on PATH.
echo.
pause
exit /b 1

:net_fail
echo.
echo  ERROR: this computer cannot reach maven.neoforged.net.
echo  NeoForge downloads are blocked on this network. That is
echo  why the build failed with plugin not found.
echo.
echo  How to fix:
echo   1. Rent the cloud PC in Hong Kong / Singapore / Japan /
echo      US west instead of a mainland location, then rerun.
echo   2. Or run a proxy / VPN on the cloud PC, then rerun.
echo   3. Or build on a PC with a direct overseas network.
echo.
echo  The mod files on your phone are unaffected.
echo.
pause
exit /b 1

:build_missing
echo.
echo  WARNING: Gradle exited without error, but no build folder
echo  was created. This means the build was interrupted.
echo.
echo  Last 40 lines of build-log.txt :
powershell -NoProfile -Command "Get-Content 'build-log.txt' -Tail 40"
echo.
echo  Send a screenshot of this window or the build-log.txt
echo  file back so it can be fixed.
echo.
pause
exit /b 1

:build_fail
echo.
echo  BUILD FAILED. Last 50 lines of build-log.txt :
powershell -NoProfile -Command "Get-Content 'build-log.txt' -Tail 50"
echo.
echo  Send a screenshot of this window or the build-log.txt
echo  file back so it can be fixed.
echo.
pause
exit /b 1
