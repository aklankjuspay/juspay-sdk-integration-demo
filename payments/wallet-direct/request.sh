curl -X POST https://smartgateway.hdfcbank.com/txns \
-u your_api_key: \
-d "order_id=:order_id" \
-d "merchant_id=:merchant_id" \
-d "payment_method_type=WALLET" \
-d "direct_wallet_token=:direct_wallet_token"
-d "payment_method=MOBIKWIK" \
-d "redirect_after_payment=true" \
-d "format=json"
