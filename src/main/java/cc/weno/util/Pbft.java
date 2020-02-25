package cc.weno.util;

import cc.weno.config.AllNodeCommonMsg;
import cc.weno.dao.node.Node;
import cc.weno.dao.pbft.MsgType;
import cc.weno.dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;

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
 * @data: 2020/1/22 下午3:53
 * @description: pbft的工作流程
 * this is the most important thing
 * 这个是整个算法的流程
 */
@Slf4j
public class Pbft {

    private Node node = Node.getInstance();

    /**
     * 发送view请求
     *
     * @return
     */
    public boolean pubView() {

        /**
         * 如果区块链中的网络节点小于3
         */
        if (AllNodeCommonMsg.allNodeAddressMap.size() < 3) {
            log.warn("区块链中的节点小于等于3");
            node.setViewOK(true);
            // 将节点消息广播出去
            ClientUtil.publishIpPort(node.getIndex(), node.getAddress().getIp(), node.getAddress().getPort());
            return true;
        }

        log.info("结点开始进行view同步操作");
        // 初始化view的msg
        PbftMsg view = new PbftMsg(MsgType.GET_VIEW, node.getIndex());
        ClientUtil.clientPublish(view);
        return true;
    }

    /**
     * 视图发送该表
     *
     * @return
     */
    public boolean changeView() {

        return true;
    }
}
