package com.quange.viewModel;

import java.util.ArrayList;
import java.util.List;

import com.quange.jhds.AppCommon;
import com.quange.jhds.PhotosActivity;
import com.quange.jhds.R;
import com.quange.views.RoundImageView;
import com.sina.weibo.sdk.openapi.models.Status;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class JHDSShareRepostAdapter extends BaseAdapter {

	private Activity mAct;
	
	private List<Status> mlList = new ArrayList<Status>();
	public JHDSShareRepostAdapter(Activity act, List<Status>lList) {
		this.mAct = act;
		this.mlList = lList;
	}
	@Override
	public int getCount() {
		return mlList.size();
	}

	@Override
	public Status getItem(int position) {
		return mlList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View cv, ViewGroup parent) {
		Status ls = getItem(position);
		HoldView hv = null;
		if (null == cv) {
			hv = new HoldView();
			cv = View.inflate(mAct, R.layout.list_item_share_repost, null);

			hv.userNickName = (TextView) cv.findViewById(R.id.userNickName);
			hv.userIcon = (RoundImageView) cv.findViewById(R.id.userIcon);
			hv.createTime = (TextView) cv.findViewById(R.id.createTime);
			hv.tv_content = (TextView) cv.findViewById(R.id.tv_content);
			hv.shareImgBox1 = (LinearLayout) cv.findViewById(R.id.shareImgBox1);
			hv.shareImgBox2 = (LinearLayout) cv.findViewById(R.id.shareImgBox2);
			hv.shareImgBox3 = (LinearLayout) cv.findViewById(R.id.shareImgBox3);
			hv.shareImg[0] = (ImageView) cv.findViewById(R.id.shareImg0);
			hv.shareImg[1] = (ImageView) cv.findViewById(R.id.shareImg1);
			hv.shareImg[2] = (ImageView) cv.findViewById(R.id.shareImg2);
			hv.shareImg[3] = (ImageView) cv.findViewById(R.id.shareImg3);
			hv.shareImg[4] = (ImageView) cv.findViewById(R.id.shareImg4);
			hv.shareImg[5] = (ImageView) cv.findViewById(R.id.shareImg5);
			hv.shareImg[6] = (ImageView) cv.findViewById(R.id.shareImg6);
			hv.shareImg[7] = (ImageView) cv.findViewById(R.id.shareImg7);
			hv.shareImg[8] = (ImageView) cv.findViewById(R.id.shareImg8);
			
			cv.setTag(hv);
		} else {
			hv = (HoldView) cv.getTag();
		}
		
		hv.userNickName.setText(ls.user.screen_name);
		hv.tv_content.setText(ls.text);
		AppCommon.getInstance().imageLoader.displayImage(ls.user.avatar_large, hv.userIcon, AppCommon.getInstance().userIconOptions);
		hv.createTime.setText(ls.created_at );
		if(ls.pic_urls.size()<7)
			hv.shareImgBox3.setVisibility(View.GONE);
		if(ls.pic_urls.size()<4)
			hv.shareImgBox2.setVisibility(View.GONE);
		if(ls.pic_urls.size() ==0)
			hv.shareImgBox1.setVisibility(View.GONE);
		String url= "";
		String[] urlsubs = ls.original_pic.split("/");
		for(int j = 0;j<urlsubs.length-2;j++)
		{
			url = url+urlsubs[j]+"/";
		}
		
		for(int j = 0;j<9;j++)
		{
			hv.shareImg[j].setVisibility(View.INVISIBLE);
		}
		for(int i = 0;i<ls.pic_urls.size();++i)
		{
			hv.shareImg[i].setVisibility(View.VISIBLE);
			hv.shareImg[i].setTag(i+"");
		
			AppCommon.getInstance().imageLoader.displayImage(ls.pic_urls.get(i), hv.shareImg[i], AppCommon.getInstance().options);
			hv.shareImg[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
						gotoBigImage(position, Integer.valueOf(((ImageView)v).getTag().toString()).intValue());
					
				}
			});
		}
		
		return cv;
	}
	
	private class HoldView {
		
		private TextView userNickName; 
		private RoundImageView userIcon;
		private TextView createTime; 
		private TextView tv_content; 
		private LinearLayout shareImgBox1; 
		private LinearLayout shareImgBox2; 
		private LinearLayout shareImgBox3; 
		private ImageView shareImg[] = new ImageView[9];
		
		
	}
	
	private void gotoBigImage(int pos,int imgIndex)
	{
		Bundle bundle = new Bundle();
		String allUrl = "";
		for (int i = 0;i<mlList.get(pos).pic_urls.size();i++)
		{
			if(i==mlList.get(pos).pic_urls.size()-1)
				allUrl = allUrl+mlList.get(pos).pic_urls.get(i);
			else 
				allUrl = allUrl +mlList.get(pos).pic_urls.get(i)+"*";
		}
		bundle.putString("allUrl", allUrl);
		String cururl = allUrl+mlList.get(pos).pic_urls.get(imgIndex);
		bundle.putString("curUrl",cururl );
		Intent intent = new Intent(mAct, PhotosActivity.class);
		intent.putExtras(bundle);
		mAct.startActivity(intent);
	}
}