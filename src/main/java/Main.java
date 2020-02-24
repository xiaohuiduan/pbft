import dao.node.Node;
import dao.node.NodeAddress;
import dao.pbft.MsgType;
import dao.pbft.PbftMsg;
import util.ClientUtil;
import util.StartPbft;

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
 */
public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        int i = 3;
        Node node = Node.getInstance();
        node.setIndex(i);
        NodeAddress nodeAddress = new NodeAddress();
        nodeAddress.setIp("127.0.0.1");
        nodeAddress.setPort(8080+i);
        node.setAddress(nodeAddress);
        StartPbft.start();

        while (true){
            String str = s.next();
            PbftMsg msg = new PbftMsg(MsgType.PRE_PREPARE,0);
            msg.setBody(str);
            ClientUtil.prePrepare(msg);
        }
    }


}
