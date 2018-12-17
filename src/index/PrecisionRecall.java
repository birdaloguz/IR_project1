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
	
	private TopDocs results;
	private double precision;
	private double recall;
	
	public PrecisionRecall() {
		// TODO Auto-generated constructor stub
	}
	
	public double[] calculatePrecisionRecall(){
		double[] precisionRecall = new double[3];
		
		int numOfFound = results.totalHits;
		int topKResults = results.scoreDocs.length; // true positives without rocchio algorithm
		int relevantResults = Rocchio.getNumOfRelevant(); //true positives for rocchio algorithm
		
		double falsePositives = numOfFound - relevantResults;
		double falsePositives2 = numOfFound - topKResults;
		double falseNegatives =  Math.round((103256 - numOfFound)*5/100);
		
		double precision = relevantResults / (relevantResults + falsePositives);
		double precision2 = topKResults / (topKResults + falsePositives2);
		double recall = relevantResults / (relevantResults + falseNegatives);
		
		precisionRecall[0] = precision;
		precisionRecall[1] = recall;
		precisionRecall[2] = precision2;
		
		return precisionRecall;
		
	}

	public TopDocs getResults() {
		return results;
	}

	public void setResults(TopDocs results) {
		this.results = results;
	}
	
}
