package dao.pbft;

import cn.hutool.core.util.IdUtil;
import config.AllNodeCommonMsg;
import lombok.Data;

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
 * @data: 2020/1/22 下午3:56
 * @description: 进行Pbft发送的消息、
 */
@Data
public class PbftMsg {
    /**
     * 消息类型
     */
    private int msgType;

    /**
     * 消息体
     */
    private String body;

    /**
     * 消息发起的结点编号
     */
    private int node;

    /**
     * 消息发送的目的地
     */
    private int toNode;

    /**
     * 消息时间戳
     */
    private long time;

    /**
     * 检测是否通过
     */
    private boolean isOk;

    /**
     * 结点视图
     */
    private int viewNum;

    /**
     * 使用UUID进行生成
     */
    private String id;

    private PbftMsg() {
    }

    public PbftMsg(int msgType, int node) {
        this.msgType = msgType;
        this.node = node;
        this.time = System.currentTimeMillis();
        this.id = IdUtil.randomUUID();
        this.viewNum = AllNodeCommonMsg.view;
    }
}
