package in.juspay.devtools;

import androidx.core.view.ViewCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import in.juspay.devtools.databinding.ActivityCheckoutBinding;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import org.json.JSONObject;
import okhttp3.*;
import java.io.IOException;
import java.util.Base64;
import android.webkit.WebView;
import in.juspay.hyperinteg.HyperServiceHolder;
import java.util.*;
import in.juspay.devtools.databinding.ActivityCheckoutBinding;


public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private Button processButton;
    private HyperServiceHolder hyperServicesHolder;
    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog dialog;
    private String amountString;
    private int item1Price, item2Price, item1Count, item2Count;
    private TextView item1PriceTv, item2PriceTv, item1CountTv, item2CountTv, totalAmountTv, taxTv, totalPayableTv;
    private double taxPercent = 0.18;
    private ImageView backImage;

    private String order_id = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updatingUI();

        hyperServicesHolder = new HyperServiceHolder(this);
        hyperServicesHolder.setCallback(createHyperPaymentsCallbackAdapter());
        processButton = findViewById(R.id.rectangle_9);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

                new Thread(() -> fetchPaymentPageUrl()).start();
            }
        });
        setContentView(binding.getRoot());
        backImage = findViewById(R.id.imageView);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    //block:start:process-sdk
    private void startPayments(JSONObject sdk_payload) {
        // Make sure to use the same hyperServices instance which was created in
        // ProductsActivity.java
        Helper helper = new Helper();
        helper.showSnackbar("Process Called!", coordinatorLayout);

        // To get this sdk_payload you need to hit your backend API which will again hit the Create Order API 
        // and the sdk_payload resposne from the Create Order API need to be passed here
        hyperServicesHolder.process(sdk_payload);

    }
    //block:end:process-sdk


    protected void fetchPaymentPageUrl() {
        setLoaderVisibility(true);
        JSONObject payload = new JSONObject();
        String apiKey = getString(R.string.api_key);
        String clientId = getString(R.string.client_id);
        String merchantId = getString(R.string.merchant_id);
        String amountString = "1.0";
        UUID orderId = UUID.randomUUID();

        try {
            payload.put("order_id", orderId);
            payload.put("amount", amountString);
            payload.put("customer_id", "test1234");
            payload.put("customer_email", "test@juspay.in");
            payload.put("customer_phone", "9876543201");
            payload.put("payment_page_client_id", clientId);
            payload.put("action", "paymentPage");
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.get("application/json");
        RequestBody requestBody = RequestBody.create(payload.toString(), mediaType);
        String authorization = "Basic " + apiKey;
        Request request = new Request.Builder()
                .url("https://sandbox.juspay.in/session")
                .method("POST", requestBody)
                .addHeader("x-merchantid", merchantId)
                .addHeader("Authorization", authorization)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String apiResponse = response.body().string();
                    JSONObject jsonResponse = new JSONObject(apiResponse);
                    String paymentUrl = jsonResponse.getJSONObject("payment_links").getString("web");

                    order_id = jsonResponse.getString("order_id");

                    // Update UI on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        launchWebView(paymentUrl);
                        setLoaderVisibility(false);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }






    private void setLoaderVisibility(final boolean isVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isVisible) {
                    binding.progressBarCyclic.setVisibility(View.VISIBLE);
                } else {
                    binding.progressBarCyclic.setVisibility(View.GONE);
                }
            }
        });
    }

    // block:start:create-hyper-callback
    private HyperPaymentsCallbackAdapter createHyperPaymentsCallbackAdapter() {
        return new HyperPaymentsCallbackAdapter() {
            @Override
            public void onEvent(JSONObject jsonObject, JuspayResponseHandler responseHandler) {
                Intent redirect = new Intent(CheckoutActivity.this, ResponsePage.class);
                redirect.putExtra("responsePayload", jsonObject.toString());
                System.out.println("jsonObject>>> " +jsonObject);
                try {
                    String event = jsonObject.getString("event");
                    if (event.equals("hide_loader")) {
                        // Hide Loader
                        dialog.hide();
                    }
                    // Handle Process Result
                    // This case will reach once the Hypercheckout screen closes
                    // block:start:handle-process-result
                    else if (event.equals("process_result")) {
                        boolean error = jsonObject.optBoolean("error");
                        JSONObject innerPayload = jsonObject.optJSONObject("payload");
                        String status = innerPayload.optString("status");

                        if (!error) {
                            switch (status) {
                                case "charged":
                                    // Successful Transaction
                                    // check order status via S2S API
                                    redirect.putExtra("status", "OrderSuccess");
                                    startActivity(redirect);
                                    break;
                                case "cod_initiated":
                                    redirect.putExtra("status", "CODInitiated");
                                    startActivity(redirect);
                                    break;
                            }
                        } else {
                            switch (status) {
                                case "backpressed":
                                    // user back-pressed from PP without initiating transaction

                                    break;
                                case "user_aborted":
                                    // user initiated a txn and pressed back
                                    // check order status via S2S API
                                    Intent successIntent = new Intent(CheckoutActivity.this, ResponsePage.class);
                                    redirect.putExtra("status", "UserAborted");
                                    startActivity(redirect);
                                    break;
                                case "pending_vbv":
                                    redirect.putExtra("status", "PendingVBV");
                                    startActivity(redirect);
                                    break;
                                case "authorizing":
                                    // txn in pending state
                                    // check order status via S2S API
                                    redirect.putExtra("status", "Authorizing");
                                    startActivity(redirect);
                                    break;
                                case "authorization_failed":
                                    redirect.putExtra("status", "AuthorizationFailed");
                                    startActivity(redirect);
                                    break;
                                case "authentication_failed":
                                    redirect.putExtra("status", "AuthenticationFailed");
                                    startActivity(redirect);
                                    break;
                                case "api_failure":
                                    redirect.putExtra("status", "APIFailure");
                                    startActivity(redirect);
                                    break;
                                    // txn failed
                                    // check order status via S2S API
                                default:
                                    redirect.putExtra("status", "APIFailure");
                                    startActivity(redirect);
                                    break;
                            }
                        }
                    }
                    // block:end:handle-process-result
                } catch (Exception e) {
                    // merchant code...
                }
            }
        };
    }
    // block:end:create-hyper-callback



    private void launchWebView(String url) {
        WebView.setWebContentsDebuggingEnabled(true);
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("return_url", "");
        startActivityForResult(intent, 124);
        setLoaderVisibility(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 124) {
            if (order_id != null) {
                // Handle the payment status using the order_id set while calling the session API
                // You can show a loader until the payment_status response and hide it after the response
            } else {
                // Handle the case where order_id is null
            }
        }
    }

    //block:start:onBackPressed
    @Override
    public void onBackPressed() {
        boolean handleBackpress = hyperServicesHolder.onBackPressed();
        if(!handleBackpress) {
            super.onBackPressed();
        }

    }
    //block:end:onBackPressed

    private void updatingUI(){
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        dialog = new ProgressDialog(CheckoutActivity.this);
        dialog.setMessage("Processing...");

        Intent i = getIntent();
        item1Count = i.getIntExtra("item1Count", 1);
        item2Count = i.getIntExtra("item2Count", 1);
        item1Price = i.getIntExtra("item1Price", 1);
        item2Price = i.getIntExtra("item2Price", 1);

        item1PriceTv = findViewById(R.id.some_id1);
        item2PriceTv = findViewById(R.id.some_id2);
        item1CountTv = findViewById(R.id.x2);
        item2CountTv = findViewById(R.id.x3);

        item1CountTv.setText("x"+Integer.toString(item1Count));
        item2CountTv.setText("x"+Integer.toString(item2Count));
        int item1Amount = item1Price*item1Count;
        int item2Amount = item2Price*item2Count;
        item1PriceTv.setText("₹ "+Integer.toString(item1Amount));
        item2PriceTv.setText("₹ "+Integer.toString(item2Amount));

        totalAmountTv = findViewById(R.id.some_id);
        taxTv = findViewById(R.id.some_id3);
        totalPayableTv = findViewById(R.id.some_id5);

        int totalAmount = item1Amount + item2Amount;
        double tax = totalAmount * taxPercent;
        double totalPayable = totalAmount + tax;
        Helper helper = new Helper();
        amountString = Double.toString(helper.round(totalPayable, 2));
        String taxString = Double.toString(helper.round(tax, 2));
        totalAmountTv.setText("₹ "+Integer.toString(totalAmount));
        taxTv.setText("₹ "+taxString);
        totalPayableTv.setText("₹ "+amountString);
    }

    /*
    Optional Block
    These functions are only supposed to be implemented in case if you are overriding
    onActivityResult or onRequestPermissionsResult Life cycle hooks


    - onActivityResult
    - Handling onActivityResult hook and passing data to HyperServices Instance, to handle App Switch
    @Override
    public void onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // block:start:onActivityResult

        // If super.onActivityResult is available use following:
        // super.onActivityResult(requestCode, resultCode, data);

        // In case super.onActivityResult is NOT available please use following:
        // if (data != null) {
        //    hyperServices.onActivityResult(requestCode, resultCode, data);
        // }

        // block:end:onActivityResult

        // Rest of your code.
    }


    - onRequestPermissionsResult
    - Handling onRequestPermissionsResult hook and passing data to HyperServices Instance, to OTP reading permissions
    @Override
    public void onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // block:start:onRequestPermissionsResult

        //  If super.onRequestPermissionsResult is available use following:
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // In case super.onActivityResult is NOT available please use following:
        // hyperServices.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // block:end:onRequestPermissionsResult
    }
     */
}
