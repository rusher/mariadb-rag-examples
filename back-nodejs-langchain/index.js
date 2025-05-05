import {MariaDBStore} from "@langchain/community/vectorstores/mariadb";
import {OpenAIEmbeddings} from "@langchain/openai";
import mariadb from "mariadb";
import { ChatOpenAI } from "@langchain/openai";
import { WebSocketServer } from 'ws';

async function search() {
    const vectorStore = await MariaDBStore.initialize(
        new OpenAIEmbeddings(),
        {
            pool: mariadb.createPool("mariadb://root@localhost:3306/langchainjs"),
            distanceStrategy: 'EUCLIDEAN',
        }
    );

    const wss = new WebSocketServer({ port: 80 });
    wss.on('connection', function connection(ws) {
        ws.on('error', console.error);

        ws.on('message', async function message(data) {
            const question = JSON.parse(data.toString('utf8')).messages[0].text;
            const llm = new ChatOpenAI({temperature: 0});
            const retrievedDocs = await vectorStore.similaritySearch(question);
            const docsContent = retrievedDocs.map((doc) => doc.pageContent).join("\n");

            const answer = await llm.invoke(
                `You are an assistant for question-answering tasks. 
                Use the following pieces of retrieved context to answer the question. 
                If you don't know the answer, just say that you don't know. 
                Question: ${question}
            Context: ${docsContent}
            Answer:`);
            ws.send(JSON.stringify({html:answer.content}));
        });

        ws.send('{"html": "How can I help ?"}');
    });
}
search();
