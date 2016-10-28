package fbrec.error;
/**
 * Aggregative-exception for all errors occuring while setting up configuration.
 * For more convinient error handling
 * @author Daniel
 */
public class ConfigException extends Exception{
    public ConfigException(Throwable cause) {
        super(cause);
    }
    
    public ConfigException(){
        super();
    }
    
    
    @Override
    public String getMessage(){
        return "Error on loading configuration, propertyfile inaccessible or does not contain all required value.";
    }
}
