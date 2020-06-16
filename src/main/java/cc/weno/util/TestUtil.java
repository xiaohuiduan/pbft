package cc.weno.util;

import cc.weno.config.StartConfig;
import cc.weno.dao.bean.ReplayJson;
import cc.weno.dao.node.Node;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.json.JSONUtil;
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
 * @data: 2020/2/26 下午8:12
 * @description: 实验日志写入
 */
@Slf4j
public class TestUtil {


    public static String timePath = StartConfig.basePath+"oldTime.txt";

    public static boolean flag = true;
    public static long startTime;
    public static long endTime;

    /**
     * 信息写入文件和AllNodeCommonMsg.allNodeAddressMap
     */
    synchronized public static void writeOkTime(long time, int index) {
        if (!flag) {
            return;
        }
        log.info(String.format("节点%s写入时间日志", index));
        FileWriter writer = new FileWriter(timePath);
        StringBuffer result = new StringBuffer();
        result.append(index);
        result.append("耗时：");
        result.append(time);
        result.append("\n");
        writer.append(result.toString());
        flag = false;
    }

    /**
     * 信息写入文件和AllNodeCommonMsg.allNodeAddressMap
     */
    synchronized public static void writeBadTime(long time, int index) {
        if (!flag) {
            return;
        }
        log.info(String.format("节点%s写入时间日志", index));
        FileWriter writer = new FileWriter(timePath);
        StringBuffer result = new StringBuffer();
        result.append(index);
        result.append("bad耗时：");
        result.append(time);
        result.append("\n");
        writer.append(result.toString());
        flag = false;
    }
}
