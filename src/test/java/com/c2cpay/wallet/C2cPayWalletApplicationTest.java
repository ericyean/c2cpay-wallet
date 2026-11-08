package com.c2cpay.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class C2cPayWalletApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsCoins() throws Exception {
        mockMvc.perform(get("/api/v1/coins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void derivesEthereumAddress() throws Exception {
        String body = """
                {
                  "coin": "ethereum",
                  "mnemonic": "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
                }
                """;
        mockMvc.perform(post("/api/v1/wallet/address/derive")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("0x9858EfFD232B4033E47d90003D41EC34EcaEda94"));
    }
}
