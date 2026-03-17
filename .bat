@echo off
setlocal

REM ─────────────────────────────
REM PETicle 프로젝트용 .env 자동 생성기
REM by GPT

REM 
if exist .env (
    echo [!] 기존에 .env 파일이 있습니다.
    choice /M "덮어쓰시겠습니까?"
    if errorlevel 2 (
        echo [🚫] .env 파일을 유지합니다.
        goto CHECK_GITIGNORE
    )
)

REM 
echo MYSQL_PORT=3306 > .env
echo [✔] .env 파일이 생성되었습니다!
echo     사용 포트: 3306
echo.

:CHECK_GITIGNORE
REM 
findstr /C:".env" .gitignore >nul 2>&1
if errorlevel 1 (
    echo .env>> .gitignore
    echo [✔] .gitignore에 .env 자동 추가됨
) else (
    echo [ℹ️] .gitignore에 이미 .env가 포함되어 있습니다.
)

pause
endlocal
