#Product recommender based on facebook api

I created this recommender system during my bachelorthesis. Implemented as Java Servlet

Requires

[fbRecommenderProducts](https://github.com/dburgmann/fbRecommenderProducts) to connect to a product database [fbRecommenderDbIndexer](https://github.com/dburgmann/fbRecommenderDbIndexer) in order to generate a lucene index on the product database

UML Diagram:

![UML Diagram](https://github.com/dburgmann/fbRecommender/blob/master/UML.png)

Recommendation works in 4 Steps:

1.Tag extraction from Facebook profile 2.Tag processing 3.Index lookup 4.Recommendation post processing / ranking

The Recommender uses

[Apache Lucene](http://lucene.apache.org/core/) - to generate a index and search the product database [GermaNet](http://www.sfs.uni-tuebingen.de/GermaNet/) - a semantic network for the german language [StanfordPosttagger](http://nlp.stanford.edu/software/tagger.shtml) - for part of speech tagging

during the Recommendation Process
