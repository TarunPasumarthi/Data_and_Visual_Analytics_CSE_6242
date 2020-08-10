from util import entropy, information_gain, partition_classes
import numpy as np 
import ast

class DecisionTree(object):
    def __init__(self):
        # Initializing the tree as an empty dictionary or list, as preferred
        self.tree = []
        self.ct=0
        #self.tree = {}
        pass

    def learn(self, X, y):
        # TODO: Train the decision tree (self.tree) using the the sample X and labels y
        # You will have to make use of the functions in utils.py to train the tree
        
        # One possible way of implementing the tree:
        #    Each node in self.tree could be in the form of a dictionary:
        #       https://docs.python.org/2/library/stdtypes.html#mapping-types-dict
        #    For example, a non-leaf node with two children can have a 'left' key and  a 
        #    'right' key. You can add more keys which might help in classification
        #    (eg. split attribute and split value)
        self.ct=self.ct+1
        if(len(set(y)) == 1 ):
            node={"attribute":-1, "value":-1, "left":-1, "right":-1, "label":y[0]}
            self.tree.append(node)
            self.ct=self.ct-1
            return len(self.tree)-1
        
        best_attr=0
        best_val=0
        best_ig=0
        best_xl=[]
        best_xr=[]
        best_yl=[]
        best_yr=[]
        first=True
        npX=np.array(X)
        
        for attr in range(len(npX.T)):
            col=npX.T[attr]
            val=np.median(col)
            xl,xr,yl,yr=partition_classes(X,y,attr,val)
            ig=information_gain(y,[yl,yr])
            if(first or ig>best_ig):
                best_ig=ig
                best_attr=attr
                best_val=val
                best_xl=xl
                best_xr=xr
                best_yl=yl
                best_yr=yr
                first=False
        
        if(len(best_xl)==0 or len(best_xr)==0 or self.ct>20):
            mode=max(set(y), key=y.count)
            node={"attribute":-1, "value":-1, "left":-1, "right":-1, "label":mode}
            self.tree.append(node)
            self.ct=self.ct-1
            return len(self.tree)-1    
                                
        ix_l=self.learn(best_xl,best_yl)
        ix_r=self.learn(best_xr,best_yr)
        node={"attribute":best_attr, "value":best_val, "left":ix_l, "right":ix_r, "label":-1}
        self.tree.append(node)
        self.ct=self.ct-1
        return len(self.tree)-1

    def classify(self, record):
        # TODO: classify the record using self.tree and return the predicted label
        node= self.tree[-1]
        while(node["label"]==-1):
            attr=node["attribute"]
            val=node["value"]
            if(isinstance(val, str)):
                if(record[attr]==val):
                    node=self.tree[node["left"]]
                else:
                    node=self.tree[node["right"]]
            else:
                if(record[attr]<=val):
                    node=self.tree[node["left"]]
                else:
                    node=self.tree[node["right"]]
        return node["label"]
