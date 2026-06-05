"""RAG Service for Retrieval-Augmented Generation."""

import json
import logging
import math
import os
from typing import Optional

import numpy as np

from app.config import SIMILARITY_TOP_K, VECTOR_DIMENSION
from app.services.ollama_service import generate_embeddings

logger = logging.getLogger(__name__)


class VectorStore:
    """Simple in-memory vector store for document embeddings."""

    def __init__(self):
        self.documents: list[dict] = []
        self.embeddings: list[list[float]] = []
        self.initialized = False

    def add_document(self, doc: dict, embedding: list[float]):
        self.documents.append(doc)
        self.embeddings.append(embedding)

    def search(self, query_embedding: list[float], k: int = SIMILARITY_TOP_K) -> list[dict]:
        """Find top-k most similar documents using cosine similarity."""
        if not self.embeddings:
            return []

        query_vec = np.array(query_embedding)
        doc_vecs = np.array(self.embeddings)

        # Normalize for cosine similarity
        query_norm = query_vec / (np.linalg.norm(query_vec) + 1e-10)
        doc_norms = doc_vecs / (np.linalg.norm(doc_vecs, axis=1, keepdims=True) + 1e-10)

        similarities = np.dot(doc_norms, query_norm)

        top_indices = np.argsort(similarities)[::-1][:k]

        results = []
        for idx in top_indices:
            if similarities[idx] > 0.3:  # Minimum similarity threshold
                results.append({
                    **self.documents[idx],
                    "similarity": float(similarities[idx]),
                })

        return results

    @property
    def count(self) -> int:
        return len(self.documents)


# Global vector store instance
vector_store = VectorStore()


async def initialize_knowledge_base():
    """Load and embed the banking knowledge base."""
    if vector_store.initialized:
        return

    knowledge_file = os.path.join(
        os.path.dirname(os.path.dirname(__file__)),
        "data", "banking_knowledge", "faq.json"
    )

    try:
        with open(knowledge_file, "r") as f:
            documents = json.load(f)
    except FileNotFoundError:
        logger.warning(f"Knowledge base file not found: {knowledge_file}")
        return

    logger.info(f"Loading {len(documents)} documents into vector store...")

    for doc in documents:
        # Create embedding text from question + answer
        embed_text = f"Q: {doc['question']}\nA: {doc['answer']}"
        embedding = await generate_embeddings(embed_text)
        vector_store.add_document(doc, embedding)

    vector_store.initialized = True
    logger.info(f"Vector store initialized with {vector_store.count} documents")


async def retrieve_relevant_context(query: str, k: int = SIMILARITY_TOP_K) -> list[dict]:
    """Retrieve relevant context from the knowledge base."""
    query_embedding = await generate_embeddings(query)
    results = vector_store.search(query_embedding, k=k)
    return results


async def get_rag_context(query: str) -> str:
    """Get formatted RAG context for a query."""
    relevant_docs = await retrieve_relevant_context(query)

    if not relevant_docs:
        return ""

    context_parts = []
    for i, doc in enumerate(relevant_docs, 1):
        context_parts.append(
            f"[{i}] Q: {doc['question']}\n   A: {doc['answer']}\n"
            f"   (Category: {doc.get('category', 'general')})"
        )

    return "\n\n".join(context_parts)


# Banking knowledge for insights
BANKING_INSIGHTS = {
    "savings": "Based on your spending patterns, increasing your monthly savings by 15% could help you reach your financial goals 3 months earlier. Consider setting up an auto-transfer to your Savings account.",
    "dining": "Your dining expenses have increased. Our data shows that setting a monthly budget of $350 for dining out can save you approximately $1,800 annually.",
    "investing": "The technology sector is showing strong growth. Consider rebalancing your portfolio to maintain optimal asset allocation. A diversified portfolio typically includes 60% stocks, 30% bonds, and 10% alternatives.",
    "emergency": "Financial experts recommend maintaining an emergency fund covering 3-6 months of expenses. Based on your monthly spending, your target emergency fund should be approximately $15,000-$30,000.",
    "retirement": "Starting retirement savings early maximizes compound interest. If you invest $500 monthly with a 7% annual return, you could accumulate over $600,000 in 30 years.",
    "credit": "Maintaining a credit utilization ratio below 30% is recommended for optimal credit scores. Your current utilization indicates room for improvement.",
    "budgeting": "The 50/30/20 budgeting rule is a popular guideline: 50% of income for needs, 30% for wants, and 20% for savings and debt repayment.",
}
