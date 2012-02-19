java -Xms1024m -Xmx1024m -cp ../build/mymedialite.jar ItemRecommendation ^
  --recommender=BPRMF ^
  --data-dir=C:\dataset\ ^
  --training-file=viewings.csv ^
  --test-ratio=0.2

pause