import config.AllNodeSharedMsg;
import dao.Node;
import dao.NodeAddress;
import org.tio.core.Tio;

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
        /**
         * 初始化配置
         */
        AllNodeSharedMsg.size = 4;

        /**
         * 进行结点初始化
         */
        Node node = new Node(new NodeAddress("192.18.0.1",8080),0);

    }
}
