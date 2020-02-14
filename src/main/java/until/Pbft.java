package until;

import com.alibaba.fastjson.JSON;
import dao.node.Node;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.core.Tio;
import p2p.P2PConnectionMsg;
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
    private boolean pubView() {
        log.info("结点开始进行view同步操作");
        // 初始化view的msg
        PbftMsg view = new PbftMsg(MsgType.GET_VIEW, node.getIndex());
        String jsonView = JSON.toJSONString(view);
        MsgPacket msgPacket = new MsgPacket();
        try {
            msgPacket.setBody(jsonView.getBytes(MsgPacket.CHARSET));
            for (ClientChannelContext client : P2PConnectionMsg.CLIENTS.values()) {
                Tio.send(client, msgPacket);
            }
        } catch (UnsupportedEncodingException e) {
            log.error(String.format("结点%d同步失败", node.getIndex()));
            return false;
        }
        return true;
    }
}
