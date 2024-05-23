package com.psl.ordermodule20.viewHolder;

import com.psl.ordermodule20.Helper.DefaultConstants;

public class OrderDetails {

    private String PalletNumber, PalletTagID, CurrentPalletName,CurrentPalletTagID, LastUpdatedDateTime,
            PickupLocation, PickupLocationTagID, BinLocation, BinLocationTagID, ListItemStatus, WorkorderType;

    private String ordertype, serialNo;

    public String getPalletNumber() {
        return (PalletNumber != null) ? PalletNumber : DefaultConstants.DEFAULT_PALLET_NUMBER;
    }

    public void setPalletNumber(String palletNumber) {
        PalletNumber = palletNumber;
    }

    public String getPalletTagID() {
        return (PalletTagID != null) ? PalletTagID : DefaultConstants.DEFAULT_PALLET_TAG_ID;
    }

    public void setPalletTagID(String palletTagID) {
        PalletTagID = palletTagID;
    }

    public String getCurrentPalletName() {
        return (CurrentPalletName != null) ? CurrentPalletName : DefaultConstants.DEFAULT_PALLET_NAME;
    }

    public void setCurrentPalletName(String currentPalletName) {
        CurrentPalletName = currentPalletName;
    }
    public String getCurrentPalletTagID() {
        return (CurrentPalletTagID != null) ? CurrentPalletTagID : DefaultConstants.DEFAULT_PALLET_TAG_ID;
    }

    public void setCurrentPalletTagID(String currentPalletTagID) {
        CurrentPalletTagID = currentPalletTagID;
    }

    public String getLastUpdatedDateTime() {
        return (LastUpdatedDateTime != null) ? LastUpdatedDateTime : DefaultConstants.DEFAULT_LAST_UPDATED_DATETIME;
    }

    public void setLastUpdatedDateTime(String lastUpdatedDateTime) {
        LastUpdatedDateTime = lastUpdatedDateTime;
    }

    public String getPickupLocation() {
        return (PickupLocation != null) ? PickupLocation : DefaultConstants.DEFAULT_PICKUP_LOCATION;
    }

    public void setPickupLocation(String pickupLocation) {
        PickupLocation = pickupLocation;
    }

    public String getPickupLocationTagID() {
        return (PickupLocationTagID != null) ? PickupLocationTagID : DefaultConstants.DEFAULT_PICKUP_LOCATION_TAG_ID;
    }

    public void setPickupLocationTagID(String pickupLocationTagID) {
        PickupLocationTagID = pickupLocationTagID;
    }

    public String getBinLocation() {
        return (BinLocation != null) ? BinLocation : DefaultConstants.DEFAULT_BIN_LOCATION;
    }

    public void setBinLocation(String binLocation) {
        BinLocation = binLocation;
    }

    public String getBinLocationTagID() {
        return (BinLocationTagID != null) ? BinLocationTagID : DefaultConstants.DEFAULT_BIN_LOCATION_TAG_ID;
    }

    public void setBinLocationTagID(String binLocationTagID) {
        BinLocationTagID = binLocationTagID;
    }

    public String getListItemStatus() {
        return (ListItemStatus != null) ? ListItemStatus : DefaultConstants.DEFAULT_LIST_ITEM_STATUS;
    }

    public void setListItemStatus(String listItemStatus) {
        ListItemStatus = listItemStatus;
    }
    public String getWorkorderType() {
        return (WorkorderType != null) ? WorkorderType : DefaultConstants.DEFAULT_LIST_ITEM_STATUS;
    }

    public void setWorkorderType(String workorderType) {
        WorkorderType = workorderType;
    }

    public String getSerialNo() {
        return (serialNo != null) ? serialNo : "defaultSerialNo";
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getOrdertype() {
        return (ordertype != null) ? ordertype : "defaultOrdertype";
    }

    public void setOrdertype(String ordertype) {
        this.ordertype = ordertype;
    }
}
