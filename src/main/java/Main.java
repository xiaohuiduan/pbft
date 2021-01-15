import cc.weno.config.StartConfig;
import cc.weno.dao.node.Node;
import cc.weno.dao.node.NodeAddress;
import cc.weno.dao.pbft.MsgType;
import cc.weno.dao.pbft.PbftMsg;
import cc.weno.util.ClientUtil;
import cc.weno.util.StartPbft;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


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
 * @data: 2020/1/22 下午2:46
 * @description: 程序运行开始类
 * 启动参数顺序：ip，port，index，认证请求消息
 */
@Slf4j
public class Main {

    public static void main(String[] args) {

        int i = 0;
        String ip = "127.0.0.1";
        int port = 8080 + i;

        StartConfig.basePath = "C:\\Users\\XiaoHui\\Desktop\\data\\";
        int index = i;

//        if (args.length != 4) {
//            log.error("参数错误");
//            return;
//        }
////        程序启动ip地址
//        String ip = args[0];
////        端口
//        int port = Integer.parseInt(args[1]);
////        程序启动index
//        int index = Integer.parseInt(args[2]);
////        文件保存位置，在文件保存位置必须存在一个oldIp.json的文件
//        StartConfig.basePath = args[3];


        createIpJsonFile(StartConfig.basePath);

        Node node = Node.getInstance();
        node.setIndex(index);
        NodeAddress nodeAddress = new NodeAddress();
        nodeAddress.setIp(ip);
        nodeAddress.setPort(port);
        node.setAddress(nodeAddress);
        StartPbft.start();

//        可以在这里发送消息
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String str = scanner.next();
            PbftMsg msg = new PbftMsg(MsgType.PRE_PREPARE, 0);
            msg.setBody(str);
            ClientUtil.prePrepare(msg);
        }
    }

    /**
     * 如果文件或者文件夹不存在则创建
     *
     * @param basePath
     */
    private static void createIpJsonFile(String basePath) {
        File dir = new File(basePath);

        // 如果目录不存在
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(basePath + "ip.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

