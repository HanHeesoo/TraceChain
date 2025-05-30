package com.Django.TraceChain.api;

import com.Django.TraceChain.dto.*;
import com.Django.TraceChain.model.Wallet;
import com.Django.TraceChain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RestApiController {

    private final WalletService walletService;
    private final DetectService detectService;

    @Autowired
    public RestApiController(WalletService walletService, DetectService detectService) {
        this.walletService = walletService;
        this.detectService = detectService;
    }

    @GetMapping("/search")
    public ResponseEntity<WalletDto> search(@RequestParam String address,
                                            @RequestParam(defaultValue = "bitcoin") String chain) {
        
        Wallet wallet = walletService.findAddress(chain, address);
        wallet.setTransactions(walletService.getTransactions(chain, address));
        return ResponseEntity.ok(DtoMapper.mapWallet(wallet));
    }

    @GetMapping("/search-limited")
    public ResponseEntity<WalletDto> searchLimited(@RequestParam String address,
                                                   @RequestParam(defaultValue = "bitcoin") String chain,
                                                   @RequestParam(defaultValue = "10") int limit) {
        
        Wallet wallet = walletService.findAddress(chain, address);
        wallet.setTransactions(walletService.getTransactions(chain, address, limit));
        return ResponseEntity.ok(DtoMapper.mapWallet(wallet));
    }

    @GetMapping("/trace")
    public ResponseEntity<Set<String>> trace(@RequestParam String address,
                                             @RequestParam(defaultValue = "bitcoin") String chain,
                                             @RequestParam(defaultValue = "0") int depth,
                                             @RequestParam(defaultValue = "2") int maxDepth) {
        Set<String> visited = new HashSet<>();
        walletService.traceAllTransactionsRecursive(chain, address, depth, maxDepth, visited);
        return ResponseEntity.ok(visited);
    }

    @GetMapping("/trace-limited")
    public ResponseEntity<Map<Integer, List<WalletDto>>> traceDetailed(@RequestParam String address,
                                                                       @RequestParam(defaultValue = "bitcoin") String chain,
                                                                       @RequestParam(defaultValue = "0") int depth,
                                                                       @RequestParam(defaultValue = "2") int maxDepth) {
        Set<String> visited = new HashSet<>();
        Map<Integer, List<Wallet>> depthMap = new TreeMap<>();

        walletService.traceLimitedTransactionsRecursive(chain, address, depth, maxDepth, depthMap, visited);

        Map<Integer, List<WalletDto>> dtoMap = depthMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(DtoMapper::mapWallet).collect(Collectors.toList())
                ));

        return ResponseEntity.ok(dtoMap);
    }

    @GetMapping("/graph")
    public ResponseEntity<List<WalletDto>> graph() {
        List<Wallet> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(wallets.stream().map(DtoMapper::mapWallet).collect(Collectors.toList()));
    }

    @GetMapping("/detect")
    public ResponseEntity<List<WalletDto>> detectAllPatterns() {
        // 1. 모든 지갑을 가져옴
        List<Wallet> wallets = walletService.getAllWallets();


        // 2. 모든 탐지기 실행
        detectService.runAllDetectors(wallets);

        // 3. 결과 매핑 및 반환
        List<WalletDto> results = wallets.stream()
                                         .map(DtoMapper::mapWallet)
                                         .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @PostMapping("/detect-selected")
    public ResponseEntity<List<WalletDto>> detectSelected(@RequestBody List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 주소 없으면 400 에러
        }

        List<Wallet> wallets = addresses.stream()
                .map(walletService::findByIdSafe)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        detectService.runAllDetectors(wallets);

        List<WalletDto> results = wallets.stream()
                .map(DtoMapper::mapWallet)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

}
