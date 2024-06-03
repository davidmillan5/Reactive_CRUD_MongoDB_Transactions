package co.com.bancolombia.transactions.services;

import co.com.bancolombia.transactions.models.Transaction;
import co.com.bancolombia.transactions.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransactionService {

    private final WebClient.Builder webClientBuilder;

    private final TransactionRepository transactionRepository;

    public TransactionService(WebClient.Builder webClientBuilder, TransactionRepository transactionRepository) {
        this.webClientBuilder = webClientBuilder;
        this.transactionRepository = transactionRepository;
    }

    public Mono<Transaction> prepareTransactions(Flux<Transaction> transactions) {
        return transactions
                .map(this::validateTransaction)
                .flatMap(this::calculateFees)
                .reduce(this::aggregateTransactions)
                .flatMap(transactionRepository::save);
    }


    private Transaction validateTransaction(Transaction transaction) {
        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid transaction amount");
        }
        return transaction;
    }

    private Mono<Transaction> calculateFees(Transaction transaction) {
        WebClient webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        return webClient.post()
                .uri("/calculateFees")
                .bodyValue(transaction)
                .retrieve()
                .bodyToMono(Transaction.class)
                .map(response -> {
                    transaction.setFee(response.getFee());
                    return transaction;
                });
    }

    private Transaction aggregateTransactions(Transaction t1, Transaction t2) {
        t1.setAmount(t1.getAmount() + t2.getAmount());
        t1.setFee(t1.getFee() + t2.getFee());
        return t1;
    }


}
