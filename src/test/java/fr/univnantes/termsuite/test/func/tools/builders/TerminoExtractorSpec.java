package fr.univnantes.termsuite.test.func.tools.builders;


import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.Document;
import fr.univnantes.termsuite.api.TerminoExtractor;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoExtractorSpec {
	
	Lang lang;
	List<Document> documents;
	Document document1;
	Document document2;
	
	
	@Before
	public void setup() {
		lang = Lang.FR;
		documents = Lists.newArrayList();
		document1 = new Document(lang, "url1", "L'énergie éolienne est l'énergie de demain.");
		documents.add(document1);
		document2 = new Document(lang, "url2", "Une éolienne donne de l'énergie.");
		documents.add(document2);
	}


	@Test
	public void fromPreprocessedJsonFiles() {
		String jsonDirPath = Paths.get(FunctionalTests.CORPUS2_PATH.toString(), "json").toString();
		TermIndex termIndex = TerminoExtractor
				.fromPreprocessedJsonFiles(Lang.FR, jsonDirPath)
				.setTreeTaggerHome(FunctionalTests.getTaggerPath())
				.execute();
		assertTermIndex(termIndex);
	}


	@Test
	public void fromPreprocessedXmiFiles() {
		String jsonDirPath = Paths.get(FunctionalTests.CORPUS2_PATH.toString(), "xmi").toString();
		TermIndex termIndex = TerminoExtractor
				.fromPreprocessedXmiFiles(Lang.FR, jsonDirPath)
				.setTreeTaggerHome(FunctionalTests.getTaggerPath())
				.execute();
		assertTermIndex(termIndex);
	}


	@Test
	public void fromTxtCorpus() {
		TermIndex termIndex = TerminoExtractor
				.fromTxtCorpus(Lang.FR, FunctionalTests.CORPUS1_PATH.toString(), "**/*.txt", "UTF-8")
				.setTreeTaggerHome(FunctionalTests.getTaggerPath())
				.execute();
		
		assertTermIndex(termIndex);
	}


	private void assertTermIndex(TermIndex termIndex) {
		
		assertThat(termIndex)
			.hasNTerms(3)
			.containsTerm("na: énergie éolien", 2)
			.containsTerm("npn: énergie du futur", 1)
			.containsTerm("npn: énergie de demain", 1)
			;
	}

	@Test
	public void fromCustomDocumentStream() {
		TermIndex termIndex = TerminoExtractor.fromDocumentStream(Lang.FR, documents.stream(), 2)
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
			.execute();
		
		assertThat(termIndex)
			.hasNTerms(4)
			.containsTerm("n: éolienne", 1)
			.containsTerm("n: énergie", 3)
			.containsTerm("na: énergie éolien", 1)
			.containsTerm("npn: énergie de demain", 1)
			;
	}

}