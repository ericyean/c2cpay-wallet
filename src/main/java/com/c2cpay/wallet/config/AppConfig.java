package com.c2cpay.wallet.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** AppConfig 类 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(WalletProperties.class)
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private final WalletProperties properties;

    /** 构造 AppConfig */
    public AppConfig(WalletProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void warnIfInsecure() {
        if (properties.getSecurity().isWarnOnStartup()
                && properties.getSecurity().isAllowPrivateKeyInRequest()) {
            log.warn("c2cpay.wallet.security.allow-private-key-in-request=true — "
                    + "only expose crypto APIs on a trusted intranet / behind a gateway.");
        }
        if (!properties.getHd().isConfigured()) {
            log.info("HD mnemonic not configured — deposit address allocation via HD is disabled "
                    + "until c2cpay.wallet.hd.mnemonic is set.");
        }
    }
}
