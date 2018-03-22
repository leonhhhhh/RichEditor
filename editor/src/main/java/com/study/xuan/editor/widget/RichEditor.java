package com.study.xuan.editor.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.study.xuan.editor.R;
import com.study.xuan.editor.adapter.RichAdapter;
import com.study.xuan.editor.model.RichModel;
import com.study.xuan.editor.operate.IParamManger;
import com.study.xuan.editor.operate.span.factory.IAbstractSpanFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.study.xuan.editor.adapter.RichAdapter.DEFAULT_HINT;
import static com.study.xuan.editor.common.Const.BASE_LOG;
import static com.study.xuan.editor.common.Const.TYPE_EDIT;
import static com.study.xuan.editor.common.Const.TYPE_IMG;

/**
 * Author : xuan.
 * Date : 2017/11/16.
 * Description :input the description of this file.
 */

public class RichEditor extends RecyclerView implements ViewTreeObserver.OnGlobalLayoutListener {
    private Context mContext;
    private RichAdapter mAdapter;
    private List<RichModel> mDatas;
    private List<String> photoPaths;

    public RichEditor(Context context) {
        this(context, null);
    }

    public RichEditor(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichEditor(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initDatas();
        initAdapter();
        initEvent();
    }

    private void initDatas() {
        photoPaths = new ArrayList<>();
        mDatas = new LinkedList<>();
    }

    private void initAdapter() {
        setLayoutManager(new LinearLayoutManager(mContext));
        mDatas.add(new RichModel(TYPE_EDIT, "", DEFAULT_HINT));
        mAdapter = new RichAdapter(mDatas, mContext, this);
        mAdapter.addHeaderView(LayoutInflater.from(mContext).inflate(R.layout.head_item, null));
        setAdapter(mAdapter);
    }

    private void initEvent() {
        mAdapter.setOnScrollIndex(new RichAdapter.onScrollIndex() {
            @Override
            public void scroll(int pos) {
                scrollToPosition(pos);
            }
        });
        //添加文本滑动自动滑动
        mAdapter.setOnScrollIndex(onScrollIndex);
        //删除图片
        mAdapter.setOnPhotoDelete(onPhotoDelete);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    RichAdapter.onScrollIndex onScrollIndex = new RichAdapter.onScrollIndex() {
        @Override
        public void scroll(int pos) {
            scrollToPosition(pos);
        }
    };

    RichAdapter.onPhotoDelete onPhotoDelete = new RichAdapter.onPhotoDelete() {
        @Override
        public void onDelete(String path) {
            photoPaths.remove(path);
        }
    };

    private void addPhoto(ArrayList<String> photos) {
        if (mDatas != null && mAdapter != null) {
            //首行空行删除，直接增加图片+行
            if (mDatas.size() == 1) {
                mDatas.get(0).hint = "";
                if (mDatas.get(0).source.length() == 0) {
                    mDatas.remove(0);
                    mAdapter.index = 0;
                }
            }
            removeEmptyLine();
            BitmapFactory.Options options;
            if (mDatas.size() == 0 || mAdapter.index == (mDatas.size() - 1)) {//无文本或者图片在末尾添加
                for (String newFile : photos) {
                    mDatas.add(new RichModel(TYPE_IMG, newFile));
                }
                mDatas.add(new RichModel(TYPE_EDIT, "", ""));
                mAdapter.index = mDatas.size() - 2;
            } else {//图片在文中添加
                for (String newFile : photos) {
                    mDatas.add(mAdapter.index + 1, new RichModel(TYPE_IMG, newFile));
                }
                mAdapter.index++;
            }
            mAdapter.notifyDataChanged();
            scrollToPosition(mAdapter.index + 2);
        }
    }

    /**
     * 如果光标在文本行，判断是否为空行，是则，移除一行
     */
    private void removeEmptyLine() {
        if (mDatas.size() > 0 & mAdapter.index < mDatas.size()) {
            if (mDatas.get(mAdapter.index).type == TYPE_EDIT) {
                if (mDatas.get(mAdapter.index).source.length() == 0) {
                    mDatas.remove(mAdapter.index);
                    mAdapter.index--;
                }
            }
        }
    }

    public RichModel getCurIndexModel() {
        return mDatas.get(mAdapter.index);
    }

    public void setFactory(IAbstractSpanFactory factory) {
        mAdapter.setFactory(factory);
    }

    public void setParamManager(IParamManger manager) {
        mAdapter.setParamManger(manager);
    }

    public void saveInfo() {
        View child = getFocusedChild();
        if (child instanceof EditText) {
            getCurIndexModel().curIndex = ((EditText) child).getSelectionEnd();
        }
    }

    public void notifyEvent() {
        mAdapter.notifyDataChanged();
    }

    @Override
    public void onGlobalLayout() {
        mAdapter.isEnter = false;
    }
}
