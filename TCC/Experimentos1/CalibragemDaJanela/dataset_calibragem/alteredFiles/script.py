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

#essa função pega o dia da semana
def getWeekDay(date):
    daysNames = ['segunda-feira', 'terca-feira', 'quarta-feira',
                 'quinta-Feira', 'sexta-feira', 'sabado', 'domingo']
    return daysNames[date.weekday()]

#essa função pega o turno do dia
def getDayTurn(date):
    dayTurns = [datetime(2019, 10, 18, 6, 0).time(), datetime(2019, 10, 18, 12, 0).time(
    ), datetime(2019, 10, 18, 18, 0).time(), datetime(2019, 10, 18, 23, 59).time()]
    if date.time() < dayTurns[0]:
        return 'madrugada'
    elif date.time() < dayTurns[1]:
        return 'manha'
    elif date.time() < dayTurns[2]:
      return 'tarde'
    elif date.time() < dayTurns[3]:
        return 'noite'

csvFiles = getCsvFiles()
print("Iniciando criação nos seguintes arquivos: "+str(csvFiles))
for cvsFile in getCsvFiles():
   csvTnput = open(cvsFile, newline='')

   file = csv.reader(csvTnput, delimiter=',')

   for row in file:
      print("Atributos no arquivo "+str(cvsFile)+":"+str(len(row)))
      break
      
         
   #fecha arquivos
   csvTnput.close()
