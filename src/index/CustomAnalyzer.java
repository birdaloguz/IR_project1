package index;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class CustomAnalyzer extends Analyzer{

	@Override
	public TokenStream tokenStream(String field, Reader reader) {
		// TODO Auto-generated method stub
		StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_36, reader);
		TokenStream result = new StandardFilter(tokenizer);
		result = new LowerCaseFilter(result);
		result = new PorterStemFilter(result);
		result = new StopFilter(Version.LUCENE_36, result, StandardAnalyzer.STOP_WORDS_SET);
		
		return result;
	}

}
