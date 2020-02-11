package msg;

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
 * @data: 2020/1/22 下午3:34
 * @description: pbft的提交的阶段类型
 */
public enum PBFTTypeEnum {
    REQUEST_VIEW("请求视图",0),
    CHANGE_VIEW("视图变更",1),
    REQUEST_DATA("请求数据",2),
    PRE_PREPARE("预准备阶段",3),
    PREPARE("准备阶段",4),
    COMMIT("提交阶段",5),
    REPLAY("回复",6);

    private String msg;
    private int code;

    PBFTTypeEnum(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    /**
     * 根据状态码返回对应的enum
     * @param code
     * @return
     */
    public static PBFTTypeEnum find(int code){
        for (PBFTTypeEnum ve:PBFTTypeEnum.values()){
            if (ve.code == code){
                return ve;
            }
        }
        return null;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
