package com.data.collection;

public class Constants {

    public static final String HOST = "http://192.168.1.227/";

    public static final String BASE_URL_V1 = HOST + "collect/index.php/app/v1/";

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

    public static final String NO_PROJECT_INFO  ="没有项目信息，请先连接网络，登录服务器";

    public static final String SUCCEED = "1";


}
