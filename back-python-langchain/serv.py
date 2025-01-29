import os
from typing import TypedDict, List
import json

from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_mariadb import MariaDBStore
import mariadb
import asyncio
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import RunnablePassthrough
from websockets import ConnectionClosedOK
from websockets.asyncio.server import serve
import markdown

conn_string = os.environ.get("MARIADB_URL", "mariadb+mariadbconnector://root:@localhost/langchain")

vectorstore = MariaDBStore(
    embeddings=OpenAIEmbeddings(model="text-embedding-3-small"),
    embedding_length=1536,
    datasource=conn_string,
    collection_name="my_docs"
)
retriever = vectorstore.as_retriever()
llm = ChatOpenAI(model="gpt-4o-mini")

from langchain.prompts import ChatPromptTemplate

template = """Answer the question based only on the following context:
{context}
 
Question: {question}
"""

prompt = ChatPromptTemplate.from_template(template)

rag_chain = (
        {"context": retriever, "question": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
)

async def handler(websocket):
    await websocket.send(json.dumps({"html":"How can I help ?"}))
    while True:
        try:
            message = await websocket.recv()
        except ConnectionClosedOK:
            break
        js = json.loads(message)
        question = js["messages"][0]["text"]

        response = rag_chain.invoke(question)

        # response is in markdown, transform it to html
        htmlresponse = markdown.markdown(response, extensions=['fenced_code', 'codehilite'])

        await websocket.send(json.dumps({"html":htmlresponse}))

async def main():
    async with serve(handler, "localhost", 80) as a:
        await asyncio.get_running_loop().create_future()  # run forever

asyncio.run(main())