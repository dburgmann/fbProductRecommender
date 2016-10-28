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
import de.tuebingen.uni.sfs.germanet.api.*;
import de.tuebingen.uni.sfs.germanet.relatedness.*;
import java.util.*;

public class RelatednessMeasureOutput {

    /**
     * 
     * @param s1 the list of synsets for Word 1
     * @param s2 the list of synsets for Word 2
     * @param measure the measure which was chosen to be calculated
     * @param gnet Germanet
     * @param frequencies 
     * @return The chosen Relatedness measures for all the synsets of Word 1 and Word 2
     * 
     */
    public static String resultAllSynsets(List<Synset> s1, List<Synset> s2, String measure, GermaNet gnet, HashMap<String, Integer> frequencies) {
        String output = "";
        Relatedness rel = new Relatedness(gnet);
        //going through all the synsets for Word 1
        for (Synset synset1 : s1) {
            //going through all the synsets of Word 2
            for (Synset synset2 : s2) {
                
                output += "Measures for synset with ID " + synset1.getId()+": "+synset1.getAllOrthForms() + " and synset with ID " + synset2.getId()+": "+synset2.getAllOrthForms() + ":\n\n";
                //if Path Measure or Alle Measures is chosen
                if (measure.equalsIgnoreCase("path") || measure.equalsIgnoreCase("All Measures")) {
                    
                    output += "Path: " + rel.path(synset1, synset2).getResult() + "\n";
                }
                if (measure.equalsIgnoreCase("Hirst and StOnge") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Hirst and StOnge: " + rel.hirstAndStOnge(synset1, synset2).getResult() + "\n";
                }

                if (measure.equalsIgnoreCase("Jiang and Conrath") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Jiang And Conrath: " + rel.jiangAndConrath(synset1, synset2, frequencies).getResult()+ "\n";
                }
                if (measure.equalsIgnoreCase("Leacock and Chodorow") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Leacock and Chodorow: " + rel.leacockAndChodorow(synset1, synset2).getResult() + "\n";
                }

                if (measure.equalsIgnoreCase("Resnik") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Resnik: " + rel.resnik(synset1, synset2, frequencies).getResult()+" Normalized Result:"+rel.resnik(synset1,synset2,frequencies).getNormalizedResult() + " \n";
                }
                
               
                //problems initianizing stemmer for lesk
                if(measure.equalsIgnoreCase("Lesk")||measure.equalsIgnoreCase("All Measures")){
                 output+= "Lesk: "+rel.lesk(synset1,synset2,gnet).getResult()+"\n";
                 }
                if (measure.equalsIgnoreCase("Wu and Palmer") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Wu and Palmer: " + rel.wuAndPalmer(synset1, synset2).getResult() +" "+ "\n";
                }
                
                 if (measure.equalsIgnoreCase("Lin") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Lin: " + rel.lin(synset1, synset2, frequencies) + "\n";
                }
                output+="__________________________________________________________________\n\n";
            }
          
        }
        return output;
    }
    
    public static String result(Synset synset1,Synset synset2,String measure,GermaNet gnet, HashMap<String,Integer>frequencies){
        String output="";
        Relatedness rel = new Relatedness(gnet);
         output += "Measures for" + synset1.toString() + "and " + synset2.toString() + ":\n";
                //if Path Measure or Alle Measures is chosen
              
                if (measure.equalsIgnoreCase("path") || measure.equalsIgnoreCase("All Measures")) {
                    
                    output += "Path: " + rel.path(synset1, synset2).getResult() + "\n";
                }
                if (measure.equalsIgnoreCase("Hirst and StOnge") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Hirst and StOnge: " + rel.wuAndPalmer(synset1, synset2).getResult() + "\n";
                }

                if (measure.equalsIgnoreCase("Jiang and Conrath") || measure.equalsIgnoreCase("All Measures")) {

                    output += "Jiang And Conrath: " + rel.jiangAndConrath(synset1, synset2, frequencies) + "\n";
                }
                if (measure.equalsIgnoreCase("Leacock and Chodorow") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Leacock and Chodorow: " + rel.leacockAndChodorow(synset1, synset2) + "\n";
                }

                if (measure.equalsIgnoreCase("Resnik") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Resnik: " + rel.resnik(synset1, synset2, frequencies) + "\n";
                }
                
                 if(measure.equalsIgnoreCase("Lesk")||measure.equalsIgnoreCase("All Measures")){
                 output+= "Lesk: "+rel.lesk(synset1,synset2,gnet).getResult()+"\n";
                 }
                if (measure.equalsIgnoreCase("Wu and Palmer") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Wu and Palmer: " + rel.wuAndPalmer(synset1, synset2).toString() + "\n";
                }
                
                if (measure.equalsIgnoreCase("Lin") || measure.equalsIgnoreCase("All Measures")) {
                    output += "Lin: " + rel.lin(synset1, synset2, frequencies) + "\n";
                }
        
        return output;
        
    }
    
    public static String resultforWordList(List<String[]> wordList,String measure,GermaNet gnet, HashMap<String,Integer>frequencies){
        String output="";
        for(String[] currWords:wordList){
            output+="\n Output for the words:\n";
            if(currWords.length==2){
            String word1=currWords[0];
            String word2=currWords[1];
            output+=word1+" and " +word2;
           
            List<Synset> synsets1=gnet.getSynsets(word1);
            List<Synset> synsets2=gnet.getSynsets(word2);
            if(synsets1.isEmpty()){
                output+=word1+" not found in GermaNet, no calculations possible";
            }else if(synsets2.isEmpty()){
                output+=word2+" not found in GermaNet, no calculations possible";
            }else{
                output+=resultAllSynsets(synsets1, synsets2,measure,gnet,frequencies);
            }
            }
            else{
                int i=0;
                while(i<currWords.length){
                output+=currWords[i]+" ";
                output+="\n Less or more than two words in line\n";
                i++;
                }
            }
            output+="\n";
            
            
        }
        return output;
    }
}
