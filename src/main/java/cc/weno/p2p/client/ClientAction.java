package cc.weno.p2p.client;

import cc.weno.config.AllNodeCommonMsg;
import cc.weno.dao.bean.DbDao;
import cc.weno.dao.node.Node;
import cc.weno.dao.pbft.MsgCollection;
import cc.weno.dao.pbft.MsgType;
import cc.weno.dao.pbft.PbftMsg;
import cc.weno.util.*;
import cn.hutool.db.Db;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;

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
 * @data: 2020/2/14 下午9:24
 * @description: 当client接受到消息时，会在这里进行处理
 */
@Slf4j
public class ClientAction {
    private MsgCollection collection = MsgCollection.getInstance();
    private Node node = Node.getInstance();

    /**
     * 使用单例设计模式
     */
    private static ClientAction action = new ClientAction();

    public static ClientAction getInstance() {
        return action;
    }

    private ClientAction() {
    }

    /**
     * 对消息进行处理
     *
     * @param channelContext
     */
    public void doAction(ChannelContext channelContext) {
        try {
            PbftMsg msg = collection.getMsgQueue().take();

            // 当视图还未好的时候，不处理非视图事务
            if (!node.isViewOK() && msg.getMsgType() != MsgType.GET_VIEW && msg.getMsgType() != MsgType.CHANGE_VIEW) {
                collection.getMsgQueue().put(msg);
                return;
            }
            switch (msg.getMsgType()) {
                case MsgType.GET_VIEW:
                    getView(msg);
                    break;
                case MsgType.CHANGE_VIEW:
                    onChangeView(msg);
                    break;
                case MsgType.PREPARE:
                    prepare(msg);
                    break;
                case MsgType.COMMIT:
                    commit(msg);
                default:
                    break;
            }
        } catch (InterruptedException e) {
            log.debug(String.format("消息队列take错误：%s", e.getMessage()));
        }
    }

    /**
     * 提交commit
     *
     * @param msg
     */
    private void commit(PbftMsg msg) {
        ClientUtil.clientPublish(msg);
    }

    /**
     * 向所有节点发送prepare消息
     *
     * @param msg
     */
    private void prepare(PbftMsg msg) {
        ClientUtil.clientPublish(msg);
    }

    /**
     * 发送重新选举的消息
     */
    private void onChangeView(PbftMsg msg) {
        // view进行加1处理
        int viewNum = AllNodeCommonMsg.view + 1;
        msg.setViewNum(viewNum);
        ClientUtil.clientPublish(msg);
    }

    /**
     * 获得view
     *
     * @param msg
     */
    synchronized private void getView(PbftMsg msg) {
        int fromNode = msg.getNode();
        if (node.isViewOK()) {
            return;
        }

        if (!MsgUtil.isRealMsg(msg) || !msg.isOk()) {
            long count = collection.getDisagreeViewNum().incrementAndGet();
            if (count >= AllNodeCommonMsg.getMaxf()) {
                log.error("视图获取失败");
                System.exit(0);
            }
            return;
        }
        // 将消息添加到list中
        // DbUtil.addDaotoList(fromNode,msg);

        long count = collection.getViewNumCount().incrementAndGet(msg.getViewNum());
        if (count >= AllNodeCommonMsg.getAgreeNum() && !node.isViewOK()) {
            // 将节点认证消息保存
            // DbUtil.save();

            collection.getViewNumCount().clear();

            node.setViewOK(true);
            AllNodeCommonMsg.view = msg.getViewNum();
            log.info("视图初始化完成OK");
            // 将节点写入文件
            PbftUtil.writeIpToFile(node);
            ClientUtil.publishIpPort(node.getIndex(), node.getAddress().getIp(), node.getAddress().getPort());
        }
    }

}
