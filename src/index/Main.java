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
	
  
   
   public static void main(String[] args) throws ParseException, IOException {
	   
	   String indexDir = "./index";
	   String dataDir = "./data";
	   Indexer indexer = new Indexer(indexDir);
	   Searcher searcher = new Searcher(indexDir);
	   
	   Application app = new Application(indexer, searcher, indexDir, dataDir);
	   
	   Scanner scan = new Scanner(System.in);
	   int flag = 0;
			   
	   while(flag != -1){
		   app.run();
		   System.out.println("Would you like to continue? Press -1 for termination. Otherwise press 1.");
		   flag = scan.nextInt();
		   if(flag == -1){
			   System.out.println("Terminated.");
			   break;
		   }
	   }
	   
	   
//      Main main;
//      try {
//    	 System.out.println("Enter search query: ");
//    	 Scanner scan = new Scanner(System.in);
//    	 String query = scan.nextLine();
//    	 
//         main = new Main();
//         
//         File index = new File(main.indexDir);
//         File[] indexes = index.listFiles();
//         
//         if(indexes.length == 0){
//             System.out.println("Started indexing...");
//             main.createIndex();
//         }
//         
//         Suggestion suggest = new Suggestion("./dictionary");
//         Scanner command = new Scanner(System.in);
//         
//         if(!suggest.exist(query)){
//             String[] similarWord = suggest.suggest(query);
//             
//             if(similarWord.length > 0){
//            	 System.out.println("Did you mean: " + Arrays.asList(similarWord).toString() + ". If yes type Y and hit enter, otherwise just hit enter.");
//            	 
//            	 String cmd = command.nextLine();
//                 
//                 if(cmd.equalsIgnoreCase("Y")){
//                	 query = similarWord[0];
////                	 main.search(query);
//                 } else {
////                	 suggest.addToDictionary(query);
////                   main.searchFuzzy(query);
//                 }
//             }
//        	 
//         } 
//    	 main.search(query);
//         
//         scan.close();
//         
//      } catch (IOException e) {
//         e.printStackTrace();
//      }
   }

//   private void createIndex() throws IOException {
//      indexer = new Indexer(indexDir);
//      int numIndexed;
//      long startTime = System.currentTimeMillis();	
//      numIndexed = indexer.createIndex(dataDir);
//      long endTime = System.currentTimeMillis();
//      indexer.close();
//      System.out.println(numIndexed+" File indexed, time taken: "
//         +(endTime-startTime)+" ms");		
//   }
//
//   private void search(String searchQuery) throws IOException, ParseException {
//      searcher = new Searcher(indexDir);
//      long startTime = System.currentTimeMillis();
//      TopDocs hits = searcher.search(searchQuery);
//      long endTime = System.currentTimeMillis();
//   
//      System.out.println(hits.totalHits +
//         " documents found. Time :" + (endTime - startTime));
//      for(ScoreDoc scoreDoc : hits.scoreDocs) {
//         Document doc = searcher.getDocument(scoreDoc);
//            System.out.println("File: "
//            + doc.get(Config.FILE_PATH) + " Query: " + searchQuery);
//      }
//      searcher.close();
//   }
//   
//   private void searchFuzzy(String searchQuery) throws IOException{
//	   searcher = new Searcher(indexDir);
//	   
//	   long startTime = System.currentTimeMillis();
//	   Term term = new Term(Config.CONTENTS, searchQuery);
//	   Query query = new FuzzyQuery(term);
//	   
//	   TopDocs hits = searcher.search(query);
//	   long endTime = System.currentTimeMillis();
//	   
//	   System.out.println(hits.totalHits + " docs found. Time: " + (endTime - startTime) + " ms" );
//	   
//	   for(ScoreDoc scoreDoc : hits.scoreDocs){
//		   Document doc = searcher.getDocument(scoreDoc);
//		   System.out.println("File: " + doc.get(Config.FILE_PATH) + " Query: " + searchQuery);
//	   }
//   }
}
