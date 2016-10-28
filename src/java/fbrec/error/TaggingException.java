package fbrec.error;

/**
 * Aggregative-exception for all errors occuring in Modules.
 * For more convinient error handling
 * @author Daniel
 */
public class TaggingException extends Exception{
    private Class failedModule;
    
    public TaggingException(Throwable cause, Class module) {
        super(cause);
        failedModule = module;
    }
    
    @Override
    public String getMessage(){
        return "An error occured in module "+failedModule+". Recommendation computation was not possible.";
    }
}
