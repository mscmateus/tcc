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
   csvOutput = open('alteredFiles\\'+str(cvsFile), 'w' , newline='')

   file = csv.reader(csvTnput, delimiter=',')
   fileOutput = csv.writer(csvOutput, delimiter=',')

   origemIndex = 4
   weekDayIndex = 5
   dayTurnIndex = 6
   index = 0

   for row in file:
      if index==0:
         #pega index do created_at
         row.insert(weekDayIndex,"created_at_week_day")
         row.insert(dayTurnIndex,"created_at_day_turn")
         index+= 1
         fileOutput.writerow(row)
      else:
         #pegando a data unix
         unixDate = int(row[origemIndex])
         #transformando em data normal
         date = datetime.utcfromtimestamp(unixDate)
         #inserindo na lina
         row.insert(weekDayIndex,getWeekDay(date))
         row.insert(dayTurnIndex,getDayTurn(date))
         #escrevendo linha no novo arquivo
         fileOutput.writerow(row)
   #fecha arquivos
   print("Atriutos criados em: "+str(cvsFile))
   csvTnput.close()
   csvOutput.close()
