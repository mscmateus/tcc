base = [
5,8,20,21,23,31,32,33,34,39,42,43,44,47,53,57
]

for i in range(1,58):
   try:
      base.index(i)
   except :
      print(str(i)+',',end="")