java -Xms1024m -Xmx1024m -cp ../bin;../lib/* ItemRecommendation ^
  --recommender=BPRMF ^
  --data-dir=C:\testsets\online_update\viewings\ ^
  --training-file=training.csv ^
  --test-file=testing.csv

pause