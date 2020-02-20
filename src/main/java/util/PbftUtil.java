package util;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.json.JSONUtil;
import config.AllNodeCommonMsg;
import dao.bean.IpJson;
import dao.node.Node;
import dao.node.NodeAddress;
import dao.node.NodeBasicInfo;
import dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;

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
 * @data: 2020/2/15 下午10:38
 * @description: pbft算法的工具类
 */
@Slf4j
public class PbftUtil {

    public static boolean checkMsg(PbftMsg msg) {

        return true;
    }

    public static void save(PbftMsg msg) {
    }

    /**
     * 讲ip信息写入文件和AllNodeCommonMsg.allNodeAddressMap
     *
     * @param node
     */
    public static void writeIpToFile(Node node) {
        log.info(String.format("节点%s写入文件", node.getIndex()));
        FileWriter writer = new FileWriter("./ip.json");
        IpJson ipJson = new IpJson();
        ipJson.setIp(node.getAddress().getIp());
        ipJson.setPort(node.getAddress().getPort());
        ipJson.setIndex(node.getIndex());
        String json = JSONUtil.toJsonStr(ipJson);
        writer.append(json + "\n");
    }

}
