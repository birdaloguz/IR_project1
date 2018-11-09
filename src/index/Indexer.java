package index;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {

	private IndexWriter indexWriter;

	public Indexer(String indexDir) throws IOException {
		// TODO Auto-generated constructor stub
		Directory indexDirectory = FSDirectory.open(new File(indexDir));

		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_36,
				new StandardAnalyzer(Version.LUCENE_36));

		indexWriter = new IndexWriter(indexDirectory, writerConfig);
	}

	public Document getDocument(File file) throws FileNotFoundException {

		Document document = new Document();

		Field content = new Field(Config.CONTENTS, new FileReader(file));

		Field fileName = new Field(Config.FILE_NAME, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED);

		Field filePath = new Field(Config.FILE_PATH, file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED);

		document.add(content);
		document.add(fileName);
		document.add(filePath);

		return document;

	}

	public int createIndex(String dataDirPath) throws IOException {
		// get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();

		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
				index(file);
			}
		}
		return indexWriter.numDocs();
	}

	public void index(File file) throws IOException {
		Document doc = getDocument(file);
		indexWriter.addDocument(doc);
	}

	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}

}
