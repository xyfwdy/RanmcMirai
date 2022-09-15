package cn.ranmc.bean;

import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

import java.util.Date;

public class Confirm {
    @Getter
    private String player;
    @Getter
    private Member member;
    @Getter
    private String code;
    @Getter
    private Group group;
    @Getter
    private long time;
    @Setter
    @Getter
    private boolean pass;

    public Confirm(String player, Member member, Group group, String code) {
        this.player = player;
        this.member = member;
        this.code = code;
        this.group = group;
        time = new Date().getTime() + (60 * 1000);
        pass = false;
    }
}
