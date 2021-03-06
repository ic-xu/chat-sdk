package com.rance.chatui.ui.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;
import com.rance.chatui.R;
import com.rance.chatui.adapter.ChatAdapter;
import com.rance.chatui.adapter.CommonFragmentPagerAdapter;
import com.rance.chatui.enity.FullImageInfo;
import com.rance.chatui.enity.MessageInfo;
import com.rance.chatui.ui.fragment.ChatEmotionFragment;
import com.rance.chatui.ui.fragment.ChatFunctionFragment;
import com.rance.chatui.util.Constants;
import com.rance.chatui.util.GlobalOnItemClickManagerUtils;
import com.rance.chatui.util.MediaManager;
import com.rance.chatui.util.MessageCenter;
import com.rance.chatui.widget.ChatContextMenu;
import com.rance.chatui.widget.EmotionInputDetector;
import com.rance.chatui.widget.NoScrollViewPager;
import com.rance.chatui.widget.StateButton;
import com.rance.im.BaseMessage;
import com.rance.im.message.P2chatMessage;
import com.rance.im.message.PMessage;
import com.rance.im.netty.NettyTcpClient;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 作者：Rance on 2016/11/29 10:47
 * 邮箱：rance935@163.com
 */
public class IMActivity extends AppCompatActivity {
    private static final String TAG = "IMActivity";
    RecyclerView chatList;
    ImageView emotionVoice;
    EditText editText;
    TextView voiceText;
    ImageView emotionButton;
    ImageView emotionAdd;
    StateButton emotionSend;
    NoScrollViewPager viewpager;
    RelativeLayout emotionLayout;

