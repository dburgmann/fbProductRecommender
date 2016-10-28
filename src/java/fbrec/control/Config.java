package fbrec.control;

import fbrec.error.ConfigException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;

/**
 * Holds all config constants
 * @author Daniel
 */
public class Config{    
    public  static final String  FILEPATH;
    private static final String  CONFIG_PROPERTIES = "config.properties";
    
    //**************************************************************************
    //CONSTANTS FROM PROPERTY FILE
    //**************************************************************************
    
    //general constants    
    public static String    INDEX_DIR;
    public static String    GERMANET_DIR;
    public static String    GERMAN_STOPWORD_FILE;
    public static String    POS_MODEL_FILE;
    public static String    LOGPATH;
    public static Level     LOGLEVEL;
    
    //parameters influencing the recommendation process
    public static double    ENTERTAINMENT_WEIGHT;
    public static double    TEXT_WEIGHT;
    public static double    BRANDS_WEIGHT;
    public static double    SPORTS_WEIGHT;
    
    //detail parameters
    public static int       DEFAULT_NUM_RESULTS;
    public static int       DEFAULT_NUM_TAGS;
    public static int       DEFAULT_AGE;
    public static int       MAX_NUM_TAGS_PER_MODULE;
    public static int       MIN_RESULTS_PER_QUERY;
    public static double    MIN_TAG_SCORE_PERCENT;    

    
    
    
    //**************************************************************************
    //REAL CONSTANTS
    //**************************************************************************
    
    //constants concerning logging
    public static final String EVENT_LOGGER         = "events";
    public static final String RESULT_LOGGER        = "result";
    public static final String ERROR_LOGGER         = "error";
   
    public static Analyzer DEFAULT_ANALYZER;
    
    
    static{
        FILEPATH = Config.class.getResource("/../files").getFile();        
    }
    
    /**
     * Returns the file with given filename
     * @param name
     * @return 
     */
    public static File getFile(String name){
        return new File(FILEPATH+name);
    }
    
    /**
     * Loads the file with given filename in a filestream
     * @param name
     * @return
     * @throws FileNotFoundException 
     */
    public static FileReader loadFile(String name) throws FileNotFoundException{
        return new FileReader(getFile(name));
    }
    
