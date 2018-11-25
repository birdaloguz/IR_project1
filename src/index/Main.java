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

public class Main {
	
   String indexDir = "./index";
   String dataDir = "./data";
   Indexer indexer;
   Searcher searcher;
   
   public static void main(String[] args) throws ParseException {
      Main main;
      try {
    	 System.out.println("Enter search query: ");
    	 Scanner scan = new Scanner(System.in);
    	 String query = scan.nextLine();
    	 
         main = new Main();
         
         File index = new File(main.indexDir);
         File[] indexes = index.listFiles();
         
         if(indexes.length == 0){
             System.out.println("Started indexing...");
             main.createIndex();
         }
         
         Suggestion suggest = new Suggestion("./dictionary");
         Scanner command = new Scanner(System.in);
         
         if(!suggest.exist(query)){
             String[] similarWord = suggest.suggest(query);
        	 System.out.println("Did you mean: " + similarWord[0] + ". If yes type Y and hit enter, other wise just hit enter.");
        	 
        	 String cmd = command.nextLine();
             
             if(cmd.equals("Y")){
            	 query = similarWord[0];
             } else {
//            	 suggest.addToDictionary(query);
             }
         }
         
    	 main.search(query);

//         main.searchFuzzy(query);
         
         scan.close();
         
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void createIndex() throws IOException {
      indexer = new Indexer(indexDir);
      int numIndexed;
      long startTime = System.currentTimeMillis();	
      numIndexed = indexer.createIndex(dataDir);
      long endTime = System.currentTimeMillis();
      indexer.close();
      System.out.println(numIndexed+" File indexed, time taken: "
         +(endTime-startTime)+" ms");		
   }

   private void search(String searchQuery) throws IOException, ParseException {
      searcher = new Searcher(indexDir);
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
   
   private void searchFuzzy(String searchQuery) throws IOException{
	   searcher = new Searcher(indexDir);
	   
	   long startTime = System.currentTimeMillis();
	   Term term = new Term(Config.CONTENTS, searchQuery);
	   Query query = new FuzzyQuery(term);
	   
	   TopDocs hits = searcher.search(query);
	   long endTime = System.currentTimeMillis();
	   
	   System.out.println(hits.totalHits + " docs found. Time: " + (endTime - startTime) + " ms" );
	   
	   for(ScoreDoc scoreDoc : hits.scoreDocs){
		   Document doc = searcher.getDocument(scoreDoc);
		   System.out.println("File: " + doc.get(Config.FILE_PATH) + " Query: " + searchQuery);
	   }
   }
}
