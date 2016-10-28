package legacy;

import legacy.TfidfMatrix;
import com.google.common.collect.HashBasedTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Represents an TF-Matrix saving term frequency values for a term (String) in 
 * a document that is identified by id (integer).
 * Offers methods to ease the computation process
 * @author Daniel
 */
public class TfMatrix extends TermMatrix{
    private HashBasedTable<Integer, String, Double> tf;

    public TfMatrix() {
        tf = HashBasedTable.create();
    }
    
    /**
     * Creates a ne tfidf Matrix from a list of documents
     * @param documents
     * @throws IOException 
     */
    public TfMatrix(List<Doc> documents) throws IOException{
        //temp vars
        TokenStream  tokenStream;                                               //tokenStream holding tokens created form document
        String       docText;                                                   //text of a document
        String       token;                                                     //a token
        CharTermAttribute charTermAttribute;                                    //holds token text
        tf = HashBasedTable.create();
        //calculate needed data
        for(TfidfMatrix.Doc doc : documents){                                               //iterate over documents
            docText             = doc.text;                         
            tokenStream         = tokenizeSingleWords(docText);                            //tokenize text
            charTermAttribute   = tokenStream.addAttribute(CharTermAttribute.class); //get token text
            while(tokenStream.incrementToken()){                                //iterate over tokens
                token       = charTermAttribute.toString();
                increaseCount(doc.ID, token);
            }
        }
    }
    
    /**
     * Increases the count for given term in given doc
     * @param doc
     * @param term 
     */    
    public void increaseCount(int doc, String term){
        if(tf.contains(doc, term)){
            tf.put(doc, term, tf.get(doc, term)+1);
        }else{
            tf.put(doc, term, 1.0);
        }
    }
    
    /**
     * returns the tf value for given term in given doc
     * @param doc
     * @param term
     * @return 
     */
    public double get(int doc, String term){
        return (tf.contains(doc, term)) ? tf.get(doc, term) : 0;
    }

    
    public List<TfTerm> getRow(int doc){
        Map<String, Double> row = tf.row(doc);
        List<TfTerm> termList   = new ArrayList<TfTerm>(row.size());
        for(String str : row.keySet()){
            termList.add(new TfTerm(str, row.get(str)));
        }
        return termList;
    }
    
    /**
     * Applies cosine normalization to all values in the matrix 
     */
    public void applyNormalisation(){
        Map<String, Double> tfRow;
        double vectorSize;
        double normalizedValue;
        
        //apply cosine normalization
        for(int doc : tf.rowKeySet()){
            tfRow       = tf.row(doc);
            vectorSize  = 0.0;
            
            for(String term : tfRow.keySet()){
                vectorSize += Math.pow(tfRow.get(term),2);
            }
            vectorSize = Math.sqrt(vectorSize);
            
            for(String term : tfRow.keySet()){
                normalizedValue = tf.get(doc, term) / vectorSize;
                tf.put(doc, term, normalizedValue);
            }
        }
    }
    
    public static class TfTerm implements Comparable{
        public String text;
        public double tf;

        public TfTerm(String text, double tf) {
            this.tf = tf;
            this.text = text;
        }

        @Override
        public int compareTo(Object t) {
            if(t instanceof TfTerm){
                TfTerm term = (TfTerm) t;
                if(tf > term.tf ) return 1;
                if(tf < term.tf)  return -1;
            }
            return 0;
        }
   }
    
}
