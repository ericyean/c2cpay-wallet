package com.c2cpay.wallet.crypto;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * HD 钱包工具类（BIP39 + BIP32/BIP44）。
 * <p>对齐 Trust Wallet Core 的 HDWallet 用法：生成/导入助记词，并按路径派生密钥。</p>
 */
public final class HdWallet {

    private final List<String> mnemonic;
    private final byte[] seed;

    /**
     * 使用助记词单词列表与种子字节构造钱包。
     *
     * @param mnemonic 助记词单词列表
     * @param seed     BIP39 种子
     */
    private HdWallet(List<String> mnemonic, byte[] seed) {
        this.mnemonic = List.copyOf(mnemonic);
        this.seed = Arrays.copyOf(seed, seed.length);
    }

    /**
     * 随机生成新的 HD 钱包。
     *
     * @param strengthBits 熵强度，仅支持 128（12 词）或 256（24 词）
     * @return 新钱包实例
     * @throws IllegalArgumentException 熵强度不合法时
     */
    public static HdWallet create(int strengthBits) {
        if (strengthBits != 128 && strengthBits != 256) {
            throw new IllegalArgumentException("strengthBits must be 128 or 256");
        }
        byte[] entropy = new byte[strengthBits / 8];
        new SecureRandom().nextBytes(entropy);
        List<String> words = MnemonicCode.INSTANCE.toMnemonic(entropy);
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(words, "");
        return new HdWallet(words, seed.getSeedBytes());
    }

    /**
     * 从助记词短语恢复 HD 钱包。
     *
     * @param mnemonicPhrase 空格分隔的 BIP39 助记词
     * @return 钱包实例
     * @throws IllegalArgumentException 助记词非法时
     */
    public static HdWallet fromMnemonic(String mnemonicPhrase) {
        List<String> words = Arrays.asList(mnemonicPhrase.trim().split("\\s+"));
        try {
            MnemonicCode.INSTANCE.check(words);
            DeterministicSeed seed = DeterministicSeed.ofMnemonic(words, "");
            return new HdWallet(words, seed.getSeedBytes());
        } catch (MnemonicException e) {
            throw new IllegalArgumentException("Invalid mnemonic: " + e.getMessage(), e);
        }
    }

    /**
     * 返回空格分隔的助记词字符串。
     *
     * @return 助记词全文
     */
    public String mnemonic() {
        return String.join(" ", mnemonic);
    }

    /**
     * 返回助记词单词列表（不可变副本语义由构造保证）。
     *
     * @return 单词列表
     */
    public List<String> words() {
        return mnemonic;
    }

    /**
     * 按 BIP 派生路径导出私钥。
     *
     * @param derivationPath 派生路径，如 {@code m/44'/60'/0'/0/0}
     * @return 对应子私钥
     */
    public PrivateKey getKey(String derivationPath) {
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(seed);
        // bitcoinj 0.17 uses 'H' for hardened (e.g. m/44H/60H/0H/0/0)
        HDPath path = HDPath.parsePath(toBitcoinjPath(derivationPath));
        DeterministicKey key = master;
        for (ChildNumber child : path) {
            key = HDKeyDerivation.deriveChildKey(key, child);
        }
        return new PrivateKey(key.getPrivKeyBytes());
    }

    /**
     * 按派生路径导出公钥。
     *
     * @param derivationPath 派生路径
     * @param compressed     是否返回压缩公钥
     * @return 公钥
     */
    public PublicKey getPublicKey(String derivationPath, boolean compressed) {
        return getKey(derivationPath).getPublicKey(compressed);
    }

    /**
     * 校验助记词格式与校验和是否合法。
     *
     * @param mnemonicPhrase 助记词短语
     * @return true 表示合法
     */
    public static boolean isValidMnemonic(String mnemonicPhrase) {
        try {
            List<String> words = Arrays.asList(mnemonicPhrase.trim().split("\\s+"));
            MnemonicCode.INSTANCE.check(words);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将标准 BIP 路径转为 bitcoinj 可解析格式。
     * <p>{@code '} 硬化记号转为 {@code H}；若缺少 {@code m/} 前缀则自动补上。</p>
     *
     * @param path 原始派生路径
     * @return bitcoinj 路径字符串
     */
    static String toBitcoinjPath(String path) {
        String p = path.trim().replace('\'', 'H');
        if (!(p.startsWith("m/") || p.startsWith("M/"))) {
            p = "m/" + p;
        }
        return p;
    }

    /**
     * 返回种子指纹（SHA256 前 8 个十六进制字符），便于日志/调试标识，不含完整种子。
     *
     * @return 8 位十六进制指纹
     */
    public String seedFingerprint() {
        return Sha256Hash.of(seed).toString().substring(0, 8);
    }
}
