package index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Suggestion {

	private SpellChecker spellChecker;

	public Suggestion(String indexDir) throws IOException {
		// TODO Auto-generated constructor stub
		Directory directory = FSDirectory.open(new File(indexDir));
		spellChecker = new SpellChecker(directory);

		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_36,
				new StandardAnalyzer(Version.LUCENE_36));

//		IndexReader reader = IndexReader.open(directory);

		spellChecker.setStringDistance(new LevensteinDistance());
		
		spellChecker.indexDictionary(new PlainTextDictionary(new File("./words.txt")), writerConfig, true);
	}

	public String[] suggest(String query) throws IOException {
		
		String[] suggestions = spellChecker.suggestSimilar(query, 1);
		
		return suggestions;
	}

	public boolean exist(String query) throws IOException {
		
		if (spellChecker.exist(query)) {
			return true;
		} else {
			return false;
		}
	}

	public void addToDictionary(String query) {
		try {
			FileWriter writer = new FileWriter("./words.txt", true);
			writer.write("\n" + query + "\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
