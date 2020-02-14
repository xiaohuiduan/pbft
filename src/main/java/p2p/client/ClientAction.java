package p2p.client;

import config.AllNodeCommonMsg;
import dao.node.Node;
import dao.pbft.MsgCollection;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
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
 * @description: pbft算法的实现
 */
@Slf4j
public class ClientAction{
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

    private void getView(PbftMsg msg) {
        if (node.isViewOK()) {
            return;
        }
        long count = collection.getViewNumCount().incrementAndGet(msg.getViewNum());
        if (count >= 2 * AllNodeCommonMsg.getMaxf() + 1) {
            collection.getViewNumCount().clear();
            node.setViewOK(true);
            AllNodeCommonMsg.view = msg.getViewNum();
            log.info("视图初始化完成OK");
        }
    }

    /**
     * 对消息进行处理
     *
     * @param channelContext
     */
    public void doAction(ChannelContext channelContext) {
        try {
            PbftMsg msg = collection.getMsgQueue().take();
            switch (msg.getMsgType()) {
                case MsgType.GET_VIEW:
                    getView(msg);
                    break;
                default:
                    break;
            }
        } catch (InterruptedException e) {
            log.debug(String.format("消息队列take错误：%s", e.getMessage()));
        }
    }
}
