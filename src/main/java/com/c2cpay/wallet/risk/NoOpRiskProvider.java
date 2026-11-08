package com.c2cpay.wallet.risk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "c2cpay.wallet.risk.provider", havingValue = "noop", matchIfMissing = true)
public class NoOpRiskProvider implements RiskProvider {

    @Override
    public String name() {
        return "noop";
    }

    @Override
    public RiskCheckResult checkAddress(String chain, String address, String txId) {
        return RiskCheckResult.low(name());
    }
}
