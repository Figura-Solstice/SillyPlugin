@echo off
setlocal enabledelayedexpansion

if not exist ".\silly_out" mkdir ".\silly_out"
del /Q ".\silly_out\*"

call gradlew.bat clean
call gradlew.bat chiseledBuild

for /D %%d in (.\versions\*) do (
    set "ver=%%~nxd"
    for %%f in ("%%d\build\libs\sillyplugin-*.jar") do (
        set "filename=%%~nf"
        echo copying silly plugin !ver! to silly_out!
        copy "%%f" ".\silly_out\!filename!-!ver!.jar"
    )
)

endlocal
