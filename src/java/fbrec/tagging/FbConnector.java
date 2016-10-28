/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.tagging;

import com.restfb.DefaultFacebookClient;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookGraphException;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Daniel
 */
public class FbConnector{
    FacebookClient client;

    public FbConnector(String accessToken) {
        client = new DefaultFacebookClient(accessToken);
    }

    public FbProfile getProfile() throws FacebookOAuthException, FacebookNetworkException, FacebookGraphException {
        List<String> permissions = tokenPermissions();
        
        Map<String, String> queries = new HashMap<String, String>();
        queries.put("user",     "SELECT name, sex, birthday_date "
                                + "FROM user "
                                + "WHERE uid = me()");
        
        queries.put("likes",    "SELECT name, type "
                                + "FROM page WHERE page_id "
                                + "IN ("
                                    + "SELECT page_id "
                                    + "FROM page_fan "
                                    + "WHERE uid=me()"
                                + ")");
        
        queries.put("friends", "SELECT uid, first_name, last_name "
                                + "FROM user "
                                + "WHERE uid "
                                + "IN ("
                                    + "SELECT uid2 "
                                    + "FROM friend "
                                    + "WHERE uid1=me()"
                                + ")");
        
        if(permissions.contains("read_stream")){
            queries.put("statuses", "SELECT message "
                                    + "FROM status "
                                    + "WHERE uid=me()");
        }
        
        if(permissions.contains("read_mailbox")){
            queries.put("outbox",   "SELECT body "
                                    + "FROM message "
                                    + "WHERE thread_id "
                                    + "IN ("
                                        + "SELECT thread_id "
                                        + "FROM thread "
                                        + "WHERE folder_id=1"
                                    + ")");
        }
        return  client.executeMultiquery(queries, FbProfile.class);
    }
    
    /**
     * Fetches the permissions granted with the accesstoken of the connector
     * @return 
     */
    public List<String> tokenPermissions(){
        List<String> permissions;
        JsonObject  result  = client.fetchObject("me/permissions", JsonObject.class).getJsonArray("data").getJsonObject(0);
        Iterator    keys    = result.sortedKeys();
        permissions = new ArrayList();
        while(keys.hasNext()){
            permissions.add((String)keys.next());
        }
        return permissions;
    }
    
    public Set<String> expectedPermissions(){
        String[] perm = {"user_likes", "user_birthday", "read_stream", "read_mailbox"};
        return new HashSet(Arrays.asList(perm));
    }
    
    public static class FbProfile{
        @Facebook
        private List<FbUser> user;
        
        @Facebook
        private List<FbLike> likes;
        
        @Facebook
        private List<FbFriend> friends;
        
        @Facebook
        private List<FbStatus> statuses;
        
        @Facebook
        private List<FbMessage> outbox;

        public FbUser user() {
            return user.get(0);
        }

        public List<FbLike> likes() {
            return likes;
        }
       
        public List<FbLike> likesWithCategory(String category){
            List<FbLike> result = new ArrayList<FbLike>();
            for(FbLike like : likes){
                if(like.type.equals(category)) result.add(like);
            }
            return result;
        }
        
        public List<FbStatus> statuses() {
            return statuses;
        }

        public List<FbMessage> outbox() {
            return outbox;
        }
        
        public List<FbFriend> friends(){
            return friends;
        }
    }
    
    public static class FbUser{
        @Facebook
        public String name;
        
        @Facebook
        public String sex;
        
        @Facebook("birthday_date")
        public String birthday;
    }
    
    public static class FbLike{
        @Facebook
        public String name;
        
        @Facebook
        public String type;
    }
    
    public static class FbStatus{
        @Facebook
        public String message;
        
        @Override
        public String toString() {
            return message;
        }
    }
    
    public static class FbMessage{
        @Facebook("body")
        public String message;

        @Override
        public String toString() {
            return message;
        }
    }
    
    public static class FbFriend{
        @Facebook
        public String uid;
        
        @Facebook("first_name")
        public String firstName;
        
        @Facebook("last_name")
        public String lastName;
    }
}


