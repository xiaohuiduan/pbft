package p2p.server;

import com.alibaba.fastjson.JSON;
import config.AllNodeCommonMsg;
import dao.bean.IpJson;
import dao.node.Node;
import dao.node.NodeAddress;
import dao.node.NodeBasicInfo;
import dao.pbft.MsgCollection;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import p2p.client.ClientAction;
import p2p.common.MsgPacket;
import util.ClientUtil;
import util.PbftUtil;

import javax.sound.midi.MidiSystem;
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
    private MsgCollection msgCollection = MsgCollection.getInstance();
    /**
     * 单例模式构建action
     */
    private static ServerAction action = new ServerAction();

    private MsgCollection collection = MsgCollection.getInstance();

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
                onGetView(channelContext, msg);
                break;
            case MsgType.CHANGE_VIEW:
                changeView(channelContext, msg);
                break;
            case MsgType.PRE_PREPARE:
                prePrepare(msg);
                break;
            case MsgType.PREPARE:
                prepare(msg);
                break;
            case MsgType.COMMIT:
                commit(msg);
            case MsgType.IP_REPLY:
                addClient(msg);
                break;
            default:
                break;
        }
    }

    /**
     * commit阶段
     *
     * @param msg
     */
    private void commit(PbftMsg msg) {

        long count = collection.getAgreeCommit().incrementAndGet(msg);

        if (count >= 2 * AllNodeCommonMsg.getMaxf() + 1) {
            log.info("数据符合，commit成功，数据可以生成块");
            collection.getAgreeCommit().remove(msg);
            PbftUtil.save(msg);
        }
    }

    /**
     * 节点将prepare消息进行广播然后被接收到
     *
     * @param msg
     */
    private void prepare(PbftMsg msg) {
        if (!msgCollection.getVotePrePrepare().contains(msg) || !PbftUtil.checkMsg(msg)) {
            return;
        }

        long count = collection.getAgreePrepare().incrementAndGet(msg);

        if (count >= 2 * AllNodeCommonMsg.getMaxf() + 1) {
            log.info("数据符合，进行commit操作");
            collection.getVotePrePrepare().remove(msg);
            collection.getAgreePrepare().remove(msg);

            // 进入Commit阶段
            msg.setMsgType(MsgType.COMMIT);
            try {
                collection.getMsgQueue().put(msg);
                ClientAction.getInstance().doAction(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 主节点发送过来的pre_prepare消息
     *
     * @param msg
     */
    private void prePrepare(PbftMsg msg) {
        msgCollection.getVotePrePrepare().add(msg);
        if (!PbftUtil.checkMsg(msg)) {
            return;
        }

        msg.setMsgType(MsgType.PREPARE);
        try {
            msgCollection.getMsgQueue().put(msg);
            ClientAction.getInstance().doAction(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 重新设置view
     *
     * @param channelContext
     * @param msg
     */
    private void changeView(ChannelContext channelContext, PbftMsg msg) {
        if (node.isViewOK()) {
            return;
        }
        long count = collection.getViewNumCount().incrementAndGet(msg.getViewNum());

        if (count >= 2 * AllNodeCommonMsg.getMaxf() + 1 && !node.isViewOK()) {
            collection.getViewNumCount().clear();
            node.setViewOK(true);
            AllNodeCommonMsg.view = msg.getViewNum();
            log.info("视图变更完成OK");
        }
    }

    /**
     * 添加未连接的结点
     *
     * @param msg
     */
    private void addClient(PbftMsg msg) {
        if (!ClientUtil.haveClient(msg.getNode())) {
            String ipStr = msg.getBody();
            IpJson ipJson = JSON.parseObject(ipStr, IpJson.class);
            ClientChannelContext context = ClientUtil.clientConnect(ipJson.getIp(), ipJson.getPort());
            NodeBasicInfo info = new NodeBasicInfo();
            info.setIndex(msg.getNode());
            info.setAddress(new NodeAddress());
            AllNodeCommonMsg.allNodeAddressMap.put(msg.getNode(), info);
            log.info(String.format("节点%s添加ip地址：%s", node, info));
            if (context != null) {
                ClientUtil.addClient(msg.getNode(), context);
            }
        }
    }


    /**
     * 将自己的view发送给client
     *
     * @param channelContext
     * @param msg
     */
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
