package com.photor.album.activity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.photor.BuildConfig;
import com.photor.R;
import com.photor.base.activity.BaseActivity;
import com.shockwave.pdfium.PdfDocument;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

import static com.photor.base.activity.util.PhotoOperator.EXTRA_PHOTO_TO_PDF_PATH;
import static com.photor.base.activity.util.PhotoOperator.REQUEST_ACTION_PDF_FILE;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/16 16:01
 */
public class PdfPreviewActivity extends BaseActivity {

    @BindView(R.id.pdfView)
    protected PDFView pdfView;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    private Uri imgPdfUri = null;  // 图片转化成的pdf文件uri
    private String pdfFilePath = null; // pdf文件的全路径信息
    private Integer pdfPageNumber = 0; // 默认记录当前pdf文件的页数（当前看到的页数）
    private String pdfFileName; // 当前pdf文件的名字

    private static final String TAG = PdfPreviewActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);
        ButterKnife.bind(this);

        imgPdfUri = getIntent().getData();
        pdfFilePath = getIntent().getStringExtra(EXTRA_PHOTO_TO_PDF_PATH);
        if (imgPdfUri != null) {
            displayPdfFromUri(imgPdfUri);
        }

        setSupportActionBar(toolbar);
        // 获得当前ActionBar的实例
        ActionBar actionBar = getSupportActionBar();  // 当前ActionBar已经跟Toolbar结合到一起了
        if (actionBar != null) {
            // 让Activity中的导航栏按钮显示出来
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdf_preview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pickFile:
                pickFile();
                return true;
            case R.id.action_share:
                shareToOthers();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ACTION_PDF_FILE:
                Uri uri = data.getData();
                if (uri != null) {
                    displayPdfFromUri(uri);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 分享pdf文件功能
     */
    private void shareToOthers() {

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getApplicationContext(),
                    BuildConfig.APPLICATION_ID + ".provider", new File(pdfFilePath));
        } else {
            uri = Uri.fromFile(new File(pdfFilePath));
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, pdfFileName);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("*/*"); // 可以发送多种文件
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_file)));
    }

    /**
     * 打开外部文件信息
     */
    private Disposable pickFile() {
        Disposable disposable = new RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        startActivityForResult(intent, REQUEST_ACTION_PDF_FILE);
                    } else {
                        // Oups permission denied
                        Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
                    }
                });
        return disposable;
    }



    /**
     * 显示pdf文件信息
     * @param uri
     */
    private void displayPdfFromUri(Uri uri) {
        pdfFileName = getPdfFileNameFromUri(uri);
        pdfView.fromUri(uri)
                .defaultPage(pdfPageNumber)
                .onPageChange(onPdfPageChangeListener)
                .enableAnnotationRendering(true)
                .onLoad(onPdfLoadCompleteListener)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10)
                .onPageError(onPdfPageErrorListener)
                .load();
    }


    private OnPageChangeListener onPdfPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageChanged(int page, int pageCount) {
            pdfPageNumber = page; // 记录当前pdf的页数信息
            toolbar.setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
        }
    };


    private OnLoadCompleteListener onPdfLoadCompleteListener = new OnLoadCompleteListener() {
        @Override
        public void loadComplete(int nbPages) {
            PdfDocument.Meta meta = pdfView.getDocumentMeta();
            Log.e(TAG, "title = " + meta.getTitle());
            Log.e(TAG, "author = " + meta.getAuthor());
            Log.e(TAG, "subject = " + meta.getSubject());
            Log.e(TAG, "keywords = " + meta.getKeywords());
            Log.e(TAG, "creator = " + meta.getCreator());
            Log.e(TAG, "producer = " + meta.getProducer());
            Log.e(TAG, "creationDate = " + meta.getCreationDate());
            Log.e(TAG, "modDate = " + meta.getModDate());
        }
    };

    private OnPageErrorListener onPdfPageErrorListener = new OnPageErrorListener() {
        @Override
        public void onPageError(int page, Throwable t) {
            Log.e(TAG, "Cannot load page " + page);
        }
    };


    /**
     * 从Uri中获取pdf文件的名称
     * @param uri
     */
    private String getPdfFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


}
