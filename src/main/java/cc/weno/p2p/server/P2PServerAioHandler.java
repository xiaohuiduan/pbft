package cc.weno.p2p.server;

import com.alibaba.fastjson.JSON;
import cc.weno.dao.pbft.MsgType;
import cc.weno.util.MsgUtil;
import cc.weno.dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;
import cc.weno.p2p.common.MsgPacket;

import java.nio.ByteBuffer;

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
 * @data: 2020/2/12 上午12:09
 * @description: 服务端的Handler
 */
@Slf4j
public class P2PServerAioHandler implements ServerAioHandler {

    private ServerAction action = ServerAction.getInstance();

    /**
     * 根据ByteBuffer解码成业务需要的Packet对象.
     * 如果收到的数据不全，导致解码失败，请返回null，在下次消息来时框架层会自动续上前面的收到的数据
     *
     * @param buffer         参与本次希望解码的ByteBuffer
     * @param limit          ByteBuffer的limit
     * @param position       ByteBuffer的position，不一定是0哦
     * @param readableLength ByteBuffer参与本次解码的有效数据（= limit - position）
     * @param channelContext
     * @return
     * @throws AioDecodeException
     */
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        // 假如包的长度小于基本长度，毋庸置疑，包没有接收完
        if (readableLength < MsgPacket.HEADER_LENGHT) {
            return null;
        }
        // 读取发送消息的长度
        int bodyLength = buffer.getInt();

        //数据不正确，则抛出AioDecodeException异常
        if (bodyLength < 0) {
            throw new AioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
        }

        // 客户端发送消息的长度
        int neededLength = MsgPacket.HEADER_LENGHT + bodyLength;

        // 不够消息体长度(剩下的buffe组不了消息体)
        if (readableLength < neededLength) {
            return null;
        } else { //组包成功
            MsgPacket imPacket = new MsgPacket();
            if (bodyLength > 0) {
                byte[] dst = new byte[bodyLength];
                buffer.get(dst);
                imPacket.setBody(dst);
            }
            return imPacket;
        }
    }

    /**
     * 编码
     *
     * @param packet
     * @param tioConfig
     * @param channelContext
     * @return
     * @author: xiaohuiduan
     */
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        MsgPacket msgPacket = (MsgPacket) packet;
        byte[] body = msgPacket.getBody();
        int bodyLen = 0;

        if (body != null) {
            bodyLen = body.length;
        }

        //bytebuffer的总长度是 = 消息头的长度 + 消息体的长度
        int allLen = MsgPacket.HEADER_LENGHT + bodyLen;
        //创建一个新的bytebuffer
        ByteBuffer buffer = ByteBuffer.allocate(allLen);

        //设置字节序
        buffer.order(tioConfig.getByteOrder());

        //写入消息头----消息头的内容就是消息体的长度
        buffer.putInt(bodyLen);

        //写入消息体
        if (body != null) {
            buffer.put(body);
        }
        return buffer;
    }

    /**
     * 处理消息包
     *
     * @param packet
     * @param channelContext
     * @throws Exception
     * @author: xiaohuiduan
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {

        MsgPacket msgPacket = (MsgPacket) packet;
        byte[] body = msgPacket.getBody();
        // 空的很可能为心跳包
        if (body == null) {
            return;
        }

        String msg = new String(body, MsgPacket.CHARSET);
        // 如果数据不是json数据，代表数据有问题
        if (!JSON.isValid(msg)) {
            return;
        }
        log.info("服务端接受消息：" + msg);
        PbftMsg pbftMsg = JSON.parseObject(msg, PbftMsg.class);
        if (pbftMsg == null) {
            log.error("客户端将Json数据解析成pbft数据失败");
            return;
        }

        if ((pbftMsg.getMsgType() != MsgType.CLIENT_REPLAY && pbftMsg.getMsgType() != MsgType.GET_VIEW) && !MsgUtil.afterMsg(pbftMsg)) {
            log.warn("数据检查签名或者解密失败");
            return;
        }
        // 服务端对消息进行处理
        action.doAction(channelContext, pbftMsg);
    }
}
