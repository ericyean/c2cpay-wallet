package com.c2cpay.wallet.api;

import com.c2cpay.wallet.dto.business.CreatePayoutRequest;
import com.c2cpay.wallet.dto.business.PayoutOrderResponse;
import com.c2cpay.wallet.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payout")
@Tag(name = "代付", description = "热钱包代付/提现：创建、风控、审核签名")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @PostMapping
    @Operation(summary = "创建代付单", description = "创建代付订单（按 bizOrderId 幂等）；会先做目标地址风控校验")
    public PayoutOrderResponse create(@Valid @RequestBody CreatePayoutRequest request) {
        return payoutService.create(request);
    }

    @GetMapping("/{bizOrderId}")
    @Operation(summary = "查询代付单", description = "按业务单号查询代付订单状态与交易信息")
    public PayoutOrderResponse get(
            @Parameter(description = "业务单号", required = true)
            @PathVariable String bizOrderId) {
        return payoutService.get(bizOrderId);
    }

    @PostMapping("/{bizOrderId}/approve-sign")
    @Operation(summary = "审核并通过热钱包签名", description = "审核通过后使用热钱包私钥签名；返回已签名交易，由调用方广播上链")
    public PayoutOrderResponse approveAndSign(
            @Parameter(description = "业务单号", required = true)
            @PathVariable String bizOrderId) {
        return payoutService.approveAndSign(bizOrderId);
    }
}
