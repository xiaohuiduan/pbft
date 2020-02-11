package config;

import com.sun.org.apache.xalan.internal.lib.NodeInfo;
import dao.Node;
import dao.NodeAddress;

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
 * @data: 2020/1/22 下午3:27
 * @description: 所有的结点的需要共享的信息，这个地方主要是为了保存在区块链中结点的ip信息，通过node可以获得某一结点的信息
 */
public class AllNodeSharedMsg {
    /**
     * 区块链中结点的总结点数
     */
    public static int size;

    /**
     * 最大失效节点
     */
    private int maxf;

    /**
     * 获得最大失效结点的数量
     * @return
     */
    public static int getMaxf() {
        return (size-1)/3;
    }

    /**
     * 保存结点对应的ip地址和端口号
     */
    public static ConcurrentHashMap<Integer, NodeAddress> allNodeAddressMap;

    /**
     * view的值，-1代表view未被初始化
     */
    public static int view = -1;

}
