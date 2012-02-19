java -Xms1024m -Xmx1024m -cp ../build/mymedialite.jar RatingPrediction ^
  --recommender=MatrixFactorization ^
  --data-dir=C:\dataset\ ^
  --training-file=ratings.csv ^
  --test-ratio=0.2

pause