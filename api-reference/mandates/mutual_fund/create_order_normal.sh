curl --location 'http/api.juspay.in/ecr/orders' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'version: 2020-02-17' \
--header 'x-merchantid: azhar_test' \
--header 'Authorization: Basic N0JBRDA4OUExRjYwNEYwREEyNDlCNjY3QTVCMUQzM0Q6' \
--data-urlencode 'order_id=HS1720178554asds' \
--data-urlencode 'amount=1600' \
--data-urlencode 'currency=INR' \
--data-urlencode 'customer_phone=8467359820' \
--data-urlencode 'mutual_fund_details=[{"memberId":"ABCDE","userId":"ABCDEFGHIJ","mfPartner":"BSE","folioNumber":"190983010","orderNumber":"order_zer12345ssss","amount":"800","schemeCode":"LT","amcCode":"UYTIUI","panNumber":"TYLIO7823U","investmentType":"LUMPSUM"},{"memberId":"ABCDE","userId":"ABCDEFGHIJ","mfPartner":"BSE","folioNumber":"190983010","orderNumber":"order_zer12345sssss","amount":"800.00","schemeCode":"LT","amcCode":"UYTIUI","panNumber":"TYLIO7823U","investmentType":"LUMPSUM"}]' \
