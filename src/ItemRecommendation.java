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

import it.unimi.dsi.fastutil.ints.IntArraySet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.mymedialite.IItemAttributeAwareRecommender;
import org.mymedialite.IItemRelationAwareRecommender;
import org.mymedialite.IIterativeModel;
import org.mymedialite.IRecommender;
import org.mymedialite.IUserAttributeAwareRecommender;
import org.mymedialite.IUserRelationAwareRecommender;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.data.PosOnlyFeedbackSimpleSplit;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.eval.CandidateItems;
import org.mymedialite.eval.Groups;
import org.mymedialite.eval.ItemRecommendationEvaluationResults;
import org.mymedialite.eval.Items;
import org.mymedialite.eval.ItemsCrossValidation;
import org.mymedialite.eval.ItemsFiltered;
import org.mymedialite.eval.ItemsOnline;
import org.mymedialite.grouprec.Average;
import org.mymedialite.grouprec.GroupRecommender;
import org.mymedialite.grouprec.Maximum;
import org.mymedialite.grouprec.Minimum;
import org.mymedialite.io.AttributeData;
import org.mymedialite.io.ItemData;
import org.mymedialite.io.ItemDataFileFormat;
import org.mymedialite.io.ItemDataRatingThreshold;
import org.mymedialite.io.Model;
import org.mymedialite.io.NumberFile;
import org.mymedialite.io.RelationData;
import org.mymedialite.itemrec.Extensions;
import org.mymedialite.itemrec.IIncrementalItemRecommender;
import org.mymedialite.itemrec.ItemRecommender;
import org.mymedialite.util.Handlers;
import org.mymedialite.util.Memory;
import org.mymedialite.util.Recommender;
import org.mymedialite.util.Utils;

/**
 * Item prediction program, see Usage() method for more information.
 * @version 2.03
 */
public class ItemRecommendation {

  static final String VERSION = "2.03";

  // Data
  static IPosOnlyFeedback training_data;
  static IPosOnlyFeedback test_data;
  static List<Integer> test_users;
  static List<Integer> candidate_items;
  static SparseBooleanMatrix group_to_user; // rows: groups, columns: users
  static Collection<Integer> user_groups;

  static CandidateItems eval_item_mode = CandidateItems.UNION;

  // Recommenders
  static IRecommender recommender = null;

  // ID mapping objects
  static IEntityMapping user_mapping = new EntityMapping();
  static IEntityMapping item_mapping = new EntityMapping();

  // User and item attributes
  static SparseBooleanMatrix user_attributes;
  static SparseBooleanMatrix item_attributes;

  // Command-line parameters (data)
  static String training_file;
  static String test_file;
  static ItemDataFileFormat file_format = ItemDataFileFormat.DEFAULT;
  static String data_dir = "";
  static String test_users_file;
  static String candidate_items_file;
  static String user_attributes_file;
  static String item_attributes_file;
  static String user_relations_file;
  static String item_relations_file;
  static String save_model_file = null;
  static String load_model_file = null;
  static String user_groups_file;
  static String prediction_file;

  // Command-line parameters (other)
  static boolean compute_fit;
  static int cross_validation;
  static boolean show_fold_results;
  static double test_ratio;
  static double rating_threshold = Double.NaN;
  static int num_test_users;
  static int predict_items_number = -1;
  static boolean online_eval;
  static boolean filtered_eval;
  static boolean repeat_eval;
  static String group_method;
  static boolean overlap_items;
  static boolean in_training_items;
  static boolean in_test_items;
  static boolean all_items;
  static boolean user_prediction;
  static int random_seed = -1;
  static int find_iter = 0;

  // Time statistics
  static List<Double> training_time_stats = new ArrayList<Double>();
  static List<Double> fit_time_stats      = new ArrayList<Double>();
  static List<Double> eval_time_stats     = new ArrayList<Double>();

