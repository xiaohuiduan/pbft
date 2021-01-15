package cc.weno.util;

import com.alibaba.fastjson.JSON;
import cc.weno.config.AllNodeCommonMsg;
import cc.weno.dao.bean.ReplayJson;
import cc.weno.dao.node.Node;
import cc.weno.dao.pbft.MsgCollection;
import cc.weno.dao.pbft.MsgType;
import cc.weno.dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.core.Tio;
import cc.weno.p2p.P2PConnectionMsg;
import cc.weno.p2p.client.ClientAction;
import cc.weno.p2p.client.P2PClientLinstener;
import cc.weno.p2p.client.P2pClientAioHandler;
import cc.weno.p2p.common.Const;
import cc.weno.p2p.common.MsgPacket;

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
            context = client.connect(new org.tio.core.Node(ip, port), Const.TIMEOUT);
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
        // 设置目标消息发送方
        msg.setNode(Node.getInstance().getIndex());
        // 设置消息的目标方 -1 代表all
        msg.setToNode(-1);

        for (int index : P2PConnectionMsg.CLIENTS.keySet()) {
            ClientChannelContext client = P2PConnectionMsg.CLIENTS.get(index);

            // 如果不是CLIENT_REPLAY的消息类型，就进行加密和签名操作
            if (msg.getMsgType() != MsgType.CLIENT_REPLAY && msg.getMsgType() != MsgType.GET_VIEW) {
                if (!MsgUtil.preMsg(index, msg)) {
                    log.error("消息加密失败");
                    return;
                }
            }
            String json = JSON.toJSONString(msg);
            MsgPacket msgPacket = new MsgPacket();
            try {
                msgPacket.setBody(json.getBytes(MsgPacket.CHARSET));
                Tio.send(client, msgPacket);
            } catch (UnsupportedEncodingException e) {
                log.error("数据utf-8编码错误" + e.getMessage());
            }
        }
    }

    /**
     * 将自己的节点ip和公钥消息广播出去
     *
     * @param index
     * @param ip
     * @param port
     */
    public static void publishIpPort(int index, String ip, int port) {
        PbftMsg replayMsg = new PbftMsg(MsgType.CLIENT_REPLAY, index);
        replayMsg.setViewNum(AllNodeCommonMsg.view);
        // 将节点消息数据发送过去
        ReplayJson replayJson = new ReplayJson();
        replayJson.setIp(ip);
        replayJson.setPort(port);
        // 公钥部分
        replayJson.setPublicKey(Node.getInstance().getPublicKey());

        replayMsg.setBody(JSON.toJSONString(replayJson));
        ClientUtil.clientPublish(replayMsg);
        log.info(String.format("广播ip消息：%s", replayMsg));
    }

    /**
     * 主节点进行广播
     *
     * @param msg
     */
    public static void prePrepare(PbftMsg msg) {
        msg.setNode(Node.getInstance().getIndex());
        msg.setToNode(-1);
        msg.setViewNum(AllNodeCommonMsg.view);
        Node node = Node.getInstance();
        // 限制只有主节点能够发送消息
        if (node.getIndex() != AllNodeCommonMsg.getPriIndex()) {
            log.warn("该节点非主节点，不能发送preprepare消息");
            return;
        }
        msg.setMsgType(MsgType.PRE_PREPARE);
        ClientUtil.clientPublish(msg);

        MsgCollection msgCollection = MsgCollection.getInstance();
        msg.setMsgType(MsgType.PREPARE);
        msgCollection.getVotePrePrepare().add(msg.getId());
        if (!PbftUtil.checkMsg(msg)) {
            return;
        }
        try {
            msgCollection.getMsgQueue().put(msg);
            ClientAction.getInstance().doAction(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }
}
