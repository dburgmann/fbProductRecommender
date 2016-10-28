/*
 * Copyright (C) 2012 Department of General and Computational Linguistics,
 * University of Tuebingen
 *
 * This file is part of the Java Relatedness API to GermaNet.
 *
 * The Java Relatedness API to GermaNet is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Java Relatedness API to GermaNet is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this API; if not, see <http://www.gnu.org/licenses/>.
 */
package de.tuebingen.uni.sfs.germanet.relatedness.gui;

/**
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */

import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

import de.tuebingen.uni.sfs.germanet.relatedness.Path;


import de.tuebingen.uni.sfs.germanet.relatedness.PathNode;
import java.util.ArrayList;

import javax.swing.JComponent;

public class ShowPath {

    Path path;
    GermaNet gnet;

    public ShowPath(Path p, GermaNet gnet) {
        path = p;
        this.gnet = gnet;

    }

    public String showPathtoRoot(GermaNet gnet) {

        String p = "";
        p+="All paths to root for synset: "+path.getInitialSynset().getId()+":"+path.getInitialSynset().getAllOrthForms()+"\n";
        ArrayList<ArrayList<Synset>> pathsToRoot = path.getAllPaths();
        p += "Number of Paths: " + pathsToRoot.size() + "\n";


        for (ArrayList currPath : pathsToRoot) {
            PrefuseTreeStructure gs = new PrefuseTreeStructure();
            for (Object currSynset : currPath) {

                Synset synset = (Synset) currSynset;
                p += synset.getId() + " " + synset.getAllOrthForms() + "<--";
                gs.addNode(synset);

            }
            p=p.substring(0,p.length()-3)+"\n";
            TreeView gv = new TreeView(gs.getTree(), "Label");
            JComponent treeview = gv.showPath(gs.getTree(), "Label");
        }

        return p+"\n";

    }

    public String showShortestPathtoRoot() {
        String p = "";
        p+="Shortest path to root for synset: "+path.getInitialSynset().getId()+":"+path.getInitialSynset().getAllOrthForms()+"\n";
        ArrayList<ArrayList<Synset>> shortestPaths = path.getShortestPaths();
        PrefuseTreeStructure gs = new PrefuseTreeStructure();
        for (ArrayList currPath : shortestPaths) {

            for (Object currSynset : currPath) {
                Synset synset = (Synset) currSynset;
                p += synset.getId() + " " + synset.getAllOrthForms()+"<--";
                gs.addNode(synset);


            }

        }

        TreeView gv = new TreeView(gs.getTree(), "Label");
        JComponent treeview = gv.showPath(gs.getTree(), "Label");

        return p.substring(0, p.length()-3) + "\n";
    }

    public String showShortestPath(Synset s2) {
        String p = "";
        Path otherPath = new Path(s2);
        ArrayList<Synset> lcs = path.getLeastCommonSubsumerSynset(otherPath);
        for (Synset s : lcs) {
            PrefuseTreeStructure tree = new PrefuseTreeStructure();
            ArrayList<Synset> synsetPath1 = path.pathToAncestorSynset(s);
            ArrayList<Synset> synsetPath2 = otherPath.pathToAncestorSynset(s);

            for (Synset s1 : synsetPath1) {

                tree.addNode(s1);
            }
            int i = 0;
            for (Synset syn2 : synsetPath2) {

                //path from lcs to synset 2 gets added to the old root of the tree
                if (i == 1) {
                    tree.addtoRoot(syn2);
                    i++;
                } else if (i > 1) {
                    tree.addNode(syn2);
                    //already has the lcs as rootnode
                } else {
                    i++;
                }
            }
            TreeView gv = new TreeView(tree.getTree(), "Label");
            JComponent treeview = gv.showPath(tree.getTree(), "Label");
        }

        return p;
    }
}
