package fbrec.error;

/**
 * Aggregative-exception for all errors occuring in processors.
 * For more convinient error handling
 * @author Daniel
 */
public class PostProcessingException extends Exception{
    private Class failedProcessor;
        
    public PostProcessingException(Throwable cause, Class processor) {
        super(cause);
        failedProcessor    = processor;
    }
    
    @Override
    public String getMessage(){
        return "An error occured in processor "+failedProcessor.getName()+". Correct filtering of Ranking was not possible.";
    }
}
