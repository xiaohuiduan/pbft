package cc.weno.config;

import cc.weno.dao.node.NodeBasicInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
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
 * @description: 所有的结点的需要相同的信息，（假如不相同，则需要同步）
 */
@Slf4j
public class AllNodeCommonMsg {

    /**
     * 获得最大失效结点的数量
     *
     * @return
     */
    public static int getMaxf() {
        return (getSize() - 1) / 3;
    }

    public static int getAgreeNum(){
        return 2 * AllNodeCommonMsg.getMaxf() + 1;
    }

    /**
     * 获得主节点的index序号
     *
     * @return
     */
    public static int getPriIndex() {
        return view % getSize();
    }

    /**
     * 保存结点对应的ip地址和端口号
     */
    public static ConcurrentHashMap<Integer, NodeBasicInfo> allNodeAddressMap = new ConcurrentHashMap<>(2 << 10);

    /**
     * 所有节点公钥
     */
    public static Map<Integer,String> publicKeyMap = new ConcurrentHashMap<>(2<<10);


    /**
     * view的值，0代表view未被初始化
     * 当前视图的编号，通过这个编号可以算出主节点的序号
     */
    public volatile static int view = 0;

    /**
     * @return 区块链中结点的总结点数
     */
    public static int getSize() {
        return allNodeAddressMap.size() + 1;
    }
}
