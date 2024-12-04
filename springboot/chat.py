import os
from dotenv import load_dotenv
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import TextLoader
from langchain.schema.output_parser import StrOutputParser
from langchain.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from langchain_community.retrievers import BM25Retriever
from langchain.retrievers import  EnsembleRetriever
from langchain_community.vectorstores import FAISS
from langchain_openai import OpenAIEmbeddings
from langchain.embeddings import CacheBackedEmbeddings
from langchain.storage import LocalFileStore
import tempfile

from fastapi import FastAPI



from langchain_core.runnables import Runnable



import torch
torch.set_num_threads(1)

import time

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

        self.cached_embedder = CacheBackedEmbeddings.from_bytes_store(
            underlying_embeddings=self.embeddings,
            document_embedding_cache=store,
            namespace=self.embeddings.model
        )

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

        loaders = TextLoader(file_path=policy_file_path).load()
        docs = self.text_splitter.split_documents(loaders)

        vectorstore_faiss = FAISS.from_documents(docs, self.embeddings)
        bm25_retriever = BM25Retriever.from_documents(docs)
        faiss_retriever = vectorstore_faiss.as_retriever(search_kwargs={'k': 5})

        self.policy_retriever = EnsembleRetriever(
            retrievers=[bm25_retriever, faiss_retriever],
            weights=[0.5, 0.5]
        )
    def set_guideline_retriever(self, guideline_file_path: str):
        loaders = TextLoader(file_path=guideline_file_path).load()
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
        당신은 개인정보처리방침을 작성지침에 따라 평가하고, 사용자의 개인정보처리방침 관련 질문에 응답하는 어시스턴트입니다. 사용자가 입력한 개인정보처리방침을 분석하고, 주어진 평가지표를 기반으로 해당 방침이 제대로 작성되었는지 평가하세요. 평가 결과는 구체적인 근거와 함께 제시해야 합니다.
        사용자의 "개인정보처리방침을 평가해주세요"라는 첫 질문에만 개인정보를 평가하고 이후부터는 사용자의 질문에 맞게 응답하세요.

       
        ### 평가 항목
        - 개인정보의 처리 목적
        - 처리하는 개인정보의 항목 및 보유기간
        - 개인정보의 파기 절차 및 방법
         

        ### 입력
        - 작성지침 (guideline):
        {guideline}

        - 개인정보처리방침 (policy):
        {policy}

        
      
        
       
        
         
        """
        
        self.prompt = ChatPromptTemplate.from_template(template)
        self.rag_chain = (
            self.prompt | self.model | StrOutputParser()
        )
    
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

        return response #응답 반환
    
        

    def clear(self):
        self.policy_retriever = None
        self.guideline_retriever = None
        self.rag_chain = None




