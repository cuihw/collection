package com.data.collection;

public class Constants {
    public static final String HOST1 =  "http://192.168.1.227/collect"; // test haibo
    public static final String HOST2 =  "http://192.168.1.253/collection"; // test  dongjw
    public static final String HOST3 =  "http://218.28.225.140/collection"; // test public server
    public static final String HOST4 =  "http://192.168.1.215/collect"; // test ziyin

    public static final String HOST = HOST2;

    public static final String BASE_URL_V1 = HOST + "/index.php/app/v1/";

    public static final String LOGIN = BASE_URL_V1 + "login";

    public static final String REFRESH_TOKEN = BASE_URL_V1 + "refreshToken";

    public static final String USER_INFO = BASE_URL_V1 + "getUserInfo";

    public static final String GET_MSG = BASE_URL_V1 + "getMsgs";

    public static final String GET_UNREAD_MSG = BASE_URL_V1 + "getUnreadMsgCount";

    public static final String READ_MSG = BASE_URL_V1 + "readMsgs";

    public static final String UPLOAD_IMAGES = BASE_URL_V1 + "uploadMultiImg";

    public static final String SAVE_COLLECTION_POINT = BASE_URL_V1 + "saveCollectionPoint";

    public static final String GET_COLLECTION_POINT = BASE_URL_V1 + "getCollectionList";

    public static final String GET_NAVI_POINT = BASE_URL_V1 + "getInterestList";

    public static final String UPLOAD_LOCATION = BASE_URL_V1 + "uploadLocation";

    public static final String GET_TRACE = BASE_URL_V1 + "fetchTrace";

    public static final String GET_NAVI_LIST = BASE_URL_V1 + "getInterestList";

    public static final String SAVE_CHECK = BASE_URL_V1 + "saveCheck";

    public static final String GET_CHECK_LIST = BASE_URL_V1 + "getCheckList";

    public static final String NO_PROJECT_INFO  ="没有项目信息，请先连接网络，登录服务器";

    public static final String SUCCEED = "1";

    public static final int TRACE_INTERVAL = 1000 * 10 ; //  跟踪位置地点间隔时间

    public static final int UPLOAD_TRACE_INTERVAL = 1000 * 60 * 5 ; //  上报地点间隔时间
    public static final String DEGREE_MIN_SENCOND = "DMS";

    public static final double DIFF = 0.0001;

    public static final double DIFF2 = 0.0005;

    public static final double RANGE = 0.1;

    public static final int GET_FILE_PATH = 1000;

    public static final int GET_MEASURE = 1001;

    public static final double latitude = 34.7967643;
    public static final double longitude = 113.6019350;
    public static final int levelOfDetail = 14;
}
