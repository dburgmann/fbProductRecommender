package fbrec.model;

import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Represents a recommendation
 * @author Daniel
 */
public class Recommendation implements JSONAware, Comparable{
    private int     productID;  //ID of the product from the product database
    private int     rank;       //recommendation rank
    private String  title;
    private double  score;
    private Set<String> fields;
    private Set<String> modules;


    /**
     * Cosntructor
     * @param productID
     * @param rank
     * @param moduleScore
     * @param module 
     */
    public Recommendation(int productID, String title, int rank, double score, Set<String> modules, Set<String> fields) {
        this.productID= productID;
        this.title    = title;
        this.rank     = rank;
        this.score    = score;
        this.fields   = fields;
        this.modules  = modules;
    }
    
    /**
     * Returns a dummy recommendation without content
     * @return 
     */
    public static Recommendation dummy(){
        return new Recommendation(110827233, "Dummy", 0, 0.0, new HashSet<String>(), new HashSet<String>());
    }
    
    /**
     * Creates a JSON string from the recommendation data for output
     * @return JSON String
     */
    @Override
    public String toJSONString(){
        JSONArray fieldsJson    = new JSONArray();
        JSONArray modulesJson   = new JSONArray();
        fieldsJson.addAll(fields);
        modulesJson.addAll(modules);
        
        JSONObject json = new JSONObject();
        json.put("productID", new Integer(productID));
        json.put("rank", new Integer(rank));
        json.put("score", new Double(score));
        json.put("modules", modulesJson);
        json.put("fields", fieldsJson);
        return json.toJSONString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Recommendation){
            Recommendation rec = (Recommendation) o;
            return (rec.getProductID() == this.getProductID());
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.productID;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
        //hash = 97 * hash + (this.module != null ? this.module.hashCode() : 0);
        return hash;
    }
    
    @Override
    public int compareTo(Object o) {
        Recommendation rec;
        if(o instanceof Recommendation){
            rec = (Recommendation) o;
            if(this.getScore() > rec.getScore()) return 1;
            if(this.getScore() < rec.getScore()) return -1;
            return 0;
        }
        return 0;
    }

    @Override
    public String toString() {
        return rank+"-"+productID+":"+title+":"+score;
    }
    
    
    
    //Getter & setter
    public int getProductID() {
        return productID;
    }

    public int getRank() {
        return rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public void setModules(Set<String> modules) {
        this.modules = modules;
    }
}
