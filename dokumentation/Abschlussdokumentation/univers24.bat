@echo off
rem univers24.bat
rem Installation der TU Schriftarten unter Latex
rem
rem Autor: Michael Kluge, ZIH
rem        kluge@zhr.tu-dresden
rem
rem Geändert von Mathias Kortke, Fakultät EuI, IAS
rem
rem Portiert nach Windows von Klaus Bergmann
rem
rem Letzte Aenderung: 08. Juni 2007
rem        
rem Getestet auf: Microsoft Windows XP

rem LOCALTEX muss eventuell an Ihren localtexmf-Pfad angepasst werden.
set LOCALTEX=c:\Programs\Texte\TeX\localtexmf

rmdir /s /q converted
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

mkdir %LOCALTEX%\tex\latex\univers 
mkdir %LOCALTEX%\fonts\tfm\public\univers 
mkdir %LOCALTEX%\fonts\afm\public\univers
mkdir %LOCALTEX%\fonts\vf\public\univers 
mkdir %LOCALTEX%\fonts\type1\public\univers 

rem del /f /q %LOCALTEX%\tex\latex\univers\*.*
rem del /f /q %LOCALTEX%\fonts\tfm\public\univers\*.*
rem del /f /q %LOCALTEX%\fonts\afm\public\univers\*.*
rem del /f /q %LOCALTEX%\fonts\vf\public\univers\*.*
rem del /f /q %LOCALTEX%\fonts\type1\public\univers\*.*

copy *.fd  %LOCALTEX%\tex\latex\univers\
copy *.tfm %LOCALTEX%\fonts\tfm\public\univers\ 
copy *.afm %LOCALTEX%\fonts\afm\public\univers\ 
copy *.vf  %LOCALTEX%\fonts\vf\public\univers\
copy *.pfb %LOCALTEX%\fonts\type1\public\univers\

if exist %LOCALTEX%\fonts\map\dvips (
    rem pdfeTeX, Version 3.141592-1.21a-2.2 (Web2C 7.5.4)
    mkdir %LOCALTEX%\fonts\map
    copy *.map %LOCALTEX%\fonts\map\
) else (
    if exist %LOCALTEX%\dvips (
        rem TeX, Version 3.14159 (Web2C 7.4.5)
        rem pdfTeX, Version 3.14159-1.10b (Web2C 7.4.5)
        mkdir %LOCALTEX%\dvips\local
        copy *.map %LOCALTEX%\dvips\local\
    )
)

rem updmap.cfg aktualisieren
mktexlsr
updmap --enable Map univers.map

cd ..
