package cn.ranmc;

import cn.ranmc.constant.Info;
import cn.ranmc.bean.Confirm;
import cn.ranmc.constant.QQId;
import cn.ranmc.event.BroadcastEvent;
import cn.ranmc.event.ConfirmEvent;
import cn.ranmc.network.Server;
import cn.ranmc.util.DataFile;
import cn.ranmc.entries.Point;
import cn.ranmc.util.Name;
import cn.ranmc.util.Recall;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import lombok.Getter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.data.*;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public final class Main extends JavaPlugin {
    public static final Main INSTANCE = new Main();
    private Bot bot;

    public static final String TOKEN = DataFile.read("token");
    private static final int PORT = 2263;

    private static final int replyDelay = 6000;
    private long replyDelayDate = 0;

    @Getter
    private final Point point = new Point();
    @Getter
    private static List<String> recall = new ArrayList<>();
    private static List<String> reply;
    private final Map<String,Confirm> confirmMap = new HashMap<>();

    public Main() {
        super(new JvmPluginDescriptionBuilder(Info.NAME, Info.VERSION).author(Info.AUTHOR).info(Info.WEB_SITE).build());
    }

    @Override
    public void onEnable() {

        getLogger().info("-----------------------");
        getLogger().info("Ranmc By " + Info.AUTHOR);
        getLogger().info("Version: " + Info.VERSION);
        getLogger().info(Info.WEB_SITE);
        getLogger().info("-----------------------");

        reload();

        HttpServer httpserver;
        try {
            HttpServerProvider provider = HttpServerProvider.provider();
            httpserver = provider.createHttpServer(new InetSocketAddress(PORT), 100);
            httpserver.createContext("/bot", new Server());
            httpserver.setExecutor(null);
            httpserver.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("????????????????????????" + PORT);
        // ??????
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);

        // ????????????
        eventChannel.subscribeAlways(BotOnlineEvent.class, event ->  bot = event.getBot());

        // ??????
        eventChannel.subscribeAlways(BroadcastEvent.class, event -> {
            if (bot == null) {
                event.setCode(405);
                return;
            }
            if (event.isCancel()) {
                event.setCode(406);
                return;
            }
            if (event.isGroup()) {
                Group group = bot.getGroup(event.getQq());
                if (group == null) {
                    event.setCode(101);
                } else {
                    group.sendMessage(event.getMsg());
                    event.setCode(200);
                }
            } else {
                Friend friend = bot.getFriend(event.getQq());
                if (friend == null) {
                    event.setCode(101);
                } else {
                    friend.sendMessage(event.getMsg());
                    event.setCode(200);
                }
            }
        });

        // ????????????
        eventChannel.subscribeAlways(ConfirmEvent.class, event -> {
            if (bot == null) {
                event.setNum(405);
                return;
            }
            if (confirmMap.containsKey(event.getQq())) {
                Confirm confirm = confirmMap.get(event.getQq());
                if (confirm.isPass()) {
                    confirmMap.remove(event.getQq());
                    event.setNum(200);
                    String msg = "???????????????";
                    if (event.getCode().equals("1")) msg = "??????????????????";
                    confirm.getGroup().sendMessage(new At(confirm.getMember().getId()).plus("??????" + msg + event.getPlayer()));
                } else if (confirm.getTime() < new Date().getTime()) {
                    event.setNum(103);
                    confirmMap.remove(event.getQq());
                    confirm.getGroup().sendMessage(new At(confirm.getMember().getId()).plus("???????????????????????????"));
                } else {
                    event.setNum(102);
                }
            } else {
                Group group = bot.getGroup(QQId.GROUP_2);
                Member member = group.get(Long.parseLong(event.getQq()));
                if (member == null) {
                    group = bot.getGroup(QQId.GROUP_1);
                    member = group.get(Long.parseLong(event.getQq()));
                    if (member == null) {
                        event.setNum(102);
                        return;
                    }
                }
                confirmMap.put(event.getQq(), new Confirm(event.getPlayer(), member, group, event.getCode()));
                String msg = "???????????????";
                if (event.getCode().equals("1")) msg = "??????????????????";
                group.sendMessage(new At(member.getId()).plus("\n" + msg + event.getPlayer() + "\n????????????????????????\n??????60????????????????????????"));
            }
        });

        // ?????????
        eventChannel.subscribeAlways(GroupMessageEvent.class, event -> {
            String msg = event.getMessage().contentToString();
            long groupId = event.getGroup().getId();
            if (event.getSender().getId() == event.getBot().getId()) return;
            if (event.getGroup().getBotAsMember().isMuted()) return;
            // ??????
            if (event.getSender().getId() == QQId.AUTHOR) {
                if (msg.equals("reload")) {
                    reload();
                    event.getGroup().sendMessage("??????");
                    return;
                }
                MessageChain msgs = event.getMessage();
                if (msg.contains("??????+") && msgs.get(msgs.size() - 1) instanceof At at) {
                    int value;
                    try {
                        value = Integer.parseInt(msg.split("??????\\+")[1].split("@")[0]);
                    } catch (NumberFormatException e) {
                        event.getGroup().sendMessage("????????????");
                        return;
                    }
                    point.plus(at.getTarget(), value);
                    event.getGroup().sendMessage(Name.get(event.getGroup(), at.getTarget()) + " ??????+" + value + "(" + point.check(at.getTarget()) + "),??????" + point.getRank(at.getTarget()));
                    return;
                }
                if (msg.contains("??????-") && msgs.get(msgs.size() - 1) instanceof At at) {
                    int value;
                    try {
                        value = Integer.parseInt(msg.split("??????-")[1].split("@")[0]);
                    } catch (NumberFormatException e) {
                        event.getGroup().sendMessage("????????????");
                        return;
                    }
                    if (point.sub(at.getTarget(), value)) {
                        event.getGroup().sendMessage(Name.get(event.getGroup(), at.getTarget()) + " ??????-" + value + "(" + point.check(at.getTarget()) + "),??????" + point.getRank(at.getTarget()));
                    } else {
                        event.getGroup().sendMessage("????????????");
                    }
                    return;
                }
                if (msg.contains("??????") && msgs.get(msgs.size() - 1) instanceof At at) {
                    event.getGroup().sendMessage(Name.get(event.getGroup(), at.getTarget()) + " ??????" + point.check(at.getTarget()) + ",??????" + point.getRank(at.getTarget()));
                    return;
                }

            }

            // ????????????
            if (event.getGroup().getBotAsMember().getPermission() == MemberPermission.ADMINISTRATOR && event.getSender().getPermission() == MemberPermission.MEMBER) {
                long id = event.getSender().getId();
                if (!Recall.check(Name.get(event.getGroup(), id)).isEmpty()) {
                    event.getGroup().get(id).setNameCard("????????????");
                    event.getGroup().sendMessage(new At(id).plus("???????????????"));
                }
                String word = Recall.check(msg);
                if (!word.isEmpty()) {
                    getLogger().info(event.getGroup().getName() + "???" + Name.get(event.getGroup(), id) + "(" + id + ")???????????????" + word);
                    MessageSource.recall(event.getMessage());
                    return;
                }
            }

            // ????????????
            if (!QQId.IGNORE.contains(event.getSender().getId())) {
                for (String arg : reply) {
                    if (replyDelayDate > new Date().getTime()) break;
                    String[] args = arg.split("\n");
                    if (args[0].equalsIgnoreCase(String.valueOf(groupId))) {
                        List<String> list = Arrays.asList(args[1].split("???"));
                        for (String key : list) {
                            if (msg.contains(key)) {
                                replyDelayDate = new Date().getTime() + replyDelay;
                                event.getGroup().sendMessage(args[2].replace("%n", "\n"));
                                return;
                            }
                        }
                    }
                }
            }

            // ??????
            if (groupId == QQId.GROUP_1 || groupId == QQId.GROUP_2) {
                if (msg.equals("??????") || msg.equals("??????")) {
                    if (confirmMap.containsKey(String.valueOf(event.getSender().getId()))) {
                        Confirm confirm = confirmMap.get(String.valueOf(event.getSender().getId()));
                        if (!confirm.isPass()) {
                            confirm.setPass(true);
                            event.getGroup().sendMessage("?????????");
                            return;
                        }
                    }
                }

                if (msg.equals("??????") || msg.equals("??????")) {
                    if (LocalDateTime.now().getHour() >= 12) {
                        event.getGroup().sendMessage("??????????????????\n00:00 - 12:00");
                        return;
                    }
                    if (point.sign(event.getSender().getId())) {
                        int value = new Random().nextInt(8) - 2;
                        if (value <= 0) value += 3;
                        value += 5;
                        point.plus(event.getSender().getId(), value);
                        event.getGroup().sendMessage(new At(event.getSender().getId()).plus("??????+" + value + "(" + point.check(event.getSender().getId()) + "),??????" + point.getRank(event.getSender().getId())));
                    } else {
                        event.getGroup().sendMessage("???????????????");
                    }
                    return;
                }

                if (msg.equals("??????")) {
                    event.getGroup().sendMessage(new At(event.getSender().getId()).plus("??????" + point.check(event.getSender().getId()) + ",??????" + point.getRank(event.getSender().getId())));
                    return;
                }

                if (msg.equals("????????????")) event.getGroup().sendMessage(point.getRankList(event.getGroup()));
            }

        });

        // ?????????
        eventChannel.subscribeAlways(MemberJoinEvent.class, event -> {
            if (event.getGroup().getBotAsMember().isMuted()) return;
            long group = event.getGroup().getId();
            if (group == QQId.GROUP_1 || group == QQId.GROUP_2)
                event.getGroup().sendMessage(new At(event.getUser().getId()).plus("\n???????????????????????????\n???????????????????????????\n???????????????????????????\n???????????????????????????\n???????????????????????????\n???????????????????????????\n????????????QQ?????????\n????????????/bind qq???"));
            if (group == QQId.GROUP_CITY) event.getGroup().sendMessage(new PlainText("??????").plus(new At(event.getUser().getId())).plus("????????????????????????????????????~"));
            if (group == QQId.GROUP_RAN) event.getGroup().sendMessage(new PlainText("??????").plus(new At(event.getUser().getId())).plus("??????????????????~"));
        });

        // ????????????
        eventChannel.subscribeAlways(FriendMessageEvent.class, event -> {
            String msg = event.getMessage().contentToString();
            if (event.getSender().getId() == QQId.AUTHOR) {
                if (msg.equals("reload")) {
                    reload();
                    event.getSender().sendMessage("??????");
                }
            }
        });

        super.onEnable();
    }


    // ????????????
    private void reload() {
        recall = Arrays.asList(DataFile.read("recall").split("\n"));
        getLogger().info("???????????????" + recall.size() + "???");
        reply = Arrays.asList(DataFile.read("reply").split("\n,\n"));
        getLogger().info("??????????????????" + reply.size() + "???");

        point.load();
    }

    @Override
    public void onDisable() {
        getLogger().info("Bye");
        DataFile.write("point", point.getJson().toString());
        super.onDisable();
    }


}