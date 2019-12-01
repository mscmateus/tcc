import os
from datetime import datetime
import csv

#essa função pega os arquivos csv
file = csv.reader(open("appium.arff.csv", newline=''), delimiter=',')
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
