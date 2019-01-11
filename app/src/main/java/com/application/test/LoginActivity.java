package com.application.test;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.application.test.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class LoginActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private ActivityLoginBinding binding;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private PrefManagerUtils preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        init();
    }

    private void init() {
        context = this;
        preferenceManager = PrefManagerUtils.getInstance(context);
        firebaseAuth = FirebaseAuth.getInstance();
        binding.etPassword.addTextChangedListener(this);
        binding.etEmail.addTextChangedListener(this);
        binding.btLogin.setOnClickListener(this);
    }

    private void clearErrors() {
        binding.tilPassword.setError(null);
        binding.tilUserName.setError(null);
    }

    private boolean isValid() {
        String username = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            binding.tilUserName.setError("Please enter email.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            binding.tilUserName.setError("Please enter a valid email.");
            return false;
        } else if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Please enter password.");
            return false;
        } else if (password.length() < 8) {
            binding.tilPassword.setError("Password should be at least 8 characters long.");
            return false;
        }
        return true;
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

    @Override
    public void onClick(View v) {
        hideKeyboard();
        switch (v.getId()) {
            case R.id.btLogin:
                if (isValid()) {
                    isNewUser();
                }
                break;
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

    private void isNewUser() {
        ProgressDialogUtils.show(context);
        firebaseAuth.fetchSignInMethodsForEmail(binding.etEmail.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                                login();
                            } else {
                                register();
                            }
                        } else {
                            Toast.makeText(context, "We encountered a problem, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void login() {
        firebaseAuth.signInWithEmailAndPassword(binding.etEmail.getText().toString().trim(), binding.etPassword.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            goToNext();
                        } else {
                            ProgressDialogUtils.dismiss();
                            Toast.makeText(context, "Entered Password is invalid.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void register() {
        firebaseAuth.createUserWithEmailAndPassword(binding.etEmail.getText().toString().trim(), binding.etPassword.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            goToNext();
                        } else {
                            ProgressDialogUtils.dismiss();
                            Toast.makeText(context, "Account Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToNext() {
        ProgressDialogUtils.dismiss();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            preferenceManager.savePreference("email", binding.etEmail.getText().toString().trim());
            preferenceManager.savePreference("user", user);
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        firebaseAuth.signOut();
        super.onDestroy();
    }
}
