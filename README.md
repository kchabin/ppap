# PBL
한이음 및 서울여대 종단형 PBL 수업 졸업 프로젝트 
### 개인정보처리방침 평가 프로그램의 개발
- RAG를 활용한 개인정보처리방침 평가 어시스턴트 챗봇 개발
- [개발기 1](https://kchabin.notion.site/PBL-1b3bd222b43d49b4b9d96c78d7b7c970)
- [개발기 2](https://uysuiiii.tistory.com/category/PPAP)
- [ACK 2024 발표 영상](https://youtu.be/YKkzNylP65w?si=iWwrNF-u7BTk0JmR)
- [KCD Korea x NGINX Meetup](https://www.linkedin.com/posts/kchabin_kubernetes-deployments-with-helm-activity-7303054675962957826-LNfC?utm_source=share&utm_medium=member_desktop&rcm=ACoAAELbAJoBALcm8fzVYHyMqzO_lYzqsJkxlag)
- AI : ChatGPT API, LangChain, FAISS
- Backend : SpringBoot, JPA, Kotlin, FastAPI
  
  - [rag 문서 업로드 방식](https://kchabin.notion.site/rag-b437a7143dfd4a9d9b4d024093ca7f5c?pvs=4)
- Frontend : React, JavaScript, Streamlit(test용)
  - [프론트엔드 코드](https://github.com/yunziee/new-ppap)
![스크린샷 2024-08-12 오후 5 56 36](https://github.com/user-attachments/assets/eb85bfec-7156-43ac-b703-e8eb930b5593)



### 서비스 구성도 
![image](https://github.com/user-attachments/assets/fc6f15d5-4307-460d-a292-2663c5513bbc)
1. 사용자가 작성한 개인정보 처리방침을 업로드하거나, 해당 개인정보 처리방침의 웹 url을 입력한다.
2. RAG 기법을 활용하여 AI가 업로드한 문서를 기반으로 사용자의 질문에 답변할 수 있도록 한다.

### 서비스 흐름도
![image](https://github.com/user-attachments/assets/a4a86367-5330-4a7b-a236-ac69ecd80cdb)

FastAPI 서버에서 RAG 모델을 실행하는 API를 제공한다. 이를 Spring Boot 서버와 통신하는 구조로 설계했다.

### 초기 설계 및 첫번째 문제 : 딥러닝 모델 제대로 만들기 너무 어렵다
딥러닝 모델을 활용해 개인정보처리방침이 법률, 가이드라인 등에 따라 제대로 작성되었는지 정량적으로 평가하는 걸 자동화할 수 있는 프로그램을 만들고자 했으나, AI 개발 관련 경험 부족과 데이터셋 구축에서 어려움을 느꼈다.
- 각 사이트 별로 개인정보처리방침의 형태가 상이한 경우가 많다.
- 사람이 평가하는 것처럼 하려면 자연어 처리 기술이 필요하다. 
- 이를 사람 없이 기계만으로 평가하는 기술을 약 7개월 안에 제대로 구현하기엔 전공과 맞지 않아 역량 부족의 한계가 있었다.
-> 위와 같은 몇 가지 시행착오를 통해 사람에 최대한 가까운 기술인 LLM을 이용해보자는 생각을 하게 되었다.

### RAG로 평가 어시스턴트 프로그램을 만들자
AI 관련 논문, 기술블로그 등을 찾고 프로젝트에 적용할 수 있을만한 기법을 찾는 과정에서 Fine-Tuning과 RAG를 비교하는 글을 접하게 되었고 RAG를 사용해 기존 LLM의 할루시네이션을 줄이고, 저렴한 비용으로 개발할 수 있다는 점이 매우 큰 메리트로 작용했다.
RAG 기법을 활용해 만든 챗봇은 LLM에게 새로운 지식을 더하여 이를 기반으로 사용자의 질문에 응답할 수 있게 된다. AI가 사용자의 질문에 답하기 전 참고할 수 있는 도서관 등을 제공하는 것이다. 
개인정보처리방침은 일반적인 사람들이 그 방대한 양의 텍스트와 법률용어로 인해 읽기를 꺼린다. 개인정보처리방침을 평가할 평가 위원들도 수많은 텍스트들을 읽으면서 평가하게 될 텐데, 여기서 생길 수 있는 피로, 실수 등을 줄일 수 있도록 우리가 만드는 RAG 챗봇이 평가하는 데 있어 보조 역할을 톡톡히 할 수 있을 것이라고 판단했다. 

- 가독성, 요약, 모르는 용어에 대한 질문 등을 통해 평가 위원, 피평가자인 기업 및 기관, 개인정보보호에 관심 있는 일반 사용자들까지 편리하게 이용할 수 있는 챗봇이 될 수 있다. 
- 직접 가이드라인, 법률 등을 찾아보지 않고 AI에게 질문하면 관련된 조항과 지침을 알기 쉽게 요약해서 답변을 제공한다.
