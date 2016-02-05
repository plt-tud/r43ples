@echo off
rem univers25.bat
rem Installation der TU Schriftarten unter MiKTeX 2.5 oder hoeher
rem
rem Autor: Michael Kluge, ZIH
rem        kluge@zhr.tu-dresden
rem
rem Portiert nach Windows von Klaus Bergmann
rem
rem Geändert von Mathias Kortke, Fakultät EuI, IAS
rem Geändert von Klaus Bergmann, Fakultät INF
rem         (mit Dank an Thomas Neumann, Fakultät MW)
rem Geändert von Martin Heinze, FSR MW
rem
rem Letzte Aenderung: 20. August 2007
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

rem uvceb aunb8a
copy uvceb___.pfb converted\aunb8a.pfb > nul
copy uvceb___.afm converted\aunb8a.afm > nul
rem uvcel aunl8a
copy uvcel___.pfb converted\aunl8a.pfb > nul
copy uvcel___.afm converted\aunl8a.afm > nul
rem uvceo aunro8a
copy uvceo___.pfb converted\aunro8a.pfb > nul
copy uvceo___.afm converted\aunro8a.afm > nul
rem uvxbo aunbo8a
copy uvxbo___.pfb converted\aunbo8a.pfb > nul
copy uvxbo___.afm converted\aunbo8a.afm > nul
rem uvxlo aunlo8a
copy uvxlo___.pfb converted\aunlo8a.pfb > nul
copy uvxlo___.afm converted\aunlo8a.afm > nul
rem uvce  aunr8a
copy uvce____.pfb converted\aunr8a.pfb > nul
copy uvce____.afm converted\aunr8a.afm > nul
rem uvczo aubro8a
copy uvczo___.pfb converted\aubro8a.pfb > nul
copy uvczo___.afm converted\aubro8a.afm > nul
rem uvcz  aubr8a
copy uvcz____.pfb converted\aubr8a.pfb > nul
copy uvcz____.afm converted\aubr8a.afm > nul

cd converted

echo \input fontinst.sty > ltx.tex
echo \latinfamily{aun}{} >> ltx.tex
echo \bye >> ltx.tex
tex ltx.tex

echo \input fontinst.sty > ltx.tex
echo \latinfamily{aub}{} >> ltx.tex
echo \bye >> ltx.tex
tex ltx.tex

dir /b *.pl > files.txt
for /f "delims=. " %%i in (files.txt) do pltotf %%i.pl %%i.tfm

dir /b *.vpl > files.txt
for /f "delims=. " %%i in (files.txt) do vptovf %%i.vpl %%i.vf %%i.tfm

echo aunb8r UniversCE-Bold "Univers" ^<8r.enc ^<aunb8a.pfb > univers.map
echo aunl8r UniversCE-Light "Univers" ^<8r.enc ^<aunl8a.pfb >> univers.map
echo aunro8r UniversCE-Oblique "Univers" ^<8r.enc ^<aunro8a.pfb >> univers.map
echo aunbo8r UniversCE-BoldOblique "Univers" ^<8r.enc ^<aunbo8a.pfb >> univers.map
echo aunlo8r UniversCE-LightOblique "Univers" ^<8r.enc ^<aunlo8a.pfb >> univers.map
echo aunr8r UniversCE-Medium "Univers" ^<8r.enc ^<aunr8a.pfb >> univers.map
echo aubro8r Univers-BlackOblique "Univers" ^<8r.enc ^<aubro8a.pfb >> univers.map
echo aubr8r Univers-Black "Univers" ^<8r.enc ^<aubr8a.pfb" >> univers.map

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

echo map univers.map >> "%CommonConfig%\updmap.cfg"

initexmf -u
initexmf --mkmaps
initexmf -u

cd ..
