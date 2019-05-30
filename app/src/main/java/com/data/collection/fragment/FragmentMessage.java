package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.MessageData;
import com.data.collection.data.greendao.MessageDataDao;
import com.data.collection.dialog.PopupDialogMessage;
import com.data.collection.module.MessageBean;
import com.data.collection.module.MessageDatas;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.DateUtils;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentMessage#} factory method to
 * create an instance of this fragment.
 */
public class FragmentMessage extends FragmentBase {
    private static final String TAG = "FragmentMessage";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview)
    ListView listview;

    CommonAdapter<MessageData> adapter;


    List<MessageData> list;

    public static void start(Context context){
        Bundle bundle = new Bundle();
        // TODO add the constant in  CommonActivity replace FRAGMENT_PROJECT
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_MESSAGE);
        CommonActivity.start(context, bundle);
    }

    public static FragmentMessage getInstance(){
        return new FragmentMessage();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        //  replace Layout
        view = inflater.inflate(R.layout.fragment_settings_message, container, false);
        bindButterKnife();
        initListener();
        getMessageFromNet();
        initView();
        return view;
    }
    private void getMessageFromNet() {

        list = getMessageDataBase();
        Map<String, Object> params = new HashMap<>();

        if (list != null && list.size() > 1) {
            Long second = DateUtils.getSecond(list.get(0).getCreate_time());
            params.put("updated_at", second);
        }

        HttpRequest.postData(Constants.GET_MSG, params, new HttpRequest.RespListener<MessageBean>() {
            @Override
            public void onResponse(int status, MessageBean bean) {
                if (status == 0 && bean.getCode().equals("1")) {
                    handleMessage(bean);
                }
            }
        });
    }

    private void handleMessage(MessageBean bean) {
        list = bean.getData().getData();
        adapter.replaceAll(list);
        saveToDataBase(list);
    }

    private void saveToDataBase(List<MessageData> list) {
        DaoSession daoSession =  App.getInstence().getDaoSession();
        MessageDataDao messageDataDao = daoSession.getMessageDataDao();
        for (MessageData md: list) {
            if (!messageDataDao.hasKey(md)) {
                daoSession.insertOrReplace(md);
            }
        }
    }

    private List<MessageData> getMessageDataBase() {
        DaoSession daoSession =  App.getInstence().getDaoSession();
        MessageDataDao messageDataDao = daoSession.getMessageDataDao();
        QueryBuilder<MessageData> qb = daoSession.queryBuilder(MessageData.class).limit(30)
                .orderDesc(MessageDataDao.Properties.Create_time);

        List<MessageData> list = qb.list(); //查出当前对应的数据
        return list;
    }

    private void initView() {
        adapter = new CommonAdapter<MessageData>(getContext(),R.layout.item_message, list) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, MessageData item, int position) {
                int icon = item.getType().equals("0") ? R.mipmap.icon_msg_unread: R.mipmap.icon_msg_read;
                helper.setImageResource(R.id.message_read, icon);
                helper.setText(R.id.title_message, item.getTitle());
                helper.setText(R.id.messgage_body, item.getContent());
                helper.setText(R.id.messgage_date, item.getCreate_time());
            }
        };
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showMessage(position);
            }
        });
    }

    private void showMessage(int pos) {
        if (list != null && list.size() > pos) {
            MessageData messageData = list.get(pos);
            LsLog.w(TAG, "messageData = " + messageData.getTitle());

            PopupDialogMessage dialog = PopupDialogMessage.create(getContext(), messageData.getTitle(), messageData.getContent(), "确定",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMessageRead(messageData);
                        }
                    });
            String fromTime = "发件人:" + messageData.getPusher() + "            时间" + messageData.getCreate_time();
            dialog.show();
            dialog.setFromMsg(fromTime);
        }
    }

    private void setMessageRead(MessageData messageData) {
        messageData.setType("1");
        messageData.setRead_at(DateUtils.getNow(DateUtils.fmtYYYYMMDDhhmmss));
        DaoSession daoSession =  App.getInstence().getDaoSession();
        daoSession.update(messageData);

        // 尝试更新后头已读消息
    }




    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->getActivity().finish());
    }



}
