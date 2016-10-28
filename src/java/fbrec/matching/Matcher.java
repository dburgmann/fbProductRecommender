package fbrec.matching;

import fbrec.model.Recommendation;
import fbrec.error.MatchingException;
import fbrec.control.Config;
import fbrec.matching.Index.SearchResult;
import fbrec.database.Products;
import fbrec.tagging.FbConnector.FbProfile;
import fbrec.tagging.FbConnector.FbUser;
import fbrec.model.Tag;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 *
 * @author Daniel
 */
public class Matcher {
    private FbProfile               profile;
    private HashMap<Query, Tag>     queryTag;
    private Index                   index;
    private List<Recommendation>    recommendations;
    private int                     numResults;
    
    public Matcher(FbProfile profile, int numResults) {
        this.profile  = profile;
        this.numResults = numResults;
        queryTag   = new HashMap<Query, Tag>();
        recommendations = new ArrayList<Recommendation>();
    }
    
    
    /**
     * Matches the given list of tags to products in the index.
     * @param tags
     * @return
     * @throws MatchingException 
     */
    public void matchTagsToProducts(List<Tag> tags) throws MatchingException{
        try{
            //open index
            Directory dir   = new SimpleFSDirectory(Config.getFile(Config.INDEX_DIR));
            index           = new Index(dir);
            
            recommendations = matchProducts(getQueries(tags));           
        }catch(Exception e){
            throw new MatchingException(e);
        }
    }
    
    
    /**
     * Returns a list of queries for given list of tags
     * @param tags
     * @return
     * @throws java.text.ParseException
     * @throws IOException
     * @throws ParseException 
     */
    protected List<Query> getQueries(List<Tag> tags) throws java.text.ParseException, IOException, ParseException{
        List<String>    genderAge   = getGenderAgeLabels();
        List<Query>     queries     = new ArrayList<Query>();
        Query           query;
        Tag             mainTag;
        Tag             tag2;
        
        String str;
        for(int i = 0; i < tags.size(); i++){
            mainTag = tags.get(i);
            str = "+("+orQuery(mainTag.text)+")";
            str+= queryRestriction(genderAge, Products.GENDER_AGE_FIELD);
            for(int k = 0; k < tags.size(); k++){
                if(i == k) continue;
                tag2 = tags.get(k);
                str += " "+orQuery(tag2.text);
            }
            for(String word : mainTag.wordPool){
                str += " "+orQuery(word);
            }
            
            query   = index.stringToQuery(str);
            queryTag.put(query, mainTag);
            queries.add(query);
        }
        return queries;
    }
    