  static void showVersion() {
    System.out.println("MyMediaLite Item Prediction from Implicit Feedback " + VERSION);
    System.out.println("Copyright (C) 2010 Zeno Gantner, Steffen Rendle, Christoph Freudenthaler");
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
    System.out.println("MyMediaLite item recommendation from positive-only feedback " + VERSION);
    System.out.println(" usage:   item_recommendation --training-file=FILE --recommender=METHOD [OPTIONS]");
    System.out.println("   method ARGUMENTS have the form name=value");
    System.out.println();
    System.out.println("  general OPTIONS:\n" +
        "   --recommender=METHOD             use METHOD for recommendations (default: MostPopular)\n" +
        "   --group-recommender=METHOD       use METHOD to combine the predictions for several users\n" +
        "   --recommender-options=OPTIONS    use OPTIONS as recommender options\n" +
        "   --help                           display this usage information and exit\n" +
        "   --version                        display version information and exit\n" +
        "   --random-seed=N                  initialize the random number generator with N\n" +
        "\n" +

      "  files:\n" +
      "   --training-file=FILE         read training data from FILE\n" +
      "   --test-file=FILE             read test data from FILE\n" +
      "   --file-format=ignore_first_line|default\n" +
      "   --data-dir=DIR               load all files from DIR\n" +
      "   --user-attributes=FILE       file containing user attribute information, 1 tuple per line\n" +
      "   --item-attributes=FILE       file containing item attribute information, 1 tuple per line\n" +
      "   --user-relations=FILE        file containing user relation information, 1 tuple per line\n" +
      "   --item-relations=FILE        file containing item relation information, 1 tuple per line\n" +
      "   --user-groups=FILE           file containing group-to-user mappings, 1 tuple per line\n" +
      "   --save-model=FILE            save computed model to FILE\n" +
      "   --load-model=FILE            load model from FILE\n" +
      "\n" +

      "  data interpretation:\n" +
      "     --user-prediction          transpose the user-item matrix and perform user prediction instead of item prediction\n" +
      "     --rating-threshold=NUM     (for rating datasets) interpret rating >= NUM as positive feedback\n" +
      "\n" +

      "  choosing the items for evaluation/prediction (mutually exclusive):\n" +
      "   --candidate-items=FILE       use the items in FILE (one per line) as candidate items in the evaluation\n" +
      "   --overlap-items              use only the items that are both in the training and the test set as candidate items in the evaluation\n" +
      "   --in-training-items          use only the items in the training set as candidate items in the evaluation\n" +
      "   --in-test-items              use only the items in the test set as candidate items in the evaluation\n" +
      "   --all-items                  use all known items as candidate items in the evaluation\n" +

      "  choosing the users for evaluation/prediction\n" +
      "   --test-users=FILE    predict items for users specified in FILE (one user per line)\n" +
      "\n" +

      "  prediction options:\n" +
      "   --prediction-file=FILE       write ranked predictions to FILE, one user per line\n" +
      "   --predict-items-number=N     predict N items per user (needs --predict-items-file)\n" +
      "\n" +

      "  evaluation options:\n" +
      "   --cross-validation=K         perform k-fold cross-validation on the training data\n" +
      "   --show-fold-results          show results for individual folds in cross-validation\n" +
      "   --test-ratio=NUM             evaluate by splitting of a NUM part of the feedback\n" +
      "   --num-test-users=N           evaluate on only N randomly picked users (to save time)\n" +
      "   --online-evaluation          perform online evaluation (use every tested user-item combination for incremental training)\n" +
      "   --filtered-evaluation        perform evaluation filtered by item attribute (expects --item-attributes=FILE)\n" +
      "   --repeat-evaluation          assume that items can be accessed repeatedly - items can occur both in the training and the test data for one user\n" +
      "   --compute-fit                display fit on training data\n" +
      "\n" +

      "   finding the right number of iterations (iterative methods)\n" +
      "   --find-iter=N                give out statistics every N iterations\n" +
      "   --max-iter=N                 perform at most N iterations\n" +
      "   --auc-cutoff=NUM             abort if AUC is below NUM\n" +
      "   --prec5-cutoff=NUM           abort if prec@5 is below NUM\n");

    System.exit(exit_code);
  }

