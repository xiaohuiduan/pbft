package cc.weno.dao.pbft;

import cn.hutool.core.util.IdUtil;
import cc.weno.config.AllNodeCommonMsg;
import lombok.Data;

import java.util.Objects;

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
     * 消息体
     */
    private String body;

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

    /**
     * 消息的签名
     */
    private String sign;

    private PbftMsg() {
    }

    public PbftMsg(int msgType, int node) {
        this.msgType = msgType;
        this.node = node;
        this.time = System.currentTimeMillis();
        this.id = IdUtil.randomUUID();
        this.viewNum = AllNodeCommonMsg.view;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PbftMsg)) {
            return false;
        }
        PbftMsg msg = (PbftMsg) o;
        return getMsgType() == msg.getMsgType() &&
                getNode() == msg.getNode() &&
                getToNode() == msg.getToNode() &&
                getTime() == msg.getTime() &&
                isOk() == msg.isOk() &&
                getViewNum() == msg.getViewNum() &&
                Objects.equals(getBody(), msg.getBody()) &&
                Objects.equals(getId(), msg.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMsgType(), getBody(), getNode(), getToNode(), getTime(),  getViewNum(), getId());
    }
}
