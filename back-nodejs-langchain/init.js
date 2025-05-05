import { MariaDBStore } from "@langchain/community/vectorstores/mariadb";
import { OpenAIEmbeddings } from "@langchain/openai";
import { PDFLoader } from "@langchain/community/document_loaders/fs/pdf";
import { RecursiveCharacterTextSplitter } from "@langchain/textsplitters";
import mariadb from "mariadb";
import markdownit from 'markdown-it'

async function createData() {

    const loader = new PDFLoader("C:/temp/MariaDBServerKnowledgeBase.pdf");
    const docs = await loader.load();
    const textSplitter = RecursiveCharacterTextSplitter.fromLanguage("markdown", {
        chunkSize: 1000,
        chunkOverlap: 400
    });

    const splitDocs = await textSplitter.splitDocuments(docs);
    const md = markdownit({
        html: true,
        linkify: true,
        typographer: true
    });

    splitDocs.map(doc => doc.pageContent = md.parse(doc.pageContent, {}))

    const vectorStore = await MariaDBStore.initialize(
        new OpenAIEmbeddings(),
        {
            pool: mariadb.createPool("mariadb://root@localhost:3306/langchainjs"),
            distanceStrategy: 'EUCLIDEAN',
        }
    );
    await vectorStore.addDocuments(splitDocs);
    process.exit()
}
createData();
