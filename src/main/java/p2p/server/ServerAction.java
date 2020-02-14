package p2p.server;

import com.alibaba.fastjson.JSON;
import config.AllNodeCommonMsg;
import dao.node.Node;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import p2p.common.MsgPacket;
import until.ClientUtil;

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
 * @data: 2020/2/14 下午10:07
 * @description: 服务端的Action
 */
@Slf4j
public class ServerAction {
    private Node node = Node.getInstance();

    /**
     * 单例模式构建action
     */
    private static ServerAction action = new ServerAction();

    public static ServerAction getInstance() {
        return action;
    }

    private ServerAction() {
    }


    /**
     * 对PBFT消息做出回应
     *
     * @param channelContext 谁发送的请求
     * @param msg            消息内容
     */
    public void doAction(ChannelContext channelContext, PbftMsg msg) {
        switch (msg.getMsgType()) {
            case MsgType.GET_VIEW:
                addClient(channelContext, msg);
                onGetView(channelContext, msg);
                break;
            default:
                break;
        }
    }

    /**
     * 添加未连接的结点
     * @param channelContext
     * @param msg
     */
    private void addClient(ChannelContext channelContext, PbftMsg msg) {
        if (!ClientUtil.haveClient(msg.getNode())) {
            org.tio.core.Node clientNode = channelContext.getClientNode();
            ClientChannelContext context = ClientUtil.clientConnect(clientNode.getIp(), clientNode.getPort());
            if (context != null) {
                ClientUtil.addClient(msg.getNode(), context);
            }
        }
    }


    private void onGetView(ChannelContext channelContext, PbftMsg msg) {
        log.info("server结点回复视图请求操作");
        int fromNode = msg.getNode();
        // 设置消息的发送方
        msg.setNode(node.getIndex());
        // 设置消息的目的地
        msg.setToNode(fromNode);
        msg.setViewNum(AllNodeCommonMsg.view);

        String jsonView = JSON.toJSONString(msg);
        MsgPacket msgPacket = new MsgPacket();
        try {
            msgPacket.setBody(jsonView.getBytes(MsgPacket.CHARSET));
            Tio.send(channelContext, msgPacket);
        } catch (UnsupportedEncodingException e) {
            log.error(String.format("server结点发送view消息失败%s", e.getMessage()));
        }
    }
}
