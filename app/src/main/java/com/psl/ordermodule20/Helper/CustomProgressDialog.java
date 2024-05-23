package com.psl.ordermodule20.Helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.psl.ordermodule20.R;

public class CustomProgressDialog {
    private final Dialog dialog;
    public CustomProgressDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_layout, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void setMessage(String message) {
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);
        if (messageTextView != null) {
            messageTextView.setText(message);
            messageTextView.setVisibility(View.VISIBLE);
        }
    }

    public void show() {
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if(!dialog.isShowing())
        dialog.show();
    }

    public void hide() {
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);
        if (messageTextView != null) {
            messageTextView.setVisibility(View.GONE);
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
