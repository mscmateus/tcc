import os
from datetime import datetime
import csv
import re

atributos = [
"projeto"  
,"created_at_week_day"
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
def valores():
   matriz = []
   matriz.append(atributos)
   linha = iniciaLinha(len(atributos))
   file = open("resultado.txt", newline="")
   primeiro = True
   valor = []
   for line in file:
      valor = re.sub(r'\(.+?\)',";",line.replace(" ","")).replace(".",",").replace("\n","").split(";")
      if valor[1] == "projeto":
         if primeiro == True:
            primeiro = False
            linha[atributos.index(valor[1])] = valor[0]
         else:
            matriz.append(linha)
            linha = iniciaLinha(len(atributos))
            linha[atributos.index(valor[1])] = valor[0]
      else:
         linha[atributos.index(valor[1])] = valor[0]
   matriz.append(linha)
   return matriz

def iniciaLinha(size):
   aux = []
   for i in range(0,size):
      aux.append("")
   return aux

file = open("resultados.csv","w",newline='')
csvFile = csv.writer(file, delimiter=';')
csvFile.writerows(valores())
# file = open("resultado.txt")
# for line in file:
#    print(re.sub(r'\(.+?\)',";",line.replace(" ","")).replace(".",",").replace("\n","").split(";"))