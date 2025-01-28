package org.test.ia.mariadbtest;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private static String MARIADB_PDF_URL =
          "https://mariadb.org/wp-content/uploads/2025/01/MariaDBServerKnowledgeBase.pdf";

  @Autowired
  private VectorStore vectorStore;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    // only load the first time
    boolean loaded = false;
    try {
      int embeddings = jdbcTemplate.queryForObject("select count(*) from vector_store", Integer.class);
      if (embeddings > 0) loaded = true;
    } catch (DataAccessException e) { }

    if (!loaded) {
      // read and transform pdf into Documents
      Resource pdf = UrlResource.from(MARIADB_PDF_URL);
      var reader = new PagePdfDocumentReader(pdf);
      var splitter = new TokenTextSplitter();

      // change markdown content to html
      List<Document> documents = splitter.apply(reader.get());
      Parser parser = Parser.builder().build();
      List<Document> htmlDocs = documents.stream().map(d -> {
        Node document = parser.parse(d.getText());
        String render = HtmlRenderer.builder().build().render(document);
        return new Document(render, d.getMetadata());
      }).collect(Collectors.toUnmodifiableList());

      // save documents into vector store
      vectorStore.add(htmlDocs);
    }
  }
}
