curl -X POST https://smartgateway.hdfcbank.com/txns \
-d "order_id=:order_id" \
-d "merchant_id=:merchant_id" \
-d "payment_method_type=UPI" \
-d "payment_method=UPI" \
-d "txn_type=UPI_COLLECT" \
-d "upi_vpa=:vpa" \
-d "redirect_after_payment=true" \
-d "format=json"