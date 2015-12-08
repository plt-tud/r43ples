@echo off
rem univers29.bat
rem Installation der TU Schriftarten unter MiKTeX 2.9
rem
rem Autor: Klaus Bergmann
rem
rem Basierend auf dem Skript univers.sh von Michael Kluge
rem
rem Letzte Aenderung: 25. Juli 2012
rem
rem Getestet auf:
rem		Microsoft Windows 7 Home Premium x64

cls
echo.
echo   Installation der TU Schriftarten unter MiKTeX 2.9
echo  ==============================================================================
echo.
echo.
echo HINWEIS: Alle hier angegebenen Verzeichnisse finden Sie im Settings-
echo          Programm von MiKTeX unter dem Reiter "Roots".
echo.
echo  ==============================================================================
echo.
echo Bitte geben Sie den Pfad Ihres localtexmf-Verzeichnisses ein!
echo Zum Beispiel:
echo               "C:\localtexmf"
echo.
echo.
:proof_texmf
set /p texmf=localtexmf-Verzeichnis (ohne Anfuerungszeichen):
echo.
if not exist "%texmf%" (
    echo FEHLER: Das angebene Verzeichnis existiert nicht!
    echo         Bitte geben Sie den Pfad Ihres localtextmf-Verzeichnisses ein!
    echo.
    echo.
    goto proof_texmf
)
echo.
:start_install
echo.
echo  ==============================================================================
echo.
echo Beginne Installation ... bitte warten ...
echo.
echo.

if exist converted rmdir /s /q converted > nul
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

rem din-bold
copy DINBd___.pfb converted\dinb8a.pfb > nul
copy DINBd___.afm converted\dinb8a.afm > nul

cd converted

echo \input fontinst.sty > ltx.tex
echo \latinfamily{aun}{} >> ltx.tex
echo \latinfamily{din}{} >> ltx.tex
echo \bye >> ltx.tex
tex ltx.tex

echo \input fontinst.sty > ltx.tex
echo \latinfamily{aub}{} >> ltx.tex
echo \latinfamily{din}{} >> ltx.tex
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
echo dinb8r DINBold "DIN-Bold" ^<8r.enc ^<dinb8a.pfb >> univers.map

if not exist "%texmf%\tex\latex\tud\univers" mkdir "%texmf%\tex\latex\tud\univers"
if not exist "%texmf%\fonts\tfm\tud\univers" mkdir "%texmf%\fonts\tfm\tud\univers"
if not exist "%texmf%\fonts\afm\tud\univers" mkdir "%texmf%\fonts\afm\tud\univers"
if not exist "%texmf%\fonts\vf\tud\univers" mkdir "%texmf%\fonts\vf\tud\univers"
if not exist "%texmf%\fonts\type1\tud\univers" mkdir "%texmf%\fonts\type1\tud\univers"
if not exist "%texmf%\fonts\map\dvips\tud" mkdir "%texmf%\fonts\map\dvips\tud"
if not exist "%texmf%\fonts\map\pdftex\tud" mkdir "%texmf%\fonts\map\pdftex\tud"

copy /y *.fd  "%texmf%\tex\latex\tud\univers"
copy /y *.tfm "%texmf%\fonts\tfm\tud\univers"
copy /y *.afm "%texmf%\fonts\afm\tud\univers"
copy /y *.vf  "%texmf%\fonts\vf\tud\univers"
copy /y *.pfb "%texmf%\fonts\type1\tud\univers"
copy /y *.map "%texmf%\fonts\map\dvips\tud"
copy /y *.map "%texmf%\fonts\map\pdftex\tud"

texhash --update-fndb="%texmf%"
updmap

echo  ==============================================================================
echo Es öffnet sich nun ein Editorfenster.
echo Bitte fügen Sie hier folgende Zeile hinzu:
echo.
echo.
echo Map univers.map
echo.
echo.
echo Anschließend speichern Sie bitte und beenden den Editor.
echo  ==============================================================================
pause
initexmf --edit-config-file updmap
initexmf --mkmaps
cd ..
echo.
echo.
echo  ==============================================================================
echo Installation abgeschlossen.
pause