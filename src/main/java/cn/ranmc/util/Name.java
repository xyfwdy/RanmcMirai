package cn.ranmc.util;

import net.mamoe.mirai.contact.Group;

public class Name {

    /**
     * 获取用户在该群昵称
     * @param group 群
     * @param id QQ
     * @return 昵称
     */
    public static String get(Group group, long id) {
        String name = group.get(id).getNameCard();
        if (name.isEmpty()) name = group.get(id).getNick();
        if (name.length() > 6) name = name.substring(0, 6);
        return name;
    }
}
