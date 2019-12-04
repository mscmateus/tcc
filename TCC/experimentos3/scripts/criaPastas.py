import os

#essa função pega os arquivos csv
def getFilesNames():
   filesName = []
   for p, _, files in os.walk(os.getcwd()) :
      for file in files:
         filesName.append(str(file).split('.')[0])
   return filesName

pasta = os.getcwd()+"\\selecaoAtributos"
os.mkdir(pasta)
print("Criando pasta selecaoAtributos...")
for filename in getFilesNames():
   print("Criando pasta "+str(filename)+"...")
   os.mkdir(str(pasta)+"\\"+str(filename))
   for i in range(1,55):
      os.mkdir(str(pasta)+"\\"+str(filename)+"\\"+str(i)+"atributos")
      open(str(pasta)+"\\"+str(filename)+"\\"+str(i)+"atributos\\acc.txt")
   
