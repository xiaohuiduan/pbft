package cc.weno.util;

import cc.weno.config.AllNodeCommonMsg;
import cc.weno.dao.bean.DbDao;
import cc.weno.dao.node.Node;
import cc.weno.dao.pbft.MsgCollection;
import cc.weno.dao.pbft.PbftMsg;
import lombok.extern.slf4j.Slf4j;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.*;
import java.util.*;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

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
 * @data: 2020/3/1 上午12:13
 * @description: levelDB操作
 */
@Slf4j
public class DbUtil {
    public static String dbFilePath;

    private static DB db = null;
    private static Options options = new Options();
    private static boolean flag = true;

    private static boolean init() {
        options = new Options();
        options.createIfMissing(true);
        try {
            db = factory.open(new File(dbFilePath), options);
        } catch (IOException e) {
            log.warn(String.format("数据库获取失败%s", e.getMessage()));
            return false;
        }
        return true;
    }


    /**
     * 插入一个dao
     *
     * @param dao
     */
    private static void insert(DbDao dao) {
        try {
            db.put(String.valueOf(dao.getNode()).getBytes(), daoToBytes(dao));
        } catch (IOException e) {
            log.warn(String.format("对象序列化失败%s", e.getMessage()));
        }
    }

    synchronized public static void save() {
        if (!flag) {
            return;
        }
        flag = false;
        log.info(String.format("保存的大小%s", MsgCollection.getInstance().getDbDaos().size()));
        if (init()) {
            for (DbDao dao :
                    MsgCollection.getInstance().getDbDaos()) {
                insert(dao);
            }
            try {
                db.close();
            } catch (IOException e) {
                log.warn(String.format("levelDB关闭失败%s", e.getMessage()));
            }
        }

    }

    /**
     * 进行遍历
     */
    private static void get() {
        Options options = new Options();
        options.createIfMissing(true);
        DB db = null;
        try {
            db = factory.open(new File(dbFilePath), options);
        } catch (IOException e) {
            log.warn(String.format("数据库获取失败%s", e.getMessage()));
            return;
        }
        DBIterator iterator = db.iterator();
        List<byte[]> list = new ArrayList<byte[]>();
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> next = iterator.next();
            byte[] value = next.getValue();
            list.add(value);
        }
        System.out.println(list.size());
        for (byte[] bytes : list) {
            try {
                DbDao dbDao = (DbDao) bytesToDao(bytes);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        dbFilePath = "/home/xiaohui/桌面/test/29.db";
        get();
    }

    /**
     * @param node 谁发送的信息
     * @param msg
     */
    public static void addDaotoList(int node, PbftMsg msg) {
        DbDao dbDao = new DbDao();
        dbDao.setNode(node);
        dbDao.setPublicKey(AllNodeCommonMsg.publicKeyMap.get(node));
        dbDao.setTime(msg.getTime());
        dbDao.setViewNum(msg.getViewNum());
        MsgCollection.getInstance().getDbDaos().add(dbDao);
    }

    /**
     * 将对象序列化
     *
     * @param dao
     * @return
     * @throws IOException
     */
    private static byte[] daoToBytes(DbDao dao) throws IOException {
        //创建内存输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dao);
        oos.writeObject(null);
        oos.close();
        baos.close();
        return baos.toByteArray();
    }

    /**
     * 将对象反序列化
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    private static Object bytesToDao(byte[] bytes) throws IOException, ClassNotFoundException {
        //创建内存输出流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        Object obj = null;
        while ((obj = inputStream.readObject()) != null) {
            DbDao dbDao = (DbDao) obj;
            System.out.println(dbDao);
        }
        inputStream.close();
        byteArrayInputStream.close();
        return obj;

    }
}
