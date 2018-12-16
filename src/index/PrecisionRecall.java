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
 
// From appendix C
 
/* This code was extracted from the Lucene
   contrib/benchmark sources */
 
public class PrecisionRecall {
	
	private Searcher searcher;
	private File topicsFile;
	private File qrelsFile;
	
	public PrecisionRecall(Searcher searcher) {
		// TODO Auto-generated constructor stub
		this.searcher = searcher;
		this.topicsFile = new File("./topics.txt");
		this.qrelsFile = new File("./qrels.txt");
	}
	
	public void calculatePrecisionRecall() throws Exception{
		IndexSearcher searcher = this.searcher.getSearcher();
		
		String documentNameField = "filename";
		
		PrintWriter logger = new PrintWriter(System.out, true);
		
		TrecTopicsReader qReader = new TrecTopicsReader();
		QualityQuery[] qqs = qReader.readQueries(new BufferedReader(new FileReader(topicsFile)));
		Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelsFile)));
		
		judge.validateData(qqs,  logger);
		
		QualityQueryParser qqParser = new SimpleQQParser("title", "contents");
		QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, documentNameField);
		SubmissionReport submitLog = null;
		QualityStats[] stats = qrun.execute(judge, submitLog, logger);
		QualityStats avg = QualityStats.average(stats);
		avg.log("SUMMARY", 2, logger, " ");
		
	}
	
}
 
/*
#1 Read TREC topics as QualityQuery[]
#2 Create Judge from TREC Qrel file
#3 Verify query and Judge match
#4 Create parser to translate queries into Lucene queries
#5 Run benchmark
#6 Print precision and recall measures
*/