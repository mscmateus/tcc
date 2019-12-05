import os
from datetime import datetime
import csv
import re
proetos = [
   "Appium"
   ,"Bundler"
   ,"Candlepin"
   ,"Diaspora"
   ,"Hazelcast"
   ,"Kuma"
   ,"Marathon"
   ,"Metasploit Framework"
   ,"Meteor"
   ,"Nancy"
   ,"Netty"
   ,"Node"
   ,"Okhttp"
   ,"Pouchdb"
   ,"Pulp"
   ,"Rosdistro"
   ,"Scala IDE"
   ,"Scala JS"
   ,"Scikit"
   ,"Vagrant"
]
atributos = [
"created_at_week_day"
,"created_at_day_turn"
,"conflict"
,"forward_links"
,"intra_branch"
,"description_length"
,"num_commits"
,"files_added"
,"files_deleted"
,"files_modified"
,"files_changed"
,"src_files"
,"doc_files"
,"other_files"
,"src_churn"
,"test_churn"
,"new_entropy"
,"entropy_diff"
,"commits_on_files_touched"
,"commits_to_hottest_file"
,"hotness"
,"at_mentions_description"
,"at_mentions_comments"
,"prev_pull_reqs_project"
,"project_succ_rate"
,"perc_external_contribs"
,"sloc"
,"test_lines_per_kloc"
,"test_cases_per_kloc"
,"asserts_per_kloc"
,"stars"
,"team_size"
,"project_age"
,"workload"
,"ci"
,"requester"
,"prev_pullreqs"
,"requester_succ_rate"
,"followers"
,"following"
,"requester_age"
,"main_team_member"
,"watcher_project"
,"req_follows_integrator"
,"integrator_follows_req"
,"prior_interaction_issue_events"
,"prior_interaction_issue_comments"
,"prior_interaction_pr_events"
,"prior_interaction_pr_comments"
,"prior_interaction_commits"
,"prior_interaction_commit_comments"
,"first_response"
]

def pegaValor(elem):
    return elem[0]
 
def printBest(lista):
   print("15 melhohres: ")
   for i in range(0,15):
      print(atributos.index(lista[i][1])+1, end=", ")
      
file = open("resultados.csv", "r",newline='')
csvFile = csv.reader(file, delimiter=';')
# matriz = []
# for registro in csvFile:
registros = []
for linha in csvFile:
   registro = []
   for i in range(0,52):
      registro.append((float(linha[i]),atributos[i]))
   registros.append(registro)
      
for j in range(14,20):
   registros[j].sort(reverse=True,key=pegaValor)
   print(str(proetos[j])+":")
   for i in range(0,52):
      print(str(i+1)+" possição: "+str(atributos.index(registros[j][i][1])+1)+", nome: "+str(registros[j][i][1])+", valor: "+str(registros[j][i][0]))
   print()
   printBest(registros[j])
   

