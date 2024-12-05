from fastapi import FastAPI, File, UploadFile, Body
from fastapi.responses import StreamingResponse
from typing import List
import shutil
import os
from chat import RagEnsemble
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # React 앱의 URL을 허용할 수도 있습니다.
    allow_credentials=True,
    allow_methods=["*"],  # 모든 메서드 (GET, POST 등 허용)
    allow_headers=["*"],  # 모든 헤더 허용
)

# 파일 저장 디렉토리 설정
UPLOAD_DIR = "./uploads/"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.post("/api/upload-policy")
async def upload_policy(file: UploadFile = File(...)):
    # 정책 파일 업로드 처리
    if file.content_type != "application/pdf":
        return {"error": "Only PDF files are allowed."}
    
    policy_file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(policy_file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return {"filename": file.filename, "file_path": policy_file_path}

@app.post("/api/upload-guideline")
async def upload_guideline(file: UploadFile = File(...)):
    # 지침 파일 업로드 처리
    if file.content_type != "application/pdf":
        return {"error": "Only PDF files are allowed."}
    
    guideline_file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(guideline_file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return {"filename": file.filename, "file_path": guideline_file_path}

@app.post("/api/ask")
async def ask_question(
     query: str = Body(), 
     policy_file_path: str = Body(), 
     guideline_file_path: str = Body()
):
    # 여기에 기존의 RAG 처리 로직이 들어갑니다
    # 예시로 입력된 파일 경로를 사용하여 처리
    gpt_rag = RagEnsemble()
    gpt_rag.set_policy_retriever(policy_file_path)
    gpt_rag.set_guideline_retriever(guideline_file_path)
    gpt_rag.create_rag_chain()

    response = gpt_rag.ask(query, policy_file_path, guideline_file_path)
    
    # 모델 처리 결과 반환
    return {"response" : response}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
