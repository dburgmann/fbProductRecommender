package legacy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Represents a TfidfMatrix which stores an tf-idf value for each term (string) 
 * in a document represented by a id (integer).
 * The matrix is created from a list of documents
 * @author Daniel
 */
public class TfidfMatrix extends TermMatrix{
    private HashBasedTable<Integer, String, Double> tfidfMatrix;                //result, matrix containing tfidf values for all term, document combinations
    
    /**
     * Creates a ne tfidf Matrix from a list of documents
     * @param documents
     * @throws IOException 
     */
    public TfidfMatrix(List<Doc> documents) throws IOException{
        //vars for calculation
        HashMultimap<String, Integer>   termDocMap;                             //term->documents map
        TfMatrix                        tfMatrix;                               //term,document->tf matrix
        int                             noOfDocsPerTerm;                        //no of documents a term occours in
        int                             noOfDocs;                               //total no of documents
        double                          tf;
        double                          idf;                                    //idf value of a term
        double                          tfidf;                                  //tfidf value of a term in a document
       
        //temp vars
        TokenStream  tokenStream;                                               //tokenStream holding tokens created form document
        String       docText;                                                   //text of a document
        String       token;                                                     //a token
        Set<Integer> termDocs;                                                  //set of documents (IDs) containing a specific term
        CharTermAttribute charTermAttribute;                                    //holds token text
        
        //init vars
        tfidfMatrix = HashBasedTable.create();
        tfMatrix    = new TfMatrix();
        termDocMap  = HashMultimap.create();
        noOfDocs    = documents.size();
        
        //calculate needed data
        for(Doc doc : documents){                                               //iterate over documents
            docText             = doc.text;                         
            tokenStream         = tokenizeSingleWords(docText);                            //tokenize text
            charTermAttribute   = tokenStream.addAttribute(CharTermAttribute.class); //get token text
            while(tokenStream.incrementToken()){                                //iterate over tokens
                token       = charTermAttribute.toString();
                termDocMap.put(token, doc.ID);
                tfMatrix.increaseCount(doc.ID, token);
            }
        }
        
        tfMatrix.applyNormalisation();

        //calculate tfidf matrix
        for(String term : termDocMap.keySet()){                                 //iterate over terms
            termDocs        = termDocMap.get(term);
            noOfDocsPerTerm = termDocs.size();
            idf             = Math.log( (double) noOfDocs / (double) noOfDocsPerTerm);
            
            for(int docID : termDocs){                                          //iterate over documents the current term occours in
                tf      = tfMatrix.get(docID, term);
                tfidf   = tf * idf; 
                tfidfMatrix.put(docID, term, tfidf);
            }
        }
    }
    
    
    /**
     * Returns the tfidf value for given term in given document
     * @param docID
     * @param term
     * @return 
     */
    public double tfidf(int docID, String term){
        return (tfidfMatrix.contains(docID, term)) ? tfidfMatrix.get(docID, term) : 0;
    }
   
    
    /**
     * Returns a specified number of top terms in the given document.
     * Top terms are the n terms with the highest tf-idf value.
     * @param docID
     * @param noOfTerms
     * @return 
     */
    public List<String> topTerms(int docID, int noOfTerms){
        //vars
        List<String>        result      = new ArrayList<String>(noOfTerms);
        Map<String, Double> docTerms    = new HashMap<String, Double>(tfidfMatrix.row(docID)); //new Hashmap needed because of removal during loop
        
        //temp vars
        int     noOfDocTerms = docTerms.size(); //needed because size will change during selection process
        String  max          = "";              //term with the current highest tfidf value
        int     count        = 0;               //number of top-terms found
        
        while(count < noOfTerms && count < noOfDocTerms){   
            max = "";
            for(String term : docTerms.keySet()){                               //iterate over terms of the document
                if(max.isEmpty()){                                              //if no max is defined => current term = new max
                    max = term;
                }
                else{ 
                    if(docTerms.get(term) > docTerms.get(max)){                 //if tfidf(currentTerm) > tfidf(max) => currenterm is new max
                        max = term;
                    }
                }
            }
            result.add(max);
            docTerms.remove(max);
            count++;
        }
       
       return result;
   }
}
