package index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

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
    private static final int NUM_OF_RELEVANT = 30;

	
	

	public Rocchio(float alpha, float beta, Searcher searcher) {
		// TODO Auto-generated constructor stub
		this.alpha = alpha;
		this.beta = beta;
		this.searcher = searcher;
	}
	
	//Query expansion using Rocchio algorithm
	public Query expandQuery(String query) throws IOException, ParseException{
		Query expandedQuery = null;
		Vector<Document> relatedDocuments = getRelatedDocumentVectors(query);
		Vector<QueryTermVector> docsTermVector = getDocsTerms(relatedDocuments);
		
		if(!(relatedDocuments.size() < NUM_OF_RELEVANT)){
			Vector<TermQuery> docsTerms = setWeightDocTerms(docsTermVector, beta);
			
			QueryTermVector queryTermsVector = new QueryTermVector(query, new StandardAnalyzer(Version.LUCENE_36));
			Vector<TermQuery> queryTerms = setWeightQueryTerms(queryTermsVector, alpha);
			
			Vector<TermQuery> expandedQueryVector = combineWeights(queryTerms, docsTerms);
				
//	        Collections.sort(expandedQueryVector, new Comparator<Object>() {
//
//				@Override
//				public int compare(Object o1, Object o2) {
//					// TODO Auto-generated method stub
//					Query q1 = (Query) o1;
//					Query q2 = (Query) o2;
//					
//					if(q1.getBoost() > q2.getBoost()){
//						return -1;
//					} else if(q2.getBoost() > q1.getBoost()){
//						return 1;
//					} else {
//		 				return 0;
//					}
//				}
//			} ); 
			
	        
	        try{
	        	expandedQuery = mergeQueries(expandedQueryVector, 5);
	        	System.out.println("\u001B[32mExpanded Query: " + expandedQuery.toString("contents") + "\033[0m");
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
	        
		} else {
			System.err.println("Not enough initial documents have been returned.");
		}
	
		return expandedQuery;
        
	}
	
	public Query mergeQueries(Vector<TermQuery> termQueries, int maxTerms){
		Query query = null;

		// Select only the maxTerms number of terms
		int termCount = Math.min(termQueries.size(), maxTerms);

		// Create Query String
		StringBuffer queryBuffer = new StringBuffer();
		for (int i = 0; i < termCount; i++) {
			TermQuery termQuery = (TermQuery) termQueries.elementAt(i);
			Term term = termQuery.getTerm();
			queryBuffer.append(QueryParser.escape(term.text()).toLowerCase() + " ");
		}

		// Parse StringQuery to create Query
		String targetStr = queryBuffer.toString();
		try {
			query = new QueryParser(Version.LUCENE_36, Config.CONTENTS, new StandardAnalyzer(Version.LUCENE_36)).parse(targetStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return query;
	}
	
	private void merge(Vector<TermQuery> terms) {
		for (int i = 0; i < terms.size(); i++) {
			TermQuery term = terms.elementAt(i);
			// Itterate through terms and if term is equal then merge: add the
			// boost; and delete the term
			for (int j = i + 1; j < terms.size(); j++) {
				TermQuery tmpTerm = terms.elementAt(j);

				// If equal then merge
				if (tmpTerm.getTerm().text().equals(term.getTerm().text())) {
					// Add boost factors of terms
					term.setBoost(term.getBoost() + tmpTerm.getBoost());
					// delete uncessary term
					terms.remove(j);
					// decrement j so that term is not skipped
					j--;
				}
			}
		}
	}
	
	//Combine the weights in the vectors. Based on Rocchio algorithm.
	public Vector<TermQuery> combineWeights(Vector<TermQuery> queryTerms, Vector<TermQuery> docsTerms){
		Vector<TermQuery> terms = new Vector<TermQuery>();
		terms.addAll(docsTerms);
		
		for(int i = 0; i < queryTerms.size(); i++){
			TermQuery term = queryTerms.elementAt(i);
			TermQuery duplicateTerm = findDuplicate(term, terms);
			if(duplicateTerm != null){
				float weight = term.getBoost() + duplicateTerm.getBoost();
				duplicateTerm.setBoost(weight);
			} else {
				terms.add(term);
			}
		}
		return terms;
	}
	
	public TermQuery findDuplicate(TermQuery term, Vector<TermQuery> terms) {
		TermQuery termFound = null;

		Iterator<TermQuery> iterator = terms.iterator();
		while (iterator.hasNext()) {
			TermQuery currentTerm = iterator.next();
			if (term.equals(currentTerm)) {
				termFound = currentTerm;
			}
		}

		return termFound;
	}
	
	//Set the weights of query terms for vectorizing using TFIDF weighting
	public Vector<TermQuery> setWeightQueryTerms(QueryTermVector termVector, float alpha) {
		Vector<TermQuery> terms = new Vector<TermQuery>();

		String[] documentTerms = termVector.getTerms();
		int[] termFreqs = termVector.getTermFrequencies();

		for (int j = 0; j < documentTerms.length; j++) {
			String termText = documentTerms[j];
			Term term = new Term(Config.CONTENTS, termText);

			int termFreq = termFreqs[j];
			float inverseDocFreq = searcher.getSimilarity().idf(termFreq, termVector.size());
			float weight = termFreq * inverseDocFreq;

			TermQuery query = new TermQuery(term);
			query.setBoost(alpha * weight);
			terms.add(query);
		}
		
		merge(terms);
		
		return terms;
	}
	
	//Set the weights of document terms for vectorizing using TFIDF weighting
	public Vector<TermQuery> setWeightDocTerms(Vector<QueryTermVector> docsTerms, float factor){
		Vector<TermQuery> terms = new Vector<TermQuery>();
		
		for(int i = 0; i < docsTerms.size(); i++){
			QueryTermVector docTerms = docsTerms.elementAt(i);
			String[] documentTerms = docTerms.getTerms();
			int[] termFreqs = docTerms.getTermFrequencies();
			
			for(int j = 0; j < documentTerms.length; j++){
				String termText = documentTerms[j];
				Term term = new Term(Config.CONTENTS, termText);
				
				int termFreq = termFreqs[j];
				float inverseDocFreq = searcher.getSimilarity().idf(termFreq, docTerms.size());
				float weight = termFreq * inverseDocFreq;
				
				TermQuery query = new TermQuery(term);
				query.setBoost((factor / NUM_OF_RELEVANT) * weight);
				terms.add(query);
			}
		}
		
		merge(terms);
		
		return terms;
		
	}
	
	//Get the terms in the relevant documents
	public Vector<QueryTermVector> getDocsTerms(Vector<Document> hits) throws IOException {
		Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>();
		try {
			for (int i = 0; i < NUM_OF_RELEVANT; i++) {
				Document doc = hits.elementAt(i);
				StringBuffer documentText = new StringBuffer();
				String mailBody = "";
				String text;
				BufferedReader reader = new BufferedReader(new FileReader(doc.get(Config.FILE_PATH)));
				while ((text = reader.readLine()) != null) {
					if (text.startsWith("X-FileName")) {
						String next;
						while ((next = reader.readLine()) != null) {
							String[] line = next.split(" ");
							for (String s : line)
								mailBody += s + " ";
						}
					}
				}
				String[] docText = mailBody.split("[^a-zA-Z]+");
				if (docText.length == 0)
					continue;
				for (int j = 0; j < docText.length; j++) {
					documentText.append(docText[j] + " ");
				}

				QueryTermVector docTerms = new QueryTermVector(documentText.toString(),
						new StandardAnalyzer(Version.LUCENE_36));
				docsTerms.add(docTerms);
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("List error");
		}

		return docsTerms;
	}
	
	//Get the documents that are assumed to be relevant
	public Vector<Document> getRelatedDocumentVectors(String query) throws IOException, ParseException{
		TopDocs initResults = searcher.search(query);
		
		Vector<Document> relatedDocVectors = new Vector<Document>();
		try{
			for(int i = 0; i < NUM_OF_RELEVANT; i++){
				relatedDocVectors.add(searcher.getDocument(initResults.scoreDocs[i]));
			}
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Initial results are not enough.");
		}
		
		return relatedDocVectors;
		
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
	
	public static int getNumOfRelevant() {
		return NUM_OF_RELEVANT;
	}

}
