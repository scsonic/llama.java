import os


from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import CharacterTextSplitter
from langchain_community.vectorstores import FAISS

print("loadding embeding")
from langchain_community.embeddings import HuggingFaceEmbeddings
model_name = "sentence-transformers/all-MiniLM-L6-v2"
embedding = HuggingFaceEmbeddings(model_name=model_name,)
print("end loading embeding");

# Load the document, split it into chunks, embed each chunk and load it into the vector store.
raw_documents = TextLoader('UNOlite_user_manual_eng.txt').load()
text_splitter = CharacterTextSplitter(chunk_size=256, chunk_overlap=0)
print("return type=", type(text_splitter))

print("split doc")
documents = text_splitter.split_documents(raw_documents)
print("put in FAISS")
db = FAISS.from_documents(documents, embedding)
print("FAISS END")

from langchain.callbacks.manager import CallbackManager
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler
from langchain_community.llms import LlamaCpp
model_path = "/sdcard/Download/rocket-3b.Q4_0.gguf"

print("load llm")
llm = LlamaCpp(
    model_path=model_path,
    n_gpu_layers=0,
    n_ctx=2048,
    f16_kv=True,
    callback_manager=CallbackManager([StreamingStdOutCallbackHandler()]),
    verbose=True,
)

print("llm loaded")


from langchain.globals import set_verbose, set_debug
set_debug(True)
set_verbose(True)
from langchain.chains import RetrievalQA

retriever = db.as_retriever(search_kwargs={"k":6})

qa = RetrievalQA.from_chain_type(
    llm=llm,
    chain_type="stuff",
    retriever=retriever,
    verbose=True,
)


while True:
    print("user input:")
    query = input()
    response = qa.invoke(query)

