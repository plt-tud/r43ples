#!/usr/bin/python
# encoding: utf-8
'''
Testdata - Script to generate testdata for semantic storage evaluation.

@author:     Thomas Koren
@contact:    thomas.koren@a1.net
'''

import sys
import csv
import os

from argparse import RawDescriptionHelpFormatter
from argparse import ArgumentParser

__all__ = []
__version__ = 0.1
__date__ = '2013-09-03'
__updated__ = '2013-09-03'

DEBUG = 1
TESTRUN = 0
PROFILE = 0

ttlgit_folder_depth = 3

scenarioId = None
factor = 10000
#commitNr = None
fileFormat = None
changeset  = False
targetPath = None

s1_targetdata = 1000
s1_op_delta   = 500
s1_steps      = int(s1_targetdata / s1_op_delta)
s2_targetdata = 1000000
s2_initialdata= 100000
s2_op_delta   = 10000

turtle_header = '''@prefix ns1: <http://www.owl-ontologies.com/Ontology1370788248.owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xml: <http://www.w3.org/XML/1998/namespace> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
'''.replace('\n','\r\n')

turtle_template = '''
ns1:%s a ns1:EPLAN_Signal ;
    rdfs:label "%s"^^xsd:string ;
    ns1:functionText "%s"^^xsd:string ;
    ns1:plcAddress "%s"^^xsd:string ;
    ns1:signalNumber "%s"^^xsd:string .
'''.replace('\n','\r\n')

nt_template = '''
<http://www.owl-ontologies.com/Ontology1370788248.owl#%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1370788248.owl#EPLAN_Signal> .
<http://www.owl-ontologies.com/Ontology1370788248.owl#%s> <http://www.w3.org/2000/01/rdf-schema#label> "%s"^^<http://www.w3.org/2001/XMLSchema#string> .
<http://www.owl-ontologies.com/Ontology1370788248.owl#%s> <http://www.owl-ontologies.com/Ontology1370788248.owl#functionText> "%s"^^<http://www.w3.org/2001/XMLSchema#string> .
<http://www.owl-ontologies.com/Ontology1370788248.owl#%s> <http://www.owl-ontologies.com/Ontology1370788248.owl#plcAddress> "%s"^^<http://www.w3.org/2001/XMLSchema#string> .
<http://www.owl-ontologies.com/Ontology1370788248.owl#%s> <http://www.owl-ontologies.com/Ontology1370788248.owl#signalNumber> "%s"^^<http://www.w3.org/2001/XMLSchema#string> .
'''.replace('\n','\r\n')

class CLIError(Exception):
    '''Generic exception to raise and log different fatal errors.'''
    def __init__(self, msg):
        super(CLIError).__init__(type(self))
        self.msg = "E: %s" % msg
    def __str__(self):
        return self.msg
    def __unicode__(self):
        return self.msg

