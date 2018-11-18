package index;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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

	public int createIndex(String directoryName) throws IOException {
		File candidateFile = new File(directoryName);

		// Get all files from a directory.
		if (candidateFile.isDirectory()) {
			Path path = Paths.get(candidateFile.getAbsolutePath());
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					try {
						File fileToIndex = file.toFile();
						index(fileToIndex);
					} catch (IOException ex) {
						ex.printStackTrace();
					}

					return FileVisitResult.CONTINUE;
				}

			});
		} else {
			index(candidateFile);
		}

		return indexWriter.numDocs();
	}

	public void commit() throws IOException {
		indexWriter.commit();
	}

	public void index(File file) throws IOException {
		Document doc = getDocument(file);
		indexWriter.addDocument(doc);
//		indexWriter.commit();
	}

	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}

}
