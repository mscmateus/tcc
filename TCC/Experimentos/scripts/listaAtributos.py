import os
from datetime import datetime
import csv

#essa função pega os arquivos csv
def getCsvFile():
   for p, _, files in os.walk(os.getcwd()) :
      for file in files:
         if str(file).split('.')[1] == "csv":
            return file

file = csv.reader(open(getCsvFile(), newline=''), delimiter=',')
print("Arquivo: "+str(file))
conta = 1
for line in file:
   for atribut in line:
      print(str(atribut))
      conta+=1
   break
      # atributos=line.split(,)
      # print("Atributos no arquivo "+str(cvsFile)+":"+str(len(row)))
      # break
