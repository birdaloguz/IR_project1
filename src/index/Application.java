package index;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Application{
	
	   private String indexDir = "./index";
	   private String dataDir = "./data";
	   private Indexer indexer;
	   private Searcher searcher;
	   private Scanner scan = new Scanner(System.in);
	   private Scanner command = new Scanner(System.in);
	   
	public Application(){
		// TODO Auto-generated constructor stub
		
		
	}
	
	public void run() throws ParseException, IOException{
//		indexer = new Indexer(indexDir);
		 try {
			 
	    	 System.out.println("Enter search query: ");
	    	 
	    	 String query = scan.nextLine();
	         
	         File index = new File(indexDir);
	         if(!index.exists()){
	        	 index.mkdir();
	         }
	         File[] indexes = index.listFiles();
	         
	         if(indexes.length == 0){
	             System.out.println("Started indexing...");
	             createIndex();
	         }
//	         searcher = new Searcher(indexDir);
	         Suggestion suggest = new Suggestion("./dictionary");
	         
	         if(!suggest.exist(query)){
	             String[] similarWord = suggest.suggest(query);
	             
	             if(similarWord.length > 0){
	            	 System.out.println("Did you mean: " + Arrays.asList(similarWord).toString() + ". If yes type Y and hit enter, otherwise just hit enter.");
	            	 
	            	 String cmd = command.nextLine();
	                 
	                 if(cmd.equalsIgnoreCase("Y")){
	                	 query = similarWord[0];
	                 } 
	             }
	        	 
	         } 
	         
	         searcher = new Searcher(indexDir);
	    	 
	    	 //Search without Rocchio
	         System.out.println("Initial results: \n");
	    	 TopDocs totalHits = search(query);
	    	 PrecisionRecall beforeRocchio = new PrecisionRecall();
	    	 beforeRocchio.setResults(totalHits);
	    	 beforeRocchio.calculatePrecisionRecall();
	    	 
	    	 double[] precisionRecallBeforeRocchio = beforeRocchio.calculatePrecisionRecall();
	         System.out.println("Precision: " + precisionRecallBeforeRocchio[2] + "\nRecall: " + precisionRecallBeforeRocchio[1]);
	         
	    	 //Search with query expansion
	    	 Rocchio r = new Rocchio(0.5f, 0.5f, searcher);
	    	 System.out.println("Results after Rocchio: \n");
	    	 TopDocs hitsAfterRocchio = search(r.expandQuery(query).toString("contents"));
	    	 
	    	 PrecisionRecall quality = new PrecisionRecall();
	    	 quality.setResults(hitsAfterRocchio);
	    	 
	    	 double[] precisionRecall = quality.calculatePrecisionRecall();
	         System.out.println("Precision: " + precisionRecall[0] + "\nRecall: " + precisionRecall[1]);
	         
	      } catch (Exception e) {
	         System.err.println("Error while processing query.");
	      }
	}
	
	public void close(){
		scan.close();
        command.close();
	}
	
	public void createIndex() throws IOException {
		indexer = new Indexer(indexDir);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir);
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
	}
	
	 public TopDocs search(String searchQuery) throws IOException, ParseException {
//	      searcher = new Searcher(indexDir);
	      long startTime = System.currentTimeMillis();
	      TopDocs hits = searcher.search(searchQuery);
	      long endTime = System.currentTimeMillis();
	   
	      System.out.println(hits.totalHits +
	         " documents found. Time :" + (endTime - startTime) + " ms");
	      for(ScoreDoc scoreDoc : hits.scoreDocs) {
	         Document doc = searcher.getDocument(scoreDoc);
	            System.out.println("File: "
	            + doc.get(Config.FILE_PATH) + " Query: " + searchQuery);
	      }
	      searcher.close();
	      return hits;
	   }
	/*
	 * CURRENTLY NOT USED
	 */
	public void searchFuzzy(String searchQuery) throws IOException { 
		searcher = new Searcher(indexDir);

		long startTime = System.currentTimeMillis();
		Term term = new Term(Config.CONTENTS, searchQuery);
		Query query = new FuzzyQuery(term);

		TopDocs hits = searcher.search(query);
		long endTime = System.currentTimeMillis();

		System.out.println(hits.totalHits + " docs found. Time: " + (endTime - startTime) + " ms");

		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.getDocument(scoreDoc);
			System.out.println("File: " + doc.get(Config.FILE_PATH) + " Query: " + searchQuery);
		}
	}

}
