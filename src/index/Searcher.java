package index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

	private IndexSearcher indexSearcher;
	private QueryParser queryParser;
	private Query query;

	public Searcher(String indexDir) throws IOException {
		// TODO Auto-generated constructor stub
		Directory indexDirectory = FSDirectory.open(new File(indexDir));
		
		IndexReader reader = IndexReader.open(indexDirectory);
		
		indexSearcher = new IndexSearcher(reader);

		queryParser = new QueryParser(Version.LUCENE_36, Config.CONTENTS, new StandardAnalyzer(Version.LUCENE_36));
	}

	public TopDocs search(String searchQuery) throws IOException, ParseException {
		query = queryParser.parse(searchQuery);
		return indexSearcher.search(query, Config.MAX_SEARCH);
	}

	public TopDocs search(Query query) throws IOException {
		return indexSearcher.search(query, Config.MAX_SEARCH);
	}

	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		return indexSearcher.doc(scoreDoc.doc);
	}

	public void close() throws IOException {
		indexSearcher.close();
	}
}
