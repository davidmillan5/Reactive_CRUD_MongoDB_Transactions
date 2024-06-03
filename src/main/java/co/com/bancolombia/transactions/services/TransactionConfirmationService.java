package co.com.bancolombia.transactions.services;

import co.com.bancolombia.transactions.models.Transaction;
import co.com.bancolombia.transactions.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TransactionConfirmationService {

    private final WebClient.Builder webClientBuilder;

    private final TransactionRepository transactionRepository;

    public TransactionConfirmationService(WebClient.Builder webClientBuilder, TransactionRepository transactionRepository) {
        this.webClientBuilder = webClientBuilder;
        this.transactionRepository = transactionRepository;
    }

    public Mono<Void> confirmTransactions(Flux<Transaction> transactions) {
        return transactions
                .buffer(10)
                .flatMap(this::executeTransactions)
                .then();
    }

    private Mono<Void> executeTransactions(List<Transaction> transactions) {
        WebClient webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        return webClient.post()
                .uri("/executeBatch")
                .bodyValue(transactions)
                .retrieve()
                .bodyToMono(Void.class)
                .then(transactionRepository.saveAll(transactions).then()); // Guardar confirmaciones en la base de datos
    }
}
