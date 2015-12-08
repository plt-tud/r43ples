@echo off
rem din25.bat
rem Installation der TU Schriftarten (DIN-Bold) unter MiKTeX 2.5 oder hoeher
rem
rem Autor: Klaus Bergmann
rem nach dem Univers-Installationsskript von
rem Michael Kluge, ZIH, kluge@zhr.tu-dresden
rem Mathias Kortke, Fakultät EuI, IAS
rem Martin Heinze, FSR MW
rem
rem Letzte Aenderung: 26. Januar 2008
rem
rem Getestet auf: Microsoft Windows XP

cls
echo.
echo   Installation der TU Schriftarten unter MiKTeX 2.5 oder hoeher
echo  ==============================================================================
echo.
echo.
echo HINWEIS: Alle hier angegebenen Verzeichnisse finden Sie im Settings-
echo          Programm von MiKTeX unter dem Reiter "Roots".
echo.
echo  ==============================================================================
echo.
echo Bitte geben Sie den Pfad zum MiKTeX-Verzeichnis ein!
echo Zum Beispiel:
echo               "C:\Programme\MiKTeX 2.5"
echo.
echo.
:proof_Install
set /p Install=MiKTeX-Verzeichnis (ohne Anfuerungszeichen):
echo.
if not exist "%Install%\miktex\bin\tex.exe" (
echo FEHLER: Das angebene Verzeichnis scheint nicht das MiKTeX-Verzeichnis
echo         zu sein ^(tex.exe nicht gefunden^)!
echo         Bitte geben Sie den Pfad zu MiKTeX ein!
echo.
echo.
goto proof_Install
)
echo.
if not "%UserConfig%" == "" goto start_install
echo.
echo  ==============================================================================
echo.
echo Bitte geben Sie den Pfad zum CommonConfig-Verzeichnis ein!
echo Zum Beispiel:
echo               "C:\Dokumente und Einstellungen\All Users\Anwendungsdaten\MiKTeX"
echo.
echo.
:proof_CommonConfig
set /p CommonConfig=CommonConfig-Verzeichnis (ohne Anfuerungszeichen):
if not exist "%CommonConfig%\miktex\base\mf.base" (
echo FEHLER: Das angebene Verzeichnis scheint nicht das CommonConfig-Verzeichnis
echo         zu sein ^(mf.base nicht gefunden^)!
echo         Bitte geben Sie den Pfad zu MiKTeX ein!
echo.
echo.
goto proof_CommonConfig
)
:start_install
echo.
echo  ==============================================================================
echo.
echo Beginne Installation ... bitte warten ...
echo.
echo.

set PATH=%PATH%;"%Install%\miktex\bin"

rmdir /s /q converted > nul
mkdir converted

copy DINBd___.pfb converted\dinb8a.pfb > nul
copy DINBd___.afm converted\dinb8a.afm > nul

cd converted

echo \input fontinst.sty > ltx.tex
echo \latinfamily{din}{} >> ltx.tex
echo \bye >> ltx.tex
tex ltx.tex

dir /b *.pl > files.txt
for /f "delims=. " %%i in (files.txt) do pltotf %%i.pl %%i.tfm

dir /b *.vpl > files.txt
for /f "delims=. " %%i in (files.txt) do vptovf %%i.vpl %%i.vf %%i.tfm

echo dinb8r DINBold "DIN-Bold" ^<8r.enc ^<dinb8a.pfb > dinbold.map

mkdir "%Install%\tex\latex\univers"
mkdir "%Install%\fonts\tfm\public\univers"
mkdir "%Install%\fonts\afm\public\univers"
mkdir "%Install%\fonts\vf\public\univers"
mkdir "%Install%\fonts\type1\public\univers"
mkdir "%CommonConfig%\miktex\config"

rem del /f /q "%Install%\tex\latex\univers\*.*"
rem del /f /q "%Install%\fonts\tfm\public\univers\*.*"
rem del /f /q "%Install%\fonts\afm\public\univers\*.*"
rem del /f /q "%Install%\fonts\vf\public\univers\*.*"
rem del /f /q "%Install%\fonts\type1\public\univers\*.*"

copy /y *.fd  "%Install%\tex\latex\univers\"
copy /y *.tfm "%Install%\fonts\tfm\public\univers\"
copy /y *.afm "%Install%\fonts\afm\public\univers\"
copy /y *.vf  "%Install%\fonts\vf\public\univers\"
copy /y *.pfb "%Install%\fonts\type1\public\univers\"
copy /y *.map "%CommonConfig%"

echo map dinbold.map >> "%CommonConfig%\updmap.cfg"

initexmf -u
initexmf --mkmaps
initexmf -u

cd ..