  public static void main(String[] args) {

    // Handlers for uncaught exceptions and interrupts
    Thread.setDefaultUncaughtExceptionHandler(new Handlers());
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        displayStats();
      }
    });
    
    // Recommender arguments
    String method      = null;
    String recommender_options = "";

    // Help/version
    boolean show_help    = false;
    boolean show_version = false;

    // Variables for iteration search
    int max_iter        = 500;
    double auc_cutoff   = 0;
    double prec5_cutoff = 0;
    compute_fit         = false;

    // Other parameters
    test_ratio     = 0;
    num_test_users = -1;
    repeat_eval    = false;

    for(String arg : args) {
      int div = arg.indexOf("=") + 1;
      String name = arg.substring(0, div);
      String value = arg.substring(div);

      // String-valued options
      if(name.equals("--training-file="))             training_file        = value;
      else if(name.equals("--test-file="))            test_file            = value;
      else if(name.equals("--recommender="))          method               = value;
      else if(name.equals("--group-recommender="))    group_method         = value;
      else if(name.equals("--recommender-options="))  recommender_options  += " " + value;
      else if(name.equals("--data-dir="))             data_dir             = value;
      else if(name.equals("--user-attributes="))      user_attributes_file = value;
      else if(name.equals("--item-attributes="))      item_attributes_file = value;
      else if(name.equals("--user-relations="))       user_relations_file  = value;
      else if(name.equals("--item-relations="))       item_relations_file  = value;
      else if(name.equals("--save-model="))           save_model_file      = value;
      else if(name.equals("--load-model="))           load_model_file      = value;
      else if(name.equals("--prediction-file="))      prediction_file      = value;
      else if(name.equals("--test-users="))           test_users_file      = value;
      else if(name.equals("--candidate-items="))      candidate_items_file = value;
      else if(name.equals("--user-groups="))          user_groups_file     = value;

      // Integer-valued options
      else if(name.equals("--find-iter="))            find_iter            = Integer.parseInt(value);
      else if(name.equals("--max-iter="))             max_iter              = Integer.parseInt(value);
      else if(name.equals("--random-seed="))          random_seed          = Integer.parseInt(value);
      else if(name.equals("--predict-items-number=")) predict_items_number = Integer.parseInt(value);
      else if(name.equals("--num-test-users="))       num_test_users       = Integer.parseInt(value);
      else if(name.equals("--cross-validation="))     cross_validation     = Integer.parseInt(value);

      // Double-valued options
      else if(name.equals("--auc-cutoff="))           auc_cutoff       = Double.parseDouble(value);
      else if(name.equals("--prec5-cutoff="))         prec5_cutoff     = Double.parseDouble(value);
      else if(name.equals("--test-ratio="))           test_ratio       = Double.parseDouble(value);
      else if(name.equals("--rating-threshold="))     rating_threshold = Double.parseDouble(value);

      // Enum options
      else if(name.equals("--file-format="))          file_format = ItemDataFileFormat.valueOf(value);

      // Boolean options
      else if(name.equals("--user-prediction"))       user_prediction   = Boolean.parseBoolean(value);
      else if(name.equals("--compute-fit"))           compute_fit       = Boolean.parseBoolean(value);
      else if(name.equals("--online-evaluation"))     online_eval       = Boolean.parseBoolean(value);
      else if(name.equals("--filtered-evaluation"))   filtered_eval     = Boolean.parseBoolean(value);
      else if(name.equals("--repeat-evaluation"))     repeat_eval       = Boolean.parseBoolean(value);
      else if(name.equals("--show-fold-results"))     show_fold_results = Boolean.parseBoolean(value);
      else if(name.equals("--overlap-items"))         overlap_items     = Boolean.parseBoolean(value);
      else if(name.equals("--all-items"))             all_items         = Boolean.parseBoolean(value);
      else if(name.equals("--in-training-items"))     in_training_items = Boolean.parseBoolean(value);
      else if(name.equals("--in-test-items"))         in_test_items     = Boolean.parseBoolean(value);
      else if(name.equals("--help"))                  show_help         = Boolean.parseBoolean(value);
      else if(name.equals("--version"))               show_version      = Boolean.parseBoolean(value);
      else usage("Did not understand " + name);
    }

    boolean no_eval = true;
    if (test_ratio > 0 || test_file != null) no_eval = false;

    if (show_version) showVersion();

    if (show_help) usage(0);

    if (random_seed != -1) org.mymedialite.util.Random.initInstance(random_seed);

    // Set up recommender
    if (load_model_file != null)
      try {
        recommender = Model.load(load_model_file);
      } catch (IOException e) {
        System.err.println("Unable to load model file: " + load_model_file);
        System.exit(0);
      }
    else if (method != null)
      recommender = Recommender.createItemRecommender(method);
    else
      recommender = Recommender.createItemRecommender("MostPopular");

    // In case something went wrong ...
    if (recommender == null && method != null) 
      usage("Unknown recommendation method: " + method);
    if (recommender == null && load_model_file != null) 
      usage("Could not load model from file " + load_model_file);

    checkParameters();

    try {
      recommender = Recommender.configure(recommender, recommender_options, new ErrorHandler());
    } catch (IllegalAccessException e) {
      System.err.println("Unable to instantiate recommender: " + recommender.toString());
      System.exit(0);
    }

    // Load all the data
    try {
      loadData();
    } catch (Exception e) {
      System.out.println("Unable to load data: " + e.getMessage());
    }
    Utils.displayDataStats(training_data, test_data, user_attributes, item_attributes);

    // Display stats if forced to exit
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {   
        displayStats();
      }
    });

    // Evaluation
    try {
      if (find_iter != 0) {
        if ( !(recommender instanceof IIterativeModel) )
          usage("Only iterative recommenders (interface IIterativeModel) support --find-iter=N.");

        IIterativeModel iterative_recommender = (IIterativeModel) recommender;
        System.out.println(recommender);

        if (cross_validation > 1) {
          ItemsCrossValidation.doIterativeCrossValidation(recommender, cross_validation, test_users, candidate_items, eval_item_mode, repeat_eval, max_iter, find_iter);
        } else {
          if (load_model_file == null)
            recommender.train();
          
          if (compute_fit)
            System.out.println("fit: " + computeFit() + " iteration " + iterative_recommender.getNumIter());

          ItemRecommendationEvaluationResults results = evaluate();
          System.out.println(results + " iteration " + iterative_recommender.getNumIter());

          for (int it = iterative_recommender.getNumIter() + 1; it <= max_iter; it++) {
            long start = Calendar.getInstance().getTimeInMillis();
            iterative_recommender.iterate();
            training_time_stats.add((double)(Calendar.getInstance().getTimeInMillis() - start) / 1000);

            if (it % find_iter == 0) {
              if (compute_fit) {
                start = Calendar.getInstance().getTimeInMillis();
                try {
                  System.out.println("fit: " + computeFit() + " iteration " + it);
                } catch (Exception e) {
                  System.out.println("Exception at line 387: " + e.getMessage());
                }
                fit_time_stats.add((double)(Calendar.getInstance().getTimeInMillis() - start) / 1000);
              }

              start = Calendar.getInstance().getTimeInMillis(); 
              results = evaluate();
              eval_time_stats.add((double)(Calendar.getInstance().getTimeInMillis() - start) / 1000);
              System.out.println(results + " iteration " + it);

              try {
                Model.save(recommender, save_model_file, it);
              } catch (IOException e) {
                System.err.println("Unable to save model file: " + save_model_file);
              }
              predict(prediction_file, test_users_file, it);

              if (results.get("AUC") < auc_cutoff || results.get("prec@5") < prec5_cutoff) {
                System.err.println("Reached cutoff after " + it + " iterations.");
                System.err.println("DONE");
                break;
              }
            }
          } // for
        }
      } else {
        System.out.println(recommender + " ");

        if (load_model_file == null) {
          if (cross_validation > 1) {
            ItemRecommendationEvaluationResults results = ItemsCrossValidation.doCrossValidation(recommender, cross_validation, test_users, candidate_items, eval_item_mode, show_fold_results);
            System.out.println(results);
            no_eval = true;
          } else {
            long start = Calendar.getInstance().getTimeInMillis(); 
            recommender.train();
            System.out.println("training_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
          }
        }

        if (compute_fit)
          System.out.println("fit: " + computeFit());

        if (prediction_file != null) {
          predict(prediction_file, test_users_file);
        } else if (!no_eval) {
          if (online_eval) {
            HashMap<String, Double> results = ItemsOnline.evaluate(recommender, test_data, training_data, test_users, candidate_items, eval_item_mode);
            System.out.println(results);
          } else if (group_method != null) {
            GroupRecommender group_recommender = null;

            System.out.println("group recommendation strategy: " + group_method);
            // TODO GroupUtils.CreateGroupRecommender(group_method, recommender);
            if (group_method == "Average")
              group_recommender = new Average(recommender);
            else if (group_method == "Minimum")
              group_recommender = new Minimum(recommender);
            else if (group_method == "Maximum")
              group_recommender = new Maximum(recommender);
            else
              usage("Unknown method : --group-recommender=METHOD");

            ItemRecommendationEvaluationResults result = Groups.evaluate(group_recommender, test_data, training_data, group_to_user, candidate_items, true);
            System.out.println(result);
          } else {
            long start = Calendar.getInstance().getTimeInMillis(); 
            System.out.println(evaluate());
            System.out.println("testing_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
          }
        }
        System.out.println();
      }
      try {
        Model.save(recommender, save_model_file);
      } catch (IOException e) {
        System.err.println("Unable to save model file: " + save_model_file);
      }
    } catch(Exception e) {
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }

  }

  static void checkParameters() {
    // TODO block group vs. filter/online, etc.

    if (training_file == null)
      usage("Parameter --training-file=FILE is missing.");

    if (online_eval && filtered_eval)
      usage("Combination of --online-eval and --filtered-eval is not (yet) supported.");

    if (online_eval && !(recommender instanceof IIncrementalItemRecommender))
      usage("Recommender" + recommender.getClass().getName() + " does not support incremental updates, which are necessary for an online experiment.");

    if (cross_validation == 1)
      usage("--cross-validation=K requires K to be at least 2.");

    if (show_fold_results && cross_validation == 0)
      usage("--show-fold-results only works with --cross-validation=K.");

    if (cross_validation > 1 && test_ratio != 0)
      usage("--cross-validation=K and --test-ratio=NUM are mutually exclusive.");

    if (cross_validation > 1 && prediction_file != null)
      usage("--cross-validation=K and --prediction-file=FILE are mutually exclusive.");

    if (test_file == null && test_ratio == 0 &&  cross_validation == 0 && save_model_file == null && test_users_file == null)
      usage("Please provide either test-file=FILE, --test-ratio=NUM, --cross-validation=K, --save-model=FILE, or --test-users=FILE.");

    if ((candidate_items_file != null ? 1 : 0) + (all_items ? 1 : 0) + (in_training_items ? 1 : 0) + (in_test_items ? 1 : 0) + (overlap_items ? 1 : 0) > 1)
      usage("--candidate-items=FILE, --all-items, --in-training-items, --in-test-items, and --overlap-items are mutually exclusive.");

    if (test_file == null && test_ratio == 0 && cross_validation == 0 && overlap_items)
      usage("--overlap-items only makes sense with either --test-file=FILE, --test-ratio=NUM, or cross-validation=K.");

    if (test_file == null && test_ratio == 0 && cross_validation == 0 && in_test_items)
      usage("--in-test-items only makes sense with either --test-file=FILE, --test-ratio=NUM, or cross-validation=K.");

    if (test_file == null && test_ratio == 0 && cross_validation == 0 && in_training_items)
      usage("--in-training-items only makes sense with either --test-file=FILE, --test-ratio=NUM, or cross-validation=K.");

    if (group_method != null && user_groups_file == null)
      usage("--group-recommender needs --user-groups=FILE.");

    if (user_prediction) {
      if (recommender instanceof IUserAttributeAwareRecommender || recommender instanceof IItemAttributeAwareRecommender ||
          recommender instanceof IUserRelationAwareRecommender  || recommender instanceof IItemRelationAwareRecommender)
        usage("--user-prediction is not (yet) supported : combination with attribute- or relation-aware recommenders.");
      if (filtered_eval)
        usage("--user-prediction is not (yet) supported : combination with --filtered-evaluation.");
      if (user_groups_file != null)
        usage("--user-prediction is not (yet) supported : combination with --user-groups=FILE.");
    }

    if (recommender instanceof IUserAttributeAwareRecommender && user_attributes_file == null)
      usage("Recommender expects --user-attributes=FILE.");

    if (recommender instanceof IItemAttributeAwareRecommender && item_attributes_file == null)
      usage("Recommender expects --item-attributes=FILE.");

    if (filtered_eval && item_attributes_file == null)
      usage("--filtered-evaluation expects --item-attributes=FILE.");

    if (recommender instanceof IUserRelationAwareRecommender && user_relations_file == null)
      usage("Recommender expects --user-relations=FILE.");

    if (recommender instanceof IItemRelationAwareRecommender && user_relations_file == null)
      usage("Recommender expects --item-relations=FILE.");
  }

  static void loadData() throws Exception {
    long start = Calendar.getInstance().getTimeInMillis(); 
    // training data
    training_file = Utils.combine(data_dir, training_file);
    training_data = Double.isNaN(rating_threshold)
        ? ItemData.read(training_file, user_mapping, item_mapping, file_format == ItemDataFileFormat.IGNORE_FIRST_LINE)
            : ItemDataRatingThreshold.read(training_file, rating_threshold, user_mapping, item_mapping, file_format == ItemDataFileFormat.IGNORE_FIRST_LINE);

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
          ((IUserRelationAwareRecommender)recommender).setUserRelation(RelationData.read(Utils.combine(data_dir, user_relations_file), user_mapping));
          System.out.println("relation over " + ((IUserRelationAwareRecommender)recommender).numUsers() + " users");
        }

        // Item relation
        if (recommender instanceof IItemRelationAwareRecommender) {
          ((IItemRelationAwareRecommender)recommender).setItemRelation(RelationData.read(Utils.combine(data_dir, item_relations_file), item_mapping));
          System.out.println("relation over " + ((IItemRelationAwareRecommender)recommender).numItems() + " items");
        }

        // User groups
        if (user_groups_file != null) {
          group_to_user = RelationData.read(Utils.combine(data_dir, user_groups_file), user_mapping); // assumption: user and user group IDs are disjoint
          user_groups = group_to_user.nonEmptyRowIDs();
          System.out.println(user_groups.size() + " user groups");
        }

        // Test data
        if (test_ratio == 0) {
          if (test_file != null) {
            test_file = Utils.combine(data_dir, test_file);
            test_data = Double.isNaN(rating_threshold)
                ? ItemData.read(test_file, user_mapping, item_mapping, file_format == ItemDataFileFormat.IGNORE_FIRST_LINE)
                    : ItemDataRatingThreshold.read(test_file, rating_threshold, user_mapping, item_mapping, file_format == ItemDataFileFormat.IGNORE_FIRST_LINE);
          }
        } else {
          // Ensure reproducible splitting
          if (random_seed != -1)
            org.mymedialite.util.Random.initInstance(random_seed);

          PosOnlyFeedback<SparseBooleanMatrix> train = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
          PosOnlyFeedback<SparseBooleanMatrix> test = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
          PosOnlyFeedbackSimpleSplit<PosOnlyFeedback<SparseBooleanMatrix>> split = new PosOnlyFeedbackSimpleSplit<PosOnlyFeedback<SparseBooleanMatrix>>(training_data, test_ratio, train, test);
          training_data = split.train().get(0);
          test_data = split.test().get(0);
        }

        if (group_method == "GroupsAsUsers") {
          System.out.println("group recommendation strategy: " + group_method);
          // TODO verify what is going on here

          //var training_data_group = new PosOnlyFeedback<SparseBooleanMatrix>();
          // Transform groups to users
          for (int group_id : group_to_user.nonEmptyRowIDs())
            for (int user_id : group_to_user.get(group_id))
              for (int item_id : training_data.userMatrix().getEntriesByRow(user_id))
                training_data.add(group_id, item_id);
          // Add the users that do not belong to groups

          //training_data = training_data_group;

          // Transform groups to users
          PosOnlyFeedback<SparseBooleanMatrix> test_data_group = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
          for (int group_id : group_to_user.nonEmptyRowIDs())
            for (int user_id : group_to_user.get(group_id))
              for (int item_id : test_data.userMatrix().getEntriesByRow(user_id))
                test_data_group.add(group_id, item_id);

          test_data = test_data_group;

          group_method = null; // deactivate s.t. the normal eval routines are used
        }

        if (user_prediction) {
          // swap file names for test users and candidate items
          String ruf = test_users_file;
          String rif = candidate_items_file;
          test_users_file = rif;
          candidate_items_file = ruf;

          // Swap user and item mappings
          IEntityMapping um = user_mapping;
          IEntityMapping im = item_mapping;
          user_mapping = im;
          item_mapping = um;

          // Transpose training and test data
          training_data = training_data.transpose();

          // Transpose test data
          if (test_data != null) test_data = test_data.transpose();
        }

        if (recommender instanceof org.mymedialite.itemrec.ItemRecommender) 
          ((ItemRecommender)recommender).setFeedback(training_data);

        // Test users
        if (test_users_file != null)
          test_users = new ArrayList<Integer>(user_mapping.toInternalID(NumberFile.readStrings(Utils.combine(data_dir, test_users_file))));
        else
          test_users = test_data != null ? test_data.allUsers() : training_data.allUsers();

        // If necessary, perform user sampling
        if (num_test_users > 0 && num_test_users < test_users.size()) {
          // Ensure reproducible splitting
          if (random_seed != -1)
            org.mymedialite.util.Random.initInstance(random_seed);

          IntArraySet old_test_users = new IntArraySet(test_users);
          List<Integer> new_test_users = new ArrayList<Integer>(num_test_users);
          for (int i = 0; i < num_test_users; i++) {
            int random_index = org.mymedialite.util.Random.getInstance().nextInt(old_test_users.size() - 1);
            new_test_users.add(i, old_test_users.toIntArray()[random_index]);
            old_test_users.remove(new_test_users.get(i));
          }
          test_users = new ArrayList<Integer>(new_test_users);
        }

        // Candidate items
        if (candidate_items_file != null) {
          candidate_items = new ArrayList<Integer>(item_mapping.toInternalID(NumberFile.readStrings(Utils.combine(data_dir, candidate_items_file))));
        } else if (all_items) {
          candidate_items = new ArrayList<Integer>();
          for(int id : item_mapping.internalIDs()) candidate_items.add(id);
        }
        if (candidate_items != null)
          eval_item_mode = CandidateItems.EXPLICIT;
        else if (in_training_items)
          eval_item_mode = CandidateItems.TRAINING;
        else if (in_test_items)
          eval_item_mode = CandidateItems.TEST;
        else if (overlap_items)
          eval_item_mode = CandidateItems.OVERLAP;
        else
          eval_item_mode = CandidateItems.UNION;

        System.out.println("loading_time " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
        System.out.println("memory " + Memory.getUsage() + " MB");
  }

  static ItemRecommendationEvaluationResults computeFit() throws Exception {
    if (filtered_eval)
      return ItemsFiltered.evaluateFiltered(recommender, training_data, training_data, item_attributes, test_users, candidate_items, true);
    else
      return Items.evaluate(recommender, training_data, training_data, test_users, candidate_items, eval_item_mode, true);
  }

  static ItemRecommendationEvaluationResults evaluate() throws Exception {
    if (filtered_eval)
      return ItemsFiltered.evaluateFiltered(recommender, test_data, training_data, item_attributes, test_users, candidate_items, repeat_eval);
    else 
      return Items.evaluate(recommender, test_data, training_data, test_users, candidate_items, eval_item_mode, repeat_eval);
  }

  static void predict(String prediction_file, String predict_for_users_file, int iteration) throws IOException {
    if (prediction_file == null) return;
    predict(prediction_file + "-it-" + iteration, predict_for_users_file);
  }

  static void predict(String prediction_file, String predict_for_users_file) throws IOException {
    if (candidate_items == null)
      candidate_items = training_data.allItems();

    List<Integer> user_list = null;
    if (predict_for_users_file != null)
      user_list = user_mapping.toInternalID(NumberFile.readStrings(predict_for_users_file));

    long start = Calendar.getInstance().getTimeInMillis();
    Extensions.writePredictions(
        recommender,
        training_data,
        candidate_items, predict_items_number,
        prediction_file, user_list,
        user_mapping, item_mapping);
    System.err.println("Wrote predictions to " + prediction_file);

    System.out.println(" prediction_time " + (Calendar.getInstance().getTimeInMillis() - start));
  }

  static void displayStats() {
    if (training_time_stats.size() > 0) {
      double max = Collections.max(training_time_stats);
      double min = Collections.min(training_time_stats);
      double avg = Utils.average(training_time_stats);
      System.err.println("iteration_time: min=" + min + ", max=" + max + ", avg=" + avg);
    }

    if (eval_time_stats.size() > 0) {
      double max = Collections.max(eval_time_stats);
      double min = Collections.min(eval_time_stats);
      double avg = Utils.average(eval_time_stats);
      System.err.println("eval_time: min=" + min + ", max=" + max + ", avg=" + avg);
    }

    if (compute_fit && fit_time_stats.size() > 0) {
      double max = Collections.max(fit_time_stats);
      double min = Collections.min(fit_time_stats);
      double avg = Utils.average(fit_time_stats);
      System.err.println("fit_time: min=" + min + ", max=" + max + ", avg=" + avg);
    }

    System.out.println("Memory: " + Memory.getUsage());
  }

}
