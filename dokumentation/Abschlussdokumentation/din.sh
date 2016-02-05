#!/bin/sh
# din.sh
# Installation der TU Schriftarten (DIN-Bold) unter Latex
#
# Autor: Klaus Bergmann
# nach dem Univers-Installationsskript von
# Michael Kluge, ZIH, Tel. 32424, kluge@zhr.tu-dresden.de
# Mathias Kortke, Fakultaet EuI, IAS
# Stefan Berthold, Fakultaet INF
# Stefan Hauptmann, Fakultaet EuI, CCN
#
# Letzte Aenderung: 26. Januar 2008

INSTDIR=`kpsexpand '$TEXMFHOME'`
if [$INSTDIR == ""] ; then 
    INSTDIR="$HOME/texmf"
fi

echo "Installing to directory $INSTDIR"

unzip din_ps.zip

rm -fr converted
mkdir converted

mv DINBd___.pfb converted/dinb8a.pfb
mv DINBd___.afm converted/dinb8a.afm

cd converted

cat > fiaun.tex <<%EOF
\\input fontinst.sty
\\latinfamily{din}{}
\\bye
%EOF
latex fiaun.tex

for f in *.pl ; do
    pltotf $f
done

for f in *.vpl ; do
    vptovf $f
done

cat > dinbold.map <<%EOF
dinb8r DINBold "DIN-Bold" <8r.enc <dinb8a.pfb
%EOF

mkdir -p $INSTDIR/tex/latex/univers
mkdir -p $INSTDIR/fonts/tfm/adobe/univers
mkdir -p $INSTDIR/fonts/vf/adobe/univers
mkdir -p $INSTDIR/fonts/type1/adobe/univers

mv *.fd $INSTDIR/tex/latex/univers/
mv *.tfm $INSTDIR/fonts/tfm/adobe/univers/
mv *.vf $INSTDIR/fonts/vf/adobe/univers/
mv *.pfb $INSTDIR/fonts/type1/adobe/univers/

if [ -e $INSTDIR/fonts/map/dvips ] ; then
    # pdfeTeX, Version 3.141592-1.21a-2.2 (Web2C 7.5.4)
    mkdir -p $INSTDIR/fonts/map
    mv dinbold.map $INSTDIR/fonts/map
else
    if [ -e $INSTDIR/dvips/ ] ; then
        # TeX, Version 3.14159 (Web2C 7.4.5)
        # pdfTeX, Version 3.14159-1.10b (Web2C 7.4.5)
        # mkdir -p $INSTDIR/dvips/local
        mv *.map $INSTDIR/dvips/
    fi
fi
# psfonts.map & Co. aktualisieren
mktexlsr

mkdir $INSTDIR/dvips/config
mkdir $INSTDIR/web2c
cat >> $INSTDIR/web2c/updmap.cfg <<%EOF
Map dinbold.map
%EOF

updmap --outputdir $INSTDIR/dvips/config --cnffile $INSTDIR/web2c/updmap.cfg
# updmap --enable Map dinbold.map
cd ..
