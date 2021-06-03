package com.codepath.android.booksearch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.android.booksearch.GlideApp;
import com.codepath.android.booksearch.R;
import com.codepath.android.booksearch.models.Book;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BookDetailActivity extends AppCompatActivity {
    private static final String KEY_BOOK = "bookInquired";

    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;

    private ShareActionProvider shareAction;
    Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        //Fetch views:
        ivBookCover = (ImageView) findViewById(R.id.ivBookCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Extract book object from intent extras:
        Intent i = getIntent();
        Book bookReceived = (Book) Parcels.unwrap(i.getParcelableExtra(KEY_BOOK));

        // Use book object to populate data into views
        tvTitle.setText(bookReceived.getTitle());
        tvAuthor.setText(bookReceived.getAuthor());
        getSupportActionBar().setTitle(bookReceived.getTitle());
        // Load the book cover + setup shareIntent when completed:
        GlideApp.with(this).load(bookReceived.getCoverUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        prepareShareIntent(((BitmapDrawable) resource).getBitmap());
                        attachShareIntentAction();
                        // Let Glide handle resource load
                        return false;
                    }
                })
                .into(ivBookCover);
    }


    @Override
    //Purpose:          Handles all clicks to menu items:
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //------------- Share Intent methods:---------------------
    @Override
    //Purpose:          Initialized the ShareActionProvider, "shareAction"
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch reference to the share action provider
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        attachShareIntentAction();                  // called here in case this method fires second (may be fired through onResourceReady when loading the image)
        return true;
    }

    //Purpose:       Attaches the share intent to the share menu item provider
    public void attachShareIntentAction() {
        if (shareAction != null && shareIntent != null)
            shareAction.setShareIntent(shareIntent);
    }

    //Purpose:      Gets the image URI and setup the associated share intent to hook into the provider
    public void prepareShareIntent(Bitmap drawableImage) {
        // Fetch Bitmap Uri locally
        Uri bmpUri = getBitmapFromDrawable(drawableImage);

        // Construct share intent as described above based on bitmap
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);                          //Intend to send this URI to another app on this phone
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/*");
    }

    //Purpose:      Method when launching drawable within Glide
    public Uri getBitmapFromDrawable(Bitmap bmp){

        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(BookDetailActivity.this, "com.codepath.fileprovider.booksearch", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;

    }
}
