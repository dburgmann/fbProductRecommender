package fbrec.error;

/**
 * Aggregative-exception for all exceptions concerning errors during the matching process.
 * For more convinient error handling.
 * @author Daniel
 */
public class MatchingException extends Exception{

    public MatchingException(Throwable cause) {
        super(cause);
    }
    
    @Override
    public String getMessage(){
        return "An error occured while matching. Recommendation computation was not possible.";
    }
}
