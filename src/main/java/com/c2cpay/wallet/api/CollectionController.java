package com.c2cpay.wallet.api;

import com.c2cpay.wallet.dto.business.CollectionTaskResponse;
import com.c2cpay.wallet.dto.business.CreateCollectionRequest;
import com.c2cpay.wallet.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collection")
@Tag(name = "归集", description = "将代收子地址余额归集到热钱包或风控隔离钱包")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/pending")
    @Operation(summary = "待归集任务列表", description = "查询状态为 PENDING 的归集任务")
    public List<CollectionTaskResponse> pending() {
        return collectionService.listPending();
    }

    @PostMapping("/sign")
    @Operation(summary = "创建并签名归集交易", description = "从子地址签名转出到热钱包/隔离钱包；返回原始交易，由调用方负责广播")
    public CollectionTaskResponse sign(@Valid @RequestBody CreateCollectionRequest request) {
        return collectionService.createAndSign(request);
    }
}
