package dao;

import lombok.Data;

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
 * @data: 2020/1/22 下午3:22
 * @description: 结点自身的信息
 */
@Data
public class Node {

    /**
     * 结点地址的信息
     */
    private NodeAddress address;
    /**
     * 这个代表了结点的序号
     */
    private int index;

    /**
     * 当前视图的编号，通过这个编号可以算出主节点的序号
     */
    private int viewNum;

    /**
     * 判断结点是否运行
     */
    private boolean isRun = false;

    /**
     * 视图状态，判断是否ok，
     */
    private volatile boolean viewOK;

    public Node(NodeAddress address, int index) {
        this.address = address;
        this.index = index;
    }
}
