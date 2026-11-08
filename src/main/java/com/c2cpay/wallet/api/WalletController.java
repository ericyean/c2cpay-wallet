package com.c2cpay.wallet.api;

import com.c2cpay.wallet.dto.BroadcastRequest;
import com.c2cpay.wallet.dto.BroadcastResponse;
import com.c2cpay.wallet.dto.DeriveAddressRequest;
import com.c2cpay.wallet.dto.DeriveAddressResponse;
import com.c2cpay.wallet.dto.MnemonicRequest;
import com.c2cpay.wallet.dto.MnemonicResponse;
import com.c2cpay.wallet.dto.RecoverSignRequest;
import com.c2cpay.wallet.dto.RecoverSignResponse;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import com.c2cpay.wallet.dto.ValidateAddressRequest;
import com.c2cpay.wallet.dto.ValidateAddressResponse;
import com.c2cpay.wallet.service.BroadcastService;
import com.c2cpay.wallet.service.SignRecoverService;
import com.c2cpay.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 钱包基础 REST 接口。
 * <p>提供助记词、地址派生/校验、交易签名、签名恢复验签、链上广播。</p>
 */
@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "钱包基础", description = "助记词、地址派生/校验、交易签名、签名恢复验签、链上广播")
public class WalletController {

    private final WalletService walletService;
    private final SignRecoverService signRecoverService;
    private final BroadcastService broadcastService;

    /** 注入钱包、验签、广播服务。 */
    public WalletController(
            WalletService walletService,
            SignRecoverService signRecoverService,
            BroadcastService broadcastService
    ) {
        this.walletService = walletService;
        this.signRecoverService = signRecoverService;
        this.broadcastService = broadcastService;
    }

    /** 生成 BIP39 助记词。 */
    @PostMapping("/mnemonic")
    @Operation(summary = "生成 BIP39 助记词", description = "strengthBits 支持 128（12 词）或 256（24 词），默认 128")
    public MnemonicResponse createMnemonic(@Valid @RequestBody(required = false) MnemonicRequest request) {
        int strength = request == null ? 128 : request.strengthOrDefault();
        return walletService.createMnemonic(strength);
    }

    /** 校验 BIP39 助记词是否合法。 */
    @GetMapping("/mnemonic/validate")
    @Operation(summary = "校验 BIP39 助记词", description = "校验助记词格式与校验和是否合法")
    public Map<String, Object> validateMnemonic(
            @Parameter(description = "助记词，空格分隔", required = true)
            @RequestParam @NotBlank String mnemonic) {
        return Map.of("valid", walletService.validateMnemonic(mnemonic));
    }

    /** 按币种校验链上地址格式。 */
    @PostMapping("/address/validate")
    @Operation(summary = "校验链上地址", description = "按币种规则校验地址格式是否合法")
    public ValidateAddressResponse validateAddress(@Valid @RequestBody ValidateAddressRequest request) {
        return walletService.validateAddress(request);
    }

    /** 从助记词或私钥派生地址。 */
    @PostMapping("/address/derive")
    @Operation(summary = "派生地址", description = "从助记词或私钥派生地址；可指定派生路径，默认使用币种标准路径")
    public DeriveAddressResponse deriveAddress(@Valid @RequestBody DeriveAddressRequest request) {
        return walletService.deriveAddress(request);
    }

    /** 使用助记词或私钥签名交易（不落库）。 */
    @PostMapping("/sign")
    @Operation(summary = "交易签名", description = "使用助记词或私钥签名交易，密钥仅在本次请求内使用，不落库；返回已签名原始交易")
    public SignResponse sign(@Valid @RequestBody SignRequest request) {
        return walletService.sign(request);
    }

    /** 从已签名交易恢复付款地址并验签。 */
    @PostMapping("/sign/recover")
    @Operation(
            summary = "签名恢复验签（SDK recover）",
            description = "从已签名交易恢复付款地址。EVM 用 TransactionDecoder；TRON 用 rawJson 的 signature 对 raw_data 做 secp256k1 recover；"
                    + "BTC 从 witness 提取公钥推导地址。可选传 expectedAddress 做比对。"
    )
    public RecoverSignResponse recover(@Valid @RequestBody RecoverSignRequest request) {
        return signRecoverService.recover(request);
    }

    /** 将已签名交易广播到对应链节点。 */
    @PostMapping("/broadcast")
    @Operation(
            summary = "广播已签名交易",
            description = "EVM → eth_sendRawTransaction；TRON → /wallet/broadcasttransaction（需 rawJson）；"
                    + "BTC → Blockstream /tx。需在配置中填写对应 RPC/API。"
    )
    public BroadcastResponse broadcast(@Valid @RequestBody BroadcastRequest request) {
        return broadcastService.broadcast(request);
    }

    /** 服务健康检查。 */
    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
