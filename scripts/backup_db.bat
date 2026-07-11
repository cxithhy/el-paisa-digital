@echo off
REM ============================================================
REM  Script de MANTENIMIENTO: Backup automatico de la base de datos.
REM  Genera un dump .sql con fecha y hora en la carpeta backups\.
REM
REM  Requiere: mysqldump (viene con MySQL Server / Workbench).
REM            Si no esta en el PATH del sistema, definir MYSQL_BIN_DIR
REM            en credenciales_db.bat (ver credenciales_db.bat.ejemplo).
REM  Requiere: el archivo credenciales_db.bat con las variables:
REM            DB_NAME, DB_USER, DB_PASSWORD, (opcional) MYSQL_BIN_DIR
REM  (ese archivo NO se sube a GitHub, ver credenciales_db.bat.ejemplo)
REM ============================================================

setlocal enabledelayedexpansion

REM Carga las credenciales desde el archivo local (no versionado)
if not exist "%~dp0credenciales_db.bat" (
    echo [ERROR] No se encontro scripts\credenciales_db.bat
    echo         Copia credenciales_db.bat.ejemplo, renombralo y completa tus datos reales.
    exit /b 1
)
call "%~dp0credenciales_db.bat"

REM Si se definio MYSQL_BIN_DIR, usar mysqldump desde ahi; si no, asumir que esta en el PATH
if defined MYSQL_BIN_DIR (
    set MYSQLDUMP_EXE="%MYSQL_BIN_DIR%\mysqldump.exe"
) else (
    set MYSQLDUMP_EXE=mysqldump
)

REM Carpeta de backups (se crea si no existe)
set BACKUP_DIR=%~dp0..\backups
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Fecha y hora via PowerShell: evita problemas con la configuracion regional de Windows
for /f "delims=" %%a in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HHmm"') do set FECHA_HORA=%%a
set ARCHIVO=%BACKUP_DIR%\%DB_NAME%_%FECHA_HORA%.sql

echo Generando backup de %DB_NAME% en %ARCHIVO% ...
%MYSQLDUMP_EXE% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% > "%ARCHIVO%"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Backup generado correctamente: %ARCHIVO%
) else (
    echo [ERROR] Fallo la generacion del backup.
    echo         Si el error dice "no se reconoce como un comando", agrega MYSQL_BIN_DIR
    echo         en scripts\credenciales_db.bat con la carpeta donde esta mysqldump.exe.
)

REM Limpieza: conserva solo los ultimos 10 backups para no llenar el disco
for /f "skip=10 delims=" %%f in ('dir "%BACKUP_DIR%\%DB_NAME%_*.sql" /b /o-d 2^>nul') do (
    echo Eliminando backup antiguo: %%f
    del "%BACKUP_DIR%\%%f"
)

endlocal
