import os
from datetime import datetime
import csv

file = open("resultado.txt")  
matriz = [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]
registro = []
contador = 0
for line in file:
   if contador == 20 :
      contador = 0
   valor = line.replace("\n","").split("-")
   matriz[contador].append(valor[1])
   contador+=1
matriz.append(registro)
file = open("resultadosParametros.csv","w",newline='')
csvFile = csv.writer(file, delimiter=';')
csvFile.writerows(matriz)