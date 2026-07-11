@echo off
REM ============================================================
REM  Script de MANTENIMIENTO: Backup automatico de la base de datos.
REM  Genera un dump .sql con fecha y hora en la carpeta backups\.
REM
REM  Requiere: mysqldump en el PATH (viene con MySQL Server / Workbench)
REM  Requiere: el archivo credenciales_db.bat con las variables:
REM            DB_NAME, DB_USER, DB_PASSWORD
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

REM Carpeta de backups (se crea si no existe)
set BACKUP_DIR=%~dp0..\backups
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Nombre de archivo con fecha y hora: elpaisadb_2026-07-11_1930.sql
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do set FECHA=%%d-%%b-%%c
set HORA=%time:~0,2%%time:~3,2%
set HORA=%HORA: =0%
set ARCHIVO=%BACKUP_DIR%\%DB_NAME%_%FECHA%_%HORA%.sql

echo Generando backup de %DB_NAME% en %ARCHIVO% ...
mysqldump -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% > "%ARCHIVO%"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Backup generado correctamente: %ARCHIVO%
) else (
    echo [ERROR] Fallo la generacion del backup. Verifica que mysqldump este en el PATH y las credenciales sean correctas.
)

REM Limpieza: conserva solo los ultimos 10 backups para no llenar el disco
for /f "skip=10 delims=" %%f in ('dir "%BACKUP_DIR%\%DB_NAME%_*.sql" /b /o-d 2^>nul') do (
    echo Eliminando backup antiguo: %%f
    del "%BACKUP_DIR%\%%f"
)

endlocal
