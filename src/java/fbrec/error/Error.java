package fbrec.error;

import fbrec.control.Config;
import org.apache.log4j.Logger;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Class for encapsulation of exceptions to allow proper output of errors in 
 * json format
 * @author Daniel
 */
public class Error implements JSONAware {
    Exception exception;
    
    
    /**
     * Convinience factory method for method chaining
     * @param e
     * @return 
     */
    public static Error factory(Exception e){
        return new Error(e);
    }

    
    /**
     * Constructor
     * @param e 
     */
    private Error(Exception e) {
        Logger.getLogger(Config.EVENT_LOGGER).error("A "+e.getClass().getSimpleName()+" occoured. Check the error log file for more information.");
        Logger.getLogger(Config.ERROR_LOGGER).error(e.getMessage(), e);
        exception = e;
    }
   
    
    /**
     * Creates a jsonString from the object
     * @return json representation of object as string
     */
    @Override
    public String toJSONString(){
        JSONObject json  = new JSONObject();
        JSONObject error = new JSONObject();
        error.put("type", exception.getClass().toString());
        error.put("message", exception.getMessage());
        json.put("error", error);
        return json.toJSONString();
    }  
    
    
}
