package com.psl.ordermodule20;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.psl.ordermodule20.Helper.APIConstants;
import com.psl.ordermodule20.Helper.AssetUtils;
import com.psl.ordermodule20.Helper.ConnectionDetector;
import com.psl.ordermodule20.Helper.CustomProgressDialog;
import com.psl.ordermodule20.Helper.DefaultConstants;
import com.psl.ordermodule20.Helper.SharedPreferencesManager;
import com.psl.ordermodule20.adapter.WorkOrderDetailsAdapter;
import com.psl.ordermodule20.databinding.ActivityLoadingUnloadingBinding;
import com.psl.ordermodule20.viewHolder.OrderDetails;
import com.psl.ordermodule20.viewHolder.TagWithDestination;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoadingUnloadingActivity extends AppCompatActivity {
    private Context context = this;
    private ConnectionDetector cd;
    private JSONObject workOrderDetailsJsonObject = new JSONObject();
    private String workOrderType = "";
    private String workOrderNo = "";
    private String workOrderStatus = "";
    private int POLLING_TIMER=10000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private WorkOrderDetailsAdapter workOrderDetailsRecAdapter, workOrderDetailsDisAdapter;
    private List<OrderDetails> orderDetailsList, recOrderDetailsList, disOrderDetailsList;
    private boolean isPollingInProgress = false;

    boolean IS_WORK_NEED_TO_STOP = false;
    boolean IS_ON = false;
    boolean IS_API_CALL_IS_IN_PROGRESS = false;
    private ActivityLoadingUnloadingBinding binding;
    private BottomSheetDialog bottomSheetDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_loading_unloading);
        binding = DataBindingUtil.setContentView(LoadingUnloadingActivity.this,R.layout.activity_loading_unloading);
        cd = new ConnectionDetector(context);

        try {
            String action = "ON";
            startReader(action);
            binding.btnOnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("ON".equals(binding.btnOnOff.getText())) {
                        IS_ON = false;
                        binding.btnOnOff.setText("OFF");
                        binding.btnOnOff.setBackground(getDrawable(R.drawable.custom_off_button));
                    } else if ("OFF".equals(binding.btnOnOff.getText())) {
                        IS_ON = true;
                        binding.btnOnOff.setText("ON");
                        binding.btnOnOff.setBackground(getDrawable(R.drawable.custom_on_button));
                    }

                    String action = IS_ON ? "OFF" : "ON";
                    startReader(action);
                }
            });

            POLLING_TIMER = SharedPreferencesManager.getPollingTimer(LoadingUnloadingActivity.this);

            binding.rvPallet.setLayoutManager(new GridLayoutManager(LoadingUnloadingActivity.this, 1));
            if (orderDetailsList != null) {
                orderDetailsList.clear();
            }
            binding.rvPallet.setLayoutManager(new GridLayoutManager(LoadingUnloadingActivity.this, 1));
            if (recOrderDetailsList != null) {
                recOrderDetailsList.clear();
            }
            binding.disPallet.setLayoutManager(new GridLayoutManager(LoadingUnloadingActivity.this, 1));
            if (disOrderDetailsList != null) {
                disOrderDetailsList.clear();
            }
            orderDetailsList = new ArrayList<>();
            recOrderDetailsList = new ArrayList<>();
            disOrderDetailsList = new ArrayList<>();
            workOrderDetailsRecAdapter = new WorkOrderDetailsAdapter(LoadingUnloadingActivity.this, recOrderDetailsList, workOrderType);
            binding.rvPallet.setAdapter(workOrderDetailsRecAdapter);
            workOrderDetailsDisAdapter = new WorkOrderDetailsAdapter(LoadingUnloadingActivity.this, disOrderDetailsList, workOrderType);
            binding.disPallet.setAdapter(workOrderDetailsDisAdapter);
            binding.textDestination.setText("");
            binding.textPalletNo.setText("");
            new GetWorkOrderDetailsTask(this).execute();//changed
        }catch (Exception ex){
            Log.e("excp",ex.getMessage());

        }


    }
    private Handler workOrderPollingApiHandler = new Handler();
    private Runnable workOrderPollingApiRunnable;
    private void startWorkOrderPollingApiHandler() {
        try {
            workOrderPollingApiRunnable = new Runnable() {
                @Override
                public void run() {
                    // Do something after every 15 seconds
                    if (!IS_WORK_NEED_TO_STOP) {
                        binding.textDestination.setText("");
                        binding.textPalletNo.setText("");
                        new GetWorkOrderDetailsTask(LoadingUnloadingActivity.this).execute();
                    }

                    Log.e("pollingtimer", "" + POLLING_TIMER);
                    workOrderPollingApiHandler.postDelayed(this, POLLING_TIMER);

                }
            };
        }catch (Exception ex){
            Log.e("HANDEXC", ex.getMessage());
        }
        // Post the initial Runnable with a delay of 2 seconds first time start handler after 2 seconds
        workOrderPollingApiHandler.postDelayed(workOrderPollingApiRunnable, 2000);
    }
    @Override
    protected void onDestroy() {
        // Stop the handler when the activity is destroyed
        String action = "OFF";
        startReader(action);
        stopWorkOrderPollingApiHandler();
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //getWorkOrderList();
        try {
            startWorkOrderPollingApiHandler();
        }catch (Exception e){
            Log.e("onresumesxc",e.getMessage());
        }

        // handler.postDelayed(runnable, CALL_INTERVAL);

    }
    private void startReader(String action){

        String readerStat = action.equals("ON") ? "1" : "0";
        IS_API_CALL_IS_IN_PROGRESS = true;
        JSONObject obj = new JSONObject();
        try {
            obj.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(LoadingUnloadingActivity.this));
            obj.put(APIConstants.K_READER_STATUS, readerStat);
            Log.e("StatusObj", obj.toString());
        } catch (JSONException e) {
            Log.e("exccc",e.getMessage());

            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        AndroidNetworking.post(SharedPreferencesManager.getHostUrl(LoadingUnloadingActivity.this) + APIConstants.M_START_REDAER).addJSONObjectBody(obj)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("ResponseReader", response.toString());
                        IS_API_CALL_IS_IN_PROGRESS = false;
                        IS_WORK_NEED_TO_STOP = false;
                        Log.e("resultReader", response.toString());
                        try {
                            if (response.getString(APIConstants.K_STATUS).equalsIgnoreCase("true")) {

                            }
                        } catch (JSONException e) {
                            Log.e("RESEXC",e.getMessage());
                            // throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        IS_API_CALL_IS_IN_PROGRESS = false;
                        IS_WORK_NEED_TO_STOP = false;

                    }
                });
    }
    private void stopWorkOrderPollingApiHandler() {
        // Remove any pending callbacks and messages
        workOrderPollingApiHandler.removeCallbacks(workOrderPollingApiRunnable);
    }

    public class GetWorkOrderDetailsTask extends AsyncTask<Void, Void, JSONObject> {
        private WeakReference<LoadingUnloadingActivity> activityReference;

        public GetWorkOrderDetailsTask(LoadingUnloadingActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            final LoadingUnloadingActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            try {
                IS_API_CALL_IS_IN_PROGRESS = true;
                JSONObject obj = new JSONObject();
                try {
                    obj.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(LoadingUnloadingActivity.this));//changed
                } catch (JSONException e) {
                    //throw new RuntimeException(e);
                    IS_API_CALL_IS_IN_PROGRESS = false;
                    return null;
                }
                Log.e("GETWORKDETAILSREQ", obj.toString());
                OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                        .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                        .build();
                AndroidNetworking.post(SharedPreferencesManager.getHostUrl(LoadingUnloadingActivity.this) + APIConstants.M_GET_WORK_ORDER_DETAILS).addJSONObjectBody(obj)
                        .setTag("test")
                        .setPriority(Priority.LOW)
                        .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("GETWORKDETAILSRES", response.toString());
                                IS_API_CALL_IS_IN_PROGRESS = false;
                                if (response != null) {
                                    parseWorkDetailsObjectAndDoAction(response);
                                } else {
                                    //handler.postDelayed(runnable, SharedPreferencesManager.getPollingTimer(LoadingUnloadingActivity.this));
                                    AssetUtils.showCommonBottomSheetErrorDialog(LoadingUnloadingActivity.this, getResources().getString(R.string.communication_error));
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                IS_API_CALL_IS_IN_PROGRESS = false;
                                handler.postDelayed(runnable, SharedPreferencesManager.getPollingTimer(LoadingUnloadingActivity.this));
                                //TODO need to uncomment below line
                                String orderDetailsString = AssetUtils.getJsonFromAssets(LoadingUnloadingActivity.this, "updateworkorderstatus.json");
                                try {
                                    JSONObject mainObject = new JSONObject(orderDetailsString);
                                    parseWorkDetailsObjectAndDoAction(mainObject);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                    AssetUtils.showCommonBottomSheetErrorDialog(LoadingUnloadingActivity.this, getResources().getString(R.string.communication_error));
                                } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                    AssetUtils.showCommonBottomSheetErrorDialog(LoadingUnloadingActivity.this, getResources().getString(R.string.internet_error));
                                } else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(LoadingUnloadingActivity.this, getResources().getString(R.string.internet_error));
                                }
                            }
                        });
            } catch (Exception e) {
                IS_API_CALL_IS_IN_PROGRESS = false;
                Log.e("apiexc",e.getMessage());

                return null;
            }
            IS_API_CALL_IS_IN_PROGRESS = false;
            return null;
        }
    }

    private void parseWorkDetailsObjectAndDoAction(JSONObject response) {

        //String status = null;
        boolean status = false;
        try {
            status = response.getBoolean(APIConstants.K_STATUS);
            if (status) {
                JSONObject dataObject = response.getJSONObject(APIConstants.K_DATA);
                boolean AppStatus = dataObject.getBoolean(APIConstants.K_APP_STATUS);
                binding.appLed.setImageResource(AppStatus ? R.drawable.on_indicator : R.drawable.off_indicator);
                Log.e("Timer2",response.toString());
                if (orderDetailsList != null) {
                    orderDetailsList.clear();
                }
                if (recOrderDetailsList != null) {
                    recOrderDetailsList.clear();
                }if (disOrderDetailsList != null) {
                    disOrderDetailsList.clear();
                }
                binding.textDestination.setText("");
                if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.hide();
                }

                if(dataObject.has(APIConstants.K_POLLING_TIMER)){
                    POLLING_TIMER = dataObject.getInt(APIConstants.K_POLLING_TIMER);
                    SharedPreferencesManager.setPollingTimer(LoadingUnloadingActivity.this,POLLING_TIMER);
                }
                recOrderDetailsList.clear();
                disOrderDetailsList.clear();
                JSONArray detailsArray = dataObject.getJSONArray(APIConstants.K_WORK_ORDER_DETAILS_ARRAY);
                for(int i=0;i<detailsArray.length();i++){
                    JSONObject detailObj = detailsArray.getJSONObject(i);
                    OrderDetails orderDetails = new OrderDetails();
                    if(detailObj.getString(APIConstants.K_CURRENT_PALLET_NAME)!=null){
                        binding.textPalletNo.setText(detailObj.getString(APIConstants.K_CURRENT_PALLET_NAME));
                        binding.textPalletNo.setSelected(true);
                    }
                    detailObj.getString(APIConstants.K_WORK_ORDER_TYPE);
                    if(detailObj.getString(APIConstants.K_WORK_ORDER_NUMBER)!=null){
                        workOrderNo =  detailObj.getString(APIConstants.K_WORK_ORDER_NUMBER);
                    }
                    if (detailObj.has(APIConstants.K_PALLET_NUMBER) && !detailObj.isNull(APIConstants.K_PALLET_NUMBER)){
                        orderDetails.setPalletNumber(detailObj.getString(APIConstants.K_PALLET_NUMBER));

                    } else {
                        orderDetails.setPalletNumber(DefaultConstants.DEFAULT_PALLET_NUMBER);
                    }
                    if (detailObj.has(APIConstants.K_PALLET_TAG_ID) && !detailObj.isNull(APIConstants.K_PALLET_TAG_ID)){
                        orderDetails.setPalletTagID(detailObj.getString(APIConstants.K_PALLET_TAG_ID));
                    } else {
                        orderDetails.setPalletTagID(DefaultConstants.DEFAULT_PALLET_TAG_ID);
                    }
                    if(detailObj.has(APIConstants.K_WORK_ORDER_TYPE)){
                        orderDetails.setWorkorderType(detailObj.getString(APIConstants.K_WORK_ORDER_TYPE));
                    }
                    orderDetails.setLastUpdatedDateTime(detailObj.getString(APIConstants.K_LAST_UPDATED_DATE_TIME));
                    orderDetails.setListItemStatus(detailObj.getString(APIConstants.K_LIST_ITEM_STATUS));
                        if(detailObj.getString(APIConstants.K_WORK_ORDER_TYPE).equals("U0")){
                            if(detailObj.has(APIConstants.K_LOCATION_NAME)){
                                orderDetails.setPickupLocation(detailObj.getString(APIConstants.K_LOCATION_NAME));
                                Log.e("Pickup Location1",orderDetails.getPickupLocation());
                            }
                        } else if(detailObj.getString(APIConstants.K_WORK_ORDER_TYPE).equals("U1")){
                            if(detailObj.has(APIConstants.K_LOCATION_NAME)){
                                orderDetails.setPickupLocation(detailObj.getString(APIConstants.K_LOCATION_NAME));
                            }
                        } else if (detailObj.getString(APIConstants.K_WORK_ORDER_TYPE).equals("L0")){
                            if(detailObj.has(APIConstants.K_BIN_LOCATION)){
                                orderDetails.setPickupLocation(detailObj.getString(APIConstants.K_BIN_LOCATION));
                            }
                        } else if(detailObj.getString(APIConstants.K_WORK_ORDER_TYPE).equals("L1")){
                            if(detailObj.has(APIConstants.K_LOCATION_NAME)){
                                orderDetails.setPickupLocation(detailObj.getString(APIConstants.K_LOCATION_NAME));
                            }
                        } else if(detailObj.getString(APIConstants.K_WORK_ORDER_TYPE).equals("I0")){
                            if(detailObj.has(APIConstants.K_LOADING_AREA)){
                                orderDetails.setPickupLocation(detailObj.getString(APIConstants.K_LOADING_AREA));
                            }
                        }


                    if (detailObj.getString(APIConstants.K_PALLET_NUMBER).contentEquals(binding.textPalletNo.getText())) {
                        String palletName = detailObj.getString(APIConstants.K_PALLET_NUMBER);
                        String workOrderType = detailObj.getString(APIConstants.K_WORK_ORDER_TYPE);
                        String palletTag = detailObj.getString(APIConstants.K_PALLET_TAG_ID);
                        String WorkOrderNo = detailObj.getString(APIConstants.K_WORK_ORDER_NUMBER);
                        String destination = "";

                        if (workOrderType.equals("U0") && detailObj.has(APIConstants.K_LOADING_AREA)) {
                            destination = detailObj.getString(APIConstants.K_LOADING_AREA);
                        } else if (workOrderType.equals("U1") && detailObj.has(APIConstants.K_BIN_LOCATION)) {
                            destination = detailObj.getString(APIConstants.K_BIN_LOCATION);
                        } else if (workOrderType.equals("L0") && detailObj.has(APIConstants.K_TEMP_STORAGE)) {
                            destination = detailObj.getString(APIConstants.K_TEMP_STORAGE);
                        } else if (workOrderType.equals("L1") && detailObj.has(APIConstants.K_LOADING_AREA)) {
                            destination = detailObj.getString(APIConstants.K_LOADING_AREA);
                        } else if (workOrderType.equals("I0") && detailObj.has(APIConstants.K_BIN_LOCATION)) {
                            destination = detailObj.getString(APIConstants.K_BIN_LOCATION);
                        }

                        // Append the destination information for the current pallet number to the textDestination
                        if (!destination.isEmpty()) {
                            String currentDestination = binding.textDestination.getText().toString();
                            if (!currentDestination.isEmpty()) {
                                currentDestination += ", ";
                            }
                            currentDestination += destination;
                            binding.textDestination.setText(currentDestination);
                            binding.textDestination.setSelected(true);
                        }
                        String currentBinDestination = "";
                        String currentDestinationTag = "";
                        if(detailObj.has(APIConstants.K_CURRENT_BIN_NAME)){
                            if(detailObj.getString(APIConstants.K_CURRENT_BIN_NAME).equalsIgnoreCase("null")){
                                currentBinDestination = "__";
                            }
                            else{
                                currentBinDestination = detailObj.getString(APIConstants.K_CURRENT_BIN_NAME);
                            }
                            currentDestinationTag = detailObj.getString(APIConstants.K_CURRENT_BIN_TAGID);

                        }
                        showPopup(palletName, workOrderType, destination, currentBinDestination, palletTag, currentDestinationTag, WorkOrderNo);
                    } else{
                        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                            //bottomSheetDialog.hide();
                        }
                    }


                    orderDetailsList.add(orderDetails);

                    String workOrderType = detailObj.getString(APIConstants.K_WORK_ORDER_TYPE);
                    if ("U0".equals(workOrderType) || "U1".equals(workOrderType)) {
                        recOrderDetailsList.add(orderDetails);
                    }
                    else if("I0".equals(workOrderType)){
                        disOrderDetailsList.add(orderDetails);
                    }
//                    else if ("L0".equals(workOrderType) || "L1".equals(workOrderType)) {
//                        disOrderDetailsList.add(orderDetails);
//                    }

                }

                if (workOrderDetailsRecAdapter != null) {
                    workOrderDetailsRecAdapter.notifyDataSetChanged();
                }
                if (workOrderDetailsDisAdapter != null) {
                    workOrderDetailsDisAdapter.notifyDataSetChanged();
                }
                binding.recCount.setText(String.valueOf(recOrderDetailsList.size()));
                binding.disCount.setText(String.valueOf(disOrderDetailsList.size()));
                boolean allItemsCompleted = checkAllItemsCompleted(orderDetailsList);

                if (allItemsCompleted) {
                    String action = "Complete";
                   // updateWorkOrderStatus(action);
                }
            }//changed
        } catch (JSONException e) {
            Log.e("GETWORKDETAILSEXC", e.getMessage());
            AssetUtils.showCommonBottomSheetErrorDialog(LoadingUnloadingActivity.this, "Error parsing work details");

        }
        handler.postDelayed(runnable, SharedPreferencesManager.getPollingTimer(LoadingUnloadingActivity.this));

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(!IS_WORK_NEED_TO_STOP) {

                handler.postDelayed(runnable, SharedPreferencesManager.getPollingTimer(LoadingUnloadingActivity.this));
            }
        }
    };


    @Override
    protected void onPause() {
        String action = "OFF";
        startReader(action);
        super.onPause();
    }

    private Dialog confirmationDIalog;
    public void showCustomConfirmationDialogSpecial(String msg, String action, String palletTag, String workOrderType, String destinationTag, String WorkOrderNo) {
        if (confirmationDIalog != null) {
            confirmationDIalog.dismiss();
        }
        confirmationDIalog = new Dialog(LoadingUnloadingActivity.this);
        confirmationDIalog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmationDIalog.setCancelable(false);
        confirmationDIalog.setContentView(R.layout.custom_alert_dialog_layout2);
        TextView text = (TextView) confirmationDIalog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) confirmationDIalog.findViewById(R.id.btn_dialog);
        Button btnCancel = (Button) confirmationDIalog.findViewById(R.id.btn_dialog_cancel);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDIalog.dismiss();
                if(action.equalsIgnoreCase("SAVE")){
                    uploadWorkOrderItemToServer(palletTag, workOrderType, destinationTag, WorkOrderNo);
                }
                if(action.equalsIgnoreCase("Pause")){
                    //updateWorkOrderStatus(action);
                }

                if(action.equalsIgnoreCase("Complete")){
                   // updateWorkOrderStatus(action);
                    Log.e("customAction", action);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDIalog.dismiss();
                IS_API_CALL_IS_IN_PROGRESS = false;
                IS_WORK_NEED_TO_STOP = false;
                handler.postDelayed(runnable, SharedPreferencesManager.getPollingTimer(LoadingUnloadingActivity.this));
            }
        });
        if (LoadingUnloadingActivity.this instanceof Activity) {
            Activity activity = (Activity) LoadingUnloadingActivity.this;
            if (!activity.isFinishing()) {
                confirmationDIalog.show();
            }
        }
    }

    private boolean hasPendingWorkOrderPresent(){
        boolean hasPendingWorkOrder = false;
        for (OrderDetails order : orderDetailsList) {
            if ("Pending".equalsIgnoreCase(order.getListItemStatus())) {
                hasPendingWorkOrder = true;
                break; // Break the loop since you found an order with "R" status
            }else{

            }
        }
        return hasPendingWorkOrder;
    }
    private boolean checkAllItemsCompleted(List<OrderDetails> orderDetailsList) {
        boolean allItemsCompleted = true;

        for (OrderDetails order : orderDetailsList) {
            if (!"Completed".equalsIgnoreCase(order.getListItemStatus())) {
                allItemsCompleted = false;
                break;
            }
        }

        return allItemsCompleted;
    }

    @Override
    public void onBackPressed() {


        String action = "OFF";
        startReader(action);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
    private void showPopup(String palletNumber, String workOrderType, String suggestedDestination, String currentDestination, String palletTag, String destinationTag, String WorkorderNo) {
        // Create a custom layout for your popup
        LayoutInflater inflater = LayoutInflater.from(LoadingUnloadingActivity.this);
        View popupView = inflater.inflate(R.layout.custom_popup_layout, null);

        // Initialize views in the popup layout
        TextView textPalletName = popupView.findViewById(R.id.textPalletNo);
        TextView textSuggestDestination = popupView.findViewById(R.id.textSuggestDestination);
        TextView textActualDestinationHeader = popupView.findViewById(R.id.textActualDestinationHeader);
        TextView textActualDestination = popupView.findViewById(R.id.textActualDestination);
        Button btnPost = popupView.findViewById(R.id.btnPost);

        // Set the values based on the workOrderType
        switch (workOrderType) {
            case "U0":
            case "L0":
            case "L1":
                textPalletName.setVisibility(View.VISIBLE);
                textSuggestDestination.setVisibility(View.VISIBLE);
                textActualDestinationHeader.setVisibility(View.GONE);
                textActualDestination.setVisibility(View.GONE);
                textPalletName.setText(palletNumber);
                textSuggestDestination.setText(suggestedDestination);
                break;
            case "U1":
                textPalletName.setVisibility(View.VISIBLE);
                textSuggestDestination.setVisibility(View.VISIBLE);
                textActualDestination.setVisibility(View.VISIBLE);
                textActualDestinationHeader.setVisibility(View.VISIBLE);
                btnPost.setVisibility(View.VISIBLE);
                textPalletName.setText(palletNumber);
                textSuggestDestination.setText(suggestedDestination);
                textActualDestination.setText(currentDestination);
                if(!currentDestination.equalsIgnoreCase("__")){
                    if(!suggestedDestination.equalsIgnoreCase(currentDestination)){
                    btnPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCustomConfirmationDialogSpecial( "Do you want to put "+palletNumber+" in "+currentDestination+" ?", "SAVE", palletTag, workOrderType, destinationTag, WorkorderNo);
                        }
                    });
                    }
                    else{
                        btnPost.setVisibility(View.INVISIBLE);
                    }
                } else{
                    btnPost.setVisibility(View.INVISIBLE);
                }
                break;
            case "I0":
                textPalletName.setVisibility(View.VISIBLE);
                textSuggestDestination.setVisibility(View.VISIBLE);
                textActualDestination.setVisibility(View.VISIBLE);
                textActualDestinationHeader.setVisibility(View.VISIBLE);

                textPalletName.setText(palletNumber);
                textSuggestDestination.setText(suggestedDestination);
                textActualDestination.setText(currentDestination);
                if(!currentDestination.equalsIgnoreCase("__")) {
                    if(!suggestedDestination.equalsIgnoreCase(currentDestination)){
                        btnPost.setVisibility(View.VISIBLE);
                        btnPost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCustomConfirmationDialogSpecial("Do you want to put " + palletNumber + " in " + currentDestination + " ?", "SAVE", palletTag, workOrderType, destinationTag, WorkorderNo);
                            }
                        });
                    }
                    else{
                        btnPost.setVisibility(View.INVISIBLE);
                    }
                } else{
                    btnPost.setVisibility(View.INVISIBLE);
                }
                break;
        }

        // Create the popup dialog if it's null
        if (bottomSheetDialog == null) {
            bottomSheetDialog = new BottomSheetDialog(LoadingUnloadingActivity.this);
            bottomSheetDialog.setContentView(popupView);
            bottomSheetDialog.setCancelable(false);
        } else {
            // Update the content if the dialog is already created
            bottomSheetDialog.setContentView(popupView);
        }
        bottomSheetDialog.show();

    }
    private void uploadWorkOrderItemToServer(String palletTag, String WorkOrderType, String destinationTag, String workOrderNo) {
        try {
            TagWithDestination allTags = new TagWithDestination(workOrderNo, palletTag, destinationTag, WorkOrderType);
            allTags.setWorkOrderNo(workOrderNo);
            allTags.setDestinationTag(destinationTag);
            allTags.setWorkOrderType(WorkOrderType);
            allTags.setPalletTag(palletTag);
            Log.e("DEST_ID",destinationTag);
                String palletTagId = palletTag;
                String workOrderNumber = workOrderNo;
                String workOrderType = WorkOrderType;
                String listItemStatus = "Completed";
                String palletTagRssi = "" + 65;
                String palletTagCount = "1";
                String TransID = UUID.randomUUID().toString();
                String palletTagAntenaId = "" + 1;
                String date_time = AssetUtils.getUTCSystemDateTimeInFormatt();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                jsonObject.put(APIConstants.K_TRANS_ID, TransID);
                jsonObject.put(APIConstants.K_WORK_ORDER_NUMBER, workOrderNumber);
                jsonObject.put(APIConstants.K_LIST_ITEM_STATUS, listItemStatus);
                jsonObject.put(APIConstants.K_WORK_ORDER_TYPE, workOrderType);
                jsonObject.put(APIConstants.K_RSSI, palletTagRssi);
                jsonObject.put(APIConstants.TRANSACTION_DATE_TIME, date_time);
                jsonObject.put(APIConstants.COUNT, palletTagCount);
                jsonObject.put(APIConstants.K_PALLET_TAG_ID, palletTagId);
                jsonObject.put(APIConstants.ANTENA_ID, palletTagAntenaId);
                jsonObject.put(APIConstants.SUB_TAG_CATEGORY_ID, "PalletTag");
                jsonObject.put(APIConstants.TOUCH_POINT_TYPE, "T");

                JSONArray tagDetailsArray = new JSONArray();
                List<TagWithDestination> tags = new ArrayList<>();
                tags.add(allTags);
                for (int i = 0; i < tags.size(); i++) {
                    JSONObject obj = new JSONObject();
                    TagWithDestination tagBean = tags.get(i);
                    obj.put(APIConstants.SUB_TAG_ID, tagBean.getDestinationTag());
                    obj.put(APIConstants.COUNT, "1");
                    obj.put(APIConstants.K_RSSI, "" + 65);
                    obj.put(APIConstants.SUB_TAG_CATEGORY_ID,  "BinTag");
                    obj.put(APIConstants.SUB_TAG_TYPE, "" + 3);
                    obj.put(APIConstants.TRANSACTION_DATE_TIME, date_time);
                    tagDetailsArray.put(obj);
                }
                jsonObject.put(APIConstants.SUB_TAG_DETAILS, tagDetailsArray);
                //jsonObject.put(APIConstants.K_ASSET_SERIAL_NUMBER,serialnumber);
                Log.e("OFFLINEDATA", jsonObject.toString());
                postInventoryData(jsonObject);
        } catch (JSONException e) {
            Log.e("Exception", e.getMessage());
        }
    }

    public void postInventoryData(final JSONObject loginRequestObject) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        String Url = SharedPreferencesManager.getHostUrl(context) + APIConstants.M_POST_INVENTORY;

        AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_POST_INVENTORY).addJSONObjectBody(loginRequestObject)
                .setTag("test")
                //.addHeaders("Authorization",SharedPreferencesManager.getAccessToken(context))
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.e("result", result.toString());

                        try {
                            if (result.getString("status").equalsIgnoreCase("true")) {

                                AssetUtils.showCommonBottomSheetSuccessDialog(context, "The pallet has been moved successfully");
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, result.getString("message").trim());
                            }
                        } catch (JSONException e) {
                            // throw new RuntimeException(e);
                            AssetUtils.showCommonBottomSheetErrorDialog(context, e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("error", anError.getErrorDetail());
                        Log.e("errorcode", "" + anError.getErrorCode());
                    }
                });
        Log.e("URL", "" + Url);
        Log.e("URL", loginRequestObject.toString());
    }

}