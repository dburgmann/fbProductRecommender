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
import java.util.Map.Entry;

/**
 * Demo class demonstrating some features of the Relatedness package, namely of
 * the Path and Relatedness classes.
 * 
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class Demo {

    /**
     * Shows functionality of the Path and Relatedness classes.
     * The input arguments need to be: <br>
     * - path1 to GermaNet xml directory <br>
     * - path1 to directory with frequency list(synset1); POS of each list
     *   needs to be included in name (nn,vv,adj)<br>
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("USAGE: java Main outputFile, pathToGermaNet, "+
                               "frequencyDirectory");
            System.exit(0);
        }
        //check if all three input Strings are valid file/directory names:
        for (int i=0; i<args.length; i++) {
            File f1 = new File(args[i]);
            //if no directory name, using current -> no check necessary
            if (args[i].contains("/")) {
                File path = new File (f1.getPath().substring(
                    0,f1.getPath().lastIndexOf("/"))); //get path1 to input file
                if (!path.isDirectory()) {
                    System.out.println("Path "+f1.getAbsolutePath()+" is invalid.");
                    System.exit(0);
                }
            }
        }

        try {
            GermaNet gnet = new GermaNet(args[1], true); //ignore case

            System.out.print("\nGermaNet version (this is written for 7.0): ");
            String germaNet = args[1].substring(args[1].lastIndexOf("/")+1,
                args[1].length());
            System.out.println(germaNet);
            System.out.println("Output file: "+args[0]);
            System.out.println("Location of frequency files: "+args[2]+"\n");

            Scanner keyboard = new Scanner(System.in);
            String word1, word2;
            List<Synset> synsets1 = null;
            List<Synset> synsets2 = null;
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(args[0])), "UTF-8"));

            System.out.println("1. Demonstrating some uses of Path class. " +
                               "\n\nEnter two words. The first sense contained " +
                               "in GermaNet for each word will be used.");
            word1 = keyboard.next();
            word2 = keyboard.next();

            synsets1 = gnet.getSynsets(word1);
            if (synsets1.size() == 0) {
                System.out.println(word1 + " not found in GermaNet");
                System.exit(0);
            }
            Synset synset1 = synsets1.get(0);
            //System.out.println("First sense/synset contained in GermaNet for word "+
            //        word1+":\n"+synset1);

            synsets2 = gnet.getSynsets(word2);
            if (synsets2.size() == 0) {
                System.out.println(word2 + " not found in GermaNet");
                System.exit(0);
            }
            Synset synset2 = synsets2.get(0);
            //System.out.println("First sense/synset contained in GermaNet for word "+
            //        word2+":\n"+synset2);

            System.out.println("Getting paths to root for first sense of each "+
                               "word (i.e. for synsets "+synset1.getId()+" and "
                               + synset2.getId()+")");
            Path path1 = new Path(synset1);
            Path path2 = new Path(synset2);

            writer.write("Shortest and other paths from input words to root:\n\n");
            writer.write(word1);
            writer.write("\nDepth: "+path1.getDepth());
            writer.write("\n\nShortest Path(s) to root: "+
                    pprint(path1.getShortestPaths()));
            writer.write("\nAll Paths (including shortest) to root: "+
                    pprint(path1.getAllPaths()));

            writer.write("\n\n"+word2);
            writer.write("\nDepth: "+path2.getDepth());
            writer.write("\n\nShortest Path(s) to root: "+
                    pprint(path2.getShortestPaths()));
            writer.write("\nAll Paths (including shortest) to root: "+
                    pprint(path2.getAllPaths()));

            System.out.println("Get least common subsumer (lcs) and distance of "
                    +word1+" and "+word2+".");
            ArrayList<PathNode> lcs = path1.getLeastCommonSubsumer(path2); //explicit method call
            writer.write("\n\nLCS (least common subsumer) of "+word1+" and "+word2
                        +"= "+lcs+"\n"+"distance: "+path1.getDistance(path2));
            //implicit call to getLeastCommonSubsumer

            System.out.println("\n\n2. Demonstrating some uses of Frequency "+
                    "class.\n");
            /*System.out.println("Please comment out the cleanLists() method " +
                               "if not needed, as it takes some time.");

            Frequency.cleanLists(args[2]); //slow; comment out if not needed */
            Frequency.assignFrequencies(args[2], gnet);
            System.out.println("ICs (settings for information content) done.");
            HashMap<String, Double> ics = Frequency.loadIC(args[2]+"/frequencies.csv");

            System.out.println("Loaded 'information content' values. " +
                    "Printing a few frequencies to file as example.");
            writer.write("\n\n\nA few example frequency values: ");
            Set<Entry<String,Double>> entries = ics.entrySet();
            Iterator it = entries.iterator();
            for (int i=0; i<5; i++) {
                Entry entry = (Entry)it.next();
                String id = (String)entry.getKey();
                double freq = (Double)entry.getValue();
                writer.write("\nsynset "+gnet.getSynsetByID(Integer.parseInt(id))
                        +", frequency: "+freq);
            }

            System.out.println("\n\n2. Demonstrating use of Relatedness class.\n");
            Relatedness rel = new Relatedness(gnet);

            System.out.println("Calculating maximum depth of the hierarchy "+
                               "(edge counting).");
            int i = Statistics.getMaxDepth(gnet);
            writer.write("\n\nMaximum depth of this GermaNet version = "+i+"\n");
            
            System.out.println("Loading frequency table.");
            HashMap<String,Integer> frequencies =
                    Frequency.loadFreq(args[2]+"/frequencies.csv");
            System.out.println("Calculating relatedness of the input words.");
            
            writer.write("\nRelatedness of "+word1+" and "+word2+":\n");
            writer.format("\nPath measure: %.2f",
                    rel.path(synset1,synset2).getResult());
            writer.format("\nWu and Palmer's measure : %.2f",
                    rel.wuAndPalmer(synset1,synset2).getResult());
            writer.format("\nLeacock and Chodorow's measure : %.2f",
                    rel.leacockAndChodorow(synset1,synset2).getResult());
            writer.format("\nResnik's measure: %.2f",
                    rel.resnik(synset1,synset2,frequencies).getResult());
            writer.format("\nLin's measure: %.2f",
                    rel.lin(synset1,synset2,frequencies).getResult());
            writer.format("\nJiang and Conrath's measure: %.2f",
                    rel.jiangAndConrath(synset1,synset2,frequencies).getResult());
            writer.format("\nHirst and St-Onge's measure: %.2f",
                    rel.hirstAndStOnge(synset1, synset2).getResult());
            writer.format("\nLesk's measure (adapted): %.2f",
                    rel.lesk(synset1,synset2,gnet).getResult());
            
            System.out.println("Adding a more generalized comparison. ");
            writer.write("\n\nFor comparison, calculating relatedness of " +
                    "a) Katze-Katze, b) Katze-Hund and c) Katze-Auto.\n");
            Synset katze = gnet.getSynsets("Katze").get(0);
            Synset hund = gnet.getSynsets("Hund").get(0);  //0=animal,1=bad person
            Synset auto = gnet.getSynsets("Auto").get(0);

            writer.format("\n1. Path measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.path(katze,katze).getResult(),
                               rel.path(katze,hund).getResult(),
                               rel.path(katze,auto).getResult());
            writer.format("\n2. Wu and Palmer's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.wuAndPalmer(katze,katze).getResult(),
                               rel.wuAndPalmer(katze,hund).getResult(),
                               rel.wuAndPalmer(katze,auto).getResult());
            writer.format("\n3. Leacock and Chodorow's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.leacockAndChodorow(katze,katze).getResult(),
                               rel.leacockAndChodorow(katze,hund).getResult(),
                               rel.leacockAndChodorow(katze,auto).getResult());
            writer.format("\n4. Resnik's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.resnik(katze,katze,frequencies).getResult(),
                               rel.resnik(katze,hund,frequencies).getResult(),
                               rel.resnik(katze,auto,frequencies).getResult());
            writer.format("\n5. Lin's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.lin(katze,katze,frequencies).getResult(),
                               rel.lin(katze,hund,frequencies).getResult(),
                               rel.lin(katze,auto,frequencies).getResult());
            writer.format("\n6. Jiang and Conrath's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.jiangAndConrath(katze,katze,frequencies).getResult(),
                               rel.jiangAndConrath(katze,hund,frequencies).getResult(),
                               rel.jiangAndConrath(katze,auto,frequencies).getResult());
            writer.format("\n7. Hirst and St-Onge's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.hirstAndStOnge(katze,katze).getResult(),
                               rel.hirstAndStOnge(katze,hund).getResult(),
                               rel.hirstAndStOnge(katze,auto).getResult());
            writer.format("\n8. Lesk's measure (adapted): a=%.2f, b=%.2f, c=%.2f",
                               rel.lesk(katze,katze,gnet).getResult(),
                               rel.lesk(katze,hund,gnet).getResult(),
                               rel.lesk(katze,auto,gnet).getResult());

            writer.write("\n\nNow we normalize the return values of the " +
                    "relatedness measures.\n" +
                    "(Note: Identity for Resnik may be less than 1.)\n");

            writer.format("\n1. Path measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.path(katze,katze).getNormalizedResult(),
                               rel.path(katze,hund).getNormalizedResult(),
                               rel.path(katze,auto).getNormalizedResult());
            writer.format("\n2. Wu and Palmer's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.wuAndPalmer(katze,katze).getNormalizedResult(),
                               rel.wuAndPalmer(katze,hund).getNormalizedResult(),
                               rel.wuAndPalmer(katze,auto).getNormalizedResult());
            writer.format("\n3. Leacock and Chodorow's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.leacockAndChodorow(katze,katze).getNormalizedResult(),
                               rel.leacockAndChodorow(katze,hund).getNormalizedResult(),
                               rel.leacockAndChodorow(katze,auto).getNormalizedResult());
            writer.format("\n4. Resnik's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.resnik(katze,katze,frequencies).getNormalizedResult(),
                               rel.resnik(katze,hund,frequencies).getNormalizedResult(),
                               rel.resnik(katze,auto,frequencies).getNormalizedResult());
            writer.format("\n5. Lin's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.lin(katze,katze,frequencies).getNormalizedResult(),
                               rel.lin(katze,hund,frequencies).getNormalizedResult(),
                               rel.lin(katze,auto,frequencies).getNormalizedResult());
            writer.format("\n6. Jiang and Conrath's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.jiangAndConrath(katze,katze,frequencies).getNormalizedResult(),
                               rel.jiangAndConrath(katze,hund,frequencies).getNormalizedResult(),
                               rel.jiangAndConrath(katze,auto,frequencies).getNormalizedResult());
            writer.format("\n3. Hirst and St-Onge's measure: a=%.2f, b=%.2f, c=%.2f",
                               rel.hirstAndStOnge(katze,katze).getNormalizedResult(),
                               rel.hirstAndStOnge(katze,hund).getNormalizedResult(),
                               rel.hirstAndStOnge(katze,auto).getNormalizedResult());
            writer.format("\n8. Lesk's measure (adapted): a=%.2f, b=%.2f, c=%.2f",
                               rel.lesk(katze,katze,gnet).getNormalizedResult(),
                               rel.lesk(katze,hund,gnet).getNormalizedResult(),
                               rel.lesk(katze,auto,gnet).getNormalizedResult());

            writer.write("\n\nEnd of demonstration.");
            writer.close();
            System.out.println("Results (depth, shortest path(s) and complete "+
                               "set of paths; least common subsumer and distance; " +
                               "example information content and relatedness " +
                               "values are in file "+args[0]);
        }
       /* catch (FileNotFoundException e) {
            System.out.println("Could not find file. Is the path1 to your GermaNet "
                    +"xml correct?");
            System.exit(0);
        }
        catch (NullPointerException e) {
            System.out.println("NullPointerException, may be related to Germanet xml."
                    +"Is the path1 to your GermaNet xml correct? "+
                    "("+e.getMessage()+")");
            System.exit(0);
        }*/
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * A little method to produce nice path1 output.
     * @param list An ArrayList of ArrayLists of Synsets
     * @return a String containing that list in nice, easy to understand format
     */
    public static String pprint(ArrayList<ArrayList<Synset>> list) {
        String pretty = list.size()+" path(s): \n";
        for (int i=0; i<list.size(); i++) {
            pretty += "\n"+list.get(i).size()+" synsets:\n";
            for (int j=0; j<list.get(i).size(); j++) {
                pretty += " "+list.get(i).get(j)+"\n";
            }
        }
        return pretty;
    }

}