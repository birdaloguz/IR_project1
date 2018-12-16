package index;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.apache.lucene.queryParser.ParseException;

public class Main {
	

   
   public static void main(String[] args) throws ParseException, IOException {
	   
	   Application app = new Application();
	   
	   Scanner scan = new Scanner(System.in);
	   int flag = 1;
			   
	   while(flag == 1){
		   app.run();
		   System.out.println("Would you like to continue? Press 0 for termination. Otherwise press 1.");
		   try{
			   flag = scan.nextInt();
			   if(flag == 0){
				   System.out.println("Terminated.");
				   break;
			   } else if(flag == 1){
				   System.out.println("Moving on...");
			   }
		   } catch(InputMismatchException ex){
			   System.out.println("Input is wrong. Changing it to 1.");
			   scan.next();
		   }
		   
	   }
	   
	   scan.close();
	   app.close();
   }
}
	   
