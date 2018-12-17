package index;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.utils.*;
import org.apache.lucene.benchmark.quality.trec.*;
 
/* This code was extracted from the Lucene
   contrib/benchmark sources */
 
public class PrecisionRecall {
	
	private File topicsFile;
	private File qrelsFile;
	private TopDocs results;
	private double precision;
	private double recall;
	
	public PrecisionRecall() {
		// TODO Auto-generated constructor stub
		this.topicsFile = new File("./topics.txt");
		this.qrelsFile = new File("./qrels.txt");
	}
	
	public double[] calculatePrecisionRecall(){
		double[] precisionRecall = new double[3];
		
		int numOfFound = results.totalHits;
		int topKResults = results.scoreDocs.length;
		int relevantResults = Rocchio.getNumOfRelevant(); //true positives
		
		double falsePositives = numOfFound - relevantResults;
		double falseNegatives = topKResults - relevantResults;
		double falseNegatives2 = numOfFound - topKResults;
		
		double precision = relevantResults / (relevantResults + falsePositives);
		double recall = relevantResults / (relevantResults + falseNegatives);
		double recall2 = topKResults / (relevantResults + falseNegatives2);
		
		precisionRecall[0] = precision;
		precisionRecall[1] = recall;
		precisionRecall[2] = recall2;
		
		return precisionRecall;
		
	}

	public TopDocs getResults() {
		return results;
	}

	public void setResults(TopDocs results) {
		this.results = results;
	}
	
	
}
