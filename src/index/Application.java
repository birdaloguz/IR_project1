package index;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Application {
	
	private Indexer indexer;
	private Searcher searcher;
	private String indexDir;
	private String dataDir;
	
	public Application(Indexer indexer, Searcher searcher, String indexDir, String dataDir) {
		// TODO Auto-generated constructor stub
		this.dataDir = dataDir;
		this.indexDir = indexDir;
		this.indexer = indexer;
		this.searcher = searcher;
	}
	
	public void run() throws ParseException{
		
		 try {
			 
	    	 System.out.println("Enter search query: ");
	    	 Scanner scan = new Scanner(System.in);
	    	 String query = scan.nextLine();
	         
	         File index = new File(indexDir);
	         File[] indexes = index.listFiles();
	         
	         if(indexes.length == 0){
	             System.out.println("Started indexing...");
	             createIndex();
	         }
	         
	         Suggestion suggest = new Suggestion("./dictionary");
	         Scanner command = new Scanner(System.in);
	         
	         if(!suggest.exist(query)){
	             String[] similarWord = suggest.suggest(query);
	             
	             if(similarWord.length > 0){
	            	 System.out.println("Did you mean: " + Arrays.asList(similarWord).toString() + ". If yes type Y and hit enter, otherwise just hit enter.");
	            	 
	            	 String cmd = command.nextLine();
	                 
	                 if(cmd.equalsIgnoreCase("Y")){
	                	 query = similarWord[0];
//	                	 main.search(query);
	                 } else {
//	                	 suggest.addToDictionary(query);
//	                   main.searchFuzzy(query);
	                 }
	             }
	        	 
	         } 
	    	 search(query);
	         
//	         scan.close();
//	         command.close();
	         
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	}
	
	public void createIndex() throws IOException {
//		indexer = new Indexer(indexDir);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir);
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
	}
	
	 public void search(String searchQuery) throws IOException, ParseException {
//	      searcher = new Searcher(indexDir);
	      long startTime = System.currentTimeMillis();
	      TopDocs hits = searcher.search(searchQuery);
	      long endTime = System.currentTimeMillis();
	   
	      System.out.println(hits.totalHits +
	         " documents found. Time :" + (endTime - startTime));
	      for(ScoreDoc scoreDoc : hits.scoreDocs) {
	         Document doc = searcher.getDocument(scoreDoc);
	            System.out.println("File: "
	            + doc.get(Config.FILE_PATH) + " Query: " + searchQuery);
	      }
	      searcher.close();
	   }

}
