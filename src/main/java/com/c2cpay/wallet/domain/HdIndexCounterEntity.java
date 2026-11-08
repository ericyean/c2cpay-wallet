package com.c2cpay.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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

    public HdIndexCounterEntity() {
    }

    public HdIndexCounterEntity(String coin, int nextIndex) {
        this.coin = coin;
        this.nextIndex = nextIndex;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int allocate() {
        int current = nextIndex;
        nextIndex = current + 1;
        return current;
    }
}
