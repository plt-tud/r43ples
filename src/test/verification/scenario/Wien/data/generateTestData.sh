#/bin/bash

rm -R scenario_1 scenario_2
mkdir scenario_1
mkdir scenario_2

# 10 is the factor, base 100 entries
python testdata.py 1 -c -f 100 nt scenario_1
python testdata.py 2 -c -f 100 nt scenario_2

