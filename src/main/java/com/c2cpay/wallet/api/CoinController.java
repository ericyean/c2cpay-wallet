package com.c2cpay.wallet.api;

import com.c2cpay.wallet.dto.CoinInfoResponse;
import com.c2cpay.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 币种查询 REST 接口。 */
@RestController
@RequestMapping("/api/v1/coins")
@Tag(name = "币种", description = "已支持的币种与派生路径信息")
public class CoinController {

    private final WalletService walletService;

    /** 注入钱包服务。 */
    public CoinController(WalletService walletService) {
        this.walletService = walletService;
    }

    /** 查询已支持币种列表。 */
    @GetMapping
    @Operation(summary = "查询支持币种列表", description = "返回 coinId、符号、精度、默认 BIP 派生路径等")
    public List<CoinInfoResponse> list() {
        return walletService.listCoins();
    }
}
