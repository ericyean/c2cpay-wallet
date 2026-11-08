package com.c2cpay.wallet.risk;

import com.c2cpay.wallet.config.WalletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskService.class);

    private final WalletProperties properties;
    private final RiskProvider riskProvider;

    public RiskService(WalletProperties properties, RiskProvider riskProvider) {
        this.properties = properties;
        this.riskProvider = riskProvider;
    }

    public RiskCheckResult check(String chain, String address, String txId) {
        if (!properties.getRisk().isEnabled()) {
            return RiskCheckResult.low("disabled");
        }
        try {
            return riskProvider.checkAddress(chain, address, txId);
        } catch (Exception e) {
            log.warn("Risk provider {} failed for {}: {}", riskProvider.name(), address, e.getMessage());
            return RiskCheckResult.unknown(
                    riskProvider.name(),
                    "provider-error: " + e.getMessage(),
                    properties.getRisk().isFailClosed());
        }
    }
}
