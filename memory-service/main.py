from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
import uuid, datetime
import os
import json
import numpy as np

app = FastAPI()

os.makedirs("./chroma_data", exist_ok=True)
collection_name = os.environ.get("USER_COLLECTION_NAME", "user_memory")
db_path = f"./chroma_data/{collection_name}.json"

encoder = SentenceTransformer('all-MiniLM-L6-v2')

class MemoryEntry(BaseModel):
    text: str
    type: str = "journal"
    metadata: dict = {}

class QueryRequest(BaseModel):
    query: str
    n_results: int = 3

def load_db():
    if os.path.exists(db_path):
        with open(db_path, "r") as f:
            return json.load(f)
    return {"embeddings": [], "documents": [], "metadatas": [], "ids": []}

def save_db(db):
    with open(db_path, "w") as f:
        json.dump(db, f)

@app.post("/memory/store")
def store(entry: MemoryEntry):
    embedding = encoder.encode(entry.text).tolist()
    db = load_db()
    
    db["embeddings"].append(embedding)
    db["documents"].append(entry.text)
    db["metadatas"].append({**entry.metadata, "type": entry.type, "date": str(datetime.date.today())})
    db["ids"].append(str(uuid.uuid4()))
    
    save_db(db)
    return {"status": "stored"}

def cosine_similarity(a, b):
    a = np.array(a)
    b = np.array(b)
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b) + 1e-10)

@app.post("/memory/retrieve")
def retrieve(req: QueryRequest):
    db = load_db()
    if not db["embeddings"]:
        return {"memories": []}
        
    query_embedding = encoder.encode(req.query).tolist()
    
    similarities = []
    for idx, emb in enumerate(db["embeddings"]):
        sim = cosine_similarity(query_embedding, emb)
        similarities.append((sim, db["documents"][idx]))
        
    similarities.sort(key=lambda x: x[0], reverse=True)
    top_results = [doc for _, doc in similarities[:req.n_results]]
    
    return {"memories": top_results[0] if top_results else []}
