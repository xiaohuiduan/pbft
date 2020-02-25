package cc.weno.dao.pbft;
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
 * @data: 2020/2/14 上午11:35
 * @description: 消息类型
 */
public class MsgType {
    /**
     * 请求视图
     */
    public static final int GET_VIEW = -1;

    /**
     * 变更视图
     */
    public static final int CHANGE_VIEW = 0;

    /**
     * 预准备阶段
     */
    public static final int PRE_PREPARE = 1;

    /**
     * 准备阶段
     */
    public static final int PREPARE = 2;

    /**
     * 提交阶段
     */
    public static final int COMMIT = 3;

    /**
     * ip消息回复回复阶段
     */
    public static final int CLIENT_REPLAY = 4;
}
