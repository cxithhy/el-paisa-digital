@echo off
REM ============================================================
REM  Script de MANTENIMIENTO: Restaura la base de datos desde un backup.
REM  Uso: restore_db.bat nombre_del_archivo.sql
REM  (el archivo debe estar dentro de la carpeta backups\)
REM ============================================================

setlocal

if "%~1"=="" (
    echo Uso: restore_db.bat nombre_del_archivo.sql
    echo Ejemplo: restore_db.bat elpaisadb_11-07-2026_1930.sql
    exit /b 1
)

if not exist "%~dp0credenciales_db.bat" (
    echo [ERROR] No se encontro scripts\credenciales_db.bat
    exit /b 1
)
call "%~dp0credenciales_db.bat"

if defined MYSQL_BIN_DIR (
    set MYSQL_EXE="%MYSQL_BIN_DIR%\mysql.exe"
) else (
    set MYSQL_EXE=mysql
)

set ARCHIVO=%~dp0..\backups\%~1

if not exist "%ARCHIVO%" (
    echo [ERROR] No se encontro el archivo: %ARCHIVO%
    exit /b 1
)

echo ADVERTENCIA: esto sobrescribira los datos actuales de %DB_NAME% con el contenido de %~1
set /p CONFIRMAR="Escribe SI para continuar: "
if /I not "%CONFIRMAR%"=="SI" (
    echo Operacion cancelada.
    exit /b 0
)

%MYSQL_EXE% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < "%ARCHIVO%"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Base de datos restaurada desde %~1
) else (
    echo [ERROR] Fallo la restauracion.
)

endlocal
