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
import javax.xml.stream.*;
import org.tartarus.snowball.*;

/**
 * Implements some of the more well-known relatedness measures for GermaNet
 * API version 7.0. <br>
 * Where paths are involved (all methods but Lesk's and Hirst&St.Onge's),
 * the methods all do edge counting,
 * i.e. <code>identity = distance 0, parent = 1, sister nodes = 2.</code><br>
 * They also all return -1 if the input words have different categories, as no
 * useful relatedness measure can be computed in that case
 * (reason: GermaNet keeps nouns, verbs and adjectives in different subtrees of
 * the hypernym hierarchy, though connected by a common root node; paths between
 * different categories are overly long and falsify relatedness results).<br>
 * In the following short summary of the methods, LCS= least common subsumer of 
 * synsets s1 and s2, dist = distance between two synsets.<br>
 *<p>
 * path: <br>
 * <code>rel(s1,s2) = (max_dist-dist(s1,s2))/max_dist </code><br>
 * wuAndPalmer:<br>
 * <code>rel(s1,s2) = (2*depth(lcs)) / (dist(s1,lcs)+dist(s2,lcs)+2*depth(lcs))</code><br>
 * leacockAndChodorow:  <br>
 * <code>rel(s1,s2) = -log(dist(s1,s2)/2*max_depth)</code><br>
 * resnik:  <br>
 * <code>rel(s1,s2) = -log(p(lcs)) = IC(lcs)</code><br>
 * lin:  <br>
 * <code>rel(s1,s2) = 2*IC(lcs) / (IC(s1) + IC(s2))</code><br>
 * jiangAndConrath:  <br>
 * <code>rel(s1,s2) = max_dist - (IC(c1) + IC(c2) − 2*IC(lcs))</code><br>
 * hirstAndStOnge:
 * <code>rel(s1,s2) = 15</code><br> for strong Relations<br>
 * <code>rel(s1,s2) = C-pathLength-k*directionChanges</code><br> for
 * medium-strong Relations<br>
 * lesk: <br>
 * <code>rel(s1,s2) = sum(word_overlap(s1,s2)), extended by related synsets</code><br>
 *<p>
 * The javadoc for each method includes the hypothetical minimum and maximum
 * values for that method, which may or may not ever be reached in practice.
 * Values in javadoc are taken from GermaNet API version 7.0 and XML version 6.0
 * and may not apply to later versions.
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class Relatedness {

    public static GermaNet gnet;  //an instance of GermaNet
    public static int MAX_DEPTH = 20;  //maximum depth of this version of GermaNet
    public static int MAX_SHORTEST_PATH = 39; //longest existing 'shortest path'
                                              //btw. any 2 synsets
    public static String ROOT = "51001"; //GermaNet root node ID

    /**
     * Constructor taking a GermaNet instance as input.
     * @param germanet an instance of the GermaNet class.
     */
    public Relatedness(GermaNet germanet) {
        gnet = germanet;
        initialize();
    }

    /**
     * Constructor taking the path to a GermaNet-XML directory as input;
     * instanciates a GermaNet object with (germanetDirectory, true), i.e.
     * case insensitive.
     * @param germanetDirectory
     */
    public Relatedness (String germanetDirectory) {
        try {
            gnet = new GermaNet(germanetDirectory, true);
            initialize();
        }
        catch (IOException e) {
            System.out.println("Problem reading GermaNet file: "+e.getMessage());
            System.exit(0);
        }
        catch (XMLStreamException e) {
            System.out.println("Cannot build GermaNet object from given xml: "
                    +e.getMessage());
            System.exit(0);
        }

    }

    /* if a relatedness.ini file is found, reads its values;
     * if not, constructs a new ini file with presumed version,
     * calculated max_depth and default max_distance. 
     */
    private void initialize() {
        try {
            File iniFile = new File("./relatedness.ini");
            if (iniFile.exists()) {
                Scanner sc = new Scanner(iniFile);
                String[] line = sc.nextLine().split("\\s+");
                if (line[0].equalsIgnoreCase("VERSION")) {
                    if (!line[1].equals("7.0") && ! line[1].equals("7")) {
                        System.out.println("Wrong version: This is written for " +
                                "Germanet API Version 7.0!");
                        System.exit(0);
                    }
                }
                line = sc.nextLine().split("\\s+");
                if (line[0].equalsIgnoreCase("MAX_DEPTH")) {
                    MAX_DEPTH = Integer.parseInt(line[1]);
                }
                else {
                    System.out.println("Error reading ini file. Expected " +
                            "MAX_DEPTH not found.");
                    System.exit(0);
                }
                line = sc.nextLine().split("\\s+");
                if (line[0].equalsIgnoreCase("MAX_SHORTEST_PATH")) {
                    MAX_SHORTEST_PATH = Integer.parseInt(line[1]);
                }
                else {
                  System.out.println("Error reading ini file. Expected " +
                          "MAX_SHORTEST_PATH not found.");
                  System.exit(0);
                }
                
                /*line = sc.nextLine().split("\\s+");
                if (line[0].equalsIgnoreCase("ROOT_FREQ")) {
                    ROOT_FREQ = Double.parseDouble(line[1]);
                }
                else {
                    System.out.println("Error reading ini file. Expected contents" +
                            "not found.");
                    System.exit(0);
                }*/
            }
            else {  //no ini file exists: create one!
                System.out.println("Creating ini file. \n" +
                        "Assuming use of GermaNet API version 7.0. \n" +
                        "Assuming root node id = 51001. \n" +
                        "Calculating MAX_DEPTH. \n" +
                        "Substituting 2*MAX_DEPTH for MAX_SHORTEST_PATH: " +
                        "actual value is slightly lower, but slow to calculate. " +
                        "Calculate and replace value in ini-file if needed " +
                        "(for more exact Path relatedness measure).");
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(iniFile), "UTF-8"));
                MAX_DEPTH = Statistics.getMaxDepth(gnet);
                MAX_SHORTEST_PATH = Statistics.getMaxShortestPath(gnet);
                //MAX_SHORTEST_PATH = 2*MAX_DEPTH;
                writer.write("VERSION 7.0\n");
                writer.write("MAX_DEPTH "+MAX_DEPTH+"\n");
                writer.write("MAX_SHORTEST_PATH "+MAX_SHORTEST_PATH+"\n");
                writer.close();
            }
        }
        catch (IOException e) {
            System.out.println("Problem reading ini file: "+e.getMessage());
            System.exit(0);
        }
        catch (NoSuchElementException e) {
            System.out.println("Your .ini file seems incomplete. Please delete " +
                    "or complete it.");
            System.exit(0);
        }
    }

    /**
     * A very simple relatedness measure. <br>
     * Calculates relatedness as a function of the distance between two nodes
     * and the longest possible 'shortest path' between any two nodes: <br>
     * rel(s1,s2) = (MAX_SHORTEST_PATH - distance(s2,s2)) / MAX_SHORTEST_PATH .<br>
     * Can only compare words of same class; returns -1 otherwise.<br>
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @return a value 0 <= x <= 1, where 1 is identity and 0 is unrelated. <br>
     *         Min = 0/40 = 0 (distance 40 = currently max. 'shortest' path)<br>
     *         Max = 40/40 = 1 (identity) <br>
     */
    public RelatednessResult path(Synset s1, Synset s2) {
        double result = -1;
        if (null==s1||null==s2||!sameCat(s1,s2)) {
            return new RelatednessResult("path", result, null);
        }
        Path p1 = new Path(s1);
        Path p2 = new Path(s2);
        int distance = p1.getDistance(p2);
        if (distance == MAX_SHORTEST_PATH) result = 0;
        else result = (MAX_SHORTEST_PATH-distance)*1.0/(MAX_SHORTEST_PATH*1.0);
        return new RelatednessResult("path", result, null);
    }

    /**
     * Relatedness/Similarity according to Wu and Palmer, 1994: "Verb Semantics
     * and Lexical Selection"<br>
     *<p>
     *  ConSim(C1, C2) =   (2*N3) / (N1+N2+2*N3)<br>
     *<p>
     *  C1, C2: two synsets<br>
     *  C3: their least common subsumer/'superconcept' (LCS)<br>
     *  N1 = path length C1,C3<br>
     *  N2 = path length C2,C3<br>
     *  N3 = depth of C3<br>
     *<p>
     *  Can only compare words of same class; returns -1 otherwise.<br>
     *
     * @param c1 first synset to be compared
     * @param c2 second synset to be compared
     * @return a value 0 <= x <= 1, where 1 is identity and 0 is unrelated. <br>
     *         Min = 2*0/(20+20+2*0) = 0 (leaf nodes with LCS=root)<br>
     *         Max = 2*20/(0+0+2*20) = 1 (for leaf node identity)<br>
     */
    public RelatednessResult wuAndPalmer(Synset c1, Synset c2) {
        double result = -1;
        if (null==c1||null==c2||!sameCat(c1,c2)) {
            return new RelatednessResult("wup", result, null);
        }
        Path p1 = new Path(c1);
        Path p2 = new Path(c2);
        ArrayList<PathNode> lcs = p1.getLeastCommonSubsumer(p2); //get first of possibly several lcs
        int n3 = 0; //depth of lcs
        Path p3 = new Path(p1.getLeastCommonSubsumer(p2).get(0).synset); //need to init
        int distance = 0;
        //find lcs with greatest depth: 
        for (PathNode node: lcs) {
            Path nodePath = new Path (node.synset);
            if (nodePath.getDepth() > n3) {
                p3 = nodePath;
                n3 = nodePath.getDepth();
                distance = node.index;//N1+N2: path from c1 to c2 through the lcs
            }                         // edge-counting
        }
        if (n3 == 0) result = 0;
        else result = (2.0*n3/(distance+2*n3));
        return new RelatednessResult("wup", result, null);
    }


    /**
     * Relatedness according to Leacock&Chodorow, 1998: "Combining Local Context
     * and WordNet Relatedness for Word Sense Identification".<br>
     *<p>
     * <code>rel(a,b) = max [-log(N_p/2D)]</code> <br>
     * max is only relevant if no unique root node exists, thus here:
     * <code>rel(a,b) = -log(N_p/2D)</code><br>
     *<p>
     * N_p = path length from a to b<br>
     * D = max depth of taxonomy (for xml 6.0: maxDepth = 20, edge counting)<br>
     * In this implementation, 1 is added to numerator and denominator to avoid
     * -log(0) = infinity (for identity).
     *<p>
     * Can only compare words of same class; returns -1 otherwise.<br>
     *
     * @param a first synset to be compared
     * @param b second synset to be compared
     * @return a value 0 <= x < 1, where larger values indicate greater relatedness.<br>
     *         Min = -log((40+1)/(2*20+1)) = 0 (for maximally distant leaf nodes)<br>
     *         Max = -log((0+1)/(2*20+1)) =~ 3.71 (for leaf node identity)<br>
     */
    public RelatednessResult leacockAndChodorow(Synset a, Synset b) {
        double result = -1;
        if (null==a||null==b||!sameCat(a,b)) {
            return new RelatednessResult("lch", result, null);
        }
        Path p1 = new Path(a);
        Path p2 = new Path(b);
        int np = p1.getDistance(p2);

        //adding 1 to both numerator and denominator to avoid -log(0) = infinity.
        result = (-Math.log((np*1.0 +1)/(2.0*MAX_DEPTH +1)));
        return new RelatednessResult("lch", result, null);
    }


    /**
     * Relatedness according to Resnik 1995: "Using Information Content to
     * Evaluate Semantic Relatedness in a Taxonomy".<br>
     *<p>
     * rel(c1,c2) = max(c in S(c1,c2)) [-log(p(c))] = IC(c)<br>
     *<p>
     * where S(c1,c2) is the set of concepts that subsume both c1 and c2.<br>
     * -> in short: max(lcs)[-log(p(lcs))] = -log(freq(lcs)/rootFreq) . <br>
     * If there are several LCS (least common subsumers), take the
     * 'most informative' one.<br>
     *<p>
     * Note that with Resnik's measure, it is possible for a synset to be 'more
     * related' to a different synset with a larger IC (information content)
     * than to itself. <br>
     * As this measure uses the LCS, it must be counted as somewhat path-based
     * and as such, it also must not be used on synsets of different categories.
     * Returns -1 in that case. <br>
     *
     * @param c1 first concept (synset) to be compared
     * @param c2 second synset to be compared
     * @param freqs HashMap<String,Integer> holding the frequencies of all synsets
     * @return a value 0 <= x < 18.75,
     *         where larger values indicate greater relatedness. <br>
     *         Min = -log(1) = -0.0 = -log(freq(root)/freq(root)) = ic(root)<br>
     *         Max = -log(1/freq(root)) =~ 18.748 = ic(least frequent, i.e. most informative)<br>
     */
    public RelatednessResult resnik(Synset c1, Synset c2,
            HashMap<String,Integer> freqs) {
        double result = -1;
        double rootFreq = freqs.get(ROOT)*1.0; //non-cumulative total freqs
        Object[] args = {rootFreq};
        if (null==c1||null==c2||!sameCat(c1,c2)) {
            return new RelatednessResult("res", result, args);
        }
        Path p1 = new Path(c1);
        Path p2 = new Path(c2);
        ArrayList<PathNode> lcs = p1.getLeastCommonSubsumer(p2);
        long freq = (long)0;
        //find lcs with greatest frequency:
        for (int i=0; i<lcs.size(); i++) {
            long thisFreq = freqs.get(lcs.get(i).synset.getId()+"");
            if (thisFreq>freq) freq = thisFreq;
        }
        result = (-Math.log(freq*1.0/rootFreq));  //both freqs are at least 1
        return new RelatednessResult("res", result, args);
    }

    /**
     * Relatedness according to Lin 1998: "An Information-Theoretic Definition of
     * Relatedness"<br>
     *<p>
     * <code>rel(x1,x2) = 2*log P(C0)/(log P(C1) + log P(C2))</code><br>
     *<p>
     * where x1 and x2 are members of the classes C1 and C2 and C0 is the most
     * specific class that subsumes both C1 and C2.<br>
     * Since <code>-log(p(s)) = ic(s)</code> and the negative signs cancel out,<br>
     * <code>rel(s1,s2) = 2*ic(lcs)/(ic(s1) + ic(s2))</code><br>
     *<p>
     * As this measure uses the LCS, it must be counted as somewhat path-based
     * and as such, it also must not be used on synsets of different categories.
     * Returns -1 in that case.<br>
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @param freqs HashMap<String,Integer> holding the frequencies of all synsets
     * @return a value 0 <= x <= 1, where 1 is identity and 0 is unrelated. <br>
     *         Min = 0 if lcs is root (ic(root) = 0)<br>
     *         Max = 1 (identity; otherwise, ic(lcs) will be less specific = smaller) <br>
     */
    public RelatednessResult lin(Synset s1, Synset s2,
            HashMap<String,Integer> freqs) {
        double result = -1;
        if (null==s1||null==s2||!sameCat(s1,s2)) {
            return new RelatednessResult("lin", result, null);
        }
        Path p1 = new Path(s1);
        Path p2 = new Path(s2);
        ArrayList<PathNode> lcs = p1.getLeastCommonSubsumer(p2);
        long freqLCS = (long)0;
        //find lcs with greatest frequency
        for (int i=0; i<lcs.size(); i++) { 
            long thisFreq = freqs.get(lcs.get(i).synset.getId()+"");
            if (thisFreq>freqLCS) freqLCS = thisFreq;
        }
        double rootFreq = freqs.get(ROOT)*1.0; //non-cumulative total freqs
        double icLcs = Math.log(freqLCS*1.0/rootFreq);
        if (((Double)icLcs).compareTo(0.0)==0) 
            return new RelatednessResult("lin", 0, null);
        double icS1 = Math.log(freqs.get(s1.getId()+"")*1.0/rootFreq);
        double icS2 = Math.log(freqs.get(s2.getId()+"")*1.0/rootFreq);
        result = 2*icLcs/(icS1+icS2); //left off the minus: cancels out
        return new RelatednessResult("lin", result, null);
    }


    /**
     * Relatedness according to Jiang and Conrath 1997: "Semantic Relatedness
     * Based on Corpus Statistics and Lexical Taxonomy"<br>
     *<p>
     * <code>rel(c1,c2) = max_dist - (IC(c1)+IC(c2)−2*IC(lcs))</code><br>
     *<p>
     * where c1 and c2 are synsets.<br>
     * The distance measure presented in the paper is turned into a relatedness
     * measure simply by substracting it from the maximum possible 'distance'
     * (2*max_IC), see Statistics.getMaxJcnValue).
     *<p>
     * As this measure uses the LCS, it must be counted as somewhat path-based
     * and as such, it also must not be used on synsets of different categories.
     * Returns -1 in that case.<br>
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @param freqs HashMap<String,Integer> holding the frequencies of all synsets
     * @return a value 0 <= x <= 37.51, where 37.51 is identity and 0 is unrelated. <br>
     *          Min = 0 =  max_dist - (2*maxIC)-2*0.0)
     *                (for maximally specific, maximally distant leaf nodes)<br>
     *          Max = max_dist - (0+0-2*0.0) =~ 37.51 (identity)<br>
     */
    public RelatednessResult jiangAndConrath(Synset s1, Synset s2,
            HashMap<String,Integer> freqs) {
        double result = -1;
        if (null==s1||null==s2||!sameCat(s1,s2)) {
            return new RelatednessResult("jcn", result, null);
        }
        double rootFreq = freqs.get(ROOT)*1.0; //non-cumulative total freqs
        Object[] args = {freqs};
        
        Path p1 = new Path(s1);
        Path p2 = new Path(s2);
        ArrayList<PathNode> lcs = p1.getLeastCommonSubsumer(p2);
        long freqLCS = (long)0;
        //find lcs with greatest frequency
        for (int i=0; i<lcs.size(); i++) { 
            long thisFreq = freqs.get(lcs.get(i).synset.getId()+"");
            if (thisFreq>freqLCS) freqLCS = thisFreq;
        }
        double icLcs = -Math.log(freqLCS*1.0/rootFreq);
        double icS1 = -Math.log(freqs.get(s1.getId()+"")*1.0/rootFreq);
        double icS2 = -Math.log(freqs.get(s2.getId()+"")*1.0/rootFreq);
        result = Statistics.getMaxJcnValue(freqs) - (icS1+icS2-2*icLcs);
        return new RelatednessResult("jcn", result, args);
    }


    /**
     * Relatedness according to Hirst and St-Onge 1998: "Lexical chains as
     * representations of context for the detection and correction of
     * malapropisms". <br>
     *<p>
     * <code>rel(s1,s2) = 15</code><br> for strong Relations<br>
     * <code>rel(s1,s2) = C-pathLength-k*directionChanges</code><br> for
     * medium-strong Relations<br>
     * where by default, C=10 and k=1.<br>
     *<p>
     * 'Strong relation' = synset identity, direct horizontal link or one word
     * (orthForm of one Synset, presumably a compound) containing the other. <br>
     * 'medium-strong relation' = a path with length 5 or less between the two
     * synsets, using all types of relations, but only according to specified
     * patterns (call an upwards relation 'u', downwards 'd', horizontal 'h',
     * then the allowed paths are u+, u+d+, u+h+, u+h+d+, d+, d+h+, h+d+, h+).
     * For more information see the paper by Hirst and St-Onge. 
     *<p>
     *
     * May be used on Synsets of different categories. <br>
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @return a value 0 <= x <= 15, where 15 is strongly related and 0 is
     *         unrelated. <br>
     *          Min = 0 for no relation<br>
     *          Max = 15 for strong Relation<br>
     */
    public RelatednessResult hirstAndStOnge(Synset s1, Synset s2) {
        int constantC = 10;
        int constantK = 1;
        return hirstAndStOnge(s1, s2, 10, 1);
    }


    /**
     * Relatedness according to Hirst and St-Onge 1995: "Lexical chains as
     * representations of context for the detection and correction of
     * malapropisms". <br>
     *<p>
     * <code>rel(s1,s2) = 15</code><br> for strong Relations<br>
     * <code>rel(s1,s2) = C-pathLength-k*directionChanges</code><br> for
     * medium-strong Relations<br>
     * where pathLength is between 0 and 5, direction Changes between 0 and 2
     * and C and k are variables such that k,c>=0 and (2*k+5)<C<15; .<br>
     *<p>
     * 'Strong relation' = synset identity, direct horizontal link or one word
     * (orthForm of one Synset, presumably a compound) containing the other. <br>
     * 'medium-strong relation' = a path with length 5 or less between the two
     * synsets, using all types of relations, but only according to specified
     * patterns (call an upwards relation 'u', downwards 'd', horizontal 'h',
     * then the allowed paths are u+, u+d+, u+h+, u+h+d+, d+, d+h+, h+d+, h+,
     * h+u+).
     * For more information see the paper by Hirst and St-Onge.<br>
     * Note: As GermaNet relations always go both ways, this method only looks
     * for paths in one direction.
     * Thus, the reverse of d+h+, namely h+u+, has been
     * added although the original paper does not allow for it. The reverses of
     * all other paths are already included in the 'allowed' list. 
     *<p>
     *
     * May be used on Synsets of different categories. <br>
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @param c maximum value for medium strength relations
     * @param k variable to scale number of direction changes
     * @return a value 0 <= x <= 15, where 15 is strongly related and 0 is
     *         unrelated. <br>
     *          Min = 0 for no relation<br>
     *          Max = 15 for strong Relation<br>
     */
    public RelatednessResult hirstAndStOnge(Synset s1, Synset s2, double c,
            double k) {
        double result = -1;
        if (null==s1||null==s2) {
            return new RelatednessResult("hso", result, null);
        }
        if (c<=(2*k+5) || c>15 || c<0 || k<0) {
            System.out.println("Invalid C and k!");
            System.exit(0);
        }

        //extra-strong relation: applies to words only, not to synsets

        //strong relation: synset identity, direct horizontal link or contained
        int strongRelation = 15;

        //synset identity:
        if (s1.getId() == s2.getId()) {
            return new RelatednessResult("hso", strongRelation, null);
        }
        //one word contained in other, presumably compound:
        List<String> orthForms1 = s1.getAllOrthForms();
        List<String> orthForms2 = s2.getAllOrthForms();
        for (String form1: orthForms1) {
            for (String form2: orthForms2) {
                if (form1.contains(form2) || form2.contains(form1)) {
                    return new RelatednessResult("hso", strongRelation, null);
                }
            }
        }
        //horizontal link: synonym, antonym, participle, pertainym; related to
        List<LexUnit> lexUnits1 = s1.getLexUnits();
        List<LexUnit> lexUnits2 = s2.getLexUnits();
        for (LexUnit lu1: lexUnits1) {
            for (LexUnit lu2: lexUnits2) {
                if (lu1.getRelatedLexUnits().contains(lu2)) {
                   // as all GN relations go both ways, this is unnecessary: 
                   // || lu2.getRelatedLexUnits().contains(lu1)) {
                    return new RelatednessResult("hso", strongRelation, null);
                }
            }
        }
        //one more horizontal link: ConRel.isRelatedTo
        List<Synset> relatedSynsets1 = s1.getRelatedSynsets(
                ConRel.is_related_to);
        //List<Synset> relatedSynsets2 = s2.getRelatedSynsets(
        //        ConRel.is_related_to);
        if (relatedSynsets1.contains(s2) ) {
                // again, unnecessary because of symmentry (relations go both ways)
                //|| relatedSynsets2.contains(s1)) {
            return new RelatednessResult("hso", strongRelation, null);
        }

        //medium-strong relation: path.
        //Get all relations of all three types, but only on allowed paths.

        String path = "";
        double score = 0;
        //start from the synset that has fewer relations (not counting ConRels):
        int numRelationsS1 = s1.getRelatedSynsets().size();
        int numRelationsS2 = s2.getRelatedSynsets().size();
        Synset fromSynset = s1;
        Synset toSynset = s2;
        List<LexUnit> lexUnits = lexUnits1;
        if (numRelationsS1 > numRelationsS2) {
            fromSynset = s2;
            toSynset = s1;
            lexUnits = lexUnits2;
        }
        HashSet<Synset> visited = new HashSet<Synset>();
        visited.add(fromSynset);  
        result = HsoMethods.findBestPath(fromSynset, toSynset, lexUnits, path,
                score, c, k, visited);
        //once again, unnecessary: paths should be identical from either side,
        //so a check from one side is sufficient:
        /*double score2 = HsoMethods.findBestPath(s2, s1, lexUnits2, path,
                result, c, k);
        if (Double.compare(score2,result)>0) {
            result = score2;
        } */
        return new RelatednessResult("hso", result, null); //return 0
    }


    /**
     * Extended Lesk relatedness (original Lesk 1987: "Automatic sense
     * disambiguation using machine readable dictionaries: How to tell a pine
     * cone from a ice cream cone.") using lexical field (or 'pseudo-glosses',
     * losely following Gurevych 2005: "Using the Structure of a Conceptual Network
     * in Computing Semantic Relatedness"). <br>
     * This method returns the value computed with the default values using <br>
     * - all orthForms of each synset  <br>
     * - size = 4 (path length for including related synsets) <br>
     * - limit = 2 (distance from root inside which synsets are excluded (abstract)) <br>
     * - only using hypernyms (opposite to using all available relations except hyponyms) <br>
     * - not including existing GermaNet glosses in lexical field <br>
     * - using snowball stemmer for German <br>
     *
     * May be used on Synsets of different categories.
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @return a value x >= 0, where 0 is no overlap and greater values indicate
     *         greater relatedness
     *          Min = 0
     *          Max = n.a.
     */
    public RelatednessResult lesk(Synset s1, Synset s2, GermaNet gnet) {
        double result = -1;
        if (null==s1||null==s2||null==gnet) {
            return new RelatednessResult("lesk", result, null);
        }
        //try and make this more efficient: initiate stemmer only once (input?)
        try {
            Class stemClass = Class.forName("org.tartarus.snowball.ext.germanStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
            return lesk(s1,s2,gnet,stemmer,4,2,false,true,false,false);
        }
        catch (Exception e) {
            System.out.println("Problem initializing stemmer for lesk: "+
                    e.getMessage());
            System.exit(0);
        }
        return null; //something went wrong...
    }

    /**
     * Extended Lesk relatedness (original Lesk 1987: "Automatic sense
     * disambiguation using machine readable dictionaries: How to tell a pine
     * cone from a ice cream cone.") using lexical field (or 'pseudo-glosses',
     * losely following Gurevych 2005: "Using the Structure of a Conceptual 
     * Network in Computing Semantic Relatedness"). <br>
     *
     * May be used on Synsets of different categories. <br>
     *
     * @param s1 first synset to be compared
     * @param s2 second synset to be compared
     * @param oneOrthForm if set to true, only one orthForm of each synset will be used;
     *        if false, all forms will be included in the lexical field
     * @param size path length: how many related synsets will be included; if
     *        size=0 and includeGloss=true, then Lesk is applied in its original
     *        definition, i.e., it compares glosses to compute the similarity
     * @param limit distance from root: how many synset layers will be excluded
     *        for being too abstract
     * @param hypernymsOnly if true, use only hypernymy relation; otherwise, use
     *        all types of relations except hyponymy
     * @param includeGermanetGloss if true, GermaNet's own glosses will
     *        be included in the lexical field where they exist
     * @param includeWiktionaryGlosses if true, optionally loaded Wiktionary glosses
     *        will be included in the lexical field where they exist
     * @return a value x >= 0,, where 0 is no overlap and greater values indicate
     *         greater relatedness.
     *         Min = 0
     *         Max = n.a.
     */ 
    public RelatednessResult lesk(Synset s1, Synset s2, GermaNet gnet,
            SnowballStemmer stemmer, int size, int limit, boolean oneOrthForm,
            boolean hypernymsOnly, boolean includeGermanetGloss, boolean includeWiktionaryGlosses) {
        // TODO: size=0 und includeGloss=false abfangen, denn dann wäre das LexicalField ja leer;
        // die Frage ist: soll dann einfach eine Info ausgegeben werden oder soll dann bspw. size
        // einfach auf 1 gesetzt werden o.ä.?
        //--> not true: die orthForms des Synsets müssten noch drin sein, oder?

        if (null==s1||null==s2||null==gnet) { //stemmer allowed to be null
            return new RelatednessResult("lesk", -1, null);
        }
        double overlap = 0;

        LexicalField field1 = new LexicalField(s1,gnet, stemmer, size, limit,
                oneOrthForm, hypernymsOnly, includeGermanetGloss, includeWiktionaryGlosses);
        LexicalField field2 = new LexicalField(s2,gnet, stemmer, size, limit,
                oneOrthForm, hypernymsOnly, includeGermanetGloss, includeWiktionaryGlosses);

        HashSet<String> gloss1 = field1.getField();
        HashSet<String> gloss2 = field2.getField();
        //compute 'overlap' for the two constructed glosses:
        Iterator it1 = gloss1.iterator();
        while (it1.hasNext()) {
            String word = (String)it1.next();
            //System.out.println(word);
            if (gloss2.contains(word)) {
                overlap++;
            }
        }

        /*System.out.println("Word 2:");
        Iterator it2 = pseudoGloss2.iterator();
        while (it2.hasNext()) {
            System.out.println((String)it2.next());
        }*/
        //Object[] args = {oneOrthForm,size,limit,hypernymsOnly,includeGloss};

        int maxFieldSize = gloss1.size();
        if (gloss2.size() > maxFieldSize) {
            maxFieldSize = gloss2.size();
        }
        Object[] args = {maxFieldSize};
        return new RelatednessResult("lesk", overlap, args);
    }

    /* checks two synsets have the same category;
     * if not, path based relatedness measures cannot return useful results */
    private boolean sameCat(Synset s1, Synset s2) {
        if (s1.getWordCategory().equals(s2.getWordCategory())) {
            return true;
        }
        
        System.out.println("measure using paths: " +
                "Unable to compare words of different categories!");
        return false;
    }
    
/*  // object to store information while trying different paths for medium
    // strong relation
    private class HsoPathNode {
        public Synset current = null;
        public String path = "";

        public HsoPathNode(Synset s, String p) {
            current = s;
            path = p;
        }
    } */



    /* Sample results for relatedness Katze-Katze, Katze-Hund, Katze-Auto
       (edge-counting, and with improved frequency calculation (API 7.0)):
     * 1. Path measure: a=1.00, b=0.92, c=0.67
     * 2. Wu and Palmer's measure: a=1.00, b=0.84, c=0.24
     * 3. Leacock and Chodorow's measure: a=3.71, b=2.33, c=1.07
     * 4. Resnik's measure: a=10.76, b=8.59, c=1.75
     * 5. Lin's measure: a=1.00, b=0.86, c=0.19
     * 6. Jiang and Conrath's measure: a=37.51, b=34.82, c=22.73
     * 7. Lesk's measure (adapted): a=16.00, b=10.00, c=0.00
     */

    
    /* Sample results for OLD relatedness Katze-Katze, Katze-Hund, Katze-Auto
       (node-counting):
     * 1. path measure: a=1.00, b=0.25, c=0.07
     * 2. wuAndPalmer measure: a=0.90, b=0.78, c=0.29
     * 3. leacockAndChodorow measure: a=3.74, b=2.35, c=1.10
     * 4. resnik measure: a=10.75, b=8.58, c=1.75
     * 5. lin measure: a=1.00, b=0.86, c=0.19
     * 6. jiangAndConrath measure: a=37.50, b=34.81, c=22.74  */

    /**
     * Reads a list of word pairs and returns a list of their relatedness. 
     * For lesk and hirstStOnge, the default methods will be used.
     * Where more than one synset is found for a word, all combinations are tried
     * and the average of relatedness values for all pairs of synsets is used.
     * @param inFile csv file 
     * @param outFile file to print results to 
     * @param separator the char(s) used to separate words in the input file;
     *        will also be used on output file
     * @param encoding String indicating the in- and output encoding
     *        (UTF8, Cp1252, ISO8859_1, ...)
     * @param methodName name of relatedness measure to use
     * @param gnet an instance of GermaNet
     * @param frequencies for methods resnik, lin, jiangAndConrath; set to
     *        null for all others.
     * @param normalized false: original values are used;
     *                   true: all values mapped to (0..4)
     * @param cat All word categories occuring on the list (n, nv, v, va, nva...
     *        n=noun, v=verb, a=adjective)
     */
    public void runList(String inFile, String outFile, String separator,
            String encoding, String methodName, GermaNet gnet,
            HashMap<String,Integer> frequencies, boolean normalized, String cat) {
        try {
            Scanner reader = new Scanner(new File(inFile), encoding);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outFile)), encoding));
            Relatedness rel = new Relatedness(gnet);

            int methodNumber = 0;
            if (methodName.equalsIgnoreCase("path")) {
                methodNumber = 0;
            } else if (methodName.equalsIgnoreCase("wuAndPalmer")) {
                methodNumber = 1;
            } else if (methodName.equalsIgnoreCase("leacockAndChodorow")) {
                methodNumber = 2;
            } else if (methodName.equalsIgnoreCase("resnik")) {
                methodNumber = 3;
            } else if (methodName.equalsIgnoreCase("lin")) {
                methodNumber = 4;
            } else if (methodName.equalsIgnoreCase("jiangAndConrath")) {
                methodNumber = 5;
            } else if (methodName.equalsIgnoreCase("hirstStOnge")) {
                methodNumber = 6;
            } else if (methodName.equalsIgnoreCase("lesk")) {
                methodNumber = 7;
            } else {
                System.out.println("Method name not recognized.");
                System.exit(0);
            }

            while(reader.hasNextLine()) {
                String[] line = reader.nextLine().split(separator);
                if (line.length < 2) {
                    System.out.println("Error reading file: line too short!");
                    System.exit(0);
                }
                List<Synset> synsets1;
                List<Synset> synsets2;
                String word1 = line[0].trim();
                String word2 = line[1].trim();
                if (cat.length()==3) { //all 3 pos
                    synsets1 = gnet.getSynsets(word1);
                    synsets2 = gnet.getSynsets(word2);
                } else { //one or two
                    if (cat.contains("n")) {
                        synsets1 = gnet.getSynsets(word1, WordCategory.nomen);
                        synsets2 = gnet.getSynsets(word2, WordCategory.nomen);
                        if (cat.contains("v")) { //second cat
                            synsets1.addAll(gnet.getSynsets(word1, WordCategory.verben));
                            synsets2.addAll(gnet.getSynsets(word2, WordCategory.verben));
                        }
                        else if (cat.contains("a")) { //second cat
                            synsets1.addAll(gnet.getSynsets(word1, WordCategory.adj));
                            synsets2.addAll(gnet.getSynsets(word2, WordCategory.adj));
                        }
                    } else if (cat.contains("v")) { //but no n
                        synsets1 = gnet.getSynsets(word1, WordCategory.verben);
                        synsets2 = gnet.getSynsets(word2, WordCategory.verben);
                        if (cat.contains("a")) { //second cat
                            synsets1.addAll(gnet.getSynsets(word1, WordCategory.adj));
                            synsets2.addAll(gnet.getSynsets(word2, WordCategory.adj));
                        }
                    } else { //a, but no v or n
                        synsets1 = gnet.getSynsets(word1, WordCategory.adj);
                        synsets2 = gnet.getSynsets(word2, WordCategory.adj);
                    }
                }
                /*System.out.println("Synsets 1: ");
                for (Synset s: synsets1) {
                    System.out.println(s.getId()+" "+s.getWordCategory());
                }
                System.out.println("Synsets 2: ");
                for (Synset s: synsets2) {
                    System.out.println(s.getId()+" "+s.getWordCategory());
                }*/
                double collectedRelatedness = 0;
                boolean containsPositives = false; 
                double result = 0;
                switch (methodNumber) {
                    case 0:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.path(s1, s2).getNormalizedResult();
                                } else {
                                    result = rel.path(s1, s2).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 1:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.wuAndPalmer(s1,
                                            s2).getNormalizedResult();
                                } else {
                                    result = rel.wuAndPalmer(s1, s2).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 2:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.leacockAndChodorow(s1,
                                            s2).getNormalizedResult();
                                } else {
                                    result = rel.leacockAndChodorow(s1,
                                            s2).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 3:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.resnik(s1, s2,
                                            frequencies).getNormalizedResult();
                                } else {
                                    result = rel.resnik(s1, s2,
                                            frequencies).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 4:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.lin(s1, s2,
                                            frequencies).getNormalizedResult();
                                } else {
                                    result = rel.lin(s1, s2, frequencies).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 5:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.jiangAndConrath(s1, s2,
                                            frequencies).getNormalizedResult();
                                } else {
                                    result = rel.jiangAndConrath(s1, s2,
                                            frequencies).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 6:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.hirstAndStOnge(s1,
                                            s2).getNormalizedResult();
                                } else {
                                    result = rel.hirstAndStOnge(s1, s2).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    case 7:
                        for (Synset s1: synsets1) {
                            for (Synset s2: synsets2) {
                                if (normalized) {
                                    result = rel.lesk(s1, s2,
                                            gnet).getNormalizedResult();
                                } else {
                                    result = rel.lesk(s1, s2, gnet).getResult();
                                }
                                //do not add negatives
                                if (result >= 0) {
                                    collectedRelatedness += result;
                                    containsPositives = true;
                                }
                            }
                        }
                        break;
                    default:
                        break; //should never happen
                }
                //if there are *only* negatives, result is negative
                if (containsPositives == false) {
                    collectedRelatedness = -1;
                }
                double average = -1; //for negatives
                //unknown word or method not accepting different categories
                if (synsets1.size() == 0 || synsets2.size() == 0) { 
                    average = -1; //no change
                } else if (collectedRelatedness == 0) { //unrelated
                    average = 0;
                } else if (collectedRelatedness > 0) { //somehow related
                    average =
                       collectedRelatedness*4.0/(synsets1.size()*synsets2.size());
                    /*System.out.println("Collected = "+collectedRelatedness);
                    System.out.println("Synset sizes: "+synsets1.size()+" + "+
                            synsets2.size());
                    System.out.println("Average = "+ average);*/
                } // else if collectedRelatedness < 0, i.e. no valid result,
                  // keep the value as -1

                writer.write(line[0]+separator+line[1]+separator+average+"\n");

                //System.out.println(line[0]+" "+line[1]+" "+average);
            }
            reader.close();
            writer.close();
            System.out.println("Done.");
        }
        catch (IOException e) {
            System.out.println("Problem with file i/o: "+e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Sorts an input csv file by the numeric value in the indicated column;
     * inteded for sorting of word lists by reledness of the word pairs.
     * Deletes line "Word1 [separator] Word2 ...", i.e. header file, if present
     * Does not allow for double entries (same pair of words twice). 
     * @param inFile File to be sorted
     * @param separator the char(s) used to separate words in the input file;
     *        will also be used on output file
     * @param encoding String indicating the in- and output encoding
     *        (UTF8, Cp1252, ISO8859_1, ...)
     * @param index position of the numeric value in each line (0,1,2,...)
     */
    public void sortList(String inFile, String separator, String encoding,
            int index) {
        try {
            Scanner reader = new Scanner(new File(inFile), encoding);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                  new FileOutputStream(new File(inFile+"_sorted")), encoding));
            HashMap<Double,ArrayList<String>> map =
                    new HashMap<Double,ArrayList<String>>();
            //write to map, relatedness as key
            while (reader.hasNextLine()) {
                String[] line = reader.nextLine().split(separator);
                if (line[0].trim().equalsIgnoreCase("Word1") &&
                    line[1].trim().equalsIgnoreCase("Word2")   ) {
                    continue;
                } 
                Double key = Double.parseDouble(line[index].trim());
                String value = "";
                for (int i=0; i<index; i++) {
                    value += line[i].trim()+separator; //key ends with separator
                }
                ArrayList<String> val;
                if (map.containsKey(key)) {
                    val = map.get(key);
                } else {
                    val = new ArrayList<String>();  
                }
                val.add(value);
                map.put(key,val);
            }
            //sort
            Double[] keyArray = new Double[map.keySet().size()];
            keyArray = map.keySet().toArray(keyArray);
            Arrays.sort(keyArray);
            for (Double d: keyArray) {
                ArrayList<String> values = map.get(d);
                for (String v: values) {
                    writer.write(v+d+"\n");
                }
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println("IO Error: "+e.getMessage());
            System.exit(0);
        }

    }


}


