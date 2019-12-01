import os
from datetime import datetime
import csv

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
,"files_name"
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
   auxAtributos = atributos.copy()
   file = open("appinum.txt")
   inicio = 0
   for line in file:
      valor = []
      valor.append(line.replace(" ","",-1).split("(")[0])
      valor.append(line.replace(" ","",-1).split("(")[1].split(")")[1].replace("\n",""))
      if valor[1] == "projeto":
         if inicio < 1:
            inicio = 1
            possicao = auxAtributos.index(valor[1])
            auxAtributos[possicao] = valor[0]
         else:
            matriz.append(auxAtributos)
            auxAtributos = atributos.copy()
            possicao = auxAtributos.index(valor[1])
            auxAtributos[possicao] = valor[0]
      else:
         possicao = auxAtributos.index(valor[1])
         auxAtributos[possicao] = valor[0].replace(".",",")
   matriz.append(auxAtributos)
   return matriz
      
file = open("resultados.csv","w",newline='')
csvFile = csv.writer(file, delimiter=';')
csvFile.writerows(valores())