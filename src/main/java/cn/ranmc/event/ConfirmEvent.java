package cn.ranmc.event;

import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.AbstractEvent;

public class ConfirmEvent extends AbstractEvent {
    @Getter
    private String player;
    @Getter
    private String qq;
    @Getter
    private String code;
    @Getter
    @Setter
    private int num = 102;
    public ConfirmEvent(String player, String qq, String code) {
        this.player = player;
        this.qq = qq;
        this.code = code;
    }
}