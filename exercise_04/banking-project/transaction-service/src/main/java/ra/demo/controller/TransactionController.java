package ra.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ra.demo.dto.AccountDTO;
import ra.demo.entity.Transaction;
import ra.demo.repository.TransactionRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@RequestBody Transaction request) {
        // Gọi nội bộ tới account-service qua Docker DNS (tên container)
        String accountServiceUrl = "http://account-service:8080/api/accounts/" + request.getFromAccountId();
        log.info("[TRANSACTION-SERVICE] Đang gọi nội bộ tới: {}", accountServiceUrl);

        try {
            ResponseEntity<AccountDTO> response = restTemplate.getForEntity(accountServiceUrl, AccountDTO.class);
            AccountDTO sourceAccount = response.getBody();

            if (sourceAccount == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy tài khoản nguồn!"));
            }

            log.info("[TRANSACTION-SERVICE] Xác thực thành công chủ tài khoản: {} (Số dư: {})",
                    sourceAccount.getOwnerName(), sourceAccount.getBalance());

            request.setStatus("SUCCESS");
            Transaction saved = transactionRepository.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[TRANSACTION-SERVICE] Tài khoản nguồn ID={} không tồn tại trên account-service!", request.getFromAccountId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Tài khoản nguồn không tồn tại: ID = " + request.getFromAccountId()));
        } catch (Exception e) {
            log.error("[TRANSACTION-SERVICE] Lỗi giao tiếp với account-service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi kết nối dịch vụ tài khoản: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }
}