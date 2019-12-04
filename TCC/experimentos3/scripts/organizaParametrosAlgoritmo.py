import os
from datetime import datetime
import csv

def valores():
   matriz = []
   file = open("resultado.txt")
   inicio = 0
   registro = []
   for line in file:
      valor = line.replace("\n","").split("-")
      if valor[0] == "projeto":
         if inicio < 1:
            inicio = 1
            registro.append(valor[1])
         else:
            matriz.append(registro)
            registro = []
            registro.append(valor[1])
      else:
         registro.append(valor[1])
   matriz.append(registro)
   return matriz
      
file = open("resultadosParametros.csv","w",newline='')
csvFile = csv.writer(file, delimiter=';')
csvFile.writerows(valores())