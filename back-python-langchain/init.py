
from langchain_community.document_loaders import PyPDFLoader
from langchain_mariadb import MariaDBStore
from langchain_openai import OpenAIEmbeddings
import mariadb
from langchain_text_splitters import RecursiveCharacterTextSplitter
import os

# load PDF into documents
file_path = ("c:/temp/MariaDBServerKnowledgeBase.pdf")
loader = PyPDFLoader(file_path)
pages = []
for page in loader.lazy_load():
    pages.append(page)

text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
all_splits = text_splitter.split_documents(pages)

# configure MariaDB vector store, and save documents
embeddings = OpenAIEmbeddings(model="text-embedding-3-small")
conn_string = os.environ.get("MARIADB_URL", "mariadb+mariadbconnector://root:@localhost/langchain")

vectorstore = MariaDBStore(
    embeddings=embeddings,
    embedding_length=1536,
    datasource= conn_string,
    collection_name="my_docs"
)

vectorstore.add_documents(all_splits)
