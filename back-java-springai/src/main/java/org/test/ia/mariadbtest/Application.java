package org.test.ia.mariadbtest;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.util.List;

@SpringBootApplication
@ConfigurationProperties("spring.ai.data")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private static String MARIADB_PDF_URL =
          "https://mariadb.org/wp-content/uploads/2025/01/MariaDBServerKnowledgeBase.pdf";

  @Autowired
  private VectorStore vectorStore;

  private Boolean initializeStore;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    if (initializeStore) {
      // read and transform pdf into Documents
      Resource pdf = UrlResource.from(MARIADB_PDF_URL);
      var reader = new PagePdfDocumentReader(pdf);
      var splitter = new TokenTextSplitter();
      List<Document> documents = splitter.apply(reader.get());

      // save documents into vector store
      vectorStore.add(documents);
    }
  }
}
