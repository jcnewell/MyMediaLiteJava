// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
//
// This file is part of MyMediaLite.
//
// MyMediaLite is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// MyMediaLite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.mymedialite.IItemAttributeAwareRecommender;
import org.mymedialite.IItemRelationAwareRecommender;
import org.mymedialite.IIterativeModel;
import org.mymedialite.IUserAttributeAwareRecommender;
import org.mymedialite.IUserRelationAwareRecommender;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.Extensions;
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.ITimedRatings;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.RatingCrossValidationSplit;
import org.mymedialite.data.RatingType;
import org.mymedialite.data.RatingsChronologicalSplit;
import org.mymedialite.data.RatingsSimpleSplit;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.eval.ItemsCrossValidation;
import org.mymedialite.eval.RatingPredictionEvaluationResults;
import org.mymedialite.eval.Ratings;
import org.mymedialite.eval.RatingsCrossValidation;
import org.mymedialite.eval.RatingsOnline;
import org.mymedialite.hyperparameter.NelderMead;
import org.mymedialite.io.AttributeData;
import org.mymedialite.io.ItemDataFileFormat;
import org.mymedialite.io.Model;
import org.mymedialite.io.MovieLensRatingData;
import org.mymedialite.io.RatingData;
import org.mymedialite.io.RatingFileFormat;
import org.mymedialite.io.RelationData;
import org.mymedialite.io.StaticRatingData;
import org.mymedialite.io.TimedRatingData;
import org.mymedialite.ratingprediction.IIncrementalRatingPredictor;
import org.mymedialite.ratingprediction.RatingPredictor;
import org.mymedialite.ratingprediction.TimeAwareRatingPredictor;
import org.mymedialite.util.Handlers;
import org.mymedialite.util.Memory;
import org.mymedialite.util.Recommender;
import org.mymedialite.util.Utils;

/**
 * Rating prediction program, see usage() method for more information.
 * @version 2.03
 */
public class RatingPrediction {

  static final String VERSION = "2.03";
  static final SimpleDateFormat dateFormat = new SimpleDateFormat();

  // Data sets
  static IRatings training_data;
  static IRatings test_data;

  // Recommenders
  static RatingPredictor recommender = null;

  // ID mapping objects
  static IEntityMapping user_mapping = new EntityMapping();
  static IEntityMapping item_mapping = new EntityMapping();

  // User and item attributes
  static SparseBooleanMatrix user_attributes;
  static SparseBooleanMatrix item_attributes;

  // Time statistics
  static ArrayList<Double> training_time_stats = new ArrayList<Double>();
  static ArrayList<Double> fit_time_stats      = new ArrayList<Double>();
  static ArrayList<Double> eval_time_stats     = new ArrayList<Double>();
  static ArrayList<Double> rmse_eval_stats     = new ArrayList<Double>();

  // Command line parameters
  static String training_file;
  static String test_file;
  static String save_model_file = null;
  static String load_model_file = null;
  static String user_attributes_file;
  static String item_attributes_file;
  static String user_relations_file;
  static String item_relations_file;
  static String prediction_file;
  static boolean compute_fit;
  static RatingFileFormat file_format = RatingFileFormat.DEFAULT;
  static RatingType rating_type       = RatingType.DOUBLE;
  static int cross_validation;
  static boolean show_fold_results;
  static double test_ratio;
  static String chronological_split = null;
  static double chronological_split_ratio = -1;
  static Date chronological_split_time; // TODO = Date.MinValue;
  static int find_iter;
  static boolean online_eval = false;

