package co.com.bancolombia.transactions.controllers;

import co.com.bancolombia.transactions.models.Transaction;
import co.com.bancolombia.transactions.services.TransactionConfirmationService;
import co.com.bancolombia.transactions.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class MainController {

    private final TransactionService transactionService;

    private final TransactionConfirmationService transactionConfirmationService;

    public MainController(TransactionService transactionService, TransactionConfirmationService transactionConfirmationService) {
        this.transactionService = transactionService;
        this.transactionConfirmationService = transactionConfirmationService;
    }

    @PostMapping("/processTransactions")
    public Mono<Void> processTransactions(@RequestBody Flux<Transaction> transactions) {
        Mono<Transaction> preparedTransactions = transactionService.prepareTransactions(transactions);

        return preparedTransactions
                .zipWhen(preparedTransaction -> transactionConfirmationService.confirmTransactions(Flux.just(preparedTransaction)))
                .then();
    }

    @PostMapping("/confirmTransactions")
    public Mono<Void> confirmTransactions(@RequestBody Flux<Transaction> transactions) {
        return transactionConfirmationService.confirmTransactions(transactions);
    }
}