package cn.ranmc.bean;

import lombok.Getter;

public class RankItem {
    @Getter
    private final String name;
    @Getter
    private final int value;

    public RankItem(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
