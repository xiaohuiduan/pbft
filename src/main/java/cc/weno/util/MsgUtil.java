package cc.weno.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cc.weno.config.AllNodeCommonMsg;
import cc.weno.dao.node.Node;
import cc.weno.dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
 * @data: 2020/2/25 下午6:27
 * @description: 进行加密和签名的pbftmsg工具类
 */
@Slf4j
public class MsgUtil {
    /**
     * 直接的RSA
     */
    private static RSA selfRsa = new RSA(Node.getInstance().getPrivateKey(), Node.getInstance().getPublicKey());

    private static Map<Integer, String> publicKeyMap = AllNodeCommonMsg.publicKeyMap;

    /**
     * 使用自己的私钥进行签名
     *
     * @param msg
     */
    public static void signMsg(PbftMsg msg) {
        String hash = String.valueOf(msg.hashCode());
        String sign = selfRsa.encryptBase64(hash, KeyType.PrivateKey);
        msg.setSign(sign);
    }

    /**
     * 使用对方的公钥进行加密
     *
     * @param msg
     */
    private static boolean encryptMsg(int index, PbftMsg msg) {
        String publicKey;
        if ((publicKey = publicKeyMap.get(index)) == null) {
            log.error("对方公钥为空");
            return false;
        }

        if (msg.getBody() == null) {
            log.warn("消息体为空，不进行加密");
            return true;
        }

        RSA encryptRsa;
        try {
            encryptRsa = new RSA(null, publicKey);
        } catch (Exception e) {
            log.error("使用对方公钥创建RSA对象失败");
            return false;
        }

        // 进行加密
        msg.setBody(encryptRsa.encryptBase64(msg.getBody(), KeyType.PublicKey));
        return true;
    }

    /**
     * 使用自己的私钥进行解密
     *
     * @param msg
     */
    private static boolean decryptMsg(PbftMsg msg) {
        if (msg.getBody() == null) {
            log.warn("消息体为空，不进行解密");
            return true;
        }
        String body;
        try {
            body = selfRsa.decryptStr(msg.getBody(), KeyType.PrivateKey);
        } catch (Exception e) {
            log.error(String.format("私钥解密失败!%s", e.getMessage()));
            return false;
        }
        msg.setBody(body);
        return true;
    }

    /**
     * 消息的前置处理：
     * ①：进行加密
     * ②：进行签名
     *
     * @param index
     * @param msg
     * @return
     */
    public static boolean preMsg(int index, PbftMsg msg) {
        if (!encryptMsg(index, msg)) {
            return false;
        }
        signMsg(msg);
        return true;
    }

    /**
     * 对消息进行后置处理
     *
     * @param msg
     * @return
     */
    public static boolean afterMsg(PbftMsg msg) {
        if (!isRealMsg(msg) || !decryptMsg(msg)) {
            return false;
        }
        return true;
    }

    /**
     * 判断消息是否被改变
     * 首先判断签名是否有问题
     *
     * @param msg 解密之前的消息！！！！！
     * @return
     */
    public static boolean isRealMsg(PbftMsg msg) {
        String publicKey = publicKeyMap.get(msg.getNode());
        RSA rsa;
        try {
            rsa = new RSA(null, publicKey);
        } catch (Exception e) {
            log.error(String.format("创建公钥RSA失败%s", e.getMessage()));
            return false;
        }
        // 获得此时消息的hash值
        String nowHash = String.valueOf(msg.hashCode());

        String sign = msg.getSign();
        try {
            // 获得hash值
            String hash = rsa.decryptStr(sign, KeyType.PublicKey);
            if (nowHash.equals(hash)) {
                return true;
            }
        } catch (Exception e) {
            log.warn(String.format("验证签名失效%s", e.getMessage()));
        }
        return false;
    }

}
