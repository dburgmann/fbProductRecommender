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
import de.tuebingen.uni.sfs.germanet.api.Synset;
import java.util.List;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tree;

/**
 *Class for buildung up the graph structure in prefuse
 * @author stefanie
 */
public class PrefuseTreeStructure {

    Table nodes;
    Tree tree;
    Node activeNode;
    public static final String TYPE = "type";

    /**
     * initialising a tree, represented by a table of nodes
     */
    public PrefuseTreeStructure() {
        tree = new Tree();
        nodes = tree.getNodeTable();
        activeNode = null;
        //Column with the synset id and orthforms, will be used as node labels
        nodes.addColumn("Label", TYPE.getClass());

    }

    /**
     *@param s will be added as a child to the previous node
     */
    public void addNode(Synset s) {
        Node n;
        //first node added to the tree will be the root
        if (activeNode == null) {
            n = tree.addRoot();
        } else {
            n = tree.addChild(activeNode);
        }
        //node of the current synset is the new activeNode
        activeNode = n;
        String label = s.getId() + "\n";
        List<String> orthForms = s.getAllOrthForms();
        int j = 0;
        for (int i = 0; i < orthForms.size(); i++) {
            if (j < 2) {
                label += orthForms.get(i) + ", ";
                if (orthForms.get(i).length() > 10) {
                    j++;
                }
                j++;
            } else {
                if (orthForms.get(i).length() > 10) {
                    label += "\n" + orthForms.get(i) + ", ";
                    j = 1;
                } else {
                    label += orthForms.get(i) + ",\n";
                    j = 0;
                }
            }
        }
        label = label.substring(0, label.length() - 2);
        activeNode.set("Label", label);
    }

    /**
     *
     * @param s will be added to the rootnode
     */
    public void addtoRoot(Synset s) {
        activeNode = tree.getRoot();
        Node n = tree.addChild(activeNode);
        activeNode = n;
        String label = s.getId() + "\n";
        List<String> orthForms = s.getAllOrthForms();
        int j = 0;
        for (int i = 0; i < orthForms.size(); i++) {
            if (j < 2) {
                label += orthForms.get(i) + ", ";
                j++;
            } else {
                label += orthForms.get(i) + "\n";
                j = 0;
            }
        }
        activeNode.set("Label", label);

    }

    /**
     * @return the tree
     */
    public Tree getTree() {
        return tree;
    }
}
