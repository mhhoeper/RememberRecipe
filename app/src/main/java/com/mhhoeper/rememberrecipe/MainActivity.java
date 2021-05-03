package com.mhhoeper.rememberrecipe;

import android.app.Activity;
import android.content.Intent;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RatingBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String URL_STRINGS      = "urlstrings";
    private static final String JSON_SESSION     = "session_id";
    private static final String JSON_TITLE       = "title";
    private static final String JSON_RATING      = "rating";
    private static final String JSON_TAGS        = "tags";
    private static final String JSON_REFERENCE   = "reference";
    private static final String JSON_PHOTO       = "photodata";
    private static final String JSON_INGREDIENTS = "ingredients";
    private SliderLayout sliderLayout;
    private ArrayList<String> imageUrls;
    private BigInteger mSession;
    private int mRating;
    private ArrayList<String> mTagList;
    private ArrayList<String> mIngredientsList;


    // This function clears all fields and the slider and reinizalizes the app. After calling this
    // function the app should behave like newly started.
    private void initializeEmptyUI() {
        // Setting mSession to 0 makes all ui update functions skip the update
        mSession = BigInteger.ZERO;

        TextInputEditText titleInput = findViewById(R.id.input_title);
        titleInput.getText().clear();

        RatingBar ratingInput = findViewById(R.id.ratingBar);
        ratingInput.setRating(0);

        MultiAutoCompleteTextView tagsInput = findViewById(R.id.input_tags);
        tagsInput.getText().clear();

        TextInputEditText refInput = findViewById(R.id.input_ref);
        refInput.getText().clear();

        MultiAutoCompleteTextView ingredientsInput = findViewById(R.id.input_ingredients);
        ingredientsInput.getText().clear();

        imageUrls=new ArrayList<>();
        setSliderViews();
        //sliderLayout.getCurrentSlider().getView().refreshDrawableState();

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
                            mSession = BigInteger.ZERO;
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

                            // Prepare Autocomplete tags
                            MultiAutoCompleteTextView tagsView = findViewById(R.id.input_tags);
                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, mTagList);
                            tagsView.setAdapter(adapter);
                            tagsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                        } else {
                            mTagList = new ArrayList<String>();

                            // Disable autocomplete as no tags are available
                            MultiAutoCompleteTextView tagsView = findViewById(R.id.input_tags);
                            tagsView.setAdapter(null);
                            tagsView.setTokenizer(null);
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

                            // Prepare Autocomplete tags
                            MultiAutoCompleteTextView ingredientsView = findViewById(R.id.input_ingredients);
                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, mIngredientsList);
                            ingredientsView.setAdapter(adapter);
                            ingredientsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                        } else {
                            mIngredientsList = new ArrayList<String>();

                            // Disable autocomplete as no ingredients are available
                            MultiAutoCompleteTextView ingredientsView = findViewById(R.id.input_ingredients);
                            ingredientsView.setAdapter(null);
                            ingredientsView.setTokenizer(null);
                        }

                        Log.d("JSON Data received", String.valueOf(result));
                    }
                });
    }

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
            mRating = savedInstanceState.getInt("JSON_RATING");
            mTagList = savedInstanceState.getStringArrayList(JSON_TAGS);
            mIngredientsList = savedInstanceState.getStringArrayList(JSON_INGREDIENTS);
        } else {
            initializeEmptyUI();
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
                // exit if no session is prepared
                if(mSession.compareTo(BigInteger.ZERO) == 0)  {
                    return;
                }

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

        MultiAutoCompleteTextView tagInput = findViewById(R.id.input_tags);
        tagInput.addTextChangedListener(new TextWatcher() {
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
                // exit if no session is prepared
                if(mSession.compareTo(BigInteger.ZERO) == 0) {
                    return;
                }

                // send new reference to json interface
                JsonObject setTagData = new JsonObject();
                setTagData.addProperty(JSON_SESSION, mSession);
                setTagData.addProperty(JSON_TAGS, editable.toString());
                Ion.with(getApplicationContext())
                        .load("https://recipe.dns-cloud.net/new/set_tags")
                        .setJsonObjectBody(setTagData)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                assert e == null;
                                assert result != null;

                                Log.d("JSON Data Received", String.valueOf(result));
                            }
                        });
            }
        });

        TextInputEditText refInput = findViewById(R.id.input_ref);
        refInput.addTextChangedListener(new TextWatcher() {
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
                // exit if no session is prepared
                if(mSession.compareTo(BigInteger.ZERO) == 0) {
                    return;
                }

                // send new reference to json interface
                JsonObject setReferenceData = new JsonObject();
                setReferenceData.addProperty(JSON_SESSION, mSession);
                setReferenceData.addProperty(JSON_REFERENCE, editable.toString());
                Ion.with(getApplicationContext())
                        .load("https://recipe.dns-cloud.net/new/set_reference")
                        .setJsonObjectBody(setReferenceData)
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

        RatingBar ratinginput = findViewById(R.id.ratingBar);
        ratinginput.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                // exit if no session is prepared
                if(mSession.compareTo(BigInteger.ZERO) == 0) {
                    return;
                }

                JsonObject setRatingData = new JsonObject();
                setRatingData.addProperty(JSON_SESSION, mSession);
                setRatingData.addProperty(JSON_RATING, v);
                Ion.with(getApplicationContext())
                        .load("https://recipe.dns-cloud.net/new/set_rating")
                        .setJsonObjectBody(setRatingData)
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

        MultiAutoCompleteTextView ingredientsInput = findViewById(R.id.input_ingredients);
        ingredientsInput.addTextChangedListener(new TextWatcher() {
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
                // exit if no session is prepared
                if(mSession.compareTo(BigInteger.ZERO) == 0) {
                    return;
                }

                // send new reference to json interface
                JsonObject setIngredientsData = new JsonObject();
                setIngredientsData.addProperty(JSON_SESSION, mSession);
                setIngredientsData.addProperty(JSON_INGREDIENTS, editable.toString());
                Ion.with(getApplicationContext())
                        .load("https://recipe.dns-cloud.net/new/set_ingredients")
                        .setJsonObjectBody(setIngredientsData)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                assert e == null;
                                assert result != null;

                                Log.d("JSON Data Received", String.valueOf(result));
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

            Log.d("Photo", "Image added to list, creating byte array now." + filePath);

            // send new image to server
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bytes;
            try {
                InputStream inputStream = new FileInputStream(filePath);

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ( (bytesRead = inputStream.read(buffer)) != -1 ) {
                    baos.write(buffer, 0, bytesRead);
                }
            }
            catch (IOException e) {
                Log.e("Photo", "Error creating photo byte array.", e);
                e.printStackTrace();
            }
            bytes = baos.toByteArray();

            Log.d("Photo", "Byte array created. Do base64 encoding now. " + Integer.toString(baos.size()));

            String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);

            Log.d("Photo", "Image is encoded now. Sending data. " + Integer.toString(encodedImage.length()));

            JsonObject setPhotoData = new JsonObject();
            setPhotoData.addProperty(JSON_SESSION, mSession);
            setPhotoData.addProperty(JSON_PHOTO, encodedImage);
            try {
                JsonObject photoDataResult = Ion.with(getApplicationContext())
                        .load("https://recipe.dns-cloud.net/new/add_photo")
                        .setJsonObjectBody(setPhotoData)
                        .asJsonObject()
                        .get();

                Log.d("JSON Data received", String.valueOf(photoDataResult));
            }
            catch(ExecutionException e) {
                Log.e("Photo", "JSON Data Execution Exception " + e.toString(), e);
            }
            catch(InterruptedException e) {
                Log.e("Photo", "JSON Data Interrupted Exception " + e.toString(), e);
            }

            Log.d("Sending image finished.", "");

            // update slider
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
        outState.putInt(JSON_RATING, mRating);
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
        if (id == R.id.action_new) {
            // clear all fields
            initializeEmptyUI();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}