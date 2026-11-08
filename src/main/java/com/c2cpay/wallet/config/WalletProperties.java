package com.c2cpay.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "c2cpay.wallet")
public class WalletProperties {

    private String encSecret = "dev-only-change-me-32bytes-secret!!";
    private final Security security = new Security();
    private final Hd hd = new Hd();
    private final Hot hot = new Hot();
    private final Isolation isolation = new Isolation();
    private final Tron tron = new Tron();
    private final Risk risk = new Risk();

    public String getEncSecret() {
        return encSecret;
    }

    public void setEncSecret(String encSecret) {
        this.encSecret = encSecret;
    }

    public Security getSecurity() {
        return security;
    }

    public Hd getHd() {
        return hd;
    }

    public Hot getHot() {
        return hot;
    }

    public Isolation getIsolation() {
        return isolation;
    }

    public Tron getTron() {
        return tron;
    }

    public Risk getRisk() {
        return risk;
    }

    public static class Security {
        private boolean allowPrivateKeyInRequest = true;
        private boolean warnOnStartup = true;

        public boolean isAllowPrivateKeyInRequest() {
            return allowPrivateKeyInRequest;
        }

        public void setAllowPrivateKeyInRequest(boolean allowPrivateKeyInRequest) {
            this.allowPrivateKeyInRequest = allowPrivateKeyInRequest;
        }

        public boolean isWarnOnStartup() {
            return warnOnStartup;
        }

        public void setWarnOnStartup(boolean warnOnStartup) {
            this.warnOnStartup = warnOnStartup;
        }
    }

    public static class Hd {
        private String mnemonic = "";
        private String passphrase = "";
        private int account;
        private int change;

        public String getMnemonic() {
            return mnemonic;
        }

        public void setMnemonic(String mnemonic) {
            this.mnemonic = mnemonic;
        }

        public String getPassphrase() {
            return passphrase;
        }

        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }

        public int getAccount() {
            return account;
        }

        public void setAccount(int account) {
            this.account = account;
        }

        public int getChange() {
            return change;
        }

        public void setChange(int change) {
            this.change = change;
        }

        public boolean isConfigured() {
            return mnemonic != null && !mnemonic.isBlank();
        }
    }

    public static class Hot {
        private String privateKeyHex = "";

        public String getPrivateKeyHex() {
            return privateKeyHex;
        }

        public void setPrivateKeyHex(String privateKeyHex) {
            this.privateKeyHex = privateKeyHex;
        }

        public boolean isConfigured() {
            return privateKeyHex != null && !privateKeyHex.isBlank();
        }
    }

    public static class Isolation {
        private String address = "";

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Tron {
        private String apiBaseUrl = "https://api.trongrid.io";
        private String usdtContract = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        public String getUsdtContract() {
            return usdtContract;
        }

        public void setUsdtContract(String usdtContract) {
            this.usdtContract = usdtContract;
        }
    }

    public static class Risk {
        private boolean enabled = true;
        private String onProviderError = "fail-closed";
        private String provider = "noop";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getOnProviderError() {
            return onProviderError;
        }

        public void setOnProviderError(String onProviderError) {
            this.onProviderError = onProviderError;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public boolean isFailClosed() {
            return !"fail-open".equalsIgnoreCase(onProviderError);
        }
    }
}
