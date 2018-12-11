package index;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryTermVector;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

public class Rocchio {
	
	private float alpha;
	private float beta;
	private Searcher searcher;
	private Vector<Query> expandedQuery;
    private static Logger logger = Logger.getLogger( "Rocchio" ); 
    private static final int NUM_OF_RELEVANT = 10;

	
	public Rocchio(float alpha, float beta, Searcher searcher) {
		// TODO Auto-generated constructor stub
		this.alpha = alpha;
		this.beta = beta;
		this.searcher = searcher;
	}
	
	public Query expandQuery(String query) throws IOException, ParseException{
		Vector<QueryTermVector> docsTermVector = getDocsTerms(getRelatedDocumentVectors(query));
		
		Query expandedQuery;
		
		Vector<Query> docsTerms = setBoost(docsTermVector, beta);
		
		QueryTermVector queryTermsVector = new QueryTermVector(query, new StandardAnalyzer(Version.LUCENE_36));
		Vector<Query> queryTerms = setBoostQueryTerms(queryTermsVector, alpha);
		
		Vector<Query> expandedQueryVector = combine(queryTerms, docsTerms);
		setExpandedQuery(expandedQueryVector);
		
			
        Collections.sort( expandedQueryVector, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				Query q1 = (Query) o1;
				Query q2 = (Query) o2;
				
				if(q1.getBoost() > q2.getBoost()){
					return -1;
				} else if(q2.getBoost() > q1.getBoost()){
					return 1;
				} else {
	 				return 0;
				}
			}
		} ); 
		
        expandedQuery = null;
        try{
        	expandedQuery = mergeQueries(expandedQueryVector);
        } catch(Exception e){
        	e.printStackTrace();
        }
        
        return expandedQuery;
        
	}
	
	public Query mergeQueries(Vector<Query> termQueries){
		Query query = null;

		// Select only the maxTerms number of terms
		int termCount = termQueries.size();

		// Create Query String
		StringBuffer qBuf = new StringBuffer();
		for (int i = 0; i < termCount; i++) {
			TermQuery termQuery = (TermQuery) termQueries.elementAt(i);
			Term term = termQuery.getTerm();
			qBuf.append(QueryParser.escape(term.text()).toLowerCase());
			logger.finest(term + " : " + termQuery.getBoost());
		}

		// Parse StringQuery to create Query
		logger.fine(qBuf.toString());
		String targetStr = qBuf.toString();
		try {
			query = new QueryParser(Version.LUCENE_36, Config.CONTENTS, new StandardAnalyzer(Version.LUCENE_36)).parse(targetStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		logger.fine(query.toString());

		return query;
	}
	
	public Vector<Query> combine(Vector<Query> queryTerms, Vector<Query> docsTerms){
		Vector<Query> terms = new Vector<Query>();
		terms.addAll(docsTerms);
		
		for(int i = 0; i < queryTerms.size(); i++){
			Query term = queryTerms.elementAt(i);
			Query term2 = find(term, terms);
			if(term2 != null){
				float weight = term.getBoost() + term2.getBoost();
				term2.setBoost(weight);
			} else {
				terms.add(term);
			}
		}
		return terms;
	}
	
	public Query find( Query term, Vector<Query> terms ) 
    { 
		Query termF = null; 
 
        Iterator<Query> iterator = terms.iterator(); 
        while ( iterator.hasNext() ) 
        { 
        	Query currentTerm = iterator.next(); 
            if ( term.equals( currentTerm ) ) 
            { 
                termF = currentTerm; 
            } 
        } 
         
        return termF; 
    } 
	
	public Vector<Query> setBoostQueryTerms(QueryTermVector termVector, float alpha){
		Vector<QueryTermVector> vector = new Vector<QueryTermVector>();
		
		vector.add(termVector);
		
		return setBoost(vector, alpha);		
	}
	
	public Vector<Query> setBoost(Vector<QueryTermVector> docsTerms, float factor){
		Vector<Query> terms = new Vector<Query>();
		
		for(int i = 0; i < docsTerms.size(); i++){
			QueryTermVector docTerms = docsTerms.elementAt(i);
			String[] documentTerms = docTerms.getTerms();
			int[] termFreqs = docTerms.getTermFrequencies();
			
			for(int j = 0; j < docsTerms.size(); j++){
				String termText = documentTerms[i];
				Term term = new Term(Config.CONTENTS, termText);
				
				int termFreq = termFreqs[j];
				float inverseDocFreq = searcher.getSimilarity().idf(termFreq, docTerms.size());
				float weight = termFreq * inverseDocFreq;
				
				Query query = new TermQuery(term);
				query.setBoost(factor * weight);
				terms.add(query);
			}
		}
		
		return terms;
	}
	
	public Vector<QueryTermVector> getDocsTerms(Vector<Document> hits){
		Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>();
		
		for(int i = 0; i < NUM_OF_RELEVANT; i++){
			Document doc = hits.elementAt(i);
			StringBuffer documentText =  new StringBuffer();
			String[] docText = doc.getValues(Config.CONTENTS);
			if(docText.length == 0) continue;
			for(int j = 0; j < docText.length; j++){
				documentText.append(docText[i] + " ");
			}
			
			QueryTermVector docTerms = new QueryTermVector(documentText.toString(), new StandardAnalyzer(Version.LUCENE_36));
			docsTerms.add(docTerms);
		}
		return docsTerms;
	}
	
	public Vector<Document> getRelatedDocumentVectors(String query) throws IOException, ParseException{
		TopDocs initResults = searcher.search(query);
		
		Vector<Document> relatedDocVectors = new Vector<Document>();
		
		for(int i = 0; i < NUM_OF_RELEVANT; i++){
			relatedDocVectors.add(searcher.getDocument(initResults.scoreDocs[i]));
		}
		
		return relatedDocVectors;
		
	}
	
	public void setExpandedQuery(Vector<Query> expandedQuery){
		this.expandedQuery = expandedQuery;
	}
	
	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getBeta() {
		return beta;
	}

	public void setBeta(float beta) {
		this.beta = beta;
	}

	public Searcher getSearcher() {
		return searcher;
	}

	public void setSearcher(Searcher searcher) {
		this.searcher = searcher;
	}

}
