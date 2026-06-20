package com.banking.agent.service;

import com.banking.agent.dto.SourceInfo;
import com.banking.agent.model.RagDocument;
import com.banking.agent.repository.RagDocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final OllamaEmbeddingModel embeddingModel;
    private final RagDocumentRepository ragDocumentRepository;
    private final ObjectMapper objectMapper;

    @Value("${chroma.url:http://localhost:8000}")
    private String chromaUrl;

    @Value("${chroma.collection-name:banking_knowledge}")
    private String collectionName;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String collectionId;

    public void initializeVectorStore() {
        try {
            String listUrl = chromaUrl + "/api/v1/collections";
            HttpRequest listRequest = HttpRequest.newBuilder()
                    .uri(URI.create(listUrl))
                    .GET()
                    .build();
            HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());

            if (listResponse.statusCode() == 200) {
                JsonNode collections = objectMapper.readTree(listResponse.body());
                if (collections.isArray()) {
                    for (JsonNode col : collections) {
                        if (col.has("name") && collectionName.equals(col.get("name").asText())) {
                            collectionId = col.get("id").asText();
                            log.info("Found existing ChromaDB collection: {} with id: {}", collectionName, collectionId);
                            return;
                        }
                    }
                }
            }

            ObjectNode createBody = objectMapper.createObjectNode();
            createBody.put("name", collectionName);
            String createUrl = chromaUrl + "/api/v1/collections";
            HttpRequest createRequest = HttpRequest.newBuilder()
                    .uri(URI.create(createUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(createBody.toString()))
                    .build();
            HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

            if (createResponse.statusCode() == 200 || createResponse.statusCode() == 201) {
                JsonNode created = objectMapper.readTree(createResponse.body());
                collectionId = created.get("id").asText();
                log.info("Created ChromaDB collection: {} with id: {}", collectionName, collectionId);
            }
        } catch (Exception e) {
            log.warn("Failed to initialize ChromaDB vector store: {}", e.getMessage());
        }
    }

    public void addDocument(String title, String content, String category, String source) {
        try {
            float[] embedding = embeddingModel.embed(content).content();
            List<Float> embeddingList = new ArrayList<>();
            for (float v : embedding) {
                embeddingList.add(v);
            }

            String docId = UUID.randomUUID().toString();

            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("title", title);
            metadata.put("category", category != null ? category : "");
            metadata.put("source", source != null ? source : "");
            metadata.put("chunk_index", 0);

            ObjectNode addBody = objectMapper.createObjectNode();
            addBody.put("id", docId);
            addBody.set("values", objectMapper.valueToTree(embeddingList));
            addBody.set("metadata", metadata);

            String addUrl = chromaUrl + "/api/v1/collections/" + collectionId + "/add";
            String body = objectMapper.writeValueAsString(addBody);
            HttpRequest addRequest = HttpRequest.newBuilder()
                    .uri(URI.create(addUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> addResponse = httpClient.send(addRequest, HttpResponse.BodyHandlers.ofString());

            if (addResponse.statusCode() == 200 || addResponse.statusCode() == 201) {
                JsonNode responseJson = objectMapper.readTree(addResponse.body());
                String vectorId = responseJson.has("id") ? responseJson.get("id").asText() : docId;

                RagDocument ragDoc = RagDocument.builder()
                        .documentId(docId)
                        .title(title)
                        .content(content)
                        .category(category)
                        .source(source)
                        .chunkIndex(0)
                        .vectorId(vectorId)
                        .build();
                ragDocumentRepository.save(ragDoc);
                log.info("Added document to ChromaDB: {}", title);
            }
        } catch (Exception e) {
            log.error("Failed to add document to ChromaDB: {}", e.getMessage());
        }
    }

    public List<SourceInfo> searchSimilar(String query, int k) {
        List<SourceInfo> results = new ArrayList<>();
        try {
            float[] queryEmbedding = embeddingModel.embed(query).content();
            List<Float> queryList = new ArrayList<>();
            for (float v : queryEmbedding) {
                queryList.add(v);
            }

            ObjectNode queryBody = objectMapper.createObjectNode();
            queryBody.set("query_embeddings", objectMapper.valueToTree(List.of(queryList)));
            queryBody.put("n_results", k);

            String queryUrl = chromaUrl + "/api/v1/collections/" + collectionId + "/query";
            String body = objectMapper.writeValueAsString(queryBody);
            HttpRequest queryRequest = HttpRequest.newBuilder()
                    .uri(URI.create(queryUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> queryResponse = httpClient.send(queryRequest, HttpResponse.BodyHandlers.ofString());

            if (queryResponse.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(queryResponse.body());
                if (responseJson.has("metadatas") && responseJson.get("metadatas").isArray()) {
                    ArrayNode metadatasArray = (ArrayNode) responseJson.get("metadatas");
                    ArrayNode distances = responseJson.has("distances") && responseJson.get("distances").isArray()
                            ? (ArrayNode) responseJson.get("distances") : null;

                    for (int i = 0; i < metadatasArray.size(); i++) {
                        JsonNode metadataList = metadatasArray.get(i);
                        if (metadataList.isArray()) {
                            for (int j = 0; j < metadataList.size(); j++) {
                                JsonNode meta = metadataList.get(j);
                                SourceInfo info = SourceInfo.builder()
                                        .title(meta.has("title") ? meta.get("title").asText() : "Unknown")
                                        .content(meta.has("content") ? meta.get("content").asText() : "")
                                        .category(meta.has("category") ? meta.get("category").asText() : "")
                                        .similarity(distances != null && distances.size() > i
                                                ? distances.get(i).get(j).asDouble() : 0.0)
                                        .build();
                                results.add(info);
                            }
                        }
                    }
                }
            }

            if (results.isEmpty()) {
                results = fallbackSearch(query, k);
            }
        } catch (Exception e) {
            log.warn("ChromaDB search failed, using fallback: {}", e.getMessage());
            results = fallbackSearch(query, k);
        }
        return results;
    }

    private List<SourceInfo> fallbackSearch(String query, int k) {
        List<RagDocument> allDocs = ragDocumentRepository.findAll();
        return allDocs.stream()
                .filter(doc -> doc.getContent() != null
                        && doc.getContent().toLowerCase().contains(query.toLowerCase()))
                .limit(k)
                .map(doc -> SourceInfo.builder()
                        .title(doc.getTitle())
                        .content(doc.getContent())
                        .category(doc.getCategory())
                        .similarity(0.0)
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteDocument(String id) {
        try {
            Optional<RagDocument> doc = ragDocumentRepository.findById(UUID.fromString(id));
            if (doc.isPresent() && doc.get().getVectorId() != null) {
                String deleteUrl = chromaUrl + "/api/v1/collections/" + collectionId + "/delete";
                ObjectNode deleteBody = objectMapper.createObjectNode();
                deleteBody.put("id", doc.get().getVectorId());
                String body = objectMapper.writeValueAsString(deleteBody);
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create(deleteUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
            }
            ragDocumentRepository.deleteById(UUID.fromString(id));
            log.info("Deleted document from RAG: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete document: {}", e.getMessage());
        }
    }

    public String getRelevantContext(String query, int maxResults) {
        List<SourceInfo> sources = searchSimilar(query, maxResults);
        if (sources.isEmpty()) {
            return null;
        }
        StringBuilder context = new StringBuilder();
        context.append("Here is relevant information from the knowledge base:\n\n");
        for (int i = 0; i < sources.size(); i++) {
            SourceInfo source = sources.get(i);
            context.append("[").append(i + 1).append("] ").append(source.getTitle());
            if (source.getCategory() != null && !source.getCategory().isEmpty()) {
                context.append(" (").append(source.getCategory()).append(")");
            }
            context.append(":\n").append(source.getContent()).append("\n\n");
        }
        return context.toString();
    }

    public void batchAddKnowledgeBase() {
        log.info("Initializing banking FAQ knowledge base...");
        Map<String, String> faqEntries = new LinkedHashMap<>();
        faqEntries.put("How to open an account",
                "To open a bank account with NovaBank, you can apply online through our website or mobile app. "
                        + "You will need to provide a valid government-issued ID, proof of address (utility bill or bank statement), "
                        + "your Social Security Number or Tax ID, and an initial deposit. The application process typically takes 24-48 hours. "
                        + "Accounts can be Checking, Savings, Business, Credit, Investment, or Fixed Deposit.");
        faqEntries.put("Account types available",
                "NovaBank offers several account types: CHECKING for daily transactions with 0.5% interest, "
                        + "SAVINGS for growing your money with 4.5% interest, BUSINESS for business operations with 2.0% interest, "
                        + "CREDIT for credit lines, INVESTMENT for market investments with 6.0% interest, "
                        + "and FIXED_DEPOSIT for term deposits with 7.0% interest.");
        faqEntries.put("How to check balance",
                "You can check your account balance through: 1) Online banking at our website, "
                        + "2) Mobile banking app, 3) ATM machines, 4) By calling our customer service, "
                        + "5) Using the AI banking assistant. Your available balance may differ from your current balance "
                        + "due to pending transactions or holds.");
        faqEntries.put("Transaction limits",
                "NovaBank transaction limits vary by account type. Standard daily limits are: "
                        + "CHECKING: $10,000 for transfers, $5,000 for ATM withdrawals, $3,000 for debit card purchases. "
                        + "SAVINGS: $5,000 for withdrawals, limited to 6 per month per federal regulations. "
                        + "BUSINESS: $50,000 for transfers, $10,000 for ATM withdrawals. "
                        + "Limits can be adjusted by contacting customer service or visiting a branch.");
        faqEntries.put("Transfer between accounts",
                "You can transfer money between your NovaBank accounts instantly through online banking. "
                        + "Transfers to other banks typically take 1-3 business days. Wire transfers are processed the same business day "
                        + "if initiated before 3:00 PM EST. International transfers may take 3-5 business days and incur additional fees.");
        faqEntries.put("Security measures",
                "NovaBank employs multiple security measures: 1) Two-factor authentication (2FA) for all online access, "
                        + "2) Real-time fraud monitoring and alerts, 3) End-to-end encryption for all transactions, "
                        + "4) Biometric authentication on mobile app, 5) Automatic session timeout after 10 minutes of inactivity, "
                        + "6) Zero liability policy for unauthorized transactions when reported promptly.");
        faqEntries.put("How to apply for a loan",
                "To apply for a loan at NovaBank: 1) Check your eligibility using our loan pre-qualification tool, "
                        + "2) Submit an application with personal and financial information, 3) Provide documentation including income proof, "
                        + "employment verification, and credit history, 4) Wait for approval (typically 1-3 business days), "
                        + "5) Review and sign the loan agreement. Loan types include personal loans, mortgages, auto loans, "
                        + "and business loans.");
        faqEntries.put("Credit score information",
                "NovaBank uses credit scores to evaluate loan and credit card applications. "
                        + "Credit scores typically range from 300 to 850. A score above 700 is considered good, "
                        + "above 750 is very good, and above 800 is excellent. Factors affecting your score include: "
                        + "payment history (35%), credit utilization (30%), length of credit history (15%), "
                        + "new credit inquiries (10%), and credit mix (10%). You can check your credit score through our banking app.");
        faqEntries.put("Card services",
                "NovaBank offers debit cards (linked to checking accounts), credit cards (various rewards programs), "
                        + "and prepaid cards. Features include: contactless payments, mobile wallet integration (Apple Pay, Google Pay), "
                        + "fraud protection, travel notifications, and instant card freezing through the mobile app. "
                        + "Credit cards offer rewards points, cashback, and travel benefits based on the card tier.");
        faqEntries.put("Fraud protection",
                "NovaBank's fraud protection includes: real-time transaction monitoring using AI, "
                        + "automatic alerts for suspicious activity, immediate card freezing capability, "
                        + "zero liability for unauthorized transactions, dedicated fraud investigation team, "
                        + "and purchase protection on eligible items. Report suspected fraud immediately through our app, "
                        + "website, or by calling our 24/7 fraud hotline.");

        int index = 0;
        for (Map.Entry<String, String> entry : faqEntries.entrySet()) {
            addDocument(entry.getKey(), entry.getValue(), "FAQ", "banking_knowledge_base");
            index++;
        }
        log.info("Added {} FAQ entries to knowledge base", faqEntries.size());
    }

    public List<RagDocument> getAllDocuments() {
        return ragDocumentRepository.findAll();
    }
}
