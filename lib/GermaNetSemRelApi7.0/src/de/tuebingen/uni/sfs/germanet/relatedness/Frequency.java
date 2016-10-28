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
import java.io.*;
import de.tuebingen.uni.sfs.germanet.api.*;
import java.util.Map.Entry;

/**
 * This class deals with frequency lists and information content. <br>
 *<p>
 * Richardson and Smeaton 1995: "Many polysemous words and multi-worded synsets
 * will have an exaggerated information content value". <br>
 *<p>
 * The calculation of synset frequencies here is based on word frequencies, and
 * as such is victim to this problem: 1 occurrence of a word like "Land" is
 * counted towards 6 different synsets (property, earth, province, state, ...).
 * This is slightly improved by the separate lists for each POS, but remains an
 * inherent problem all the same. <br>
 *<p>
 * Note: to avoid 0 frequencies, a default value of 1 is assigned to each synset.
 *       A word listed with frequency 1 will thus end up in a synset with a 
 *       frequency of at least 2. <br>
 * Note 2: GermaNet has multiple inheritance. In this implementation, each synset
 *         adds its frequency value exactly once to each of its (transitive) hypernyms,
 *         no matter whether that hypernym can be reached on more than 1 path. <br>
 *         As such, the root node will hold exactly the total of assigned (not
 *         cumulative) frequencies in the entire tree. As a side effect, though,
 *         a synset's cumulative frequency can be lower than the sum of the
 *         frequencies of its direct hyponyms.  <br>
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class Frequency {

    /**
     * Creates a file "frequencies.csv" with format "ID \t frequency" in the given input
     * directory with the cumulative frequencies as described by Resnik, 1995.<br>
     * <p>
     * This method expects as input a directory with one or more frequency lists
     * containing lines with the format "frequency\tword".<br>
     * The file names must include "nn" or "noun" for nouns, "vv" or "verb" for
     * verbs, "aj" or "adj" for adjectives (and "av" or "adv" for adverbs); or
     * "all", if POS suffixes are already attached to each entry.<br>
     * Also, filenames must end in "clean" (before the file ending, .text etc,
     * f.ex.  Tuepp-ADJ_clean.txt) so that cleanFile() and
     * assignFrequencies() can be used on the same directory without the 'old'
     * files being included in the latter method. <br>
     * @param inputDirectory a directory containing one or more frequency lists
     * @param gnet an instance of the class GermaNet
     */
    public static void assignFrequencies(String inputDirectory, GermaNet gnet) {
        IntHashMap wordFreqs = new IntHashMap(); //String word, Integer freq
        LongHashMap synFreqs = new LongHashMap(); //String ID, Integer freq
        LongHashMap targetFreqs; //String ID, Integer freq
        // read entries from all input lists and initialize HashMap entries
        try {
            
            //initialize all words to frequency 0 and all synsets in GN to frequency 1
            List<Synset> all = gnet.getSynsets();
            for (Synset s: all) {
                synFreqs.put(s.getId()+"", (long)1); //regular put ok: nothing to overwrite
                List<String> words = s.getAllOrthForms();
                for (String w: words) {
                    //lower-case just in case there are unexpected-case list entries
                    wordFreqs.put(//suffixing category info as ordinal to word
                            (w+s.getWordCategory().ordinal()).toLowerCase(),0);
                }
            }

            //add list frequencies to word HashMap entries
            System.out.println("\nProcessing word frequencies.");
            File directory = new File(inputDirectory);
            File[] lists = directory.listFiles();
            for (File f: lists) {
                //check which list we're dealing with
                String posSuffix = getPosSuffix(f.getName().toLowerCase());
                String name = stripFilename(f);
                if (!name.endsWith("clean")) { //skip: possibly not useful
                    System.out.println("Skipping file "+f.getName()+
                            ": Filename does not end with word 'clean'.");
                    continue;
                }
                if (posSuffix==null) { //skip: no POS specified, may not be freq list
                    System.out.println("Skipping file "+f.getName()+
                            ": No POS specified in the name.");
                    continue;
                }
                Scanner reader = new Scanner(f);
                while (reader.hasNext()) {
                    String[] line = reader.nextLine().split("\t");
                    String wordEntry = line[1].toLowerCase()+posSuffix;
                    //only add if included in GN, i.e. already in the HashMap
                    if (wordFreqs.containsKey(wordEntry)) {
                        int value = wordFreqs.get(wordEntry); //several entries possible
                        //replace old entry with new (regular put, as check already done)
                        wordFreqs.put(wordEntry, value+Integer.parseInt(line[0]));
                    }
                }
            }

            //word freq HashMap is complete. Now turn into synset freq HashMap:
            System.out.println("Translating into Synset frequencies.");
            Iterator iterator = wordFreqs.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry thisEntry = (Entry)iterator.next();
                String word = (String)thisEntry.getKey();
                int suffix = Integer.parseInt(word.substring(word.length()-1));
                word = word.substring(0,word.length()-1);
                Integer value = (Integer)thisEntry.getValue();

                //only consider synsets with correct category
                WordCategory cat = WordCategory.nomen; //default
                if (suffix==WordCategory.adj.ordinal()) {
                    cat = WordCategory.adj;
                }
                else if (suffix==WordCategory.verben.ordinal()) {
                    cat = WordCategory.verben;
                }
                /* else if (suffix==WordCategory.adv.ordinal()) {
                    cat = WordCategory.adv;
                } */

                //add count for synsets which carry this word to synset freq HashMap
                List<Synset> syns = gnet.getSynsets(word, cat);
                for (Synset s: syns) {
                    String id = s.getId()+"";
                    synFreqs.putCumulative(id+"", (long)value);
                }
            }

            //for each synset, add frequency to all its hypernyms
            //- write to new HashMap targetFreq, else values get mixed up.
            //This will add to each synset on all paths up exactly once.
            System.out.println("Percolating frequencies upward in the hierarchy.");
            targetFreqs = (LongHashMap)synFreqs.clone();
            Iterator it = synFreqs.entrySet().iterator();
            while (it.hasNext()) {
                Entry thisEntry = (Entry) it.next();
                long freq = (Long)thisEntry.getValue();
                int id = Integer.parseInt((String)thisEntry.getKey());
                //all entries have at least freq 1, i.e. none are excluded here
                Synset s = gnet.getSynsetByID(id);
                Path p = new Path(s);
                HashSet<Synset> visited = p.getAllHypernyms();
                Iterator it2 = visited.iterator();
                while(it2.hasNext()) {
                    Synset syn = (Synset)it2.next();
                    if (id == syn.getId()) { //do not count this node twice
                        continue;
                    }
                    targetFreqs.putCumulative(syn.getId()+"", (long)freq);
                }
            }

            File outfile = new File(directory.getAbsolutePath()+"/frequencies.csv");
            //possibly TODO: sort by freq or by id before writing to file.
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outfile), "UTF-8"));
            it = targetFreqs.entrySet().iterator();
            while (it.hasNext()) {
                Entry thisEntry = (Entry) it.next();
                writer.write(thisEntry.getKey()+"\t"+thisEntry.getValue()+"\n");
            }
            writer.close();
        }
        catch (IOException e) {
            System.out.println("Problem reading lists: "+e.getMessage());
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }


    /**
     * Creates a file "frequencies" with format "ID \t frequency" in the given input
     * directory with the cumulative frequencies as described by Resnik, 1995.<br>
     * <p>
     * This method expects as input a directory with one or more frequency lists
     * containing lines with the format "frequency\tword".<br>
     * The file names must include "nn" or "noun" for nouns, "vv" or "verb" for
     * verbs, "aj" or "adj" for adjectives (and "av" or "adv" for adverbs); or
     * "all", if POS suffixes are already attached to each entry.<br>
     * Also, filenames must end in "clean" (a regular file ending like .txt may
     * still follow it), so that cleanFile() and
     * assignFrequencies() can be used on the same directory without the 'old'
     * files being included in the latter method. <br>
     * @param inputDirectory a directory containing one or more frequency lists
     * @param gnDirectory path to your GermaNet xml directory
     */
    public static void assignFrequencies(String inputDirectory, String gnDirectory) {
        try {
            System.out.println("Initializing GermaNet.");
            GermaNet gnet = new GermaNet(gnDirectory, true);
            assignFrequencies(inputDirectory, gnet);
        }
        catch (Exception e) {
            System.out.println("Could not initialize GermaNet: "+e.getMessage());
        }

    }




    /*
     * Private method that checks the file name and returns the
     * according suffix. The file names must include "nn" or "noun" for nouns,
     * "vv" or "verb" for verbs, "aj" or "adj" for adjectives (and "av" or "adv"
     * for adverbs, if implemented in future versions of GN); or "all" to
     * indicate that the list is a mix and POS suffixes are already attached to
     * each word entry.
     * Will return ENUM value for "nomen", "verben" or "adj".
     */
    private static String getPosSuffix(String name) {
        if (name.contains("nn") || name.contains("noun") || name.contains("nomen")) {
            return ""+WordCategory.nomen.ordinal();
        }
        else if (name.contains("vv") || name.contains("verb")) {
           return ""+WordCategory.verben.ordinal();
        }
        else if (name.contains("adj") || name.contains("aj")) {
            return ""+WordCategory.adj.ordinal();
        }
        /* this part only in case adverbs are implemented in future
         * versions of GN */
        // else if (name.contains("adv") || name.contains("av")) {
        //    return ""+WordCategory.adv.ordinal();;
        //}

        /* this is assuming that a list that contains all POS will
         * already include the correct suffix with each word */
        else if (name.contains("all")) {
            return "";
        }
        return null; //no POS specified in the file name
    }



    /**
     * Cleans an input list; from inputFileName(.suffix), creates new file
     * inputFileName_clean(.suffix).  <br>
     * <p>
     * - removes #'s (separable verb, adjs etc, as in "an#weisen"),<br>
     * - removes question marks as in "erste??",<br>
     * - splits variant1|variant2 into separate entries
     *   (as in "ein#fallen|ein#fällen),<br>
     * - distributes frequency equally over variants; 
     *   special case: "Alter|Alte|Altes": do minimal stemming to "alt" if ADJ,
     *   keep male and female form (not neutral) if NOUN.<br>
     * 
     * Works with lower case (assignFrequencies lower-cases everything too).<br>
     * Sorts alphabetically.<br>
     * <p>
     * This method likely needs to be alotted adequate java heap space
     * (run with -Xms512m -Xmx512m ), depending on size of list.<br>
     * @param inputFile The file to be cleaned. Format: "frequency \t word \n"
     * @return 0 if successful, 1 if an error occurred
     */
    public static int cleanList(File inputFile) {
        //this HashMap type adds values to existing entries rather than replace them
        IntHashMap map = new IntHashMap(); //String word, Integer freq
        //check which list we're dealing with
        String suffix = getPosSuffix(inputFile.getName().toLowerCase());
        if (suffix==null) {
            System.out.println("Cannot clean list "+inputFile.getName()+
                    ": no POS specified in the name.");
            return 1;
        }

        //get entries and correct where necessary
        try {
            Scanner reader = new Scanner(inputFile);
            int linecounter = 0;
            while (reader.hasNext()) {
                linecounter++;
                if (linecounter%10000==0) System.out.print(".");
                if (linecounter%100000==0) System.out.print("\n");
                String[] line = reader.nextLine().split("\t");
                if (line.length < 2) continue; //bad format
                Integer freq = Integer.parseInt(line[0]);
                String entry = line[1].toLowerCase();
                entry = entry.replace("#", "");
                entry = entry.replace("?", "");
                if (entry.equals("")) continue; //skip now empty entries
                if (entry.contains("|")) {
                    String[] entries = entry.split("\\|");
                    if (entries.length==0) continue; //skip empty "|" entries
                    //special cases:
                    if (entries.length == 3 && entries[0].endsWith("er")
                            && entries[1].endsWith("e")
                            && entries[2].endsWith("es")) {
                        if (suffix.equals(""+WordCategory.nomen.ordinal())) {
                            //noun: use male and female as main entries, split freq
                            Integer freq2 = freq/2; //rounding down
                            if (freq2 == 0) freq2=1; //avoid 0 entries
                            map.putCumulative(entries[0],freq2);
                            map.putCumulative(entries[1],freq2);
                        }
                        else if (suffix.equals(""+WordCategory.adj.ordinal())) {
                            //adj: use stem as main entry, assign full freq
                            map.putCumulative(entries[1].substring(0,
                                    entries[1].length()-1), freq);
                        }
                        else {  //just in case...
                            Integer freq2 = freq/3; //rounding down
                            if (freq2 == 0) freq2=1; //avoid 0 entries
                            map.putCumulative(entries[0],freq2);
                            map.putCumulative(entries[1],freq2);
                            map.putCumulative(entries[2],freq2);
                        }
                    }
                    else if (entries.length == 4 && entries[1].endsWith("er")
                            && entries[2].endsWith("e")
                            && entries[3].endsWith("es")) {
                        if (suffix.equals(""+WordCategory.nomen.ordinal())) {
                            //noun: use male and female as above, split freq
                            Integer freq2 = freq/2; //rounding down
                            if (freq2 == 0) freq2=1; //avoid 0 entries
                            map.putCumulative(entries[1],freq2);
                            map.putCumulative(entries[2],freq2);
                        }
                        else if (suffix.equals(""+WordCategory.adj.ordinal())) {
                            //adj: use stem as above, assign full freq
                            map.putCumulative(entries[2].substring(0,
                                    entries[2].length()-1), freq);
                        }
                        else {  //just in case...
                            Integer freq2 = freq/4; //rounding down
                            if (freq2 == 0) freq2=1; //avoid 0 entries
                            map.putCumulative(entries[0],freq2);
                            map.putCumulative(entries[1],freq2);
                            map.putCumulative(entries[2],freq2);
                            map.putCumulative(entries[3],freq2);
                        }
                    }
                    //regular case (2 variants; also use for more than 4, if exists):
                    else {
                        Integer freq2 = freq/entries.length; //rounding down
                        if (freq2 == 0) freq2=1; //avoid 0 entries
                        for (int i=0; i<entries.length; i++) {
                            //distribute freq over all variants:
                            map.putCumulative(entries[i], freq2);
                        }
                    }
                }
                else {
                    //add single entry to the HashMap
                    map.putCumulative(entry, freq);
                }
            }
            reader.close();
            //whole list: sort alphabetically and write to file
            String outPath = inputFile.getParentFile().getPath();
            //inserting 'clean' into the end of the file name (before .filetype)
            String name = stripFilename(inputFile);
            File outfile = new File(outPath+"/"+name+"_clean"+
                    inputFile.getName().substring(
                    inputFile.getName().lastIndexOf('.'),
                    inputFile.getName().length()));
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outfile), "UTF-8"));
            String[] sorted = new String[map.size()];
            sorted = map.keySet().toArray(sorted);
            Arrays.sort(sorted);
            for (Object o: sorted) {
                writer.write(map.get(o.toString())+"\t"+o+"\n");
            }
            writer.close();
            return 0;
        }
        catch  (IOException e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

    /**
     * Runs cleanList on a whole directory.  <br>
     * <p>
     * The file should contain only frequency lists. May contain already cleaned
     * lists, but they should have a name ending in '_clean' (before the file
     * type ending, i.e. 'adj-list_clean.txt' etc.). <br>
     * This method needs to be alotted adequate java heap space
     * (run with at least -Xms512m -Xmx512m, depending on list size).
     * @param inputDir The directory holding lists to be cleaned.
     */
    public static void cleanLists(String inputDir) {
        File directory = new File(inputDir);
        File[] lists = directory.listFiles();
        for (File f: lists) {
            //skip cleaned files and safety copies:
            String name = stripFilename(f);
            if (name.endsWith("_clean") || f.getName().endsWith("~")) {
                System.out.println("\nSkipping file "+f.getName());
                continue;
            }
            System.out.println("\nCleaning file "+f.getName());
            cleanList(f);
        }
    }

    /**
     * Loads the frequency file into a HashMap. <br>
     * Keys: IDs as String, values: frequencies.<br>
     * @param freqFile File holding synset frequencies, format: "ID\tfrequency\n"
     * @return a HashMap holding IDs as String key and frequency as Integer value
     */
    public static HashMap<String, Integer> loadFreq(String freqFile) {
        HashMap<String, Integer> freqs = new HashMap<String, Integer>();
        try {
            Scanner reader = new Scanner(new File(freqFile));
            int linecounter = 0;
            while (reader.hasNext()) {
                linecounter++;
                String[] line = reader.nextLine().split("\t");
                if (line.length != 2) {
                    System.out.println("Wrong format in line "+linecounter);
                    continue;
                }
                freqs.put(line[0], Integer.parseInt(line[1]));
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Problem reading input frequency file: "+
                    e.getMessage());
            System.exit(0);
        }
        return freqs;
    }


    /**
     * Loads the frequency file and writes ICs (information content values) into
     * a HashMap. <br>
     * Keys: IDs as String, values: ics (= freq/freq(root)).<br>
     * <p>
     * Uses loadFreq, i.e. a bit slow.<br>
     * Note that the Root has IC -0.0.<br>
     * @param freqFile File holding synset frequencies, format: "ID\tfrequency\n"
     * @return a HashMap holding IDs as String key and ic as double value.
     * 
     */
    public static HashMap<String,Double> loadIC(String freqFile) {
        HashMap<String, Double> ics = new HashMap<String, Double>();
        HashMap<String, Integer> freqs = loadFreq(freqFile);
        double root = freqs.get(Relatedness.ROOT);
        Iterator it = freqs.entrySet().iterator();
        while (it.hasNext()) {
            Entry thisEntry = (Entry)it.next();
            String id = (String)thisEntry.getKey();
            double freq = (Integer)thisEntry.getValue()*1.0;
            double ic = -Math.log(freq/root);
            ics.put(id, ic);
        }
        return ics;
    }

    /*
     * Strips filetype suffixes like .txt, .cvs etc. from a filename.
     * Input: File, output: bare filename
     */
    private static String stripFilename(File f) {
        String name = f.getName();
        if (name.contains(".")) { //strip filetype suffix
            name = name.substring(0,f.getName().lastIndexOf('.'));
        }
        return name;
    }
}
