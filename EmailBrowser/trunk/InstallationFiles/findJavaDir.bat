@echo off
::This batch file only tested under Windows 2000
::It will detect the short path of the current version 
::of the Java runtime executable

::First test to see if we are on NT or similar OS by seeing 
::if the ampersand is interpreted as a command separator
> reg1.txt echo 1234&rem
type reg1.txt | find "rem"
if not errorlevel 1 goto WIN9X

::Find the current (most recent) Java version
start /w regedit /e reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment"
type reg1.txt | find "CurrentVersion" > reg2.txt
if errorlevel 1 goto ERROR
for /f "tokens=2 delims==" %%x in (reg2.txt) do set JavaTemp=%%~x
if errorlevel 1 goto ERROR
REM echo Java Version = %JavaTemp%
del reg1.txt
del reg2.txt

::Get the home directory of the most recent Java
start /w regedit /e reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\%JavaTemp%"
type reg1.txt | find "JavaHome" > reg2.txt
if errorlevel 1 goto ERROR
for /f "tokens=2 delims==" %%x in (reg2.txt) do set JavaTemp=%%~x
if errorlevel 1 goto ERROR
REM echo Java home path (per registry) = %JavaTemp%
del reg1.txt
del reg2.txt

::Convert double backslashes to single backslashes
set JavaHome=
:WHILE
  if "%JavaTemp%"=="" goto WEND
  if not "%JavaHome%"=="" set JavaHome=%JavaHome%\
  for /f "delims=\" %%x in ("%JavaTemp%") do set JavaHome=%JavaHome%%%x
  for /f "tokens=1,* delims=\" %%x in ("%JavaTemp%") do set JavaTemp=%%y
  goto WHILE
:WEND
set JavaTemp=
REM echo Java home path (long, with spaces) = %JavaHome%

::Convert long path (with spaces) into a short path
for %%x in ("%JavaHome%") do set JavaHome=%%~fsx
REM echo Java home path (short path, no spaces) = %REJavaHome%

::Test the java path to see if there really is a java.exe
if not exist %JavaHome%\bin\java.exe goto ERROR

::Make changes to the PATH
REM echo Insert code here that needs to know the short path to Java.
set path=%JavaHome%\bin;%path%
goto DONE

:WIN9X
REM echo Insert code here for Windows 9x
goto DONE

:ERROR
echo CANT FIND JAVA. PLEASE MAKE SURE IT IS INSTALLED
goto DONE

:DONE
