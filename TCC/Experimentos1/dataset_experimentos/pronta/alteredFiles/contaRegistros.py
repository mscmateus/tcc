import os
from datetime import datetime
import csv

#essa função pega os arquivos csv
def getCsvFiles():
   csvFiles = []
   for p, _, files in os.walk(os.getcwd()) :
      for file in files:
         if str(file).split('.')[1] == "csv":
            csvFiles.append(file)
   return csvFiles

csvFiles = getCsvFiles()
contador = 1
totaPullRequests = 0
for cvsFile in getCsvFiles():
   file = open(cvsFile)
   registros = len(file.readlines())
   totaPullRequests += registros
   print(str(registros))
   contador+=1
   #fecha arquivos
   if contador==21:
      break
   file.close()
print("Total de pull requests na base: "+str(totaPullRequests))