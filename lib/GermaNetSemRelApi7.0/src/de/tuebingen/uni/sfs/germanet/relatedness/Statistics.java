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
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculates some values used in the Relatedness class
 * for the current GermaNet version (GN 7.0).
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class Statistics {

    /**
     * Calculates the maximum depth by finding all the leaves and comparing
     * their distance to the root (edge counting).
     * (max.depth = 20 for current version)
     * @param gnet Instance of GermaNet.
     * @return the maximum depth of the hierarchy
     */
    public static int getMaxDepth(GermaNet gnet) {
        int max = 0;
        List<Synset> leaves = Path.getLeaves(gnet);
        //find the leaf that is furthest from the root
        for (Synset s: leaves) {
            Path p = new Path(s);
            int d = p.getDepth();
            if (d>max) {
                max=d;
            }
        }
        return max;
    }

    /**
     * Returns the shortest path between the two Sysets with the largest distance.
     * (Currently 39 for version 7.0).
     * @param gnet An instance of Germanet. 
     * @return the longest "shortest path" that exists between any two synsets
     */
    public static int getMaxShortestPath(GermaNet gnet) {
        List<Synset> allSynsets = gnet.getSynsets();
        int longestDistance = 0;
        Path[] synsetsAsPaths = new Path[allSynsets.size()];
        int i=0;
        for (Synset s: allSynsets) {
            synsetsAsPaths[i] = new Path(s);
            i++;
        }
        for (int j=0; j<i; j++) {
            System.out.print(".");
            for (int k=j; k<i; k++) {
                int distance = synsetsAsPaths[j].getDistance(synsetsAsPaths[k]);
                if (distance > longestDistance) {
                    longestDistance = distance;
                }
            }
        }
        System.out.println("\n");
        return longestDistance;
    }


    /**
     * Finds the maximum possible 'distance' (sum of information content values)
     * used in the Jiang & Conrath relatedness measure, which is
     * the IC of 2 leaf nodes with the highest IC (information content), with the
     * root as their LCS (least common subsumer): <br>
     * <code>max_IC + max_IC - 2*0.0 = 2*max_IC </code><br>
     * Assuming that a leaf has the assigned default minimal frequency of 1,
     * <code>max_IC = -log(1/rootFreq)</code>, which is approx. 37.51 for the
     * current version and frequency files.
     * @param frequencies HashMap holding the frequencies of all synsets
     * @return the maximum value ("distance") possible for jcn (2*maxIC)
     */
    public static double getMaxJcnValue(HashMap<String,Integer> frequencies) {
        double rootFreq = frequencies.get(Relatedness.ROOT);
        return 2.0*(-Math.log(1.0/rootFreq));
    }


    /**
     * Retrieves the maximum number of relations of any GermaNet Synset,
     * excluding hyponymy (currently 65).
     * @param gnet instance of GermaNet
     * @return maximum number of relations contained for any Synset
     */
    public static int getMaxRelsNoHyponyms(GermaNet gnet) {
        int relCounter = 0;
        List<Synset> synsets = gnet.getSynsets();
        ConRel[] relTypes = ConRel.values();
        for (Synset s: synsets) {
            int counter = 0;
            for (int i=0;i<relTypes.length;i++) {
                if (!relTypes[i].name().equals("has_hyponym")) {
                    List<Synset> rels = s.getRelatedSynsets(relTypes[i]);
                    if (rels != null) {
                        counter += rels.size();
                    }
                }
            }
            if (counter>relCounter) relCounter = counter;
        }
        return relCounter;
    }

    /**
     * Retrieves the maximum number of hypernyms of any GermaNet Synset,
     * (currently 6).
     * @param gnet instance of GermaNet
     * @return maximum number of hypernyms of any Synset
     */
    public static int getMaxHypernyms(GermaNet gnet) {
        int hyperCounter = 0;
        List<Synset> synsets = gnet.getSynsets();
        for (Synset s: synsets) {
            int counter = s.getRelatedSynsets(ConRel.has_hypernym).size();
            if (counter>hyperCounter) hyperCounter = counter;
        }
        return hyperCounter;
    }


    /**
     * Retrieves the maximum number of hyponyms of any GermaNet Synset,
     * (currently ).
     * Not currently being used by any of the Relatedness measures. 
     * @param gnet instance of GermaNet
     * @return maximum number of hyponyms of any Synset
     */
    public static int getMaxHyponyms(GermaNet gnet) {
        int hypoCounter = 0;
        List<Synset> synsets = gnet.getSynsets();
        for (Synset s: synsets) {
            int counter = s.getRelatedSynsets(ConRel.has_hyponym).size();
            if (counter>hypoCounter) hypoCounter = counter;
        }
        return hypoCounter;
    }


    /**
     * Retrieves the maximum number of orthForms of any GermaNet Synset
     * (currently 18).
     * @param gnet instance of GermaNet
     * @return maximum number of orthForms of any Synset
     */
    public static int getMaxOrthForms(GermaNet gnet) {
        int formCounter = 0;
        List<Synset> synsets = gnet.getSynsets();
        for (Synset s: synsets) {
            int counter = s.getAllOrthForms().size();
            if (counter>formCounter) formCounter = counter;
        }
        return formCounter;
    }

    /**
     * Retrieves the maximum number of words in any GermaNet gloss
     * (currently 33).
     * @param gnet instance of GermaNet
     * @return maximum Number of words in any GermaNet gloss/paraphrase
     */
    public static int getMaxGlossLength(GermaNet gnet) {
        int glossCounter = 0;
        int numGlosses = 0;
        List<Synset> synsets = gnet.getSynsets();
        for (Synset s: synsets) {
            String gloss = s.getParaphrase();
            if (gloss != null && gloss.trim().length() > 0) {
                numGlosses++;
                String[] words = gloss.split("[\\.,;!?\\s]+");
                if (words.length>glossCounter) glossCounter = words.length;
            }
        }
        return glossCounter;
    }

    /**
     * NO LONGER IN USE.
     * Calculates the maximum value theoretically possible for this Lesk
     * implementation,
     * with oneSense = false, size = maxDepth, limit = 0, hypernymsOnly = false,
     * includeGloss = true 
     * (currently 70686).
     * In practice, values will stay far from this maximum as this counts
     * every single word as a match - which it won't be. As such, this value is
     * impractical. Use method getLeskMax() below.
     * @param gnet an instance of GermaNet
     * @return (max. orth forms + max. gloss length) * (max. rels + this synset)
     *         * max. depth
     */
    public static int getMaxLeskValue(GermaNet gnet) {
        int value = (getMaxOrthForms(gnet) + getMaxGlossLength(gnet))
                * (getMaxRelsNoHyponyms(gnet)+1) * getMaxDepth(gnet);
        return value;
    }

    /**
     * NO LONGER IN USE. 
     * Calculates the maximum value actually possible for
     * the given settings for this Lesk implementation,
     * with oneSense = true/false, size = [0,maxDepth], limit = [0,maxDepth],
     * hypernymsOnly = true/false,includeGloss = true/false.
     * In practice, values will still stay far from this maximum as this counts
     * every single word as a match - which it will not be.
     * @param gnet an instance of GermaNet
     * 
     * @return (max. orth forms + max. gloss length) * (max. rels + this synset)
     *         * max. depth
     */
    public static int getLeskMax(GermaNet gnet, boolean oneSense, int size,
            int limit, boolean hypernymsOnly, boolean includeGloss) {
        int orthForms = 1;
        if (!oneSense) orthForms = getMaxOrthForms(gnet);
        int glosses = 0;
        if (includeGloss) glosses = getMaxGlossLength(gnet);
        int maxWordsPerSynset = orthForms + glosses;
        int relatedSynsets = size*getMaxHypernyms(gnet);
        if (!hypernymsOnly) relatedSynsets = size*getMaxRelsNoHyponyms(gnet);
        int value = maxWordsPerSynset * (relatedSynsets+1);
        return value; 
    }


    /**
     * Calculates Pearson's correlation between values from two files with
     * relatedness values for the same word pairs; order does not matter.
     * Files need to have same format, though: both words need to precede the
     * value, also anything else that may precede the value in one file has to
     * be present in the second file, as well.
     * @param file1 word pairs with relatedness values from one measure)
     * @param file2 word pairs with relatedness values from another measure
     * @param index position of value in the csv file (0,1,2...). Must be behind
     *        names; must be the same for both files.
     * @param encoding Encoding of both files.
     * @param separator the char(s) used to separate words in the input files
     * @param min Smallest possible value in the distribution (e.g. 0).
     * @param max Largest possible value in the distribution (e.g. 4).
     * @param includeUnknown if true, pairs including one or two words with -1
     *        values (unknown to GermaNet) are included in the calculation;
     *        if false, correlation is calculated only based on the pairs of
     *        known words.
     *        WARNING: as is, this also excludes entries where the method failed
     *        due to different categories. Need to distinguish! 
     * @return the Pearson correlation between values in the two files
     */
    public static double correlationBetweenTwoLists(String file1, String file2, 
            int index, String encoding, String separator,
            double min, double max, boolean includeUnknown) {
        double correlation = 0;
        try {
            Scanner reader1 = new Scanner(new File(file1), encoding);
            Scanner reader2 = new Scanner(new File(file2), encoding);
            Map<String, Double> map1 =  new ConcurrentHashMap<String, Double>();
            Map<String, Double> map2 =  new ConcurrentHashMap<String, Double>();
            //read both files into maps: 
            while (reader1.hasNextLine()) {
                String[] line = reader1.nextLine().split(separator);
                if (line.length < index) {
                    System.out.println("Formatting error: Line too short.");
                    System.exit(0);
                }
                String key = ""; //word pair (+other info) as key, rel as value
                for (int i=0; i<index; i++) {
                    key += line[i].trim()+separator; //key ends with separator
                }
                Double value = Double.parseDouble(line[index].trim());
                if (!includeUnknown && value < 0) {
                    continue; //skip entries with unknown words
                }
                map1.put(key, value);
            }
            while (reader2.hasNextLine()) {
                String[] line = reader2.nextLine().split(separator);
                if (line.length < index) {
                    System.out.println("Formatting error: Line too short.");
                    System.exit(0);
                }
                String key = ""; //word pair (+other info) as key, rel as value
                for (int i=0; i<index; i++) {
                    key += line[i]+separator; //key ends with separator
                }

                Double value = Double.parseDouble(line[index]);
                if (!includeUnknown && value < 0) {
                    continue; //skip entries with unknown words
                }
                map2.put(key, value);
            }
            reader1.close();
            reader2.close();

            //compare lists, delete unmatched entries
            if (!includeUnknown) { 
                for (String key: map1.keySet()) {
                    if (!map2.containsKey(key)) {
                        map1.remove(key);
                    }
                }
                //concurrentModificationException here if use simple HashMaps
                for (String key: map2.keySet()) {
                    if (!map1.containsKey(key)) {
                        map2.remove(key);
                    }
                }
            }

            //calculate means
            double mean1 = 0;
            double mean2 = 0;
            double totalValues = map1.values().size();
            double totalValues2 = map2.values().size();
            if (totalValues != totalValues2) {
                System.out.println("Lists do not match: different number of " +
                    "entries.");
                System.exit(0);
            }
            if (totalValues == 0) {
                System.out.println("No pairs in common.");
                return -1;
            }
            
            for (Double v: map1.values()) {
                mean1 += v;
            }
            mean1 = mean1/totalValues;
            for (Double v: map2.values()) {
                mean2 += v;
            }
            mean2 = mean2/totalValues;

            //calculate sum of squares of differences of each value from mean
            double sumOfDifferenceSquares1 = 0;
            double sumOfDifferenceSquares2 = 0;
            for (Double v: map1.values()) {
                sumOfDifferenceSquares1 += Math.pow(v-mean1, 2);
            }
            for (Double v: map2.values()) {
                sumOfDifferenceSquares2 += Math.pow(v-mean2, 2);
            }

            /*
            //calculate expected -- is this correct?!
            double expected = (max-min)*0.5;
            //calculate variance = average of the above sum of squares
            double variance1 = sumOfDifferenceSquares1/totalValues;
            double variance2 = sumOfDifferenceSquares2/totalValues;
            //calculate standard deviation = square root of variance
            double standardDeviation1 = Math.sqrt(variance1);
            double standardDeviation2 = Math.sqrt(variance2);
            //calculate covariances
            //...=?!?
            //calculate correlations from covariance and standard deviations
            //...?       */

            //--> try different method!
            //source: http://www.vias.org/tmdatanaleng/cc_corr_coeff.html

            double sumOfDifferenceProducts = 0;
            for (String key: map1.keySet()) {
                /*if (!includeUnknown && !map2.containsKey(key)) {
                    continue;
                }*/  //no longer necessary, lists now have same elements
                sumOfDifferenceProducts += (map1.get(key)-mean1)*
                                           (map2.get(key)-mean2);
            }
            correlation = sumOfDifferenceProducts/(Math.sqrt(
                    sumOfDifferenceSquares1*sumOfDifferenceSquares2));

            /*System.out.println("Pairs shared by both lists: "+
                    map1.keySet().size());
            System.out.println("Mean 1: "+mean1+", mean 2: "+mean2);
            System.out.println("sumOfDifferenceProducts = "+ sumOfDifferenceProducts);
            System.out.println("sumOfDifferenceSquares1 and 2 = "+
                    sumOfDifferenceSquares1+", "+sumOfDifferenceSquares2); */
            System.out.println("Correlation = "+correlation);
            return correlation;
        } catch (IOException e) {
            System.out.println("Error reading files: "+e.getMessage());
            System.exit(0);
        }
        return -1; //should never happen
    }


}