    protected String orQuery(String value){
        String result;
        String title = Products.TITLE_FIELD+":\""+value+"\"";
        String descr = Products.TEXT_FIELD+":\""+value+"\"";
        String brand = Products.BRAND_FIELD+":\""+value+"\"";
        String cat   = Products.CATEGORY_FIELD+":\""+value+"\"";
        result = title+" "+descr+" "+brand+" "+cat;
        return result;
    }
     
    
    /**
     * Execute the search with the given List of queries.
     * The queries are combined into one resultset.
     * @param queries   the search queries
     * @return List of recommendations as result of the search
     * @throws IOException
     * @throws ParseException 
     */
    protected List<Recommendation> matchProducts(List<Query> queries) throws IOException, ParseException, MatchingException{
        //init vars
        List<Recommendation>    result = new ArrayList<Recommendation>();       //result list
        List<SearchResult>      searchResults;                                  //search result
        Set<String>             modules;
        Set<String>             fields;
        Recommendation          rec;                                            //recommendation object for result transformation
        double                  scoreMulti;
        int                     resultsPerQuery;
        int                     resultsNeeded;
        resultsNeeded   = (int) Math.ceil((double)numResults/(double)queries.size());
        resultsPerQuery = Math.max((int)  resultsNeeded, Config.MIN_RESULTS_PER_QUERY);
        
        for(Query query : queries){
            searchResults   = index.search(query, resultsPerQuery);
            modules         = new HashSet<String>();
            fields          = new HashSet<String>();
            
            modules.addAll(queryTag.get(query).source);
            fields.addAll(queryTag.get(query).base);
            scoreMulti = queryTag.get(query).score;
            
            //logging
            if(searchResults.isEmpty()){
                Logger.getLogger(Config.EVENT_LOGGER).warn("A Query did not return any result.");
                Logger.getLogger(Config.EVENT_LOGGER).debug(query);
            }else{
                Logger.getLogger(Config.EVENT_LOGGER).debug(query);
            }
            
            //transform doc list to recommendation list - docs & scores lists are orderd in the same way
            for(SearchResult sr : searchResults){
                rec = new Recommendation(Integer.parseInt(sr.doc.get(Products.ID_FIELD)),
                                         sr.doc.get(Products.TITLE_FIELD),
                                         0,                                    
                                         sr.score*scoreMulti,
                                         modules,
                                         fields);                              

                result.add(rec);
                Logger.getLogger(Config.EVENT_LOGGER).debug("new recommendation: "+rec);
            }
        }
        return result;
    }
    
    
    /**
     * Converts the given birthday string to a age (integer)
     * @param birthday
     * @return
     * @throws ParseException 
     */
    protected int birthdayToAge(String birthday) throws java.text.ParseException{
        if(birthday == null || birthday.equals("null")){
            Logger.getLogger(Config.EVENT_LOGGER).warn("Users age not specified, using default age: "+ Config.DEFAULT_AGE);
            return Config.DEFAULT_AGE;
        }
        
        SimpleDateFormat dateFormat      = new SimpleDateFormat("DD/MM/yyyy");
        
        //create calendar objects
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateFormat.parse(birthday));  
        Calendar today = Calendar.getInstance();  
            
        //calculate age
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);  
           
        //check if user already had birthady this year
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;  
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;  
        }
        
        //logging
        Logger.getLogger(Config.EVENT_LOGGER).info("user age: "+age);
        
        return age;
    }
    
    
    /**
     * Determines which Gender_Age labels that apply to the user
     * Labels are defined in Config
     * @param gender
     * @param age 
     */
    protected List<String> getGenderAgeLabels() throws java.text.ParseException{
        ArrayList<String> allowedLabels = new ArrayList<String>();
        FbUser user     = profile.user();
        
        int     age   = birthdayToAge(user.birthday);
        String  gender= user.sex;
        boolean adult = age>17;                                                 //treat person as adult when older than 17
        boolean male  = gender.equals("male");
        boolean female= gender.equals("female");
        
        if(!male && !female){
            Logger.getLogger(Config.EVENT_LOGGER).warn("Users gender not specified.");
        }
        
        if(!adult && male)   allowedLabels.add(Products.LABEL_BOYS);
        if(!adult && female)  allowedLabels.add(Products.LABEL_GIRLS);
        if(adult && male)    allowedLabels.add(Products.LABEL_MEN);
        if(adult && female)   allowedLabels.add(Products.LABEL_WOMEN);
        
        allowedLabels.add(Products.LABEL_UNDEFINED);                            //always accept undefined, unisex and baby
        allowedLabels.add(Products.LABEL_UNISEX);
        
        //logging
        for(String label: allowedLabels){
            Logger.getLogger(Config.EVENT_LOGGER).info("new user target group: "+label);
        }
        
        return allowedLabels;
    }
    
    
    /**
     * Restricts the allowed values for the given field in given query.
     * @param query     the query to that the restriction should be added
     * @param values    the values that are allowed for the field
     * @param field     the field the restriction is applied to
     * @return 
     */    
    protected String queryRestriction(List<String> values, String field){
        String str;
        str = " +(";
        for(String label : values){
            str += " "+field+":"+label;
        }
        str += ")";
        return str;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }
    
    
}
