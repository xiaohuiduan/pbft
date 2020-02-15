package config;

import dao.node.NodeAddress;
import dao.node.NodeBasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;
import p2p.P2PConnectionMsg;
import p2p.common.Const;
import p2p.server.P2PServerAioHandler;
import p2p.server.ServerListener;
import util.ClientUtil;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

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
 * @data: 2020/2/13 下午7:20
 * @description: 启动的时候需要进行的配置
 * 在这里会进行下面的操作：
 * 初始化网络链接 使用tio构建P2P网络
 */
@Slf4j
public class StartConfig {

    /**
     * 初始化操作
     *
     * @return 成功返回true，other false
     */
    public boolean startConfig() {
        if (initAddress() && initServer() && initClient()) {
            return true;
        }
        return false;
    }

    /**
     * 开启客户端
     *
     * @return
     */
    private boolean initClient() {

        P2PConnectionMsg.CLIENTS = new ConcurrentHashMap<Integer, ClientChannelContext>(AllNodeCommonMsg.allNodeAddressMap.size());

        // allNodeAddressMap保存了结点index和address信息。
        for (NodeBasicInfo basicInfo : AllNodeCommonMsg.allNodeAddressMap.values()) {
            NodeAddress address = basicInfo.getAddress();
            ClientChannelContext context = ClientUtil.clientConnect(address.getIp(), address.getPort());
            if (context != null) {
                // 将通道进行保存
                ClientUtil.addClient(basicInfo.getIndex(), context);
            } else {
                log.warn(String.format("结点%d-->%s：%d连接失败", basicInfo.getIndex(), address.getIp(), address.getPort()));
            }
        }
        if (P2PConnectionMsg.CLIENTS.size() != 0) {
            log.error("一个结点都没有连接上");
            return true;
        }
        return false;
    }


    /**
     * 开启服务端
     *
     * @return
     */
    private boolean initServer() {

        // 处理消息
        ServerAioHandler handler = new P2PServerAioHandler();
        // 监听
        ServerAioListener listener = new ServerListener();
        // 配置
        ServerTioConfig config = new ServerTioConfig("服务端", handler, listener);
        // 设置timeout，如果一定时间内client没有消息发送过来，则断开与client的连接
        config.setHeartbeatTimeout(Const.TIMEOUT);

        TioServer tioServer = new TioServer(config);
        try {
            // 启动
            tioServer.start(Const.SERVER, Const.PORT);
        } catch (IOException e) {
            log.error("服务端启动错误！！" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获得结点的ip地址
     */
    private boolean initAddress() {
        AllNodeCommonMsg.allNodeAddressMap = new ConcurrentHashMap<Integer, NodeBasicInfo>(2 << 10);
        // todo 将所有结点的信息加入到map中间
//        AllNodeSharedMsg.allNodeAddressMap.put(node.getIndex(),node.getAddress());
        return true;
    }
}
