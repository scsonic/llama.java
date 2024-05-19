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
raw_documents = TextLoader('eos1100.txt').load()
text_splitter = CharacterTextSplitter(chunk_size=256, chunk_overlap=0)
print("return type=", type(text_splitter))
documents = text_splitter.split_documents(raw_documents)

VECTOR_DB_DIR = "faiss_index"
if os.path.exists(VECTOR_DB_DIR):
  print("load exist Vector DB")
  db = FAISS.load_local(VECTOR_DB_DIR, embedding, allow_dangerous_deserialization=True)
else:
  print("create again, put into FAISS")
  db = FAISS.from_documents(documents, embedding)
  print("FAISS END")
  db.save_local(VECTOR_DB_DIR)


from langchain.callbacks.manager import CallbackManager
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler

from langchain.globals import set_verbose, set_debug
set_debug(True)
set_verbose(True)
from langchain.chains import RetrievalQA

retriever = db.as_retriever(search_kwargs={"k":6})
docs = db.similarity_search("sdcard")
print(docs)

from flask import Flask, request, jsonify

app = Flask(__name__)

# 用于存储上传的 JSON 数据
uploaded_data = {}

@app.route('/upload', methods=['POST'])
def upload_txt():
    data = request.data.decode('utf-8')  # 获取请求的原始正文，并将字节流解码为字符串
    print("@@", request.data)
    if not data:
        return jsonify({'error': 'No data provided in request body'}), 400
    # uploaded_data['text'] = data  # 存储整个请求正文

    # Load the document, split it into chunks, embed each chunk and load it into the vector store.
    ts = CharacterTextSplitter(chunk_size=256, chunk_overlap=0)
    sdata = str(data)
    print("@@ need print the type of data", type(data), sdata)
    arr = [x for x in ts.split_text(sdata)]
    db.add_texts(arr)
    return data

# 查询并返回 JSON 数据
@app.route('/query', methods=['GET'])
def query_json():
    query_string = request.args.get('query')
    if query_string is None:
        return jsonify({'error': 'No query provided'}), 400
    obj = db.similarity_search(query_string)
    print(obj)

    arr = []
    for doc in obj:
      arr.append(doc.page_content)
    result = {"docs": arr}
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=10080)
