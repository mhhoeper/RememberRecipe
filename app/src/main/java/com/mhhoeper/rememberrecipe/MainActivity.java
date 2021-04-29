package com.mhhoeper.rememberrecipe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String URL_STRINGS      = "urlstrings";
    private static final String JSON_SESSION     = "session_id";
    private static final String JSON_TITLE       = "title";
    private static final String JSON_TAGS        = "tags";
    private static final String JSON_INGREDIENTS = "ingredients";
    private SliderLayout sliderLayout;
    private ArrayList<String> imageUrls;
    private BigInteger mSession;
    private ArrayList<String> mTagList;
    private ArrayList<String> mIngredientsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sliderLayout = findViewById(R.id.imageSlider);
        if(savedInstanceState != null) {
            imageUrls = savedInstanceState.getStringArrayList(URL_STRINGS);
            setSliderViews();
            mSession = (BigInteger) savedInstanceState.getSerializable(JSON_SESSION);
            mTagList = savedInstanceState.getStringArrayList(JSON_TAGS);
            mIngredientsList = savedInstanceState.getStringArrayList(JSON_INGREDIENTS);
        } else {
            imageUrls=new ArrayList<>();

            Ion.getDefault(getApplicationContext()).getConscryptMiddleware().enable(false);
            Ion.with(getApplicationContext())
                    .load("https://recipe.dns-cloud.net/new/new_set")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            assert e == null;
                            assert result != null;
                            if(result.getAsJsonPrimitive(JSON_SESSION) != null) {
                                mSession = result.getAsJsonPrimitive(JSON_SESSION).getAsBigInteger();
                            } else {
                                mSession = new BigInteger("0");
                            }

                            Log.d("JSON Data received", String.valueOf(result));
                        }
                    });
            Ion.with(getApplicationContext())
                    .load("https://recipe.dns-cloud.net/new/get_available_tags")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            assert e == null;
                            assert result != null;
                            if(result.getAsJsonArray(JSON_TAGS) != null) {
                                mTagList = new ArrayList<String>();
                                JsonArray arr = result.getAsJsonArray(JSON_TAGS);
                                for(JsonElement el : arr){
                                    mTagList.add(el.getAsString());
                                }
                            } else {
                                mTagList = new ArrayList<String>();
                            }

                            Log.d("JSON Data received", String.valueOf(result));
                        }
                    });
            Ion.with(getApplicationContext())
                    .load("https://recipe.dns-cloud.net/new/get_available_ingredients")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            assert e == null;
                            assert result != null;
                            if(result.getAsJsonArray(JSON_INGREDIENTS) != null) {
                                mIngredientsList = new ArrayList<String>();
                                JsonArray arr = result.getAsJsonArray(JSON_INGREDIENTS);
                                for(JsonElement el : arr){
                                    mIngredientsList.add(el.getAsString());
                                }
                            } else {
                                mIngredientsList = new ArrayList<String>();
                            }

                            Log.d("JSON Data received", String.valueOf(result));
                        }
                    });
        }

//        setSliderViews_main();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickImage();
            }
        });


        // React on changes
        TextInputEditText titleinput = findViewById(R.id.input_title);
        titleinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // send new title to json interface
                JsonObject setTitleData = new JsonObject();
                setTitleData.addProperty(JSON_SESSION, mSession);
                setTitleData.addProperty(JSON_TITLE, editable.toString());
                Ion.with(getApplicationContext())
                        .load("https://recipe.dns-cloud.net/new/set_title")
                        .setJsonObjectBody(setTitleData)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                assert e == null;
                                assert result != null;

                                Log.d("JSON Data received", String.valueOf(result));
                            }
                        });
            }
        });
    }

    public void onPickImage() {
        ImagePicker.Companion.with(this).cameraOnly().saveDir(Environment.getExternalStorageDirectory()).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK){
            assert data != null;

            String filePath = ImagePicker.Companion.getFilePath(data);
            imageUrls.add(filePath);
            setSliderViews();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // See https://medium.com/hootsuite-engineering/handling-orientation-changes-on-android-41a6b62cb43f
    // and https://stackoverflow.com/questions/151777/how-to-save-an-activity-state-using-save-instance-state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(JSON_SESSION, mSession);
        outState.putStringArrayList(JSON_TAGS, mTagList);
        outState.putStringArrayList(JSON_INGREDIENTS, mIngredientsList);
        outState.putStringArrayList(URL_STRINGS, imageUrls);
    }

    private void setSliderViews() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                sliderLayout.removeAllSliders();
//        sliderLayout.removeAllViews();
                for (String theImageUrl: imageUrls) {
//            TextSliderView textSliderView = new TextSliderView(this);
//            textSliderView.image(theImageUrl).setScaleType(BaseSliderView.ScaleType.CenterCrop);
//            sliderLayout.addSlider(textSliderView);
                    File imgFile = new File(theImageUrl);
                    DefaultSliderView sliderView = new DefaultSliderView(getBaseContext());
                    sliderView.image(imgFile).setScaleType(BaseSliderView.ScaleType.CenterCrop);
                    sliderLayout.addSlider(sliderView);
                    Log.d("Filepath:", theImageUrl);
                }
            }
        });
    }
    public void setSliderViews_main() {
        for (int i=0;i<5;i++) {
            String theImageUrl="https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png";
//            imageUrls.add(theImageUrl);
            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView.image(theImageUrl).setScaleType(BaseSliderView.ScaleType.CenterCrop);
            sliderLayout.addSlider(textSliderView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
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
}