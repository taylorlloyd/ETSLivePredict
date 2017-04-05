#!/usr/bin/python
from __future__ import print_function
import sys
import json
import subprocess

config_file = open(sys.argv[1]).read()
config = json.loads(config_file)

outf = open(sys.argv[2],'w')
print("\\begin{table}",file=outf);
print("\\centering",file=outf);

# Execute the data generator
outf.flush()
subprocess.call(config['exec'], stdout=outf, shell=False)
print("\\nocaptionrule\\caption{" + config['caption'] + "}",file=outf);
print("\\label{" + config['label'] + "}",file=outf);
print("\\end{table}",file=outf);


