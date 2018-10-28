package com.xinlan.imageeditlibrary.editimage.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.ModuleConfig;
import com.xinlan.imageeditlibrary.editimage.adapter.EditorRecyclerAdapter;
import com.xinlan.imageeditlibrary.editimage.utils.Matrix3Mul3;
import com.xinlan.imageeditlibrary.editimage.view.CropImageView;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 图片剪裁Fragment
 * 
 * @author panyi
 * xujian 2018/10/14
 * 
 */
public class CropFragment extends BaseEditFragment {
    public static final int INDEX = ModuleConfig.INDEX_CROP;
	public static final String TAG = CropFragment.class.getName();
	private View mainView;
	private View backToMenu;// 返回主菜单
	public CropImageView mCropPanel;// 剪裁操作面板
	private RecyclerView cropItemRv;

	private EditorRecyclerAdapter adapter; //设置adapter信息

	public static CropFragment newInstance() {
		CropFragment fragment = new CropFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.fragment_edit_image_crop, null);
		return mainView;
	}

	private void setUpRatioList() {

		cropItemRv =  mainView.findViewById(R.id.ratio_list_group);
		adapter = new EditorRecyclerAdapter(getContext(), cropItemRv, EditImageActivity.MODE_CROP,
				new EditorRecyclerAdapter.OnEditorItemClickListener() {
			@Override
			public void onEditorItemClick(int position, View itemView) {
				// 某一项被点击之后
				float cropRatio = (float)itemView.getTag();
				// 获取裁剪比率之后直接设置裁剪比率
				mCropPanel.setRatioCropRect(activity.mainImage.getBitmapRect(), cropRatio);
			}
		});
		LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		cropItemRv.setHasFixedSize(true);
		cropItemRv.setLayoutManager(layoutManager);
		cropItemRv.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        backToMenu = mainView.findViewById(R.id.back_to_main);
        setUpRatioList();
        this.mCropPanel = ensureEditActivity().mCropPanel;
		backToMenu.setOnClickListener(new BackToMenuClick());// 返回主菜单
	}

    @Override
    public void onShow() {
        activity.mode = EditImageActivity.MODE_CROP;

        activity.mCropPanel.setVisibility(View.VISIBLE);
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mainImage.setScaleEnabled(false);// 禁用缩放
        // System.out.println(r.left + "    " + r.top);
        activity.bannerFlipper.showNext();

		//  bug  fixed  https://github.com/siwangqishiq/ImageEditor-Android/issues/59
		// 设置完与屏幕匹配的尺寸  确保变换矩阵设置生效后才设置裁剪区域
		activity.mainImage.post(new Runnable() {
			@Override
			public void run() {
				final RectF r = activity.mainImage.getBitmapRect();
				activity.mCropPanel.setCropRect(r);
			}
		});
    }

    /**
	 * 返回按钮逻辑
	 * 
	 * @author panyi
	 * 
	 */
	private final class BackToMenuClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			backToMain();
		}
	}// end class

	/**
	 * 返回主菜单
	 */
	@Override
	public void backToMain() {
		activity.mode = EditImageActivity.MODE_NONE;
		mCropPanel.setVisibility(View.GONE);
		activity.mainImage.setScaleEnabled(true);// 恢复缩放功能
		activity.bottomGallery.setCurrentItem(0);
//		if (selctedTextView != null) {
//			selctedTextView.setTextColor(UNSELECTED_COLOR);
//		}
		mCropPanel.setRatioCropRect(activity.mainImage.getBitmapRect(), -1);
		activity.bannerFlipper.showPrevious();
	}

	/**
	 * 保存剪切图片
	 */
	public void applyCropImage() {
		// System.out.println("保存剪切图片");
		CropImageTask task = new CropImageTask();
		task.execute(activity.getMainBit());
	}

	/**
	 * 图片剪裁生成 异步任务
	 * 
	 * @author panyi
	 * 
	 */
	private final class CropImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
		private Dialog dialog;

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dialog.dismiss();
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		protected void onCancelled(Bitmap result) {
			super.onCancelled(result);
			dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = BaseActivity.getLoadingDialog(getActivity(), R.string.saving_image,
					false);
			dialog.show();
		}

		@SuppressWarnings("WrongThread")
        @Override
		protected Bitmap doInBackground(Bitmap... params) {
			RectF cropRect = mCropPanel.getCropRect();// 剪切区域矩形
			Matrix touchMatrix = activity.mainImage.getImageViewMatrix();
			// Canvas canvas = new Canvas(resultBit);
			float[] data = new float[9];
			touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
			Matrix3Mul3 cal = new Matrix3Mul3(data);// 辅助矩阵计算类
			Matrix3Mul3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
			Matrix m = new Matrix();
			m.setValues(inverseMatrix.getValues());
			m.mapRect(cropRect);// 变化剪切矩形

			// Paint paint = new Paint();
			// paint.setColor(Color.RED);
			// paint.setStrokeWidth(10);
			// canvas.drawRect(cropRect, paint);
			// Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(
			// Bitmap.Config.ARGB_8888, true);
			Bitmap resultBit = Bitmap.createBitmap(params[0],
					(int) cropRect.left, (int) cropRect.top,
					(int) cropRect.width(), (int) cropRect.height());

			//saveBitmap(resultBit, activity.saveFilePath);
			return resultBit;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if (result == null)
				return;

            activity.changeMainBitmap(result,true);
			activity.mCropPanel.setCropRect(activity.mainImage.getBitmapRect());
			backToMain();
		}
	}// end inner class

	/**
	 * 保存Bitmap图片到指定文件
	 */
	public static void saveBitmap(Bitmap bm, String filePath) {
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("保存文件--->" + f.getAbsolutePath());
	}
}// end class
