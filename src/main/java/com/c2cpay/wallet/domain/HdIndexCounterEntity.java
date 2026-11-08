package com.c2cpay.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** HdIndexCounterEntity 类 */
@Entity
@Table(name = "hd_index_counter")
public class HdIndexCounterEntity {

    @Id
    @Column(length = 32)
    private String coin;

    @Column(name = "next_index", nullable = false)
    private int nextIndex;

    @Version
    private long version;

    /** 构造 HdIndexCounterEntity */
    public HdIndexCounterEntity() {
    }

    /** 构造 HdIndexCounterEntity */
    public HdIndexCounterEntity(String coin, int nextIndex) {
        this.coin = coin;
        this.nextIndex = nextIndex;
    }

    /** 获取coin */
    public String getCoin() {
        return coin;
    }

    /** 设置coin */
    public void setCoin(String coin) {
        this.coin = coin;
    }

    /** 获取下一索引 */
    public int getNextIndex() {
        return nextIndex;
    }

    /** 设置下一索引 */
    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    /** 分配下一个 HD 下标并自增 */
    public int allocate() {
        int current = nextIndex;
        nextIndex = current + 1;
        return current;
    }
}
