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

public class Application {
	
	   String indexDir = "./index";
	   String dataDir = "./data";
	   Indexer indexer;
	   Searcher searcher;
	   Scanner scan = new Scanner(System.in);
       Scanner command = new Scanner(System.in);
	   
	public Application() {
		// TODO Auto-generated constructor stub
		
	}
	
	public void run() throws ParseException{
		
		 try {
			 
	    	 System.out.println("Enter search query: ");
	    	 
	    	 String query = scan.nextLine();
	         
	         File index = new File(indexDir);
	         File[] indexes = index.listFiles();
	         
	         if(indexes.length == 0){
	             System.out.println("Started indexing...");
	             createIndex();
	         }
	         
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
	    	 search(query);
	         
	         
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (NegativeArraySizeException ex){
	    	  System.err.println("Cannot query empty string.");
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
	
	 public void search(String searchQuery) throws IOException, ParseException {
	      searcher = new Searcher(indexDir);
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
