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
import org.tartarus.snowball.*;

/**
 * Does all the calculations for the Lesk relatedness measure.
 *
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
public class LexicalField {
    private SnowballStemmer stemmer;
    private int limit;
    private boolean oneOrthForm;
    private boolean includeGermaNetGloss;
    private boolean includeWiktionaryGlosses;

    private HashSet<String> field;
    private static String regex = "[\\.\\(\\)\\\"\\',;!?\\s]+";

    /**
     * Creates a new LexicalField, adding orthForms of this synset and related
     * synsets, as well as glosses included in GermaNet as required by the
     * parameters.
     * 
     * @param synset Synset to construct a lexical field for
     * @param gnet an instance of the GermaNet class
     * @param oneOrthForm if set to true, only one orthForm of each synset will
     *        be used; if false, all forms will be included in the pseudo-gloss
     * @param size path length: how many related synsets will be included; if
     *        size=0 and includeGloss=true, then the LexicalField consists of
     *        glosses only
     * @param limit distance from root: how many synset layers will be excluded
     *        for being too abstract
     * @param hypernymsOnly if true, use only hypernymy relation; otherwise, use
     *        all types of relations except hyponymy
     * @param includeGermaNetGloss if true, GermaNet's own glosses will
     *        be included in the pseudo-gloss where they exist
     * @param includeWiktionaryGlosses if true, optionally loaded Wiktionary
     *        glosses will be included in the pseudo-gloss where they exist
     * @param stemmer a Stemmer instance (used to increase overlap); if null,
     *        no stemming will occur.
     */
    public LexicalField(Synset synset, GermaNet gnet, SnowballStemmer stemmer, 
            int size, int limit, boolean oneOrthForm, boolean hypernymsOnly,
            boolean includeGermaNetGloss, boolean includeWiktionaryGlosses) {

        if (limit >= Relatedness.MAX_DEPTH || limit < 0) {
            System.out.println("Error: limit outside of range [0,MAX_DEPTH]");
            System.exit(0);
        }
        if (size > Relatedness.MAX_DEPTH || size < 0) {
            System.out.println("Error: size outside of range [0,MAX_DEPTH]");
            System.exit(0);
        }
        else if (size > 6) {
            System.out.println("WARNING: High values for size (value > 6) " +
                    "may lead to an explosion of the Lexical Field! Program " +
                    "may not finish.");
        }

        this.stemmer = stemmer;
        this.limit = limit;
        this.oneOrthForm = oneOrthForm;
        this.includeGermaNetGloss = includeGermaNetGloss;
        this.includeWiktionaryGlosses = includeWiktionaryGlosses;
        this.field = new HashSet<String>();
        
        //add orthForms, one or all
        addForms(synset);
        //add other relations; hypernyms or all; if size > 0
        if (size > 0) {
            if (hypernymsOnly) {
                addHypernyms(synset,size);
            } else {
                addRels(synset, size);
            }
        }
        //if applicable, add glosses
        if (includeGermaNetGloss) {
            addGermaNetGloss(synset);
        }
        if (includeWiktionaryGlosses) {
            addWiktionaryGlosses(synset);
        }
    }

    /**
     * Returns the lexical field, i.e. all the words added to this LexicalField
     * instance during initiation.
     * @return the HashSet of Strings included in this lexical field
     */
    public HashSet<String> getField() {
        return field;
    }

    /*
     * adds one or all orthForms of given synset to the given HashMap,
     * depending on boolean value of oneOrthForm (only one if true, all otherwise)
     */
    private void addForms(Synset s) {
        List<String> orthForms = s.getAllOrthForms();
        if (oneOrthForm) {
            //split multiword entries like "höheres Lebewesen":
            String[] orthForm = orthForms.get(0).toLowerCase().split("\\s"); //only use first entry
            for (int i=0; i<orthForm.length; i++) {
                addStemmedEntry(orthForm[i]);
            }
        }
        else {
            for (String form: orthForms) {
                String[] orthForm = form.toLowerCase().split("\\s");
                for (int i=0; i<orthForm.length; i++) {
                    addStemmedEntry(orthForm[i]);
                }
            }
        }
    }

    /*
     * Checks if stemmer is not null; if so, stems entry before adding to field.
     */
    private void addStemmedEntry(String word) {
        if (stemmer== null) field.add(word);
        else {
            stemmer.setCurrent(word);
            stemmer.stem();
            field.add(stemmer.getCurrent());
        }
    }

    /*
     * recursively adds all hypernyms within size and outside limit to the field
     */
    private void addHypernyms(Synset s, int currentSize) {
        List<Synset> hyper = s.getRelatedSynsets(ConRel.has_hypernym);
        for (Synset h: hyper) {
            Path p = new Path(h);
            if (p.getDepth()>limit) {
                addForms(h);
                if (includeGermaNetGloss) {
                    addGermaNetGloss(s);
                }
                if (includeWiktionaryGlosses) {
                    addWiktionaryGlosses(s);
                }
                if (currentSize>1) {
                    addHypernyms(h, currentSize-1);
                }
            }
        }
    }

    /*
     * recursively adds all related synsets except hyponyms within size and
     * outside limit to the field
     */
    private void addRels(Synset s, int currentSize) {
        //as there is no way to exclude hyponyms from getRelatedSynsets(),
        //get each type of relation separately:
        ConRel[] relTypes = ConRel.values();
        for (int i=0;i<relTypes.length;i++) {
            if (!relTypes[i].name().equals("has_hyponym")) {
                List<Synset> rels = s.getRelatedSynsets(relTypes[i]);
                if (rels != null && rels.size() > 0) {
                    for (Synset r: rels) {
                       Path p = new Path(r);
                        if (p.getDepth()>limit) {
                            addForms(r);
                            if (includeGermaNetGloss) {
                                addGermaNetGloss(s);
                            }
                            if (includeWiktionaryGlosses) {
                                addWiktionaryGlosses(s);
                            }
                            if (currentSize>1) {
                                addRels(r,currentSize-1);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Adds all words in the GermaNet paraphrase (if there is one) to the field
     */
    private void addGermaNetGloss(Synset s) {
        String gloss = s.getParaphrase();
        if (gloss != null && gloss.trim().length()>0) {
            String[] glossParts = gloss.toLowerCase().split(regex);
            for (int i=0;i<glossParts.length;i++) {
                addStemmedEntry(glossParts[i]);
            }
        }
    }

    /**
     * Adds all words in all Wiktionary paraphrases (if any) to the field
     */
    private void addWiktionaryGlosses(Synset s) {
        String gloss = "";
        for (LexUnit lexUnit : s.getLexUnits()) {
            for (WiktionaryParaphrase paraphrase : lexUnit.getWiktionaryParaphrases()) {
                gloss += paraphrase.getWiktionarySense() + " ";
            }
        }
        gloss = gloss.trim();
        if (!gloss.equals("")) {
            String[] glossParts = gloss.toLowerCase().split(regex);
            for (int i=0;i<glossParts.length;i++) {
                addStemmedEntry(glossParts[i]);
            }
        }
    }

    /**
     * Returns the field as a single String. 
     * @return all elements in the field concatenated into one String
     * @override toString()
     */
    @Override
    public String toString() {
        String output = "";
        Iterator it = field.iterator();
        while (it.hasNext()) {
            output += it.next()+" ";
        }
        return output;
    }

}
