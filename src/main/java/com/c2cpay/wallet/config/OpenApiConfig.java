package com.c2cpay.wallet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI c2cPayWalletOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("C2C Pay 钱包服务 API")
                        .description("""
                                多链钱包服务：对齐 Trust Wallet Core 的助记词 / 地址派生校验 / 交易签名，
                                并提供 C2C 代收（一单一址）、黑 U 风控、归集、热钱包代付能力。
                                """)
                        .version("0.1.0")
                        .contact(new Contact().name("c2cpay-wallet")));
    }
}
