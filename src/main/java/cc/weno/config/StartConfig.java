package cc.weno.config;

import cn.hutool.core.io.file.FileReader;
import com.alibaba.fastjson.JSON;
import cc.weno.dao.bean.ReplayJson;
import cc.weno.dao.node.Node;
import cc.weno.dao.node.NodeAddress;
import cc.weno.dao.node.NodeBasicInfo;
import lombok.extern.slf4j.Slf4j;
import org.tio.client.ClientChannelContext;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;
import cc.weno.p2p.P2PConnectionMsg;
import cc.weno.p2p.common.Const;
import cc.weno.p2p.server.P2PServerAioHandler;
import cc.weno.p2p.server.ServerListener;
import cc.weno.util.ClientUtil;
import cc.weno.util.PbftUtil;

import java.io.IOException;
import java.util.List;
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
    private Node node = Node.getInstance();
    public static String basePath;

    /**
     * 初始化操作
     *
     * @return 成功返回true，other false
     */
    public boolean startConfig() {
        return initAddress() && initServer() && initClient();
    }

    /**
     * 开启客户端
     *
     * @return
     */
    private boolean initClient() {
        P2PConnectionMsg.CLIENTS = new ConcurrentHashMap<>(AllNodeCommonMsg.allNodeAddressMap.size());

        // allNodeAddressMap保存了结点index和address信息。
        for (NodeBasicInfo basicInfo : AllNodeCommonMsg.allNodeAddressMap.values()) {
            NodeAddress address = basicInfo.getAddress();
            log.info(String.format("节点%d尝试链接%s", node.getIndex(), basicInfo));
            ClientChannelContext context = ClientUtil.clientConnect(address.getIp(), address.getPort());
            if (context != null) {
                // 将通道进行保存
                ClientUtil.addClient(basicInfo.getIndex(), context);
            } else {
                log.warn(String.format("结点%d-->%s：%d连接失败", basicInfo.getIndex(), address.getIp(), address.getPort()));
            }
        }
        log.info("client链接服务端的数量" + P2PConnectionMsg.CLIENTS.size());
        return true;
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
            tioServer.start(node.getAddress().getIp(), node.getAddress().getPort());
        } catch (IOException e) {
            log.error("服务端启动错误！！" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获得结点的ip地址
     *
     * @return 成功返回true
     */
    private boolean initAddress() {
        FileReader fileReader = new FileReader(PbftUtil.ipJsonPath);
        List<String> ipJsonStr = fileReader.readLines();
        for (String s : ipJsonStr) {
            ReplayJson replayJson = JSON.parseObject(s, ReplayJson.class);
            NodeAddress nodeAddress = new NodeAddress();
            nodeAddress.setIp(replayJson.getIp());
            nodeAddress.setPort(replayJson.getPort());
            NodeBasicInfo nodeBasicInfo = new NodeBasicInfo();
            nodeBasicInfo.setAddress(nodeAddress);
            nodeBasicInfo.setIndex(replayJson.getIndex());
            AllNodeCommonMsg.allNodeAddressMap.put(replayJson.getIndex(), nodeBasicInfo);
            AllNodeCommonMsg.publicKeyMap.put(replayJson.getIndex(), replayJson.getPublicKey());
        }

        // 将自己节点信息写入文件
        if (AllNodeCommonMsg.allNodeAddressMap.values().size() < 3 && !AllNodeCommonMsg.allNodeAddressMap.containsKey(node.getIndex())) {
            PbftUtil.writeIpToFile(node);
            return true;
        }
        if (AllNodeCommonMsg.allNodeAddressMap.containsKey(node.getIndex())) {
            log.warn("已经存在此节点");
            return false;
        }
        log.info(String.format("ip.json文件数量%s", ipJsonStr.size()));
        log.info(String.format("内存地址%s", AllNodeCommonMsg.allNodeAddressMap.values().size()));
        return AllNodeCommonMsg.allNodeAddressMap.values().size() == ipJsonStr.size();
    }
}
