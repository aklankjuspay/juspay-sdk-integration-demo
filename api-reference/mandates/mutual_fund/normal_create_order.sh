curl --location 'http://localhost:8081/ecr/orders' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'version: 2020-02-17' \
--header 'x-merchantid: azhar_test' \
--header 'Authorization: Basic N0JBRDA4OUExRjYwNEYwREEyNDlCNjY3QTVCMUQzM0Q6' \
--data-urlencode 'order_id=17157583001715758300' \
--data-urlencode 'amount=1' \
--data-urlencode 'currency=INR' \
--data-urlencode 'customer_id=cth_tVMuV5dG9bgmsQSb' \
--data-urlencode 'customer_email=aa@aa.com' \
--data-urlencode 'metadata.PAYTM%3Agateway_reference_id=normal' \
--data-urlencode 'mutual_fund_details=[{"memberId":"ldjasldkad","userId":"ldjasldkad","mfPartner":"ldjasldkad","folioNumber":"ldjasldkad","orderNumber":"ldjasldkad","amount":"ldjasldkad","schemeCode":"ldjasldkad","amcCode":"ldjasldkad","panNumber":"ldjasldkad","investmentType":"ldjasldkad","asdsd":"asdsadasdasdas"}]'