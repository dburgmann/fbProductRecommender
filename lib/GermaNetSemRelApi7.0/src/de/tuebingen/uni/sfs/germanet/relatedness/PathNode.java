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

import de.tuebingen.uni.sfs.germanet.api.*;

/**
 * Represents a node in the path from one node to the root.
 * As such, it holds the synset and a counter (distance to the starting point,
 * below).
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class PathNode implements Comparable<PathNode> {
    public final Synset synset;
    public final int index;

    /**
     * Constructor taking a synset and staring index.
     * @param s This synset.
     * @param i distance to the starting point (below)
     */
    public PathNode(Synset s, int i) { 
        synset = s;
        index = i;
    }

      /**
     * Constructor taking a synset; sets starting index to 1.
     * @param s This synset.
     */
    public PathNode(Synset s) {
        synset = s;
        index = 1;
    }

    /**
     * Equals method designed towards testing equality on the path.
     * Two PathNodes are only equal if their distance from the starting point
     * is also the same, not only the synset they carry.
     * @param pn other PathNode.
     * @return true if same synset at same point in path
     */
    public boolean equals(PathNode pn) {
        return (synset.equals(pn.synset) && index==pn.index);
    }

    /**
     * String representation of this PathNode object.
     * The String contains synset and index (position on the path).
     * @return a String representation of this PathNode object
     * @override toString()
     */
    @Override
    public String toString() {
        return "Synset: "+synset+", index: "+index;
    }

    /**
     * Makes this object comparable.
     * @param pn other PathNode object
     * @return -1 if index is less, 0 if equal, 1 if greater than index of other
     *         PathNode
     */
    public int compareTo(PathNode pn) {
        if (index < pn.index) return -1;
        else if (index == pn.index) return 0;
        else return 1;
    }
}