def argcheck(argv=None): # IGNORE:C0111
    '''Command line options.'''
    
    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)

    program_name = os.path.basename(sys.argv[0])
    program_shortdesc = __import__('__main__').__doc__.split("\n")[1]
    program_license = '''%s

  Created by Thomas Koren on %s.
  Copyright 2013 organization_name. All rights reserved.
  
  Distributed on an "AS IS" basis without warranties
  or conditions of any kind, either express or implied.
''' % (program_shortdesc, str(__date__))

    try:
        # Setup argument parser
        parser = ArgumentParser(description=program_license, formatter_class=RawDescriptionHelpFormatter)
        parser.add_argument(dest="scenario",   help="id of test scenario (1 or 2)")
        #parser.add_argument(dest="commit_nr",  help='''commit to generate.
        #0 generates a baseline of data.
        #scenario 1 provides data for commits 0 through 20.
        #scenario 2 provides data for commits 0 through 13.''')
        parser.add_argument("-f", "--amount-factor", dest="factor", default=10000, type=float, help="increase/decrease amount of data by this factor. default:10000 resulting in 1mil datasets.")
        parser.add_argument(dest="file_format",   help="turtle|csv|ttlgit|nt")
        parser.add_argument("-c", "--changeset", dest="changeset", action="store_true", help="write data as changeset. not as content in storage.")
        parser.add_argument(dest="targetpath", help="path to targetfolder for testdata.")
        
        # Process arguments
        global scenarioId
        global factor
        global commitNr
        global fileFormat
        global changeset
        global targetPath
        args = parser.parse_args()
        
        scenarioId = int(args.scenario)
        if scenarioId not in (1, 2):
            raise(CLIError("invalid scenario id: %d" % scenarioId))
        
        factor = float(args.factor)
        global s1_targetdata
        global s1_op_delta
        global s1_steps
        global s2_targetdata
        global s2_initialdata
        global s2_op_delta
        s1_targetdata = int(100 * factor)
        s1_op_delta   = int(5 * factor)
        s1_steps      = int(s1_targetdata / s1_op_delta)
        s2_targetdata = int(100 * factor)
        s2_initialdata= int(10 * factor)
        s2_op_delta   = int(1 * factor)
        
        #commitNr = int(args.commit_nr)
        #if scenarioId == 1:
        #    if commitNr not in range(0, 21):
        #        raise(CLIError("invalid commit nr: %d. must be 0 through 20 for scenario 1" % commitNr))
        #else:
        #    if commitNr not in range(0, 14):
        #        raise(CLIError("invalid commit nr: %d. must be 0 through 13 for scenario 2" % commitNr))
        
        fileFormat = args.file_format
        if fileFormat not in ("turtle", "csv", "ttlgit", "nt"):
            raise(CLIError("invalid target format: %s. must be one of turtle|csv|ttlgit|nt" % fileFormat))
        
        if args.changeset == True:
            changeset = args.changeset
        
        targetPath = args.targetpath
        if targetPath is None:
            raise(CLIError("please provide a target folder"))
                  
        #if os.path.exists(targetPath):
        #    raise(CLIError("file already exists: %s" % targetPath))
        
        return 0
    except Exception, e:
        indent = len(program_name) * " "
        sys.stderr.write(program_name + ": " + repr(e) + "\n")
        sys.stderr.write(indent + "  for help use --help\n")
        
        if DEBUG or TESTRUN:
            raise(e)
        
        return 2

####################################################################################################
s1BaseKKS0 = 10
addressPool = ["007.20.05.0.00",
               "007.20.04.0.00",
               "007.20.03.0.00",
               "007.20.02.0.00",
               "007.20.01.0.00"
               ]

def createScen1Data():
    data = []
    for idx in range(0, s1_steps + 1):
        nrDel = s1_targetdata - idx*s1_op_delta
        nrUpd = idx * s1_op_delta
        nrIns = nrDel
        print("commit %2d: %12d deletes, %12d updates, %12d inserts" % (idx, nrDel, nrUpd, nrIns))
        (data, deleted)  = createDeletes(nrDel, data)
        (data, updated)  = createUpdates(idx, nrUpd, data)
        (data, inserted) = createInserts(idx, nrIns, data)
        if changeset:
            writeData(deleted,  idx, "_deletes")
            writeData(updated,  idx, "_updates")
            writeData(inserted, idx, "_inserts")
        else:
            writeData(data,     idx, "")
        
        #printData(data)

def createInserts(commitNr, nrInserts, data):
    inserts = []
    for idx in range(0, nrInserts):
        instanceId = (s1BaseKKS0 + commitNr) * 10000000 + idx
        sigNr = getSigNr(instanceId)
        funcText = "created in commit %d" % commitNr
        address = addressPool[instanceId % len(addressPool)]
        entry = [instanceId, sigNr, funcText, address]
        data.append(entry)
        inserts.append(entry)
        
    return data, inserts
        
def getSigNr(instanceId):
    kks0 = int(instanceId / 10000000)
    kks1 = int(instanceId / 100000) % 100
    kks2 = int(instanceId / 100) % 1000
    kks3 = instanceId % 100
    return "%02d.ACA%02d.CE%03d.XQ%02d" % (kks0, kks1, kks2, kks3)

def printData(data):
    for dat in data:
        print(dat)
        
def writeData(data, commitNr, comment):
    if fileFormat == "csv":
        targetFile = "%s/scenario_%d_commit_%d%s.csv" % (targetPath, scenarioId, commitNr, comment)
        writeCsvData(data, targetFile)
    elif fileFormat == "turtle":
        targetFile = "%s/scenario_%d_commit_%d%s.turtle" % (targetPath, scenarioId, commitNr, comment)
        writeTurtleData(data, targetFile)
    elif fileFormat == "ttlgit":
        writeSingleTurtleData(data, commitNr, comment)
    elif fileFormat == "nt":
        targetFile = "%s/scenario_%d_commit_%d%s.nt" % (targetPath, scenarioId, commitNr, comment)
        writeNTriplesData(data, targetFile)
    else:
        raise(CLIError("invalid target format: %s. must be one of turtle|csv|ttlgit" % fileFormat))
    
