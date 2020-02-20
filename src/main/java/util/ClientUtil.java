package util;

import com.alibaba.fastjson.JSON;
import config.AllNodeCommonMsg;
import dao.node.NodeAddress;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.core.Node;
import org.tio.core.Tio;
import p2p.P2PConnectionMsg;
import p2p.client.P2PClientLinstener;
import p2p.client.P2pClientAioHandler;
import p2p.common.Const;
import p2p.common.MsgPacket;

import java.io.UnsupportedEncodingException;

/**
 * //                            _ooOoo_
 * //                           o8888888o
 * //                           88" . "88
 * //                           (| -_- |)
 * //                            O\ = /O
 * //                        ____/`---'\____
 * //                      .   ' \\| |// `.
 * //                       / \\||| : |||// \
 * //                     / _||||| -:- |||||- \
 * //                       | | \\\ - /// | |
 * //                     | \_| ''\---/'' | |
 * //                      \ .-\__ `-` ___/-. /
 * //                   ___`. .' /--.--\ `. . __
 * //                ."" '< `.___\_<|>_/___.' >'"".
 * //               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //                 \ \ `-. \_ __\ /__ _/ .-` / /
 * //         ======`-.____`-.___\_____/___.-`____.-'======
 * //                            `=---='
 * //
 * //         .............................................
 * //                  佛祖镇楼           BUG辟易
 *
 * @author: xiaohuiduan
 * @data: 2020/2/15 上午1:12
 * @description: 服务端工具
 */
@Slf4j
public class ClientUtil {


    /**
     * client 的配置
     */
    private static ClientTioConfig clientTioConfig = new ClientTioConfig(
            new P2pClientAioHandler(),
            new P2PClientLinstener(),
            new ReconnConf(Const.TIMEOUT)
    );

    public static ClientChannelContext clientConnect(String ip, int port) {

        clientTioConfig.setHeartbeatTimeout(Const.TIMEOUT);
        ClientChannelContext context;
        try {
            TioClient client = new TioClient(clientTioConfig);
            context = client.connect(new Node(ip, port), Const.TIMEOUT);
            return context;
        } catch (Exception e) {
            log.error("%s：%d连接错误" + e.getMessage());
            return null;
        }
    }

    /**
     * 添加client到 P2PConnectionMsg.CLIENTS中
     *
     * @param index  结点序号
     * @param client
     */
    public static void addClient(int index, ClientChannelContext client) {
        P2PConnectionMsg.CLIENTS.put(index, client);
    }

    /**
     * 判断该结点是否保存在CLIENTS中
     *
     * @param index
     * @return
     */
    public static boolean haveClient(int index) {
        if (P2PConnectionMsg.CLIENTS.containsKey(index)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * client对所有的server广播
     *
     * @param msg
     */
    public static void clientPublish(PbftMsg msg) {
        String jsonView = JSON.toJSONString(msg);
        MsgPacket msgPacket = new MsgPacket();
        for (ClientChannelContext client : P2PConnectionMsg.CLIENTS.values()) {
            try {
                msgPacket.setBody(jsonView.getBytes(MsgPacket.CHARSET));
                Tio.send(client, msgPacket);
            } catch (UnsupportedEncodingException e) {
                log.error("数据utf-8编码错误" + e.getMessage());
            }
        }
    }

    /**
     * 将自己的节点ip消息广播出去
     *
     * @param index
     * @param ip
     * @param port
     */
    public static void publishIpPort(int index, String ip, int port) {
        PbftMsg ipMsg = new PbftMsg(MsgType.IP_REPLY, index);
        ipMsg.setViewNum(AllNodeCommonMsg.view);
        // 将节点消息数据发送过去
        NodeAddress address = new NodeAddress();
        address.setPort(port);
        address.setIp(ip);
        ipMsg.setBody(JSON.toJSONString(address));
        ClientUtil.clientPublish(ipMsg);
        log.info("广播ip消息");
    }
}
