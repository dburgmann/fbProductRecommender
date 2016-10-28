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
import java.util.*;

/**
 * Methods used by the Hirst and St-Onge measure in Relatedness.java.
 * 
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class HsoMethods {

    //upward, downward and horizontal relations: 
    public static ConRel[] upwardRelations = {ConRel.has_component_holonym,
               ConRel.has_member_holonym, ConRel.has_portion_holonym,
               ConRel.has_substance_holonym, ConRel.has_hypernym,
               ConRel.is_entailed_by};
    public static ConRel[] downwardRelations = {ConRel.has_component_meronym,
                ConRel.has_member_meronym, ConRel.has_portion_meronym,
                ConRel.has_substance_meronym, ConRel.has_hyponym,
                ConRel.entails, ConRel.causes};
    public static ConRel[] horizontalRelations = {ConRel.is_related_to};
    public static LexRel[] horizontalRels = {LexRel.has_synonym,
                LexRel.has_antonym, LexRel.has_participle, LexRel.has_pertainym};

   /*Allowable paths: u, ud, uh, uhd, d, dh, hd, h; at most 2 dir. changes.
     As all paths are the same either way, checking once is enough.
     Only pattern that needs to be added to even out both ways is hu,
     as reversal of dh (used to be disallowed, but needs to be included now).
     uhd is uhd in the other direction as well; same for ud.
     Still disallowed: du, dhu, duh, hud, udh, huh, dud, udu, dhd, uhu. 
     All other reversals are already present. */
   public static String allowedPaths = 
             "(?:(?:u+)?(?:h+)?(?:d+)?)|(?:d+(?:h+)?)|(?:h+u+)";

    /**
     * Recursively finds all possible paths from s to s2 and returns the score
     * of the best one.
     * @param s staring synset
     * @param s2 target synset
     * @param lexUnits lexUnits of s
     * @param path path up to this point (recursive method)
     * @param score best score to this point 
     * @param c constant given to HirstAndStOnge measure at start
     * @param k constant given to HirstAndStOnge measure at start
     * @return the best score found along all paths from s to s2
     */
    public static double findBestPath(Synset s, Synset s2, List<LexUnit> lexUnits,
           String path, double score, double c, double k,
           HashSet<Synset> visited) {

        //break if too long
        if (path.length()>=5) {
            return score;
        }

        String upwardPath = path+"u";
        //only continue if an upward transition is allowed at this point
        if (upwardPath.matches(allowedPaths)) {
            //only continue if a higher score can be reached on this path:
            double newScore = getScore(score, upwardPath, c, k);
            if (Double.compare(newScore,score)>0)  {
                //upward relations: has_*_holonym, has_hypernym, is_entailed_by
                score = checkConRels(upwardRelations, s, s2, upwardPath, score,
                    c,k, visited);
            }
        }

        String downwardPath = path+"d";
        //only continue if a downward transition is allowed at this point
        if (downwardPath.matches(allowedPaths)) {
            //only continue if a higher score can be reached on this path:
            double newScore = getScore(score, downwardPath, c, k);
            if (Double.compare(newScore,score)>0)  {
                //downward relations: has_*_meronym, has_hyponym, entails, causes
                score = checkConRels(downwardRelations, s, s2,
                    downwardPath, score, c,k, visited); //score unchanged if no larger one found
             }
        }

        String horizontalPath = path+"h";
        //only continue if a horizontal transition is allowed at this point
        if (horizontalPath.matches(allowedPaths)) {
            //only continue if a higher score can be reached on this path:
            double newScore = getScore(score, horizontalPath, c, k);
            if (Double.compare(newScore,score)>0)  {
                //check ConRel.isRelatedTo (also horizontal)
                score = checkConRels(horizontalRelations, s, s2, horizontalPath,
                    score, c, k, visited);
            }
            //check if score has changed before trying second horizontal set
            if (Double.compare(newScore,score)>0)  {
                //check all LexRels: synonym, antonym, participle, pertainym (horizontal)
                //keep track of visited units: do not double-check synonymy etc. between them
                HashSet<LexUnit> visitedUnit = new HashSet<LexUnit>();
                for (LexUnit u: lexUnits) {
                    visitedUnit.add(u);
                    score = checkLexRels(horizontalRels, s, s2, horizontalPath,
                        score, c, k, u, visited);
                }
            }
        }
        return score;
    }

    /**
     * Runs through all ConRel relations in the given array and checks the
     * resulting paths. The highest score is returned;
     * if no score is better than the current one, the current score is returned.
     * @param rels relations to be checked
     * @param s staring synset
     * @param s2 target synset
     * @param path path up to this point (recursive method)
     * @param score best score to this point
     * @param c constant given to HirstAndStOnge measure at start
     * @param k constant given to HirstAndStOnge measure at start
     * @return the best score found along the paths involving this step
     */
    public static double checkConRels(ConRel[] rels, Synset s, Synset s2,
            String path, double score, double c, double k, 
            HashSet<Synset> visited) {
        for (ConRel rel: rels) {
            List<Synset> theseRels = s.getRelatedSynsets(rel);
            if (theseRels.size()>0 ) {
                for (Synset hSynset: theseRels) {
                    if (visited.contains(hSynset))  {
                        continue;
                    }
                    //if end found, see if it yields higher score
                    if (hSynset.equals(s2)) {
                        score = getScore(score, path, c, k);
                        return score;
                    //if no end found, continue along this path
                    } else if (path.length()<5) {
                        HashSet<Synset> visited2 = (HashSet<Synset>)visited.clone();
                        visited2.add(s);
                        score = findBestPath(hSynset, s2,
                              hSynset.getLexUnits(), path, score, c, k, 
                              visited2);
                    //failed path: return old score
                    } else {
                        return score;
                    }
                }
            }
        }
        return score;
    }

    /**
     * Runs through all LexRel relations in the given array and checks the
     * resulting paths.
     * The highest score is returned; if no score is better than the
     * current one, the current score is returned.
     * @param rels relations to be checked
     * @param s staring synset
     * @param s2 target synset
     * @param path path up to this point (recursive method)
     * @param score best score to this point
     * @param c constant given to HirstAndStOnge measure at start
     * @param k constant given to HirstAndStOnge measure at start
     * @return the best score found along the paths involving this step
     */
    public static double checkLexRels(LexRel[] rels, Synset s, Synset s2,
            String path, double score, double c, double k, LexUnit u, 
            HashSet<Synset> visited) {
        for (LexRel rel: rels) {
            List<LexUnit> relatedUnits = u.getRelatedLexUnits(rel);
            if (relatedUnits.size()>0) {
                for (LexUnit relatedUnit: relatedUnits) {
                    if (visited.contains(relatedUnit.getSynset())) {
                        continue;
                    }
                    //if end found, see if it yields higher score
                    if (relatedUnit.getSynset().equals(s2)) {
                        score = getScore(score, path, c, k);
                        return score;
                    //if no end found, continue along this path
                    } else if (path.length()<5) {
                        HashSet<Synset> visited2 = (HashSet<Synset>)visited.clone();
                        visited2.add(relatedUnit.getSynset());
                        score = findBestPath(relatedUnit.getSynset(),
                                    s2, relatedUnit.getSynset().getLexUnits(),
                                    path, score, c, k, visited2);
                    //failed path: return old score
                    } else {
                        return score;
                    }
                }
            }
        }
        return score;
    }

    /*
     * Checks if the path is valid; if so, *checks* if it will lead to a higher
     * score than we have so far. *If so*, returns the new score; else, the old
     * one.
     */
    public static double getScore(double oldScore, String path, double c, double k) {
        //allowable paths: u, ud, uh, (hu), uhd, d, dh, hd, h; at most 2 dir. changes
        //if invalid path: keep old score
        if (!path.matches(allowedPaths)) {
            return oldScore;
        }
        //valid path: calculate new score
        else {
            int directionChanges = 0;
            //two different letters = 1 direction change, 3 letters = 2 changes
            if (path.contains("u")) {
                directionChanges++;
            }
            if (path.contains("d")) {
                directionChanges++;
            }
            if (path.contains("h")) {
                directionChanges++;
            }
            directionChanges -= 1;
            double newScore = c-path.length()-k*directionChanges;
            if (Double.compare(newScore, oldScore) > 0) {
                return newScore;
            }
        }
        //should only get here if something went wrong:
        return oldScore;
    }
}
