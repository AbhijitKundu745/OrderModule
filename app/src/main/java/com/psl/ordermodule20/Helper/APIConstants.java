package com.psl.ordermodule20.Helper;

import android.content.Context;

import com.psl.ordermodule20.Helper.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

public class APIConstants {
    //http://psltestapi.azurewebsites.net/


    public static final String M_USER_LOGIN = "/PDA/TabLogin";
    public static final String M_READER_STATUS = "/PDA/GetReaderStatus";
    public static final String M_GET_WORK_ORDER_LIST = "/PDA/GetWorkorderList";
    public static final String M_UPDATE_WORK_ORDER_STATUS = "/PDA/UpdateWorkorderStatus";
    public static final String M_GET_WORK_ORDER_DETAILS = "/PDA/GetWorkorderListItemsV1";
    public static final String M_START_REDAER = "/PDA/StartReader";

    public static final int API_TIMEOUT = 60;
    public static final String K_STATUS = "status";
    public static final String K_MESSAGE = "message";
    public static final String K_DATA = "data";


    public static final String K_WORK_ORDER_NUMBER = "WorkorderNumber";
    public static final String K_WORK_ORDER_TYPE = "WorkorderType";
    public static final String K_WORK_ORDER_STATUS = "WorkorderStatus";
    public static final String K_TRUCK_NUMBER = "TruckNumber";
    public static final String K_LOCATION_NAME = "LocationName";
    public static final String K_POLLING_TIMER = "PollingTimer";
    public static final String K_PO_NUMBER = "PoNumber";
    public static final String K_SO_NUMBER = "SoNumber";
    public static final String K_PALLET_NUMBER = "PalletName";//changed
    public static final String K_PALLET_TAG_ID = "PalletTagID";
    public static final String K_CURRENT_PALLET_NAME = "CurrentScannedPalletName";//changed
    public static final String K_CURRENT_PALLET_TAGID = "CurrentScannedPalletTagID";//changed
    public static final String K_CURRENT_BIN_NAME = "CurrentScannedBinName";//changed
    public static final String K_CURRENT_BIN_TAGID = "CurrentScannedBinTagID";//changed
    public static final String K_LAST_UPDATED_DATE_TIME = "LastUpdatedDateTime";
    public static final String K_LOADING_AREA = "LoadingAreaName";//changed
    public static final String K_LOADING_AREA_TAG_ID = "LoadingAreaTagID";//changed
    public static final String K_TEMP_STORAGE = "TemporaryStorageName";//changed
    public static final String K_TEMP_STORAGE_TAG_ID = "TemporaryStorageTagID";//changed
    public static final String K_BIN_LOCATION = "BinLocation";
    public static final String K_BIN_LOCATION_TAG_ID = "BinLocationTagID";
    public static final String K_LIST_ITEM_STATUS = "ListItemStatus";
    public static final String K_WORK_ORDER_DETAILS_ARRAY = "OrderDetails";
    public static final String K_WORK_ORDER_DETAILS_JSONOBJECT = "WorkOrderDetailsJsonObject";
    public static final String K_USER = "UserName";
    public static final String K_PASSWORD = "Password";
    public static final String K_DEVICE_ID = "ClientDeviceID";
    public static final String K_USER_ID = "UserID";
    public static final String K_READER_STATUS = "ReaderStatus";
    public static final String K_APP_STATUS = "ApplicationStatus";


    public static final String K_COMPANY_CODE = "CompanyCode";

    public static JSONObject getJsonObjectWithCommonProperties(Context context, JSONObject existingJsonObject) {
        try {
            existingJsonObject.put(K_USER, SharedPreferencesManager.getSavedUser(context));
            existingJsonObject.put(K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
            existingJsonObject.put(K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return existingJsonObject;
    }


}
