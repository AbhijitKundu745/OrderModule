package com.psl.ordermodule20;

import static com.psl.ordermodule20.Helper.AssetUtils.hideProgressDialog;
import static com.psl.ordermodule20.Helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.BuildConfig;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.ordermodule20.Helper.APIConstants;
import com.psl.ordermodule20.Helper.AssetUtils;
import com.psl.ordermodule20.Helper.ConnectionDetector;
import com.psl.ordermodule20.Helper.CustomProgressDialog;
import com.psl.ordermodule20.Helper.SharedPreferencesManager;
import com.psl.ordermodule20.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoginActivity extends AppCompatActivity{
    private ActivityLoginBinding binding;
    private Context context = this;
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setTitle("USER LOGIN");
        cd = new ConnectionDetector(context);

        String androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidID = androidID.toUpperCase();
        SharedPreferencesManager.setDeviceId(context, androidID);
        Log.e("DEVICEID", androidID);

        if (SharedPreferencesManager.getIsHostConfig(context)) {

        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.url_not_config));
        }
        if (SharedPreferencesManager.getIsLoginSaved(context)) {
            binding.chkRemember.setChecked(true);
            binding.edtUserName.setText(SharedPreferencesManager.getSavedUser(context));
            binding.edtPassword.setText(SharedPreferencesManager.getSavedPassword(context));
        } else {
            binding.chkRemember.setChecked(false);
            binding.edtUserName.setText("");
            binding.edtPassword.setText("");
        }


        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent1 = new Intent(LoginActivity.this, DashboardActivity.class);
                // startActivity(intent1);
                if (SharedPreferencesManager.getIsHostConfig(context)) {
                    String user = binding.edtUserName.getText().toString().trim();
                    String password = binding.edtPassword.getText().toString().trim();
                    if (binding.chkRemember.isChecked()) {
                        // Save the username and password in SharedPreferences
                        SharedPreferencesManager.setIsLoginSaved(context, true);
                        SharedPreferencesManager.setSavedUser(context, user);
                        SharedPreferencesManager.setSavedPassword(context, password);
                    } else {
                        // Clear saved credentials
                        SharedPreferencesManager.setIsLoginSaved(context, false);
                        SharedPreferencesManager.setSavedUser(context, "");
                        SharedPreferencesManager.setSavedPassword(context, "");
                    }
                    if (user.equalsIgnoreCase("") || password.equalsIgnoreCase("")) {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.login_data_validation));
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(APIConstants.K_USER, user);
                            jsonObject.put(APIConstants.K_PASSWORD, password);
                            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                            userLogin(jsonObject, APIConstants.M_USER_LOGIN,"Please wait...\nUser login is in progress");
                        } catch (JSONException e) {

                        }
                    }
                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.url_not_config));
                }
            }
        });
        binding.imgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent configIntent = new Intent(LoginActivity.this, URLConfigActivity.class);
                startActivity(configIntent);

            }
        });
        binding.btnClear.setOnClickListener(view -> {
            binding.chkRemember.setChecked(false);
            binding.edtUserName.setText("");
            binding.edtPassword.setText("");
            SharedPreferencesManager.setIsLoginSaved(context, false);
            SharedPreferencesManager.setSavedUser(context, "");
            SharedPreferencesManager.setSavedPassword(context, "");
            binding.chkRemember.setChecked(false);
        });
        binding.textDeviceId.setText("Share device ID to admin for device registration\nDevice ID: " + SharedPreferencesManager.getDeviceId(context) + "\nIgnore if device already registered.");
    }


    private void userLogin(JSONObject loginRequestObject, String METHOD_NAME, String progress_message) {


            showProgress(context, progress_message);

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + METHOD_NAME).addJSONObjectBody(loginRequestObject)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {
                            hideProgressDialog();
                            if (result != null) {
                                try {
                                    Log.e("LOGINRESULT", result.toString());
                                    String status = result.getString(APIConstants.K_STATUS).trim();
                                    String message = result.getString(APIConstants.K_MESSAGE).trim();
                                    if (status.equalsIgnoreCase("true")) {
                                        SharedPreferencesManager.setSavedUser(context, loginRequestObject.getString(APIConstants.K_USER));
                                        SharedPreferencesManager.setSavedPassword(context, loginRequestObject.getString(APIConstants.K_PASSWORD));
                                        SharedPreferencesManager.setSavedPassword(context, binding.edtPassword.getText().toString().trim());
                                        Intent intent1 = new Intent(LoginActivity.this, LoadingUnloadingActivity.class);
                                        startActivity(intent1);

                                    } else {
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                        hideProgressDialog();
                                    }
                                } catch (JSONException e) {
                                    hideProgressDialog();
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
                                }
                            } else {
                                hideProgressDialog();
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            hideProgressDialog();
                            Log.e("ERROR", anError.getErrorDetail());
//                        if (BuildConfig.DEBUG) {
//                            // do something for a debug build
//                            try {
//                                parseJson(new JSONObject(AssetUtils.getJsonFromAssets(context,"loginres.json")),new JSONObject(AssetUtils.getJsonFromAssets(context,"loginreq.json")));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }else{
                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                hideProgressDialog();
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                hideProgressDialog();
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            } else {
                                hideProgressDialog();
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            }
                            //}

                        }
                    });
    }
    private void parseJson(JSONObject result,JSONObject loginRequestObject){
        if (result != null) {
            try {
                Log.e("LOGINRESULT", result.toString());
                String status = result.getString(APIConstants.K_STATUS).trim();
                String message = result.getString(APIConstants.K_MESSAGE).trim();
                if (status.equalsIgnoreCase("true")) {
                    SharedPreferencesManager.setSavedUser(context, loginRequestObject.getString(APIConstants.K_USER));
                    SharedPreferencesManager.setSavedPassword(context, loginRequestObject.getString(APIConstants.K_PASSWORD));
                    SharedPreferencesManager.setSavedPassword(context, binding.edtPassword.getText().toString().trim());
                    Intent intent1 = new Intent(LoginActivity.this, LoadingUnloadingActivity.class);
                    startActivity(intent1);
                    //finishActivity(1);
                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                }
            } catch(JSONException e) {
                hideProgressDialog();
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
            }
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context,getResources().getString(R.string.communication_error));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
        finish();
    }
}