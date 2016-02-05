#!/bin/sh
# Installation der TU Schriftarten unter Latex
#
# Autor: Michael Kluge, ZIH, Tel. 32424
# kluge@zhr.tu-dresden.de
#
# Geaendert von Mathias Kortke, Fakultaet EuI, IAS
# Geaendert von Stefan Berthold, Fakultaet INF
# Geaendert von Klaus Bergmann, Fakultaet INF
# Geaendert von Stefan Hauptmann, Fakultaet EuI, CCN
# Geaendert von Jan Seiffert, Fakultaet MW, IfWW
# Geaendert von Andreas Eberlein, Fakultaet MatNat
# Letzte Aenderung: 24. September 2008
# 
# Skript getestet unter Xubuntu 8.04
# Nur die Befehle mktexlsr und updmap werden mit Superuserrechten ausgefuehrt (sudo)

# changed by jan:
#INSTDIR=`kpsexpand '$TEXMFHOME'`
#if [$INSTDIR == ""] ; then 
#    INSTDIR="$HOME/texmf"
#fi

INSTDIR="$HOME/texmf"
MAPDIR="/var/lib/texmf/fonts/map"
# end jan.

echo "Installing to directory $INSTDIR"

unzip Univers_ps.zip

#unzip univers_ttf.zip

rm -fr converted
mkdir converted
echo uvceb aunb8a
mv uvceb___.pfb converted/aunb8a.pfb
mv uvceb___.afm converted/aunb8a.afm
echo uvcel aunl8a
mv uvcel___.pfb converted/aunl8a.pfb
mv uvcel___.afm converted/aunl8a.afm
echo uvceo aunro8a
mv uvceo___.pfb converted/aunro8a.pfb
mv uvceo___.afm converted/aunro8a.afm
echo uvxbo aunbo8a
mv uvxbo___.pfb converted/aunbo8a.pfb
mv uvxbo___.afm converted/aunbo8a.afm
echo uvxlo aunlo8a
mv uvxlo___.pfb converted/aunlo8a.pfb
mv uvxlo___.afm converted/aunlo8a.afm
echo uvce aunr8a
mv uvce____.pfb converted/aunr8a.pfb
mv uvce____.afm converted/aunr8a.afm
echo uvczo aubro8a
mv uvczo___.pfb converted/aubro8a.pfb
mv uvczo___.afm converted/aubro8a.afm
echo uvcz aubr8a
mv uvcz____.pfb converted/aubr8a.pfb
mv uvcz____.afm converted/aubr8a.afm

#cp uv______.TTF converted/aunr8a.ttf
#cp uvb_____.TTF converted/aunb8a.ttf
#cp uvbl____.TTF converted/aubl8a.ttf
#cp uvblo___.TTF converted/aublo8a.ttf
#cp uvbo____.TTF converted/aubri.ttf
#cp uvl_____.TTF converted/aunl8a.ttf
#cp uvlo____.TTF converted/aunlo8a.ttf
#cp uvo_____.TTF converted/aunro8a.ttf

cd converted
#for f in *.ttf ; do ttf2pt1 -a -b $f ; done

cat > fiaun.tex <<%EOF
\\input fontinst.sty
\\latinfamily{aun}{}
\\bye
%EOF
latex fiaun.tex

cat > fiaub.tex <<%EOF
\\input fontinst.sty
\\latinfamily{aub}{}
\\bye
%EOF
latex fiaub.tex

for f in *.pl ; do
    pltotf $f
done

for f in *.vpl ; do
    vptovf $f
done

cat > univers.map <<%EOF
aunb8r UniversCE-Bold "TeXBase1Encoding ReEncodeFont" <8r.enc <aunb8a.pfb
aunl8r UniversCE-Light "TeXBase1Encoding ReEncodeFont" <8r.enc <aunl8a.pfb
aunro8r UniversCE-Oblique "TeXBase1Encoding ReEncodeFont" <8r.enc <aunro8a.pfb
aunbo8r UniversCE-BoldOblique "TeXBase1Encoding ReEncodeFont" <8r.enc <aunbo8a.pfb
aunlo8r UniversCE-LightOblique "TeXBase1Encoding ReEncodeFont" <8r.enc <aunlo8a.pfb
aunr8r UniversCE-Medium "TeXBase1Encoding ReEncodeFont" <8r.enc <aunr8a.pfb
aubro8r UniversCE-BlackOblique "TeXBase1Encoding ReEncodeFont" <8r.enc <aubro8a.pfb
aubr8r UniversCE-Black "TeXBase1Encoding ReEncodeFont" <8r.enc <aubr8a.pfb
%EOF

mkdir -p $INSTDIR/tex/latex/univers
mkdir -p $INSTDIR/fonts/tfm/adobe/univers
mkdir -p $INSTDIR/fonts/vf/adobe/univers
mkdir -p $INSTDIR/fonts/type1/adobe/univers
# by andreas
mkdir -p $INSTDIR/fonts/map/dvips
# end andreas

#rm $INSTDIR/tex/latex/univers/*
#rm $INSTDIR/fonts/tfm/public/univers/*
#rm $INSTDIR/fonts/vf/public/univers/*
#rm $INSTDIR/fonts/type1/public/univers/*

mv *.fd $INSTDIR/tex/latex/univers/
mv *.tfm $INSTDIR/fonts/tfm/adobe/univers/
mv *.vf $INSTDIR/fonts/vf/adobe/univers/
mv *.pfb $INSTDIR/fonts/type1/adobe/univers/

if [ -e $INSTDIR/fonts/map/dvips ] ; then
    # pdfeTeX, Version 3.141592-1.21a-2.2 (Web2C 7.5.4)
    mkdir -p $INSTDIR/fonts/map
    mv univers.map $INSTDIR/fonts/map
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
Map univers.map
%EOF

# changed by jan:
#updmap --outputdir $INSTDIR/dvips/config --cnffile $INSTDIR/web2c/updmap.cfg
sudo updmap --dvipsoutputdir $MAPDIR/dvips/updmap \
       --dvipdfmoutputdir $MAPDIR/dvipdfm/updmap \
       --pdftexoutputdir $MAPDIR/dvipdfm/updmap \
       --cnffile $INSTDIR/web2c/updmap.cfg
# end jan.

# geandert von andreas: Kommentar entfernt
sudo updmap --enable Map univers.map
# ende andreas
cd ..