  static void showVersion() {
    System.out.println("MyMediaLite Rating Prediction " + VERSION);
    System.out.println("Copyright (C) 2010 Zeno Gantner, Steffen Rendle");
    System.out.println("Copyright (C) 2011 Zeno Gantner, Chris Newell");
    System.out.println("This is free software; see the source for copying conditions.  There is NO");
    System.out.println("warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
    System.exit(0);
  }

  public static class ErrorHandler implements Recommender.ErrorHandler {
    public void reportError(String message) {
      usage(message);
    }
  }

  static void usage(String message) {
    System.out.println(message);
    System.out.println();
    usage(-1);
  }

  static void usage(int exit_code) {
    System.out.println("MyMediaLite rating prediction " + VERSION);
    System.out.println(" usage:  rating_prediction --training-file=FILE --recommender=METHOD [OPTIONS]");
    System.out.println("   method ARGUMENTS have the form name=value");
    System.out.println();
    System.out.println("  general OPTIONS:\n" +
        "   --recommender=METHOD             set recommender method (default: BiasedMatrixFactorization)\n" +
        "   --recommender-options=OPTIONS    use OPTIONS as recommender options\n" +
        "   --help                           display this usage information and exit\n" +
        "   --version                        display version information and exit\n" +
        "   --random-seed=N                  initialize the random number generator with N\n" +
        "   --rating-type=float|byte|double  store ratings internally as floats or bytes or doubles (default)\n" +
        "\n" +

        "   files:\n" +
        "     --training-file=FILE                   read training data from FILE\n" +
        "     --test-file=FILE                       read test data from FILE\n" +
        "     --file-format=movielens_1m|kddcup_2011|ignore_first_line|default\n" +
        "     --data-dir=DIR                         load all files from DIR\n" +
        "     --user-attributes=FILE                 file containing user attribute information, 1 tuple per line\n" +
        "     --item-attributes=FILE                 file containing item attribute information, 1 tuple per line\n" +
        "     --user-relations=FILE                  file containing user relation information, 1 tuple per line\n" +
        "     --item-relations=FILE                  file containing item relation information, 1 tuple per line\n" +
        "     --save-model=FILE                      save computed model to FILE\n" +
        "     --load-model=FILE                      load model from FILE\n" +
        "\n" +

        "   prediction options:\n" +
        "     --prediction-file=FILE         write the rating predictions to FILE\n" +
        "     --prediction-line=FORMAT       format of the prediction line; {0}, {1}, {2} refer to user ID, item ID,\n" +
        "                                    and predicted rating, respectively; default instanceof {0}\\t{1}\\t{2}\n" +
        "\n" +

        "   evaluation options:\n" +
        "     --cross-validation=K                perform k-fold cross-validation on the training data\n" +
        "     --show-fold-results                 show results for individual folds : cross-validation\n" +
        "     --test-ratio=NUM                    use a ratio of NUM of the training data for evaluation (simple split)\n" +
        "     --chronological-split=NUM|DATETIME  use the last ratio of NUM of the training data ratings for evaluation,\n" +
        "                                         or use the ratings from DATETIME on for evaluation (requires time information\n" +
        "                                         in the training data)\n" +
        "     --online-evaluation                 perform online evaluation (use every tested rating for incremental training)\n" +
        "     --search-hp                         search for good hyperparameter values (experimental)\n" +
        "     --compute-fit                       display fit on training data\n" +
        "\n" +

        "   options for finding the right number of iterations (iterative methods)\n" +
        "     --find-iter=N                  give out statistics every N iterations\n" +
        "     --max-iter=N                   perform at most N iterations\n" +
        "     --epsilon=NUM                  abort iterations if RMSE instanceof more than best result plus NUM\n" +
        "     --rmse-cutoff=NUM              abort if RMSE instanceof above NUM\n" +
        "     --mae-cutoff=NUM               abort if MAE instanceof above NUM\n");
    System.exit(exit_code);
  }

  public static void main(String[] args) throws Exception {
    
    // Handlers for uncaught exceptions and interrupts
    Thread.setDefaultUncaughtExceptionHandler(new Handlers());
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        displayStats();
      }
    });

    // Recommender arguments
    String method              = null;
    String recommender_options = "";

    // Help/version
    boolean show_help    = false;
    boolean show_version = false;

    // Arguments for iteration search
    int max_iter       = 100;
    double epsilon     = 0;
    double rmse_cutoff = Double.MAX_VALUE;
    double mae_cutoff  = Double.MAX_VALUE;

    // Data arguments
    String data_dir = "";

    // Other arguments
    boolean search_hp             = false;
    int random_seed            = -1;
    String prediction_line     = "{0}\t{1}\t{2}";

    for(String arg : args) {
      int div = arg.indexOf("=") + 1;
      String name = arg.substring(0, div);
      String value = arg.substring(div);

      // String-valued options
      if(name.equals("--training-file="))             training_file        = value;
      else if(name.equals("--test-file="))            test_file            = value;
      else if(name.equals("--recommender="))          method               = value;
      else if(name.equals("--recommender-options="))  recommender_options  += " " + value;
      else if(name.equals("--data-dir="))             data_dir             = value;
      else if(name.equals("--user-attributes="))      user_attributes_file = value;
      else if(name.equals("--item-attributes="))      item_attributes_file = value;
      else if(name.equals("--user-relations="))       user_relations_file  = value;
      else if(name.equals("--item-relations="))       item_relations_file  = value;
      else if(name.equals("--save-model="))           save_model_file      = value;
      else if(name.equals("--load-model="))           load_model_file      = value;
      else if(name.equals("--prediction-file="))      prediction_file      = value;
      else if(name.equals("--prediction-line="))      prediction_line      = value;
      else if(name.equals("--chronological-split="))  chronological_split  = value;

      // Integer-valued options
      else if(name.equals("--find-iter="))            find_iter            = Integer.parseInt(value);
      else if(name.equals("--max-iter="))             max_iter             = Integer.parseInt(value);
      else if(name.equals("--random-seed="))          random_seed          = Integer.parseInt(value);
      else if(name.equals("--cross-validation="))     cross_validation     = Integer.parseInt(value);

      // Double-valued options
      else if(name.equals("--epsilon="))              epsilon              = Double.parseDouble(value);
      else if(name.equals("--rmse-cutoff="))          rmse_cutoff          = Double.parseDouble(value);
      else if(name.equals("--mae-cutoff="))           mae_cutoff           = Double.parseDouble(value);
      else if(name.equals("--test-ratio="))           test_ratio           = Double.parseDouble(value);

      // Enum options
      else if(name.equals("--rating-type="))          rating_type          = RatingType.valueOf(value);
      else if(name.equals("--file-format="))          file_format          = RatingFileFormat.valueOf(value);

      // Boolean options
      else if(name.equals("--compute-fit"))           compute_fit          = Boolean.parseBoolean(value);
      else if(name.equals("--online-evaluation"))     online_eval          = Boolean.parseBoolean(value);
      else if(name.equals("--show-fold-results"))     show_fold_results    = Boolean.parseBoolean(value);
      else if(name.equals("--search-hp"))             search_hp            = Boolean.parseBoolean(value);
      else if(name.equals("--help"))                  show_help            = Boolean.parseBoolean(value);
      else if(name.equals("--version"))               show_version         = Boolean.parseBoolean(value);
    }
    // ... some more command line parameter actions ...
    boolean no_eval = true;
    if (test_ratio > 0 || test_file != null || chronological_split != null)
      no_eval = false;

    if (show_version) showVersion();

    if (show_help) usage(0);

    if (random_seed != -1) org.mymedialite.util.Random.initInstance(random_seed);

    // Set up recommender
    if (load_model_file != null)
      recommender = (RatingPredictor) Model.load(load_model_file);
    else if (method != null)
      recommender = Recommender.createRatingPredictor(method);
    else
      recommender = Recommender.createRatingPredictor("BiasedMatrixFactorization");

    // In case something went wrong ...
    if (recommender == null && method != null)
      usage("Unknown rating prediction method: " + method);
    if (recommender == null && load_model_file != null)
      usage("Could not load model from file "+ load_model_file);

    checkParameters();

    try {
      recommender = Recommender.configure(recommender, recommender_options, new ErrorHandler());
    } catch (IllegalAccessException e) {
      System.err.println("Unable to instantiate recommender: " + recommender.toString());
      System.exit(0);
    }

    // ID mapping objects
    if (file_format == RatingFileFormat.KDDCUP_2011) {
      user_mapping = new IdentityMapping();
      item_mapping = new IdentityMapping();
    }

    // Load all the data
    loadData(data_dir, user_attributes_file, item_attributes_file, user_relations_file, item_relations_file, !online_eval);

    System.out.println("ratings range: " + recommender.getMinRating() + ", " + recommender.getMaxRating());

    if (test_ratio > 0) {
      RatingsSimpleSplit split = new RatingsSimpleSplit(training_data, test_ratio);
      // TODO check
      training_data = split.train().get(0);
      recommender.setRatings(training_data);
      // TODO check
      test_data = split.test().get(0);
      System.out.println("test ratio " + test_ratio);
    }

    if (chronological_split != null) {
      RatingsChronologicalSplit split = chronological_split_ratio != -1
          ? new RatingsChronologicalSplit((ITimedRatings) training_data, chronological_split_ratio)
      : new RatingsChronologicalSplit((ITimedRatings) training_data, chronological_split_time);
          training_data = split.train().get(0);
          recommender.setRatings(training_data); 
          test_data = split.test().get(0);
          if (test_ratio != -1)
            System.out.println("test ratio (chronological) " + chronological_split_ratio);
          else
            System.out.println("split time {0}" + chronological_split_time);
    }

    System.out.print(Extensions.statistics(training_data, test_data, user_attributes, item_attributes, false));

    if (find_iter != 0) {
      if (!(recommender instanceof IIterativeModel) )
        usage("Only iterative recommenders (interface IIterativeModel) support --find-iter=N.");

      System.out.println(recommender.toString());

      if (cross_validation > 1) {
        RatingsCrossValidation.doIterativeCrossValidation(recommender, cross_validation, max_iter, find_iter);
      } else {
        IIterativeModel iterative_recommender = (IIterativeModel) recommender;

        if (load_model_file == null)
          recommender.train();

        if (compute_fit)
          System.out.println("fit " + Ratings.evaluate(recommender, training_data) + " iteration " + iterative_recommender.getNumIter());

        System.out.println(Ratings.evaluate(recommender, test_data) + " iteration " + iterative_recommender.getNumIter());

        for (int it = iterative_recommender.getNumIter() + 1; it <= max_iter; it++) {
          long start = Calendar.getInstance().getTimeInMillis();
          iterative_recommender.iterate();
          training_time_stats.add((double)(Calendar.getInstance().getTimeInMillis() - start) / 1000);

          if (it % find_iter == 0) {
            if (compute_fit) {
              start = Calendar.getInstance().getTimeInMillis();
              System.out.println("fit " + Ratings.evaluate(recommender, training_data) + " iteration " + it);
              fit_time_stats.add((double)(Calendar.getInstance().getTimeInMillis() - start) / 1000);
            }

            HashMap<String, Double> results = null;
            start = Calendar.getInstance().getTimeInMillis(); 
            results = Ratings.evaluate(recommender, test_data);
            eval_time_stats.add((double)(Calendar.getInstance().getTimeInMillis() - start) / 1000);
            rmse_eval_stats.add(results.get("RMSE"));
            System.out.println(results + " iteration " + it);

            Model.save(recommender, save_model_file, it);
            if (prediction_file != null)
              org.mymedialite.ratingprediction.Extensions.writePredictions(recommender, test_data, prediction_file + "-it-" + it, user_mapping, item_mapping, prediction_line);

            if (epsilon > 0.0 && results.get("RMSE") - Collections.min(rmse_eval_stats) > epsilon) {
              System.out.println(results.get("RMSE") + " >> " + Collections.min(rmse_eval_stats));
              System.out.println("Reached convergence on training/validation data after " + it + " iterations.");
              break;
            }
            if (results.get("RMSE") > rmse_cutoff || results.get("MAE") > mae_cutoff) {
              System.out.println("Reached cutoff after " + it + " iterations.");
              break;
            }
          }
        } // for
      }
    } else {
      long start = Calendar.getInstance().getTimeInMillis();

      System.out.print(recommender + " ");

      if (load_model_file == null) {
        if (cross_validation > 1) {
          System.out.println();
          RatingPredictionEvaluationResults results = RatingsCrossValidation.doCrossValidation(recommender, cross_validation, compute_fit, show_fold_results);
          System.out.print(results);
          no_eval = true;
        } else {
          if (search_hp) {
            double result = NelderMead.findMinimum("RMSE", recommender);
            System.out.println("Estimated quality (on split) " + result);
          }

          recommender.train();
          System.out.print(" training_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000 + " ");
        }
      }

      if (!no_eval) {
        start = Calendar.getInstance().getTimeInMillis();
        if (online_eval)
          System.out.print(RatingsOnline.evaluateOnline(recommender, test_data));
        else
          System.out.print(Ratings.evaluate(recommender, test_data));
        System.out.print(" testing_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);

        if (compute_fit) {
          System.out.print("\nfit ");
          start = Calendar.getInstance().getTimeInMillis();    
          System.out.print(Ratings.evaluate(recommender, training_data));
          System.out.print(" fit_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
        }

        if (prediction_file != null) {
          System.out.println();
          start = Calendar.getInstance().getTimeInMillis();
          org.mymedialite.ratingprediction.Extensions.writePredictions(recommender, test_data, prediction_file, user_mapping, item_mapping, prediction_line);
          System.out.print("prediction_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
        }
      }
      System.out.println();
    }
    Model.save(recommender, save_model_file);
    //displayStats();

  }

  static void checkParameters() {
    if (online_eval && !(recommender instanceof IIncrementalRatingPredictor))
      usage("Recommender " + recommender.getClass().getName() + " does not support incremental updates, which are necessary for an online experiment.");

    if (training_file == null && load_model_file == null)
      usage("Please provide either --training-file=FILE or --load-model=FILE.");

    if (cross_validation == 1)
      usage("--cross-validation=K requires K to be at least 2.");

    if (show_fold_results && cross_validation == 0)
      usage("--show-fold-results only works with --cross-validation=K.");

    if (cross_validation > 1 && test_ratio != 0)
      usage("--cross-validation=K and --test-ratio=NUM are mutually exclusive.");

    if (cross_validation > 1 && prediction_file != null)
      usage("--cross-validation=K and --prediction-file=FILE are mutually exclusive.");

    if (test_file == null && test_ratio == 0 && cross_validation == 0 && save_model_file == null && chronological_split == null)
      usage("Please provide either test-file=FILE, --test-ratio=NUM, --cross-validation=K, --chronological-split=NUM|DATETIME, or --save-model=FILE.");

    if (recommender instanceof IUserAttributeAwareRecommender && user_attributes_file == null)
      usage("Recommender expects --user-attributes=FILE.");

    if (recommender instanceof IItemAttributeAwareRecommender && item_attributes_file == null)
      usage("Recommender expects --item-attributes=FILE.");

    if (recommender instanceof IUserRelationAwareRecommender && user_relations_file == null)
      usage("Recommender expects --user-relations=FILE.");

    if (recommender instanceof IItemRelationAwareRecommender && user_relations_file == null)
      usage("Recommender expects --item-relations=FILE.");

    // handling of --chronological-split
    if (chronological_split != null) {
      try {
        chronological_split_ratio = Double.parseDouble(chronological_split);
      } catch(NumberFormatException e) {
        usage("Unable to parse chronological_split_ratio " + chronological_split_ratio + " as double");
      }

      if (chronological_split_ratio == -1)
        try {
          chronological_split_time = dateFormat.parse(chronological_split);
        } catch (ParseException e) {
          usage("Could not interpret argument of --chronological-split as number or date and time: " + chronological_split);
        }

      // check for conflicts
      if (cross_validation > 1)
        usage("--cross-validation=K and --chronological-split=NUM|DATETIME are mutually exclusive.");

      if (test_ratio > 1)
        usage("--test-ratio=NUM and --chronological-split=NUM|DATETIME are mutually exclusive.");
    }
  }

  static void loadData(
      String data_dir,
      String user_attributes_file, String item_attributes_file,
      String user_relation_file, String item_relation_file,
      boolean static_data) throws Exception {

    long start = Calendar.getInstance().getTimeInMillis();    

    // Read training data
    if ((recommender instanceof TimeAwareRatingPredictor || chronological_split != null) && file_format != RatingFileFormat.MOVIELENS_1M) {
      training_data = TimedRatingData.read(Utils.combine(data_dir, training_file), user_mapping, item_mapping, false);
    } else {
      if (file_format == RatingFileFormat.DEFAULT)
        training_data = static_data
        ? StaticRatingData.read(Utils.combine(data_dir, training_file), user_mapping, item_mapping, rating_type, false)
            : RatingData.read(Utils.combine(data_dir, training_file), user_mapping, item_mapping, false);
        else if(file_format == RatingFileFormat.IGNORE_FIRST_LINE)
          training_data = static_data
          ? StaticRatingData.read(Utils.combine(data_dir, training_file), user_mapping, item_mapping, rating_type, true)
              : RatingData.read(Utils.combine(data_dir, training_file), user_mapping, item_mapping, true);
          else if (file_format == RatingFileFormat.MOVIELENS_1M)
            training_data = MovieLensRatingData.read(Utils.combine(data_dir, training_file), user_mapping, item_mapping);
          else if (file_format == RatingFileFormat.KDDCUP_2011)
            training_data = org.mymedialite.io.kddcup2011.Ratings.read(Utils.combine(data_dir, training_file));
    }
    recommender.setRatings(training_data);

    // User attributes
    if (user_attributes_file != null)
      user_attributes = AttributeData.read(Utils.combine(data_dir, user_attributes_file), user_mapping);

    if (recommender instanceof IUserAttributeAwareRecommender)
      ((IUserAttributeAwareRecommender)recommender).setUserAttributes(user_attributes);

    // Item attributes
    if (item_attributes_file != null)
      item_attributes = AttributeData.read(Utils.combine(data_dir, item_attributes_file), item_mapping);

    if (recommender instanceof IItemAttributeAwareRecommender)
      ((IItemAttributeAwareRecommender)recommender).setItemAttributes(item_attributes);

    // User relation
    if (recommender instanceof IUserRelationAwareRecommender) {
      ((IUserRelationAwareRecommender)recommender).setUserRelation(RelationData.read(Utils.combine(data_dir, user_relation_file), user_mapping));
      System.out.println("relation over " + ((IUserRelationAwareRecommender)recommender).numUsers() + " users");
    }

    // Item relation
    if (recommender instanceof IItemRelationAwareRecommender) {
      ((IItemRelationAwareRecommender)recommender).setItemRelation(RelationData.read(Utils.combine(data_dir, item_relation_file), item_mapping));
      System.out.println("relation over " + ((IItemRelationAwareRecommender)recommender).numItems() + " items");
    }

    // Read test data
    if (test_file != null) {
      if (recommender instanceof TimeAwareRatingPredictor && file_format != RatingFileFormat.MOVIELENS_1M)
        test_data = TimedRatingData.read(Utils.combine(data_dir, test_file), user_mapping, item_mapping, false);
      else if (file_format == RatingFileFormat.MOVIELENS_1M)
        test_data = MovieLensRatingData.read(Utils.combine(data_dir, test_file), user_mapping, item_mapping);
      else if (file_format == RatingFileFormat.KDDCUP_2011)
        test_data = org.mymedialite.io.kddcup2011.Ratings.read(Utils.combine(data_dir, training_file));
      else
        test_data = StaticRatingData.read(Utils.combine(data_dir, test_file), user_mapping, item_mapping, rating_type, file_format == RatingFileFormat.IGNORE_FIRST_LINE);
    }

    System.out.println("loading_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
    System.out.println("memory " +  Memory.getUsage() + " MB");
  }

  static void displayStats() {
    if (training_time_stats.size() > 0)
      // TODO format floating point
      System.out.println("iteration_time: min=" + Collections.min(training_time_stats) + ", max=" + Collections.max(training_time_stats) + ", avg=" + Utils.average(training_time_stats));

    if (eval_time_stats.size() > 0)
      System.out.println("eval_time: min=" + Collections.min(eval_time_stats) + ", max=" + Collections.max(eval_time_stats) + ", avg=" + Utils.average(eval_time_stats));

    if (compute_fit && fit_time_stats.size() > 0)
      System.out.println("fit_time: min=" + Collections.min(fit_time_stats) + ", max=" + Collections.max(fit_time_stats) + ", avg=" + Utils.average(fit_time_stats));
    
    System.out.println("memory " + Memory.getUsage() + " MB");
  }

}