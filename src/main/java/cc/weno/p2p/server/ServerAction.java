package cc.weno.p2p.server;

import cc.weno.config.AllNodeCommonMsg;
import cc.weno.dao.bean.ReplayJson;
import cc.weno.dao.node.Node;
import cc.weno.dao.node.NodeAddress;
import cc.weno.dao.node.NodeBasicInfo;
import cc.weno.dao.pbft.MsgCollection;
import cc.weno.dao.pbft.MsgType;
import cc.weno.dao.pbft.PbftMsg;
import cc.weno.p2p.client.ClientAction;
import cc.weno.p2p.common.MsgPacket;
import cc.weno.util.ClientUtil;
import cc.weno.util.MsgUtil;
import cc.weno.util.PbftUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

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
            case MsgType.CLIENT_REPLAY:
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

        long count = collection.getAgreeCommit().incrementAndGet(msg.getId());

        log.info(String.format("server接受到commit消息：%s", msg));
        if (count >= AllNodeCommonMsg.getAgreeNum()) {
            log.info("数据符合，commit成功，数据可以生成块");
            collection.getAgreeCommit().remove(msg.getId());
            PbftUtil.save(msg);
        }
    }

    /**
     * 节点将prepare消息进行广播然后被接收到
     *
     * @param msg
     */
    private void prepare(PbftMsg msg) {
        log.info(msgCollection.getVotePrePrepare().contains(msg) + ">>>>");
        if (!msgCollection.getVotePrePrepare().contains(msg.getId()) || !PbftUtil.checkMsg(msg)) {
            return;
        }

        long count = collection.getAgreePrepare().incrementAndGet(msg.getId());
        log.info(String.format("server接受到prepare消息：%s", msg));
        if (count >= AllNodeCommonMsg.getAgreeNum()) {
            log.info("数据符合，发送commit操作");
            collection.getVotePrePrepare().remove(msg.getId());
            collection.getAgreePrepare().remove(msg.getId());

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

        log.info(String.format("server接受到pre-prepare消息：%s", msg));

        msgCollection.getVotePrePrepare().add(msg.getId());
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

        if (count >= AllNodeCommonMsg.getAgreeNum() && !node.isViewOK()) {
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
            ReplayJson replayJson = JSON.parseObject(ipStr, ReplayJson.class);
            ClientChannelContext context = ClientUtil.clientConnect(replayJson.getIp(), replayJson.getPort());

            NodeAddress address = new NodeAddress();
            address.setIp(replayJson.getIp());
            address.setPort(replayJson.getPort());
            NodeBasicInfo info = new NodeBasicInfo();
            info.setIndex(msg.getNode());
            info.setAddress(address);
            // 添加ip地址
            AllNodeCommonMsg.allNodeAddressMap.put(msg.getNode(), info);
            AllNodeCommonMsg.publicKeyMap.put(msg.getNode(), replayJson.getPublicKey());

            log.info(String.format("节点%s添加ip地址：%s", node, info));
            if (context != null) {
                // 添加client
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
        log.info(String.format("同意此节点%s的申请", msg));
        msg.setOk(true);
        msg.setViewNum(AllNodeCommonMsg.view);
        MsgUtil.signMsg(msg);
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
