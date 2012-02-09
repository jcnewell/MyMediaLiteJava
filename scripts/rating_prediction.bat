java -Xms1024m -Xmx1024m -cp ../bin;../lib/* RatingPrediction ^
  --recommender=MatrixFactorization ^
  --data-dir=C:\testsets\online_update\ratings\ ^
  --training-file=training.csv ^
  --test-file=testing.csv

pause