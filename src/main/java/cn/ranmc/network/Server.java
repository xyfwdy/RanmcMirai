package cn.ranmc.network;

import cn.ranmc.Main;
import cn.ranmc.constant.Prams;
import cn.ranmc.event.BroadcastEvent;
import cn.ranmc.event.ConfirmEvent;
import cn.ranmc.entries.Point;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.mamoe.mirai.event.EventKt;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Server implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        JSONObject json = new JSONObject();
        Map<String,String> map = new HashMap<>();
        String args[] = exchange.getRequestURI().toString().replace("/bot?", "").split("&");
        for (String arg : args) {
            String prams[] = arg.split("=");
            map.put(prams[0], prams[1]);
        }
        if (!map.containsKey(Prams.TOKEN) || !map.get(Prams.TOKEN).equals(Main.INSTANCE.TOKEN) || !map.containsKey(Prams.QQ)) {
            json.put(Prams.CODE, 401);
            os.write(json.toString().getBytes("UTF-8"));
            os.close();
            return;
        }
        Point point = Main.INSTANCE.getPoint();
        switch (map.get(Prams.MODE)) {
            case Prams.POINT:
                json.put(Prams.CODE, 200);
                json.put(Prams.POINT, point.check(map.get(Prams.QQ)));
                json.put(Prams.RANK, point.getRank(map.get(Prams.QQ)));
                break;
            case Prams.SUB_POINT:
                if (point.sub(Long.parseLong(map.get(Prams.QQ)), Integer.parseInt(map.get(Prams.POINT)))) {
                    json.put(Prams.CODE, 200);
                } else {
                    json.put(Prams.CODE, 110);
                }
                break;
            case Prams.PLUS_POINT:
                point.plus(Long.parseLong(map.get(Prams.QQ)), Integer.parseInt(map.get(Prams.POINT)));
                json.put(Prams.CODE, 200);
                break;
            case Prams.CONFIRM:
                if (map.containsKey(Prams.CODE) && map.containsKey(Prams.PLAYER)) {
                    ConfirmEvent event = new ConfirmEvent(map.get(Prams.PLAYER), map.get(Prams.QQ), map.get(Prams.CODE));
                    int code = EventKt.broadcast(event).getNum();
                    json.put(Prams.CODE, code);
                } else {
                    json.put(Prams.CODE, 403);
                }
                break;
            case Prams.BROADCAST:
                if (map.containsKey(Prams.GROUP) && map.containsKey(Prams.MSG) && map.containsKey(Prams.QQ)) {
                    BroadcastEvent event = new BroadcastEvent(map.get(Prams.GROUP).equals("true"), map.get(Prams.QQ), map.get(Prams.MSG));
                    int code = EventKt.broadcast(event).getCode();
                    json.put(Prams.CODE, code);
                } else {
                    json.put(Prams.CODE, 403);
                }
                break;
            default:
                json.put(Prams.CODE, 402);
                break;
        }
        /*
        InputStream in = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        String text = reader.readLine();
        */
        //exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.toString().getBytes().length);

        os.write(json.toString().getBytes("GBK"));
        os.close();
    }
}

