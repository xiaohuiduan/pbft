package p2p.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dao.pbft.MsgCollection;
import dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import p2p.common.MsgPacket;

import java.nio.ByteBuffer;
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
 * @data: 2020/2/12 下午6:08
 * @description: 客户端处理
 */
@Slf4j
public class P2pClientAioHandler implements ClientAioHandler {
    /**
     * this is heart packet，目的是告诉服务器端我存在
     */
    private static MsgPacket heartPacket = new MsgPacket();
    /**
     * 消息队列
     */
    private BlockingQueue<PbftMsg> msgQueue = MsgCollection.getInstance().getMsgQueue();

    /**
     * 创建心跳包
     *
     * @param channelContext
     * @return
     * @author tanyaowu
     */
    @Override
    public Packet heartbeatPacket(ChannelContext channelContext) {
        return heartPacket;
    }

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
        if (readableLength < MsgPacket.HEADER_LENGHT) {
            return null;
        }

        int bodyLength = buffer.getInt();
        if (bodyLength < 0) {
            throw new AioDecodeException("body length is invalid.romote: " + channelContext.getServerNode());
        }

        int usefulLength = MsgPacket.HEADER_LENGHT + bodyLength;

        if (usefulLength > readableLength) {
            return null;
        } else {
            MsgPacket packet = new MsgPacket();
            byte[] body = new byte[bodyLength];
            buffer.get(body);
            packet.setBody(body);
            return packet;
        }

    }

    /**
     * 编码
     *
     * @param packet
     * @param tioConfig
     * @param channelContext
     * @return
     * @author: tanyaowu
     */
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        MsgPacket msgPacket = (MsgPacket) packet;
        byte[] body = msgPacket.getBody();

        int bodyLength = 0;

        if (body != null) {
            bodyLength = body.length;
        }

        int len = MsgPacket.HEADER_LENGHT + bodyLength;

        ByteBuffer byteBuffer = ByteBuffer.allocate(len);
        byteBuffer.order(tioConfig.getByteOrder());
        byteBuffer.putInt(bodyLength);

        if (body != null) {
            byteBuffer.put(body);
        }
        return byteBuffer;
    }

    /**
     * 处理消息包
     *
     * @param packet
     * @param channelContext
     * @throws Exception
     * @author: tanyaowu
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
        PbftMsg pbftMsg = (PbftMsg) JSON.parse(msg);
        if (pbftMsg == null) {
            log.error("客户端将Json数据解析成pbft数据失败");
            return;
        }
        this.msgQueue.put(pbftMsg);
    }
}
