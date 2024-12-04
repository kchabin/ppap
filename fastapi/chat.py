import os
from dotenv import load_dotenv
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import PyMuPDFLoader
from langchain.schema.output_parser import StrOutputParser
from langchain.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain_community.retrievers import BM25Retriever
from langchain.retrievers import EnsembleRetriever
from langchain_community.vectorstores import FAISS
from langchain.storage import LocalFileStore
from fastapi import FastAPI, File, UploadFile, Body
import shutil
import tempfile
import time
import torch

# OpenMP 오류 방지
os.environ['KMP_DUPLICATE_LIB_OK'] = 'TRUE'

# API 키 정보 로드
load_dotenv()

# 로컬 파일 저장소 설정
store = LocalFileStore("./cache/")

class RagEnsemble:
    def __init__(self):
        self.model = ChatOpenAI(model="gpt-4o", temperature=0)  # Enable streaming
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=1024,
            chunk_overlap=100
        )
        self.embeddings = OpenAIEmbeddings(model="text-embedding-3-small")
        self.policy_retriever = None
        self.guideline_retriever = None
        self.rag_chain = None
        self.cached_embedder = None

    def format_docs(self, docs):
        return '\n\n'.join([d.page_content for d in docs])

    def rerank(self, query, docs):
        query_embedding = self.embeddings.embed_query(query)  # 질문 임베딩 생성
        doc_texts = [doc.page_content for doc in docs]  # 문서 텍스트 추출
        doc_embeddings = [self.embeddings.embed_query(doc) for doc in doc_texts]  # 문서 임베딩 생성
        scores = [torch.cosine_similarity(torch.tensor(query_embedding),
                                          torch.tensor(doc_embedding), dim=0).item()
                  for doc_embedding in doc_embeddings]  # 코사인 유사도 계산
        sorted_docs = [doc for _, doc in sorted(zip(scores, docs), key=lambda x: x[0], reverse=True)]  # 유사도 기준 정렬
        return sorted_docs

    def set_policy_retriever(self, policy_file_path: str):
        loaders = PyMuPDFLoader(file_path=policy_file_path).load()
        docs = self.text_splitter.split_documents(loaders)
        vectorstore_faiss = FAISS.from_documents(docs, self.embeddings)
        bm25_retriever = BM25Retriever.from_documents(docs)
        faiss_retriever = vectorstore_faiss.as_retriever(search_kwargs={'k': 5})
        self.policy_retriever = EnsembleRetriever(
            retrievers=[bm25_retriever, faiss_retriever],
            weights=[0.5, 0.5]
        )

    def set_guideline_retriever(self, guideline_file_path: str):
        loaders = PyMuPDFLoader(file_path=guideline_file_path).load()
        docs = self.text_splitter.split_documents(loaders)
        vectorstore_faiss = FAISS.from_documents(docs, self.embeddings)
        bm25_retriever = BM25Retriever.from_documents(docs)
        faiss_retriever = vectorstore_faiss.as_retriever(search_kwargs={'k': 5})
        self.guideline_retriever = EnsembleRetriever(
            retrievers=[bm25_retriever, faiss_retriever],
            weights=[0.5, 0.5]
        )

    def create_rag_chain(self):
        template = """
        ### 역할 설명
        당신은 개인정보 처리방침을 평가하는 전문가입니다. 사용자가 입력한 개인정보 처리방침을 분석하고, 주어진 지침을 기반으로 해당 방침이 제대로 작성되었는지 평가하세요. 또한, 사용자가 개인정보 처리방침과 관련된 다양한 질문에 유연하게 답변하세요.
        
        ### 입력
        - 작성지침 (guideline):
        {guideline}
        - 개인정보 처리방침 (policy):
        {policy}
        - 질문 (question):
        {question}
        
        ### 평가 기준
        1. 개인정보 수집 항목이 명확하게 명시되어 있는가?
        2. 개인정보의 수집 및 이용 목적이 구체적으로 설명되어 있는가?
        3. 개인정보 보유 및 이용 기간이 명확하게 기재되어 있는가?
        4. 개인정보 제3자 제공에 대한 내용이 포함되어 있는가?
        5. 개인정보 처리 방침의 변경에 대한 안내가 포함되어 있는가?
        
        ### 답변 예시
        - 질문: 개인정보 수집 항목이 무엇인가요?
        답변: 개인정보 수집 항목은 이름, 이메일 주소, 전화번호 등이 포함됩니다.
        
        - 질문: 개인정보 처리 방침을 변경할 때 어떻게 안내하나요?
        답변: 개인정보 처리 방침을 변경할 때는 사용자에게 이메일로 안내하거나 웹사이트에 공지합니다.
        
        - 질문: 이 문서의 주요 내용은 무엇인가요?
        답변: 이 문서는 개인정보의 수집, 이용, 보유 및 제3자 제공에 대한 내용을 포함하고 있습니다.
        
        - 질문: 이 지침에 따라 문서를 작성하려면 어떻게 해야 하나요?
        답변: 이 지침에 따라 문서를 작성하려면 개인정보 수집 항목, 이용 목적, 보유 기간 등을 명확히 기재해야 합니다.
        
        - 질문: 이 방침이 법적 요구사항을 충족하나요?
        답변: 이 방침은 대부분의 법적 요구사항을 충족하지만, 특정 항목이 부족할 수 있습니다. 예를 들어, 개인정보 보유 기간이 명확하지 않습니다.
        
        - 질문: 방침에 누락된 부분이 있나요?
        답변: 네, 개인정보 제3자 제공에 대한 내용이 누락되어 있습니다. 이를 추가해야 합니다.
    """

        self.prompt = ChatPromptTemplate.from_template(template)
        self.rag_chain = self.prompt | self.model | StrOutputParser()

    def ask(self, query: str, policy_text_path: str, guideline_text_path: str):
        if not self.rag_chain:
            return "평가할 체인을 먼저 설정하세요."

        start_time = time.time()
        
        # Guideline 문서 처리
        result_guideline = self.guideline_retriever.invoke(guideline_text_path)
        reranked_guideline_docs = self.rerank(query, result_guideline)
        formatted_guideline_docs = self.format_docs(reranked_guideline_docs)

        # Policy 문서 처리
        result_policy = self.policy_retriever.invoke(policy_text_path)
        reranked_policy_docs = self.rerank(query, result_policy)
        formatted_policy_docs = self.format_docs(reranked_policy_docs)

        # RAG 체인 실행
        response = self.rag_chain.invoke({
            "guideline": formatted_guideline_docs,
            "policy": formatted_policy_docs,
            "question": query
        })

        end_time = time.time()
        elapsed_time = end_time - start_time
        print(f"\n응답 시간: {elapsed_time:.2f}초")

        return response


app = FastAPI()

rag_ensemble = RagEnsemble()

@app.post("/upload/")
async def upload_file(policy_file: UploadFile = File(...), guideline_file: UploadFile = File(...)):
    if policy_file.content_type != "application/pdf" or guideline_file.content_type != "application/pdf":
        return {"error": "PDF 파일만 업로드할 수 있습니다."}

    with tempfile.NamedTemporaryFile(delete=False) as policy_temp_file, \
         tempfile.NamedTemporaryFile(delete=False) as guideline_temp_file:
        
        # 파일 저장
        policy_temp_file.write(await policy_file.read())
        guideline_temp_file.write(await guideline_file.read())
        
        policy_file_path = policy_temp_file.name
        guideline_file_path = guideline_temp_file.name
        
        # 문서 처리기 설정
        rag_ensemble.set_policy_retriever(policy_file_path)
        rag_ensemble.set_guideline_retriever(guideline_file_path)
        
        return {"message": "파일 업로드 및 처리기 설정 완료"}

@app.post("/ask/")
async def ask_question(query: str = Body(), policy_file_path: str = Body(), guideline_file_path: str = Body()):
    # 질문을 통해 결과 얻기
    response = rag_ensemble.ask(query, policy_file_path, guideline_file_path)