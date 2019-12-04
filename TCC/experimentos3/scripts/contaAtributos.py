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
print("Arquivos encontrados: "+str(csvFiles))
for cvsFile in getCsvFiles():
   csvTnput = open(cvsFile, newline='')

   file = csv.reader(csvTnput, delimiter=',')

   for row in file:
      print("Atributos no arquivo "+str(cvsFile)+":"+str(len(row)))
      break
      
         
   #fecha arquivos
   csvTnput.close()
