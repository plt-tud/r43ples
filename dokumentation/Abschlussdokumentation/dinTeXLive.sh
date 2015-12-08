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
# Andreas Eberlein, Fakultaet MatNat
#
# Letzte Aenderung: 24. September 2008

INSTDIR="$HOME/texmf"
# geandert von andreas
MAPDIR="/var/lib/texmf/fonts/map"
# ende andreas

echo "Installing to directory $INSTDIR"

# geandert von andreas: Archivname
unzip DIN_Bd_PS.zip

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

# geaendert von andreas:
# Verzeichnisname univers in dinbold geaendert
mkdir -p $INSTDIR/tex/latex/dinbold
mkdir -p $INSTDIR/fonts/tfm/adobe/dinbold
mkdir -p $INSTDIR/fonts/vf/adobe/dinbold
mkdir -p $INSTDIR/fonts/type1/adobe/dinbold
# Neues Verzeichnis erstellt
mkdir -p $INSTDIR/fonts/map/dvips

mv *.fd $INSTDIR/tex/latex/dinbold/
mv *.tfm $INSTDIR/fonts/tfm/adobe/dinbold/
mv *.vf $INSTDIR/fonts/vf/adobe/dinbold/
mv *.pfb $INSTDIR/fonts/type1/adobe/dinbold/
# ende andreas

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
sudo mktexlsr

# mkdir -p $INSTDIR/dvips/config
mkdir $INSTDIR/web2c
cat >> $INSTDIR/web2c/updmap.cfg <<%EOF
Map dinbold.map
%EOF


# geandert von andreas (aus Skript univers_jan.sh):
#updmap --outputdir $INSTDIR/dvips/config --cnffile $INSTDIR/web2c/updmap.cfg
sudo updmap --dvipsoutputdir $MAPDIR/dvips/updmap \
       --dvipdfmoutputdir $MAPDIR/dvipdfm/updmap \
       --pdftexoutputdir $MAPDIR/dvipdfm/updmap \
       --cnffile $INSTDIR/web2c/updmap.cfg
# ende andreas.

# geandert von andreas: Kommentar entfernt
sudo updmap --enable Map dinbold.map
# ende andreas
cd ..