def writeCsvData(data, targetFile):
    with open(targetFile, 'wb') as csvfile:
        csvWriter = csv.writer(csvfile, delimiter=';', quotechar='|', quoting=csv.QUOTE_MINIMAL)
        csvWriter.writerow(["instanceId", "sigNr", "funcText", "address"])
        for dat in data:
            csvWriter.writerow(dat)

def writeTurtleData(data, targetFile):
    with open(targetFile, 'wb') as ttlfile:
        ttlfile.write(turtle_header)
        for dat in data:
            ttlfile.write(turtle_template % (dat[0], dat[0], dat[2], dat[3], dat[1]))

def writeNTriplesData(data, targetFile):
    with open(targetFile, 'wb') as ttlfile:
        for dat in data:
            ttlfile.write(nt_template % (dat[0], dat[0], dat[0], dat[0], dat[2], dat[0], dat[3], dat[0], dat[1]))
            
def writeSingleTurtleData(data, commitNr, comment):
    for dat in data:
        fileName = "%d.turtle" % dat[0]
        hashDir = getHashDirFrom(fileName)
        filePath = "%s/scenario_%d/commit_%d%s/%s/" % (targetPath, scenarioId, commitNr, comment, hashDir)
        if not os.path.exists(filePath):
            os.makedirs(filePath, 0755)
        
        with open("%s%s" % (filePath, fileName), 'wb') as ttlfile:
            ttlfile.write(turtle_header)
            ttlfile.write(turtle_template % (dat[0], dat[0], dat[2], dat[3], dat[1]))

def getHashDirFrom(filename):
    filenameHash = "%08x" % (hash(filename) & 0xffffffff) # only lower 32bits of hash value to stay portable
    hashDir = ""
    for idx in range(0, ttlgit_folder_depth):
        hashDir = "%s/%s" % (hashDir, filenameHash[0:idx+1])
    
    return hashDir

def createDeletes(nrDeletes, data):
    deletes = data[0:nrDeletes]
    del data[0:nrDeletes]
    return data, deletes

def createUpdates(commitNr, nrUpd, data):
    updates = []
    for idx in range(0, nrUpd):
        dat = data[idx]
        dat[2] = "modified in commit %d" % commitNr
        updates.append(dat)
        
    return data, updates
        
####################################################################################################

def createScen2Data():
    data = []
    deleted = []
    updated = []
    commitNr = 0
    currentdata = s2_initialdata
    currentincrease = 0
    
    print("commit %2d: %12d instances, %12d deletes, %12d updates, %12d inserts" % (commitNr, s2_initialdata, 0, 0, s2_initialdata))
    (data, inserted) = createInserts(commitNr, s2_initialdata, data)
    if changeset:
        writeData(deleted,  commitNr, "_deletes")
        writeData(updated,  commitNr, "_updates")
        writeData(inserted, commitNr, "_inserts")
    else:
        writeData(data, commitNr, "")

    abort = False
    while not abort:
        commitNr = commitNr + 1
        currentincrease = currentincrease + s2_op_delta
        currentdata = currentdata + currentincrease
        
        print("commit %2d: %12d instances, %12d deletes, %12d updates, %12d inserts" % (commitNr, currentdata, 0, currentincrease, currentincrease))
        #(data, deleted)  = createDeletes(0, data)
        (data, updated)  = createUpdates(commitNr, currentincrease, data)
        (data, inserted) = createInserts(commitNr, currentincrease, data)
        if changeset:
            writeData(deleted,  commitNr, "_deletes")
            writeData(updated,  commitNr, "_updates")
            writeData(inserted, commitNr, "_inserts")
        else:
            writeData(data, commitNr, "")
        
        if currentdata > s2_targetdata:
            abort = True

####################################################################################################

if __name__ == "__main__":
    argcheck()
    if scenarioId == 1:
        createScen1Data()
    else:
        createScen2Data()
