import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.AtomicLongMap;
import config.AllNodeCommonMsg;
import dao.bean.IpJson;
import dao.node.Node;
import dao.node.NodeAddress;
import dao.node.NodeBasicInfo;
import dao.pbft.PbftMsg;
import org.tio.utils.json.Json;
import util.StartPbft;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
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
 * @data: 2020/1/22 下午2:46
 * @description: 程序运行开始类
 */
public class Main {
    public static void main(String[] args) {
        int i = 11;
        Node node = Node.getInstance();
        node.setIndex(i);
        NodeAddress nodeAddress = new NodeAddress();
        nodeAddress.setIp("127.0.0.1");
        nodeAddress.setPort(8080+i);
        node.setAddress(nodeAddress);
        StartPbft.start();
    }


}
