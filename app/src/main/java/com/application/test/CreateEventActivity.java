package com.application.test;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.application.test.databinding.ActivityCreateEventBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private static final int FROM_GALLERY = 101;
    private static final int TAKE_PICTURE = 102;
    private Calendar calendar;
    private Context context;
    private ActivityCreateEventBinding binding;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private PrefManagerUtils prefManagerUtils;
    private String encodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_event);
        init();
    }

    private void init() {
        context = this;
        prefManagerUtils = PrefManagerUtils.getInstance(context);
        calendar = Calendar.getInstance();
        binding.etDate.addTextChangedListener(this);
        binding.etTime.addTextChangedListener(this);
        binding.etDescription.addTextChangedListener(this);
        binding.etPlace.addTextChangedListener(this);
        binding.etPrice.addTextChangedListener(this);
        binding.etTitle.addTextChangedListener(this);
        binding.etTime.setOnClickListener(this);
        binding.etDate.setOnClickListener(this);
        binding.btSubmit.setOnClickListener(this);
        binding.btCancel.setOnClickListener(this);
        binding.ivCamera.setOnClickListener(this);
    }

    private boolean isValid() {
        String title = binding.etTitle.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String time = binding.etTime.getText().toString().trim();
        String place = binding.etPlace.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            binding.tilTitle.setError("Please enter title.");
            return false;
        } else if (title.length() < 4) {
            binding.tilTitle.setError("Title should be at least 4 characters long.");
            return false;
        } else if (TextUtils.isEmpty(date)) {
            binding.tilDate.setError("Please select date.");
            return false;
        } else if (TextUtils.isEmpty(time)) {
            binding.tilTime.setError("Please select time.");
            return false;
        } else if (TextUtils.isEmpty(place)) {
            binding.tilPlace.setError("Please enter place.");
            return false;
        } else if (TextUtils.isEmpty(price)) {
            binding.tilPrice.setError("Please enter price.");
            return false;
        } else if (TextUtils.isEmpty(description)) {
            binding.tilDescription.setError("Please enter description.");
            return false;
        } else if (description.length() < 5) {
            binding.tilDescription.setError("Description should be at least 5 characters long.");
            return false;
        }
        return true;
    }


    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                String day = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
                String mMonth = month < 10 ? "0" + month : "" + month;
                String fetchedDate = day + "-" + mMonth + "-" + year;
                binding.etDate.setText(fetchedDate);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hourActual = "" + hourOfDay;
                String minuteActual = "" + minute;
                if (hourOfDay < 10)
                    hourActual = "0" + hourOfDay;
                if (minute < 10)
                    minuteActual = "0" + minute;
                String time = hourActual + ":" + minuteActual;
                binding.etTime.setText(time);
            }
        }, 12, 0, true);
        timePickerDialog.show();
    }


    private void clearErrors() {
        binding.tilTitle.setError(null);
        binding.tilDate.setError(null);
        binding.tilDescription.setError(null);
        binding.tilPlace.setError(null);
        binding.tilPrice.setError(null);
        binding.tilTime.setError(null);
    }

    @Override
    public void onClick(View v) {
        hideKeyboard();
        switch (v.getId()) {
            case R.id.btSubmit:
                if (isValid()) {
                    addDataToFirebase();
                }
                break;
            case R.id.btCancel:
                finish();
                break;
            case R.id.etDate:
                showDatePicker();
                break;
            case R.id.etTime:
                showTimePicker();
                break;
            case R.id.ivCamera:
                openCamera();
            break;
        }
    }


    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private void addDataToFirebase() {
        EventListModel model = new EventListModel();
        model.setDate(binding.etDate.getText().toString().trim());
        model.setDescription(binding.etDescription.getText().toString().trim());
        model.setLocation(binding.etPlace.getText().toString().trim());
        model.setPrice(binding.etPrice.getText().toString().trim());
        model.setTime(binding.etTime.getText().toString().trim());
        model.setTitle(binding.etTitle.getText().toString().trim());
        model.setEmail((String) prefManagerUtils.getPreference("email"));
        model.setPicture(encodedImage);
        DatabaseReference eventsRef = ref.child("events");
        eventsRef.push().setValue(model);
        Toast.makeText(context, "Event successfully listed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Glide.with(context)
                    .asBitmap()
                    .load(photo)
                    .into(binding.ivPic);
            binding.ivPic.setImageBitmap(photo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteFormat = stream.toByteArray();
            encodedImage = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager in = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = this.findViewById(android.R.id.content);
            assert in != null;
            in.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Throwable ignored) {
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        clearErrors();
    }


    @Override
    public void afterTextChanged(Editable s) {

    }
}
