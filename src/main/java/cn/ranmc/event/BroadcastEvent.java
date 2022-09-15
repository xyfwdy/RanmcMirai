package cn.ranmc.event;

import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.AbstractEvent;

import java.net.URLDecoder;

public class BroadcastEvent extends AbstractEvent {
    @Getter
    private String msg;
    @Getter
    private long qq;
    @Getter
    private boolean group;
    @Getter
    private boolean cancel = false;
    @Setter
    @Getter
    private int code = 400;

    public BroadcastEvent(boolean group, String qq, String msg) {
        this.group = group;
        this.msg = URLDecoder.decode(msg);
        try {
            this.qq = Long.parseLong(qq);
        } catch (NumberFormatException e) {
            cancel = true;
        }
    }
}