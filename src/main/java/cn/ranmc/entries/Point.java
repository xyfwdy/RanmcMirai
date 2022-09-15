package cn.ranmc.entries;

import cn.ranmc.bean.RankItem;
import cn.ranmc.constant.Prams;
import cn.ranmc.util.DataFile;
import cn.ranmc.util.Name;
import lombok.Getter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Point {

    @Getter
    private static JSONObject json;

    /**
     * 初始化
     */
    public Point() {
        load();
    }

    /**
     * 从文件读取数据
     */
    public void load() {
        json = new JSONObject(DataFile.read("point"));
    }

    /**
     * 查询积分
     * @param id QQ
     * @return 积分
     */
    public int check(long id) {
        if (!json.has(String.valueOf(id))) return 0;
        return json.getJSONObject(String.valueOf(id)).getInt("value");
    }

    public int check(String id) {
        try {
            return check(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 积分增加
     * @param id QQ
     * @param value 积分
     */
    public void plus(long id, int value) {
        JSONObject obj;
        if (json.has(String.valueOf(id))) {
            obj = json.getJSONObject(String.valueOf(id));
        } else {
            obj = new JSONObject();
            obj.put(Prams.VALUE, 0);
            obj.put(Prams.DATE, "0");
        }
        value += obj.getInt(Prams.VALUE);
        obj.put(Prams.VALUE, value);
        json.put(String.valueOf(id), obj);
        DataFile.write("point", json.toString());
        return;
    }

    /**
     * 积分减少
     * @param id QQ
     * @param value 积分
     * @return 是否成功
     */
    public boolean sub(long id, int value) {
        JSONObject obj;
        if (json.has(String.valueOf(id))) {
            obj = json.getJSONObject(String.valueOf(id));
        } else {
            return false;
        }
        int point = obj.getInt(Prams.VALUE);
        if (value > point) return false;
        point -= value;
        obj.put(Prams.VALUE, point);
        json.put(String.valueOf(id), obj);
        DataFile.write("point", json.toString());
        return true;
    }

    /**
     * 获取排名
     * @param id QQ
     * @return 排名
     */
    public int getRank(long id) {
        return getRank(String.valueOf(id));
    }

    public int getRank(String id) {
        int rank = 0;
        List<RankItem> rankList = new ArrayList<>();
        for (String key : json.keySet()) {
            rankList.add(new RankItem(key, ((JSONObject)json.get(key)).getInt("value")));
        }
        if (rankList.size() >= 1) {
            rankList.sort((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()));
        } else {
            return rank;
        }
        for (RankItem item : rankList) {
            rank++;
            if (item.getName().equals(id)) break;
        }
        return rank;
    }

    /**
     * 获取该群积分排行
     * @param group 群
     * @return 排行
     */
    public String getRankList(Group group) {
        List<RankItem> rankList = new ArrayList<>();
        for (String key : json.keySet()) {
            rankList.add(new RankItem(key, ((JSONObject)json.get(key)).getInt("value")));
        }
        if (rankList.size() >= 1) {
            rankList.sort((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()));
        } else {
            return "没有排行榜";
        }
        StringBuilder builder = new StringBuilder("积分排行\n");
        for (int i = 0; i < 9; i++) {
            if ((i + 1) > rankList.size()) {
                break;
            }
            RankItem item = rankList.get(i);
            String name;
            NormalMember member = group.get(Long.parseLong(item.getName()));
            if (member == null) {
                name = "不在该群";
            } else {
                name = Name.get(group, member.getId());
            }
            builder.append("("+(i + 1)+") " + name + " -> "+item.getValue() + "\n");
        }
        return builder.toString();
    }

    /**
     * 用户签到
     * @param id QQ
     * @return 是否成功
     */
    public boolean sign(long id) {
        JSONObject obj;
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (json.has(String.valueOf(id))) {
            obj = json.getJSONObject(String.valueOf(id));
            if (today.equals(obj.get(Prams.DATE))) return false;
        } else {
            obj = new JSONObject();
            obj.put(Prams.VALUE, 0);
            obj.put(Prams.DATE, "0");
        }
        obj.put(Prams.DATE, today);
        json.put(String.valueOf(id), obj);
        return true;
    }
}
