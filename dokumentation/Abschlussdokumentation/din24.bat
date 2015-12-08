@echo off
rem din24.bat
rem Installation der TU Schriftarten (DIN-Bold) unter Latex
rem
rem Autor: Klaus Bergmann
rem nach dem Univers-Installationsskript von
rem Michael Kluge, ZIH, kluge@zhr.tu-dresden
rem Mathias Kortke, Fakultät EuI, IAS
rem
rem Letzte Aenderung: 26. Januar 2008

rem LOCALTEX muss eventuell an Ihren localtexmf-Pfad angepasst werden.
set LOCALTEX=c:\Programs\Texte\TeX\localtexmf

rmdir /s /q converted
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
updmap --enable Map dinbold.map

cd ..
