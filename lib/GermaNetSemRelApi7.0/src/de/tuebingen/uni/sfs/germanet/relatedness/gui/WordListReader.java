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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WordListReader {

    public File file;
    private ArrayList<String[]> wordList;

    public WordListReader(File f) {
        file = f;
        wordList=new ArrayList<String[]>();
        if(file.getName().endsWith("txt")){
            this.readTextFile();
        }
    }

   private void readTextFile() {
        
        try {

            BufferedReader in;

            in = new BufferedReader(new FileReader(file));

            String currLine;
            while ((currLine=in.readLine())!=null) {
                String[] currWords;
                currWords=currLine.split("\t");
             
                wordList.add(currWords);
                
               
            }
  
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WordListReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WordListReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    public List<String[]> getWords(){
        return wordList;
    }
}