    private EmotionInputDetector mDetector;
    private ArrayList<Fragment> fragments;
    private ChatEmotionFragment chatEmotionFragment;
    private ChatFunctionFragment chatFunctionFragment;
    private CommonFragmentPagerAdapter adapter;

    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    private List<MessageInfo> messageInfos;
    //录音相关
    int animationRes = 0;
    int res = 0;
    AnimationDrawable animationDrawable = null;
    private ImageView animView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewByIds();
        EventBus.getDefault().register(this);
        initWidget();
        handleIncomeAction();
    }

    private void findViewByIds() {
        chatList = findViewById(R.id.chat_list);
        emotionVoice = findViewById(R.id.emotion_voice);
        editText = findViewById(R.id.edit_text);
        voiceText = findViewById(R.id.voice_text);
        emotionButton = findViewById(R.id.emotion_button);
        emotionAdd = findViewById(R.id.emotion_add);
        emotionSend = findViewById(R.id.emotion_send);
        emotionLayout = findViewById(R.id.emotion_layout);
        viewpager = findViewById(R.id.viewpager);
    }

    private void handleIncomeAction() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }

        MessageCenter.handleIncoming(bundle, getIntent().getType(), this);
    }

    private void initWidget() {
        fragments = new ArrayList<>();
        chatEmotionFragment = new ChatEmotionFragment();
        fragments.add(chatEmotionFragment);
        chatFunctionFragment = new ChatFunctionFragment();
        fragments.add(chatFunctionFragment);
        adapter = new CommonFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(0);

        mDetector = EmotionInputDetector.with(this)
                .setEmotionView(emotionLayout)
                .setViewPager(viewpager)
                .bindToContent(chatList)
                .bindToEditText(editText)
                .bindToEmotionButton(emotionButton)
                .bindToAddButton(emotionAdd)
                .bindToSendButton(emotionSend)
                .bindToVoiceButton(emotionVoice)
                .bindToVoiceText(voiceText)
                .build();

        GlobalOnItemClickManagerUtils globalOnItemClickListener = GlobalOnItemClickManagerUtils.getInstance(this);
        globalOnItemClickListener.attachToEditText(editText);

        chatAdapter = new ChatAdapter(messageInfos);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatList.setLayoutManager(layoutManager);
        chatList.setAdapter(chatAdapter);
        chatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        chatAdapter.handler.removeCallbacksAndMessages(null);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        chatAdapter.handler.removeCallbacksAndMessages(null);
                        mDetector.hideEmotionLayout(false);
                        mDetector.hideSoftInput();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        chatAdapter.addItemClickListener(itemClickListener);
        LoadData();
    }

    /**
     * item点击事件
     */
    private ChatAdapter.onItemClickListener itemClickListener = new ChatAdapter.onItemClickListener() {
        @Override
        public void onHeaderClick(int position) {
            Toast.makeText(IMActivity.this, "onHeaderClick", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onImageClick(View view, int position) {
            int location[] = new int[2];
            view.getLocationOnScreen(location);
            FullImageInfo fullImageInfo = new FullImageInfo();
            fullImageInfo.setLocationX(location[0]);
            fullImageInfo.setLocationY(location[1]);
            fullImageInfo.setWidth(view.getWidth());
            fullImageInfo.setHeight(view.getHeight());
            fullImageInfo.setImageUrl(messageInfos.get(position).getFilepath());
            EventBus.getDefault().postSticky(fullImageInfo);
            startActivity(new Intent(IMActivity.this, FullImageActivity.class));
            overridePendingTransition(0, 0);
        }

        @Override
        public void onVoiceClick(final ImageView imageView, final int position) {
            if (animView != null) {
                animView.setImageResource(res);
                animView = null;
            }
            switch (messageInfos.get(position).getType()) {
                case 1:
                    animationRes = R.drawable.voice_left;
                    res = R.mipmap.icon_voice_left3;
                    break;
                case 2:
                    animationRes = R.drawable.voice_right;
                    res = R.mipmap.icon_voice_right3;
                    break;
            }
            animView = imageView;
            animView.setImageResource(animationRes);
            animationDrawable = (AnimationDrawable) imageView.getDrawable();
            animationDrawable.start();
            MediaManager.playSound(messageInfos.get(position).getFilepath(),
                    mp -> animView.setImageResource(res));
        }

        @Override
        public void onFileClick(View view, int position) {

            MessageInfo messageInfo = messageInfos.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            File file = new File(messageInfo.getFilepath());
            Uri fileUri = FileProvider.getUriForFile(IMActivity.this, Constants.AUTHORITY, file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(fileUri, messageInfo.getMimeType());
            startActivity(intent);
        }

        @Override
        public void onLinkClick(View view, int position) {
            MessageInfo messageInfo = messageInfos.get(position);
            Uri uri = Uri.parse(messageInfo.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

        @Override
        public void onLongClickImage(View view, int position) {

            ChatContextMenu chatContextMenu = new ChatContextMenu(view.getContext(), messageInfos.get(position));
//            chatContextMenu.setAnimationStyle();
            chatContextMenu.showOnAnchor(view, RelativePopupWindow.VerticalPosition.ABOVE,
                    RelativePopupWindow.HorizontalPosition.CENTER);

        }

        @Override
        public void onLongClickText(View view, int position) {
            ChatContextMenu chatContextMenu = new ChatContextMenu(view.getContext(), messageInfos.get(position));
            chatContextMenu.showOnAnchor(view, RelativePopupWindow.VerticalPosition.ABOVE,
                    RelativePopupWindow.HorizontalPosition.CENTER);
        }

        @Override
        public void onLongClickItem(View view, int position) {
            ChatContextMenu chatContextMenu = new ChatContextMenu(view.getContext(), messageInfos.get(position));
            chatContextMenu.showOnAnchor(view, RelativePopupWindow.VerticalPosition.ABOVE,
                    RelativePopupWindow.HorizontalPosition.CENTER);
        }

        @Override
        public void onLongClickFile(View view, int position) {
            ChatContextMenu chatContextMenu = new ChatContextMenu(view.getContext(), messageInfos.get(position));
            chatContextMenu.showOnAnchor(view, RelativePopupWindow.VerticalPosition.ABOVE,
                    RelativePopupWindow.HorizontalPosition.CENTER);
        }

        @Override
        public void onLongClickLink(View view, int position) {
            ChatContextMenu chatContextMenu = new ChatContextMenu(view.getContext(), messageInfos.get(position));
            chatContextMenu.showOnAnchor(view, RelativePopupWindow.VerticalPosition.ABOVE,
                    RelativePopupWindow.HorizontalPosition.CENTER);
        }
    };

    /**
     * 构造聊天数据
     */
    private void LoadData() {
        messageInfos = new ArrayList<>();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setContent("你好，欢迎使用Rance的聊天界面框架");
        messageInfo.setFileType(Constants.CHAT_FILE_TYPE_TEXT);
        messageInfo.setType(Constants.CHAT_ITEM_TYPE_LEFT);
        messageInfo.setTime(System.currentTimeMillis());
        messageInfo.setHeader("http://img0.imgtn.bdimg.com/it/u=401967138,750679164&fm=26&gp=0.jpg");
        messageInfos.add(messageInfo);

        MessageInfo messageInfo1 = new MessageInfo();
        messageInfo1.setFilepath("http://www.trueme.net/bb_midi/welcome.wav");
        messageInfo1.setVoiceTime(3000);
        messageInfo1.setFileType(Constants.CHAT_FILE_TYPE_VOICE);
        messageInfo1.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo1.setTime(System.currentTimeMillis());
        messageInfo1.setSendState(Constants.CHAT_ITEM_SEND_SUCCESS);
        messageInfo1.setHeader("http://img.dongqiudi.com/uploads/avatar/2014/10/20/8MCTb0WBFG_thumb_1413805282863.jpg");
        messageInfos.add(messageInfo1);

        MessageInfo messageInfo2 = new MessageInfo();
        messageInfo2.setFilepath("http://img4.imgtn.bdimg.com/it/u=1800788429,176707229&fm=21&gp=0.jpg");
        messageInfo2.setFileType(Constants.CHAT_FILE_TYPE_IMAGE);
        messageInfo2.setType(Constants.CHAT_ITEM_TYPE_LEFT);
        messageInfo2.setTime(1278496458000L);
        messageInfo2.setHeader("http://img0.imgtn.bdimg.com/it/u=401967138,750679164&fm=26&gp=0.jpg");
        messageInfos.add(messageInfo2);

        MessageInfo messageInfo3 = new MessageInfo();
        messageInfo3.setContent("[微笑][色][色][色]");
        messageInfo3.setFileType(Constants.CHAT_FILE_TYPE_TEXT);
        messageInfo3.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo3.setTime(1283853258000L+70000);
        messageInfo3.setSendState(Constants.CHAT_ITEM_SEND_ERROR);
        messageInfo3.setHeader("http://img.dongqiudi.com/uploads/avatar/2014/10/20/8MCTb0WBFG_thumb_1413805282863.jpg");
        messageInfos.add(messageInfo3);

        chatAdapter.addAll(messageInfos);
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
    @Subscriber(mode = ThreadMode.MAIN)
    public void MessageEventBus(final MessageInfo messageInfo) {
        messageInfo.setHeader("http://img.dongqiudi.com/uploads/avatar/2014/10/20/8MCTb0WBFG_thumb_1413805282863.jpg");
        messageInfo.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo.setSendState(Constants.CHAT_ITEM_SENDING);
        messageInfos.add(messageInfo);
        chatAdapter.notifyItemInserted(messageInfos.size() - 1);
//        chatAdapter.add(messageInfo);
        chatList.scrollToPosition(chatAdapter.getItemCount() - 1);
        //设置 messageId
        messageInfo.setMsgId(UUID.randomUUID().getLeastSignificantBits());

        //TODO 发送消息到服务器
        String messageBody = JSON.toJSONString(messageInfo);
        BaseMessage.Message msg = BaseMessage.Message
                .newBuilder()
                .setSerialType(false)
                .setText(messageBody)
                .setId(messageInfo.getMsgId())
                .setToUserId(Constants.user.getId())
                .setUser(Constants.user)
                .build();

        NettyTcpClient.getInstance().sendMsg(msg);


        //TODO 模拟更新信息状态
        new Handler().postDelayed(() -> {
            messageInfo.setSendState(Constants.CHAT_ITEM_SEND_SUCCESS);
            chatAdapter.notifyDataSetChanged();
        }, 2000);

        //TODO 模拟受到信息状态
        new Handler().postDelayed(() -> {
            MessageInfo message = new MessageInfo();
            message.setContent("这是模拟消息回复");
            message.setType(Constants.CHAT_ITEM_TYPE_LEFT);
            message.setFileType(Constants.CHAT_FILE_TYPE_TEXT);
            message.setHeader("http://img0.imgtn.bdimg.com/it/u=401967138,750679164&fm=26&gp=0.jpg");
            messageInfos.add(message);
            chatAdapter.notifyItemInserted(messageInfos.size() - 1);
            chatList.scrollToPosition(chatAdapter.getItemCount() - 1);
        }, 3000);
    }

    @Override
    public void onBackPressed() {
        if (!mDetector.interceptBackPress()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeStickyEvent(IMActivity.class);
        EventBus.getDefault().unregister(this);
        mDetector.destory();
    }


    @Subscriber(mode = ThreadMode.MAIN)
    private void onMessage(P2chatMessage message) {
        Log.w(TAG, "onMessage: " + message.getMessage().getText());
        MessageInfo myMessage;
        switch (message.getMessage().getType()) {
            case Constants.CommandType.PUSH:
                PMessage pMessage = JSON.parseObject(message.getMessage().getText(), PMessage.class);
                break;
            case Constants.CommandType.MSG:
                myMessage = JSON.parseObject(message.getMessage().getText(), MessageInfo.class);
                chatAdapter.add(myMessage);
                chatAdapter.notifyDataSetChanged();
                saveMessage(myMessage);
                break;
            case Constants.CommandType.MSG_PIC:
            case Constants.CommandType.MSG_VIDEO:
                break;
        }

    }

    public void saveMessage(MessageInfo message) {
    }
}
