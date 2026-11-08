package com.c2cpay.wallet.risk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** NoOpRiskProvider 类 */
@Component
@ConditionalOnProperty(name = "c2cpay.wallet.risk.provider", havingValue = "noop", matchIfMissing = true)
public class NoOpRiskProvider implements RiskProvider {

    /** 返回风控服务商名称 */
    @Override
    public String name() {
        return "noop";
    }

    /** 检查地址/交易风险 */
    @Override
    public RiskCheckResult checkAddress(String chain, String address, String txId) {
        return RiskCheckResult.low(name());
    }
}