    /**
     * Loads the config properties file
     * @throws ConfigException 
     */
    public static void loadConfig() throws ConfigException{ 
        //load property file
        Properties prop = new Properties();
        try{
            FileReader fr = loadFile(CONFIG_PROPERTIES); 
            prop.load(fr);
            fr.close();
        }catch(IOException e){
            throw new ConfigException(e);
        }
        
        
        /******************
        *LOAD PATH CONFIGS
        *******************/
        
        if(prop.containsKey("INDEX_DIR")){ 
            INDEX_DIR = prop.getProperty("INDEX_DIR");
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("GERMANET_DIR")){ 
            GERMANET_DIR = prop.getProperty("GERMANET_DIR");
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("GERMAN_STOPWORD_FILE")){ 
            GERMAN_STOPWORD_FILE = prop.getProperty("GERMAN_STOPWORD_FILE");
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("LOGPATH")){ 
            LOGPATH = prop.getProperty("LOGPATH");
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("LOGLEVEL")){ 
            String level = prop.getProperty("LOGLEVEL").toLowerCase();
            if(level.equals("debug")){
                LOGLEVEL = Level.DEBUG;
            }
            else if(level.equals("info")){
                LOGLEVEL = Level.INFO;
            }
            else if(level.equals("warn")){
                LOGLEVEL = Level.WARN;
            }
            else if(level.equals("error")){
                LOGLEVEL = Level.ERROR;
            }
            else{
                LOGLEVEL = Level.INFO;
            }
        }
        else{
            throw new ConfigException();
        }
                
        if(prop.containsKey("POS_MODEL_FILE")){ 
            POS_MODEL_FILE = FILEPATH+prop.getProperty("POS_MODEL_FILE");
        }
        else{
            throw new ConfigException();
        }
        
        
        
        /******************
        *LOAD DETAIL CONFIG
        *******************/
        
        if(prop.containsKey("DEFAULT_NUM_RESULTS")){ 
            DEFAULT_NUM_RESULTS = Integer.parseInt(prop.getProperty("DEFAULT_NUM_RESULTS"));
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("DEFAULT_NUM_TAGS")){ 
            DEFAULT_NUM_TAGS = Integer.parseInt(prop.getProperty("DEFAULT_NUM_TAGS"));
        }
        else{
            throw new ConfigException();
        }
                
        if(prop.containsKey("DEFAULT_AGE")){ 
            DEFAULT_AGE = Integer.parseInt(prop.getProperty("DEFAULT_AGE"));
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("MAX_NUM_TAGS_PER_MODULE")){ 
            MAX_NUM_TAGS_PER_MODULE = Integer.parseInt(prop.getProperty("MAX_NUM_TAGS_PER_MODULE"));
        }
        else{
            throw new ConfigException();
        }
                                        
        if(prop.containsKey("MIN_RESULTS_PER_QUERY")){ 
            MIN_RESULTS_PER_QUERY = Integer.parseInt(prop.getProperty("MIN_RESULTS_PER_QUERY"));
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("MIN_TAG_SCORE_PERCENT")){ 
            MIN_TAG_SCORE_PERCENT = Double.parseDouble(prop.getProperty("MIN_TAG_SCORE_PERCENT"));
        }
        else{
            throw new ConfigException();
        }        
        
        
        
        /******************
        *LOAD WEIGHT CONFIGS
        *******************/
        
        if(prop.containsKey("ENTERTAINMENT_WEIGHT")){ 
            ENTERTAINMENT_WEIGHT = Double.parseDouble(prop.getProperty("ENTERTAINMENT_WEIGHT"));
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("BRANDS_WEIGHT")){ 
            BRANDS_WEIGHT = Double.parseDouble(prop.getProperty("BRANDS_WEIGHT"));
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("TEXT_WEIGHT")){ 
            TEXT_WEIGHT = Double.parseDouble(prop.getProperty("TEXT_WEIGHT"));
        }
        else{
            throw new ConfigException();
        }
        
        if(prop.containsKey("SPORTS_WEIGHT")){ 
            SPORTS_WEIGHT = Double.parseDouble(prop.getProperty("SPORTS_WEIGHT"));
        }
        else{
            throw new ConfigException();
        }
        
        try{
            initAnalyzer();
            initLoggers();
        }catch(IOException e){
            throw new ConfigException(e);
        }
    }    
    
    private static void initAnalyzer(){
        //init default analyzer
        CharArraySet stopWords;                                                 //list of stopword
        try {
            FileReader fr = loadFile(Config.GERMAN_STOPWORD_FILE);
            stopWords  = WordlistLoader.getWordSet(fr, Version.LUCENE_40);
            fr.close();
        } catch (IOException ex) {
            stopWords = GermanAnalyzer.getDefaultStopSet();
        }        
        DEFAULT_ANALYZER = new GermanAnalyzer(Version.LUCENE_40);               //set default analyzer
    }
    
    /**
     * initialize loggers
     * @throws IOException 
     */
    private static void initLoggers() throws IOException{
        BasicConfigurator.resetConfiguration();        
        String date     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:S").format(new Date());
        String path     = Config.FILEPATH+Config.LOGPATH;
        Layout layout   = new PatternLayout("%d{ABSOLUTE} [%t] %-5p %m%n");
        
        Logger events   = Logger.getLogger(Config.EVENT_LOGGER);
        events.addAppender(new FileAppender(layout, path+date+"_fbrec_"+Config.EVENT_LOGGER+".log"));
        events.addAppender(new ConsoleAppender(layout));
        events.setLevel(LOGLEVEL);
        
        Logger products = Logger.getLogger(Config.RESULT_LOGGER);
        products.addAppender(new FileAppender(layout, path+date+"_fbrec_"+Config.RESULT_LOGGER+".log"));
        products.setLevel(Level.INFO);
        
        Logger error   = Logger.getLogger(Config.ERROR_LOGGER);
        error.addAppender(new FileAppender(layout, path+date+"_fbrec_"+Config.ERROR_LOGGER+".log"));
        error.addAppender(new ConsoleAppender(layout));
        error.setLevel(Level.ERROR);
   }
    
}