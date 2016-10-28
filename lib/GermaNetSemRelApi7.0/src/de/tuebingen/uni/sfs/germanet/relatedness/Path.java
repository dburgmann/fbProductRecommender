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
package de.tuebingen.uni.sfs.germanet.relatedness;

import java.util.*;
import de.tuebingen.uni.sfs.germanet.api.*;

/**
 * A class that finds paths from one synset to the root
 * or from one synset to another. Also includes depth, LCS (least common subsumer) 
 * and leaf nodes. Constructor takes the starting synset as input.
 * Uses edge-counting.
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class Path {

    private HashSet<PathNode> roots; //ends of all possible paths up (GNROOT)
    private PathNode[] sorted; //roots, sorted by path length to starting point
    private HashMap<PathNode, ArrayList<PathNode>> visited; //nodes seen on the 
    //way(s) up and list of their predecessors
    private Synset initialSynset;

    /**
     * Constructor calculates in advance all paths from given synset to root.
     * @param synset The synset for which a path is built.
     */
    public Path(Synset synset) {
        roots = new HashSet<PathNode>();
        visited = new HashMap<PathNode, ArrayList<PathNode>>();
        initialSynset = synset;

        PathNode node = new PathNode(synset, 0); //no predecessors; edge counting: 0
        visited.put(node, null); //keep track of all nodes seen on the way up

        //get all possible paths to the root
        Stack<PathNode> todo = new Stack<PathNode>(); //used if several hypernyms
        pathsToRoot(node, todo);

        //sort 'roots' by path length
        sorted = new PathNode[roots.size()];
        sorted = roots.toArray(sorted);
        Arrays.sort(sorted);  //sorted[0] holds info about shortest path to root
    }


    /*
     * Finds all paths to the root node.
     */
    private void pathsToRoot(PathNode node, Stack<PathNode> todo) {
        //this set can only be empty for the root node, id 51001:
        List<Synset> hypernyms = node.synset.getRelatedSynsets(ConRel.has_hypernym);
        if (hypernyms.isEmpty()) {  //found the root
            roots.add(node);
            //if other options are left on the stack, try other paths:
            if (!todo.empty()) {
                pathsToRoot(todo.pop(), todo);
            }
        } else {
            int i = node.index + 1; //distance from staring point increases
            //assume more paths to this node might be added later:
            ArrayList<PathNode> previous = new ArrayList<PathNode>(5);
            previous.add(node);
            for (Synset synset : hypernyms) {
                PathNode pn = new PathNode(synset, i); //previous, distance from start
                if (visited.containsKey(pn)) { //tail-sharing: add to previous
                    ArrayList<PathNode> previous2 = visited.get(pn);
                    previous2.add(node);
                    visited.remove(pn);
                    visited.put(pn, previous2);
                } else { //continue on this path
                    visited.put(pn, previous);
                    pathsToRoot(pn, todo);
                }
            }
        }
    }

    /**
     * Returns the depth of this synset in GermaNet (edge counting).
     * @return the depth of the synset used to construct this Path object
     */
    public int getDepth() {
        return sorted[0].index; //path length of shortest path to root
    }

    /**
     * Returns the shortest path(s) to this synset (there may be more than one).
     * Backtracks from root to the synset indicated in the constructor.
     * @return all synsets on the shortest path(s) from the root to this synset
     */
    public ArrayList<ArrayList<Synset>> getShortestPaths() {
        PathNode root = sorted[0];
        ArrayList<Synset> before = new ArrayList<Synset>();
        before.add(root.synset);
        return paths(root, before);
    }

    /**
     * Returns all possible paths from the root to this synset. 
     * Backtracks from root to the synset indicated in the constructor. 
     * @return all paths from the root to this synset
     */
    public ArrayList<ArrayList<Synset>> getAllPaths() {
        ArrayList<ArrayList<Synset>> paths = new ArrayList<ArrayList<Synset>>();
        ArrayList<Synset> before;
        for (PathNode root : roots) {
            before = new ArrayList<Synset>();
            before.add(root.synset);
            ArrayList<ArrayList<Synset>> p = paths(root, before);
            for (ArrayList<Synset> list : p) {
                paths.add(list);
            }
        }
        return paths;
    }


    /*
     * helper method to getShortestPaths() and getAllPaths():
     * does the backtracking and fills the ArrayLists with paths
     * (shortest to longest)
     */
    private ArrayList<ArrayList<Synset>> paths(PathNode current,
            ArrayList<Synset> pathSoFar) {
        ArrayList<ArrayList<Synset>> paths = new ArrayList<ArrayList<Synset>>();
        ArrayList<PathNode> previous = visited.get(current);
        while (previous != null) { //down to starting point
            for (int i = 1; i < previous.size(); i++) { //split paths if > 1 previous
                //System.out.println("Splitting paths for previous: "+previous);
                pathSoFar.add(previous.get(i).synset);
                ArrayList<ArrayList<Synset>> p = paths(previous.get(i), pathSoFar);
                for (ArrayList<Synset> list : p) {
                    paths.add(list);
                }
            }
            pathSoFar.add(previous.get(0).synset);
            current = previous.get(0);
            previous = visited.get(previous.get(0)); //next node down
        }
        paths.add(pathSoFar);
        return paths;
    }

    /**
     * Returns an ArrayList of least common subsumers (LCS) of this Path object's
     * starting synset and that of the given other Path. <br>
     * The LCS PathNode's index is the distance between the two synsets.
     * @param otherPath The pathNode with which to find the least common subsumer.
     * @return the PathNode object(s) representing the LCS(s) of the two
     *         synsets; PathNode.index = distance between the two path nodes.
     */
    public ArrayList<PathNode> getLeastCommonSubsumer(Path otherPath) {
        PathNode[] visited1 = this.sortVisited();
        PathNode[] visited2 = otherPath.sortVisited();
        ArrayList<PathNode> matches = new ArrayList<PathNode>();
        for (int i = 0; i < visited1.length; i++) {
            for (int j = 0; j < visited2.length; j++) {
                PathNode node1 = visited1[i];
                PathNode node2 = visited2[j];
                if (node1.synset.equals(node2.synset)) {
                    /* System.out.println("Match: "+p1.synset+" (index "+p1.index+
                    ") - "+p2.synset+" (index "+p2.index+")");*/
                    //both indices are edge counting: simply add.
                    int pathLength = node1.index + node2.index;
                    /* System.out.println("PathLength: "+pathLength);*/
                    //here: index = distance btw. the 2 synsets
                    matches.add(new PathNode(node1.synset, pathLength));
                }
            }
        }
        PathNode[] matches2 = matches.toArray(new PathNode[matches.size()]);
        Arrays.sort(matches2);  //put shortest paths to the front
        int length = matches2[0].index;
        ArrayList<PathNode> shortest = new ArrayList<PathNode>();
        for (int i = 0; i < matches2.length; i++) { //only keep shortest
            if (matches2[i].index == length) {
                shortest.add(matches2[i]);
            } else {
                break;
            }
        }
        return shortest;
    }

    /**
     * Returns an ArrayList of Synsets that are LCS of this path object and otherPath
     * @param otherPath the path with which the LCS is to be found
     * @return ArrayList of LCS-Synset
     */
    public ArrayList<Synset> getLeastCommonSubsumerSynset(Path otherPath) {
        ArrayList<PathNode> lcs = getLeastCommonSubsumer(otherPath);
        ArrayList<Synset> synsetLCS = new ArrayList<Synset>();
        for (PathNode n : lcs) {
            synsetLCS.add(n.synset);
        }
        return synsetLCS;
    }

    /**
     * In case that the parameter synset is an ancestor of the current Synset,
     * this method returns the path between the two Synsets.
     * @param synset possible ancestor of the current synset
     * @return ArrayList path between the current synset and the one passed as argument
     */
    public ArrayList<Synset> pathToAncestorSynset(Synset synset) {
        ArrayList<ArrayList<Synset>> synsetPaths = new ArrayList<ArrayList<Synset>>();

        int currIndex = Integer.MAX_VALUE;
        for (PathNode n : visited.keySet()) {
            if (n.synset.equals(synset)) {
                if (n.index < currIndex) {
                    ArrayList<Synset> path = new ArrayList<Synset>();
                    path.add(synset);
                    synsetPaths = paths(n, path);
                    currIndex = n.index;
                }
            }
        }

        ArrayList<Synset> synsetPath = new ArrayList<Synset>();
        int currLength = Integer.MAX_VALUE;
        for (ArrayList<Synset> list : synsetPaths) {
            if (list.size() < currLength) {
                synsetPath = list;
                currLength = list.size();
            }
        }
        return synsetPath;
    }

    /*
     * Combines all keys and values of HashMap 'visited' into a single,
     * sorted array with no double entries.
     * The starting Synset is included in this set. Also, there may be
     * several entries for root, one for each path and its length (necessary to
     * get the least common subsumer (lcs)).
     * Returns an array of PathNode objects (with synset and distance to this 
     * Path's starting synset)
     */
    private PathNode[] sortVisited() {
        ArrayList<PathNode> all = new ArrayList();
        Set<PathNode> keys = visited.keySet();
        //add all keys (roots) and values (nodes on the paths) to the same list
        for (PathNode key : keys) {
            if (!all.contains(key)) {
                all.add(key);
            }
            ArrayList<PathNode> value = visited.get(key);
            if (value != null) {  //should only be null for key=starting synset
                for (PathNode p : value) {
                    if (!all.contains(p)) {
                        all.add(p);
                    }
                }
            }
        }
        PathNode[] ordered = all.toArray(new PathNode[all.size()]);
        Arrays.sort(ordered);  //sort from shortest to longest path (index)
        return ordered;
    }

    /**
     * Returns a HashSet that includes each synset seen on the different ways
     * from this synset to the root exactly once, including the starting synset
     * and the root.
     * More precisely, this combines all keys and values of nested HashMap
     * 'visited' (of PathNodes) into a single map of synsets with no double
     * entries. Starting synset and root node are included only once, even
     * though they may appear on several paths up to the root.
     * @return HashSet of Synsets that are on this synset's possible paths to
     *         the root, including starting point and root
     */
    public HashSet<Synset> getAllHypernyms() {
        HashSet<Synset> all = new HashSet<Synset>();
        all.add(sorted[0].synset); //add root node, but only once
        Collection<ArrayList<PathNode>> values = visited.values();
        //add all values (nodes on the paths up) to the same list
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            ArrayList<PathNode> nodes = (ArrayList<PathNode>) iterator.next();
            if (nodes != null) {
                for (int i = 0; i < nodes.size(); i++) {
                    Synset s = nodes.get(i).synset;
                    all.add(s);
                }
            }
        }
        return all;
    }

    /**
     * Returns this Path object's HashMap of nodes seen on different ways up to
     * the root. This will contain multiple entries, with the PathNodes differing
     * only in value (distance from the starting point).
     * @return a HashMap of nodes seen on the way up to the root and their
     *         distance from the starting point.
     */
    public HashMap<PathNode, ArrayList<PathNode>> getVisited() {
        return visited;
    }

    /**
     * Returns the distance between this synset and the synset in the Path
     * argument. 
     * Implicit call to getLeastCommonSubsumer: shortest path between two
     * synsets calculated in getLeastCommonSubsumer.
     * @param p the other Path object
     * @return the distance between the two synsets (edge counting)
     */
    public int getDistance(Path p) {
        ArrayList<PathNode> lcs = getLeastCommonSubsumer(p);
        return (lcs.get(0).index);
    }

    /**
     * Finds all leaf nodes of the hierarchy.
     * @param gnet Instance of Germanet.
     * @return A list of all Synsets that are leaf nodes.
     */
    public static List<Synset> getLeaves(GermaNet gnet) {
        //get all Synsets in the hierarchy:
        List<Synset> leaves = gnet.getSynsets();
        //remove the ones that are not leaves:
        for (int i = 0; i < leaves.size(); i++) {
            List<Synset> hyponyms = leaves.get(i).getRelatedSynsets(ConRel.has_hyponym);
            if (!hyponyms.isEmpty()) {
                leaves.remove(i);
                i--;
            }
        }
        return leaves;
    }

    /**
     * Returns the Synset used to initialize this Path object.
     * @return the initial Synset used to build this Path object
     */
    public Synset getInitialSynset() {
        return initialSynset;
    }
}
