@echo off

IF "%1" == "RUN" GOTO RUN

REM # ##########################################
REM # Test for Operating System                #
REM # ##########################################
IF "%OS%" == "Windows_NT" GOTO WINNT
GOTO WIN9X

:WIN9X
echo Starting on Windows 9x
command.com /e:1024 /cpda.bat RUN %1 %2 %3 %4 %5 %6 %7 %8
GOTO END

:WINNT
echo Starting on Windows NT
call pda.bat RUN %1 %2 %3 %4 %5 %6 %7 %8
GOTO END

REM # ##########################################
REM # Prepare to run Program                   #
REM # ##########################################
:RUN
echo Starting PDA
cd ..

REM # ##########################################
REM # Classpath                                #
REM # ##########################################
SET CP=lib/pda-core-2.1.0.jar;lib/sgt-3.0.jar;lib/pda-parsers-2.1.0.jar

REM # ##########################################
REM # JVM Settings                             #
REM # ##########################################
IF "%PDA_HEAP%" == "" SET PDA_HEAP=1024m
SET PDA_JAVA_ARGS=-Xmx%PDA_HEAP%

REM # ##########################################
REM # Run Program                              #
REM # ##########################################
SET PDA_JAVA_ARGUMENTS=%PDA_JAVA_ARGS% -cp %CP% de.nmichael.pda.Main %2 %3 %4 %5 %6 %7 %8 %9

echo PDA Command Line: javaw %PDA_JAVA_ARGUMENTS%

IF "%OS%" == "Windows_NT" GOTO STARTNT
GOTO START9X

:STARTNT
REM Path for Windows 7 (64 Bit)
SET PATH=%PATH%;C:\Windows\SysWOW64
start /b javaw %PDA_JAVA_ARGUMENTS%
GOTO END

:START9X
javaw %PDA_JAVA_ARGUMENTS%
GOTO END

@CLS
:END 