package com.wetongji_android.util.common;

import com.actionbarsherlock.app.SherlockActivity;
import com.androidquery.AQuery;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class WTFullScreenPicArea extends LinearLayout {
    private int imgDisplayW;

    private int imgDisplayH;

    private int imgW;

    private int imgH;

    private WTTouchImageView touchView;

    final SherlockActivity mSherlockActivity;
    
    //private DisplayMetrics dm;

    // resIdΪͼƬ��Դid
    @SuppressWarnings("deprecation")
	public WTFullScreenPicArea(Context context, String url, AQuery aq, int width, int height,
            SherlockActivity sherlockActivity) { // �ڶ���������ͼƬ����ԴID����ȻҲ�����ñ�ķ�ʽ��ȡͼƬ
        /*
         * dm = new DisplayMetrics();
         * ((Activity)context).getWindowManager().getDefaultDisplay
         * ().getMetrics(dm); imgDisplayW = dm.widthPixels; imgDisplayH =
         * dm.heightPixels;
         */
        super(context);
        
        setOnClickListener(new LayoutClickListener());
        this.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        
        mSherlockActivity = sherlockActivity;

        imgDisplayW = ((Activity) context).getWindowManager()
                .getDefaultDisplay().getWidth();

        imgDisplayH = ((Activity) context).getWindowManager()
                .getDefaultDisplay().getHeight();

        touchView = new WTTouchImageView(context, imgDisplayW, imgDisplayH, mSherlockActivity);// �������Զ���ImageView
        //touchView.setImageDrawable(drawable);
        
        touchView.setImageBitmap(aq.getCachedImage(url));// ���Զ���imageView����Ҫ��ʾ��ͼƬ
        
        touchView.setFocusable(false);
        
        imgW = width;
        imgH = height;
        
        // ͼƬ��һ�μ��ؽ������ж�ͼƬ��С�Ӷ�ȷ����һ��ͼƬ����ʾ��ʽ��
        int layout_w = imgW > imgDisplayW ? imgDisplayW : imgW;
        int layout_h = imgH > imgDisplayH ? imgDisplayH : imgH;

        if (imgW >= imgH) {
            if (layout_w == imgDisplayW) {
                layout_h = (int) (imgH * ((float) imgDisplayW / imgW));
            }
        } else {
            if (layout_h == imgDisplayH) {
                layout_w = (int) (imgW * ((float) imgDisplayH / imgH));
            }
        }
        
        LinearLayout.LayoutParams parm = new LinearLayout.LayoutParams(layout_w,
                layout_h);
        parm.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        touchView.setLayoutParams(parm);

        this.addView(touchView);
        
    }
    
    public class LayoutClickListener implements OnClickListener {

        /* (non-Javadoc)
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
          if(mSherlockActivity.getSupportActionBar().isShowing()) {
                mSherlockActivity.getSupportActionBar().hide();
          }else {
              mSherlockActivity.getSupportActionBar().show();
          }
        }
        
    }

}
