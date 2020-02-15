package p2p.client;

import config.AllNodeCommonMsg;
import dao.node.Node;
import dao.pbft.MsgCollection;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import p2p.P2PConnectionMsg;

import java.util.concurrent.BlockingQueue;

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
 * @data: 2020/2/12 下午9:43
 * @description: 客户端监听器
 */
@Slf4j
public class P2PClientLinstener implements ClientAioListener {

    private ClientAction action = ClientAction.getInstance();

    private Node node = Node.getInstance();

    /**
     * 消息队列
     */
    private BlockingQueue<PbftMsg> msgQueue = MsgCollection.getInstance().getMsgQueue();


    /**
     * 建链后触发本方法，注：建链不一定成功，需要关注参数isConnected
     *
     * @param channelContext
     * @param isConnected    是否连接成功,true:表示连接成功，false:表示连接失败
     * @param isReconnect    是否是重连, true: 表示这是重新连接，false: 表示这是第一次连接
     * @throws Exception
     * @author: tanyaowu
     */
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        if (isReconnect) {
            log.warn(String.format("结点%重新连接服务端", channelContext));
        }
        if (isConnected) {
            log.info(String.format("结点%s连接服务端成功", channelContext));
        }else{
            log.warn(String.format("结点%s连接服务端失败", channelContext));
        }
    }


    /**
     * 原方法名：onAfterDecoded
     * 解码成功后触发本方法
     *
     * @param channelContext
     * @param packet
     * @param packetSize
     * @throws Exception
     * @author: tanyaowu
     */
    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {

    }

    /**
     * 接收到TCP层传过来的数据后
     *
     * @param channelContext
     * @param receivedBytes  本次接收了多少字节
     * @throws Exception
     */
    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

    }

    /**
     * 消息包发送之后触发本方法
     *
     * @param channelContext
     * @param packet
     * @param isSentSuccess  true:发送成功，false:发送失败
     * @throws Exception
     * @author tanyaowu
     */
    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {

    }

    /**
     * @param channelContext
     * @param packet
     * @param cost           本次处理消息耗时，单位：毫秒
     * @throws Exception
     */
    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        action.doAction(channelContext);
    }

    /**
     * 连接关闭前触发本方法
     *
     * @param channelContext the channelcontext
     * @param throwable      the throwable 有可能为空
     * @param remark         the remark 有可能为空
     * @param isRemove
     * @throws Exception
     * @author tanyaowu
     */
    @Override
    public void onBeforeClose(final ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
       log.warn(String.format("客户端%s连接关闭", channelContext));
       // 假如连接中断则移除
        P2PConnectionMsg.CLIENTS.values().removeIf(v -> v.equals(channelContext));

        /**
         * 假如中断的是主结点
         */
        if (channelContext.equals(P2PConnectionMsg.CLIENTS.get(AllNodeCommonMsg.getPriIndex()))){
            log.warn("主节点链接失败，决定发起视图选举");

            node.setViewOK(false);
            PbftMsg msg = new PbftMsg(MsgType.CHANGE_VIEW,node.getIndex());
            msgQueue.put(msg);
            action.doAction(channelContext);
        }
        
    }
}
