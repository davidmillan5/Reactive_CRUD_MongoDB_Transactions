package co.com.bancolombia.transactions.repositories;

import co.com.bancolombia.transactions.models.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction,String> {
}
