#!/bin/bash
aws lambda update-function-code\
    --zip-file fileb://$(pwd)/target/marketing-facebook-metrics.jar\
    --region eu-central-1\
    --function-name MarketingFacebookMetrics
