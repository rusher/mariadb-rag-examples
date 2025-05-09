package org.test.ia.mariadbtest;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private static String MARIADB_PDF_URL =
          "https://mariadb.org/wp-content/uploads/2025/03/MariaDBServerKnowledgeBase.pdf";

  @Autowired
  private VectorStore vectorStore;

  @Autowired
  private ConfigProperties config;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    if (config.getInitializeStore()) {
      // read and transform pdf into Documents
      Resource pdf = UrlResource.from(MARIADB_PDF_URL);
      var reader = new PagePdfDocumentReader(pdf);
      var splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
      List<Document> documents = splitter.apply(reader.get());

      System.out.println("saving ...");

      // save documents into vector store
      vectorStore.add(documents);

    }
  }
}
