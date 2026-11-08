package com.c2cpay.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** WalletProperties 类 */
@ConfigurationProperties(prefix = "c2cpay.wallet")
public class WalletProperties {

    private String encSecret = "dev-only-change-me-32bytes-secret!!";
    private final Security security = new Security();
    private final Hd hd = new Hd();
    private final Hot hot = new Hot();
    private final Isolation isolation = new Isolation();
    private final Tron tron = new Tron();
    private final Ethereum ethereum = new Ethereum();
    private final Smartchain smartchain = new Smartchain();
    private final Bitcoin bitcoin = new Bitcoin();
    private final Risk risk = new Risk();

    /** 获取加密密钥材料 */
    public String getEncSecret() {
        return encSecret;
    }

    /** 设置加密密钥材料 */
    public void setEncSecret(String encSecret) {
        this.encSecret = encSecret;
    }

    /** 获取security */
    public Security getSecurity() {
        return security;
    }

    /** 获取hd */
    public Hd getHd() {
        return hd;
    }

    /** 获取hot */
    public Hot getHot() {
        return hot;
    }

    /** 获取isolation */
    public Isolation getIsolation() {
        return isolation;
    }

    /** 获取tron */
    public Tron getTron() {
        return tron;
    }

    /** 获取ethereum */
    public Ethereum getEthereum() {
        return ethereum;
    }

    /** 获取smartchain */
    public Smartchain getSmartchain() {
        return smartchain;
    }

    /** 获取bitcoin */
    public Bitcoin getBitcoin() {
        return bitcoin;
    }

    /** 获取risk */
    public Risk getRisk() {
        return risk;
    }

    public static class Security {
        private boolean allowPrivateKeyInRequest = true;
        private boolean warnOnStartup = true;

        /** 是否是否允许请求体携带私钥 */
        public boolean isAllowPrivateKeyInRequest() {
            return allowPrivateKeyInRequest;
        }

        /** 设置是否允许请求体携带私钥 */
        public void setAllowPrivateKeyInRequest(boolean allowPrivateKeyInRequest) {
            this.allowPrivateKeyInRequest = allowPrivateKeyInRequest;
        }

        /** 是否启动时是否告警 */
        public boolean isWarnOnStartup() {
            return warnOnStartup;
        }

        /** 设置启动时是否告警 */
        public void setWarnOnStartup(boolean warnOnStartup) {
            this.warnOnStartup = warnOnStartup;
        }
    }

    public static class Hd {
        private String mnemonic = "";
        private String passphrase = "";
        private int account;
        private int change;

        /** 获取mnemonic */
        public String getMnemonic() {
            return mnemonic;
        }

        /** 设置mnemonic */
        public void setMnemonic(String mnemonic) {
            this.mnemonic = mnemonic;
        }

        /** 获取passphrase */
        public String getPassphrase() {
            return passphrase;
        }

        /** 设置passphrase */
        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }

        /** 获取account */
        public int getAccount() {
            return account;
        }

        /** 设置account */
        public void setAccount(int account) {
            this.account = account;
        }

        /** 获取change */
        public int getChange() {
            return change;
        }

        /** 设置change */
        public void setChange(int change) {
            this.change = change;
        }

        /** 是否已配置 */
        public boolean isConfigured() {
            return mnemonic != null && !mnemonic.isBlank();
        }
    }

    public static class Hot {
        private String privateKeyHex = "";

        /** 获取私钥十六进制 */
        public String getPrivateKeyHex() {
            return privateKeyHex;
        }

        /** 设置私钥十六进制 */
        public void setPrivateKeyHex(String privateKeyHex) {
            this.privateKeyHex = privateKeyHex;
        }

        /** 是否已配置 */
        public boolean isConfigured() {
            return privateKeyHex != null && !privateKeyHex.isBlank();
        }
    }

    public static class Isolation {
        private String address = "";

        /** 获取address */
        public String getAddress() {
            return address;
        }

        /** 设置address */
        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Tron {
        private String apiBaseUrl = "https://api.trongrid.io";
        private String apiKey = "";
        private String usdtContract = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

        /** 获取API 根地址 */
        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        /** 设置API 根地址 */
        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        /** 获取API Key */
        public String getApiKey() {
            return apiKey;
        }

        /** 设置API Key */
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        /** 获取USDT 合约地址 */
        public String getUsdtContract() {
            return usdtContract;
        }

        /** 设置USDT 合约地址 */
        public void setUsdtContract(String usdtContract) {
            this.usdtContract = usdtContract;
        }
    }

    public static class Ethereum {
        private String rpcUrl = "";

        /** 获取RPC 地址 */
        public String getRpcUrl() {
            return rpcUrl;
        }

        /** 设置RPC 地址 */
        public void setRpcUrl(String rpcUrl) {
            this.rpcUrl = rpcUrl;
        }
    }

    public static class Smartchain {
        private String rpcUrl = "https://bsc-dataseed.binance.org";

        /** 获取RPC 地址 */
        public String getRpcUrl() {
            return rpcUrl;
        }

        /** 设置RPC 地址 */
        public void setRpcUrl(String rpcUrl) {
            this.rpcUrl = rpcUrl;
        }
    }

    public static class Bitcoin {
        private String apiBaseUrl = "https://blockstream.info/api";

        /** 获取API 根地址 */
        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        /** 设置API 根地址 */
        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }
    }

    public static class Risk {
        private boolean enabled = true;
        private String onProviderError = "fail-closed";
        private String provider = "noop";

        /** 是否enabled */
        public boolean isEnabled() {
            return enabled;
        }

        /** 设置enabled */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /** 获取服务商异常策略 */
        public String getOnProviderError() {
            return onProviderError;
        }

        /** 设置服务商异常策略 */
        public void setOnProviderError(String onProviderError) {
            this.onProviderError = onProviderError;
        }

        /** 获取provider */
        public String getProvider() {
            return provider;
        }

        /** 设置provider */
        public void setProvider(String provider) {
            this.provider = provider;
        }

        /** 失败是否拒绝 */
        public boolean isFailClosed() {
            return !"fail-open".equalsIgnoreCase(onProviderError);
        }
    }
}
