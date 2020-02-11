package until;

import config.AllNodeSharedMsg;
import dao.Node;
import dao.NodeAddress;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

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
 */
public class Pbft {
    /**
     * 进行处理的消息队列，don‘t care about what is , just get it ！！
     */
    private BlockingQueue<PbftMsg> msgQueue = new LinkedBlockingQueue<PbftMsg>();

    /**
     * 此时运行的结点
     */
    @Getter
    @Setter
    private Node node;

    /**
     * 日志处理
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void start(Node n){

        this.node = n;

        if (!initAddress()){
            logger.error(node.getIndex()+"：结点初始化所有节点的ip地址失败！");
            return ;
        }


        initServer();
        initClient();

        /**
         * 初始化连接，通过上一步获得的ip与其他结点链接
         */
        if (!initSocket()){
            logger.error(node.getIndex()+"：结点进行网络链接失败！");
            return;
        }

        /**
         * 结点开始运行
         */
        node.setRun(true);

        new Thread(new Runnable() {
            public void run() {

                while (true){
                    
                }
            }
        }).start();
    }

    /**
     * 开启客户端
     * @return
     */
    private boolean initClient() {
        return true;
    }

    /**
     * 开启服务端
     * @return
     */
    private boolean initServer() {
        return true;
    }

    /**
     *
     * todo 初始化socket链接
     */
    private boolean initSocket() {

        return true;
    }

    /**
     * 获得结点的ip地址
     */
    private boolean initAddress() {
        AllNodeSharedMsg.allNodeAddressMap = new ConcurrentHashMap<Integer, NodeAddress>(2<<10);
        // todo 将所有结点的信息加入到map中间
        AllNodeSharedMsg.allNodeAddressMap.put(node.getIndex(),node.getAddress());
        return true;
    }

    /**
     * 进行view同步
     */
    private void pubView() {

    }



}
