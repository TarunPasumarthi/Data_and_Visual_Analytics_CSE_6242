import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *


#
# implement your data retrieval code here
#
key=sys.argv[0]
url='/api/v3/lego/sets/?key='+ key +'&page_size=300&min_parts=1125&ordering=num_parts'
connection = http.client.HTTPSConnection('rebrickable.com')
connection.request('GET', url)
response = connection.getresponse()
json_str = json.loads(response.read().decode())

# complete auto grader functions for Q1.1.b,d
def min_parts():
    """
    Returns an integer value
    """
    # you must replace this with your own value
    return 1125

def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set

    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    # you must replace this line and return your own list
    result=json_str['results']
    ret_list=[]
    for i in range(len(result)):
        idx=len(result)-1-i
        item=result[idx]
        ret_list.append({"name":item['name'], "set_num":item['set_num']})
    return ret_list

### second part of data retieval

set_info_list=[]
sets=lego_sets()
for i in range(len(sets)):
    set_num=sets[i]['set_num']
    url="/api/v3/lego/sets/"+set_num+"/parts/?page_size=1000&key="+key
    connection.request('GET', url)
    response = connection.getresponse()
    part_json = json.loads(response.read().decode())
    res= part_json["results"]
    parts=[]
    for j in range(len(res)):
        part_number=res[j]["part"]["part_num"]
        part_color=res[j]["color"]["rgb"]
        part_name=res[j]["part"]["name"]
        part_quantity=res[j]["quantity"]
        parts.append({"part_number":part_number,"part_color":part_color,"part_name":part_name,"part_quantity":part_quantity})
    parts= sorted(parts, key = lambda i: i['part_quantity'],reverse=True)
    if (len(parts)>20):
        parts=parts[:20]
    set_info_list.append(parts)
    time.sleep(1)
    
for i in range(len(set_info_list)):
    for j in range(len(set_info_list[i])):
        set_info_list[i][j]["id"]=set_info_list[i][j]["part_number"]+set_info_list[i][j]["part_color"]

##### end of second part of data retreval ####

def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph
    gexf = Gexf("TarunPasumarthi", "Connections between lego sets and parts")
    graph= gexf.addGraph("undirected", "static", "Lego Sets and Parts")
    aid=graph.addNodeAttribute("Type",type="string")
    for i in range(len(sets)):
        sid=sets[i]["set_num"]
        label=sets[i]["name"]
        node=graph.addNode(sid, label, r="0", g="0", b="0", spells=[])
        node.addAttribute(aid, "set")
    for i in range(len(set_info_list)):
        for j in range(len(set_info_list[i])):
            part=set_info_list[i][j]
            sid=part["id"]
            if(not graph.nodeExists(sid)):
                label=part["part_name"]
                r=str(int(part["part_color"][:2],16))
                g=str(int(part["part_color"][2:4],16))
                b=str(int(part["part_color"][4:],16))
                node=graph.addNode(sid, label, r=r, g=g, b=b, spells=[])
                node.addAttribute(aid, "part")
            
            source=sets[i]["set_num"]
            target=sid
            weight=part['part_quantity']
            eid=source+target
            graph.addEdge(eid, source, target, weight=weight)
    file=open("bricks_graph.gexf","wb")
    gexf.write(file,print_stat=True)
    return graph

# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 5.471

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 4.407