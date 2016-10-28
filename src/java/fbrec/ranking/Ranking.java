/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.ranking;

import fbrec.model.Recommendation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 *
 * @author Daniel
 */
public class Ranking extends ArrayList<Recommendation> implements JSONAware{
    private String      accessToken  = "";
    private Set<String> permissions  = null;

    public Ranking(List<Recommendation> recommendations) {
        super(recommendations);
        permissions = new HashSet<String>();
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    
     /**
     * Creates a JSON string from the ranking
     * @return the JSON string
     */
    @Override
    public String toJSONString(){
        JSONObject  json = new JSONObject();
        JSONArray   jsonPermissions = new JSONArray();
        JSONArray   jsonRecommendations = new JSONArray();
        
        jsonPermissions.addAll(permissions);
        jsonRecommendations.addAll(this);
        
        json.put("accessToken", accessToken);
        json.put("permissions", jsonPermissions);
        json.put("recommendations", jsonRecommendations);
        
        return json.toJSONString();
    }    
}
