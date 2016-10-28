package fbrec.matching;

import fbrec.control.Config;
import fbrec.database.Products;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * Represents a Lucene index and offers simplified access to search functionality
 * @author Daniel
 */
public class Index {
    private Directory           dir;                                            //Directory the index is created in
    private Analyzer            analyzer;                                       //Analyzer used for indexing
    private QueryParser         parser;                                         //Queryparser
    private IndexReader         reader;                                         //Indexreader
    private IndexSearcher       searcher;                                       //Indexsearcher
    
    
    /**
     * Sets up a new index creator for creating a index in given directory
     * @param dir 
     */
    public Index(Directory directory) throws IOException {
        dir         = directory;
        analyzer    = Config.DEFAULT_ANALYZER;                                 //set default analyzer
        parser      = new QueryParser(Version.LUCENE_40, Products.TEXT_FIELD, analyzer);//set parser
        reader      = DirectoryReader.open(dir);
        searcher    = new IndexSearcher(reader);
    }
    
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        this.parser   = new QueryParser(Version.LUCENE_40, Products.TEXT_FIELD, analyzer); //setting of new analyzer requires also new parser
    }
    
    /**
     * converts given String to a query object
     * @param queryStr
     * @return
     * @throws ParseException 
     */
    public Query stringToQuery(String queryStr) throws ParseException{
        return parser.parse(queryStr);
    }
    
    
    /**
     * Performs a search with the given list of queries.
     * All queries are ranked together.
     * @param queries   the queries that should be executed together
     * @return List of Top Lucene Documents from index that where found
     * @throws ParseException
     * @throws IOException 
     */
    public ArrayList<SearchResult> search(Query query, int maxHits) throws ParseException, IOException{
        //init vars
        ArrayList<SearchResult>     result      = new ArrayList<SearchResult>();
        TopScoreDocCollector        collector   = TopScoreDocCollector.create(maxHits, true);
        ScoreDoc[]  hits;

        //execute queries
        searcher.search(query, collector);
            
        //get hits
        hits = collector.topDocs().scoreDocs;
        
        //generate resultset
        for(ScoreDoc hit : hits){
            result.add(new SearchResult(
                                        searcher.doc(hit.doc),  //lucene document
                                        hit.score
                                       ));
        }
        return result;
    }
        
    
    //SearchResult class for aggregation result information
    public class SearchResult {
        public Document doc;
        public double   score;

        public SearchResult(Document doc, double score) {
            this.doc                = doc;
            this.score              = score;
        }
    }
}