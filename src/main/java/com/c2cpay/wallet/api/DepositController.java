package com.c2cpay.wallet.api;

import com.c2cpay.wallet.dto.business.AllocateDepositRequest;
import com.c2cpay.wallet.dto.business.DepositAddressResponse;
import com.c2cpay.wallet.dto.business.MarkDepositRequest;
import com.c2cpay.wallet.service.DepositAddressService;
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

/** 代收地址 REST 接口：分配、查询、入账确认。 */
@RestController
@RequestMapping("/api/v1/deposit")
@Tag(name = "代收地址", description = "HD 一单一址分配、入账确认与黑 U 风控")
public class DepositController {

    private final DepositAddressService depositAddressService;

    /** 注入代收地址服务。 */
    public DepositController(DepositAddressService depositAddressService) {
        this.depositAddressService = depositAddressService;
    }

    /** 按业务单号分配 HD 代收地址（幂等）。 */
    @PostMapping("/allocate")
    @Operation(summary = "分配代收地址", description = "按业务单号分配 HD 收款子地址；同一 bizOrderId 幂等返回")
    public DepositAddressResponse allocate(@Valid @RequestBody AllocateDepositRequest request) {
        return depositAddressService.allocate(request);
    }

    /** 按业务单号查询代收地址。 */
    @GetMapping("/{bizOrderId}")
    @Operation(summary = "查询代收地址", description = "按业务单号查询已分配的收款地址及状态")
    public DepositAddressResponse get(
            @Parameter(description = "业务单号", required = true)
            @PathVariable String bizOrderId) {
        return depositAddressService.getByBizOrderId(bizOrderId);
    }

    /** 确认入账并执行黑 U 风控。 */
    @PostMapping("/mark-deposited")
    @Operation(summary = "确认入账", description = "扫链/业务侧确认到账后调用；会执行黑 U 风险检测，并更新为可归集或隔离状态")
    public DepositAddressResponse markDeposited(@Valid @RequestBody MarkDepositRequest request) {
        return depositAddressService.markDeposited(request);
    }
}
