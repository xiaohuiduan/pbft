package cc.weno.dao.node;

import cc.weno.config.AllNodeCommonMsg;
import cn.hutool.crypto.asymmetric.RSA;
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
public class Node extends NodeBasicInfo {


    private static Node node = new Node();

    /**
     * 单例设计模式
     *
     * @return
     */
    public static Node getInstance() {
        return node;
    }

    private Node() {
        RSA rsa = new RSA();
        this.setPrivateKey(rsa.getPrivateKeyBase64());
        this.setPublicKey(rsa.getPublicKeyBase64());
    }


    /**
     * 判断结点是否运行
     */
    private boolean isRun = false;

    /**
     * 视图状态，判断是否ok，
     */
    private volatile boolean viewOK;

    /**
     * 公钥
     */
    private String publicKey;
    /**
     * 私钥
     */
    private String privateKey;
}
