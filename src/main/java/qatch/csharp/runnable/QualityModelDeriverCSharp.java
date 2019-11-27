package qatch.csharp.runnable;

import qatch.analysis.ITool;
import qatch.analysis.IToolLOC;
import qatch.csharp.*;
import qatch.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class QualityModelDeriverCSharp {

    // Fields
    private static final File RESOURCES = new File("src/main/resources");
    private static final File TOOLS = new File(RESOURCES, "tools");

    /**
     * Main method for deriving a C# quality model.
     * Elicites weights using an AHP matrix and determines thresholds using a benchmark repository.
     * This method's main purpose is to assist configuration and then just call the
     * msusel-qatch QualityModelDriver.deriveModel() method.
     *
     * @param args configuration array in following order:
     *             0: Path to quality model description file
     *             1: Path to benchmark repository directory
     *             2: Path to comparison matrices directory
     *             3: Temporary path to file to fill with tool scan analysis results. This should be a filename
     *                path such as a/b/benchmark_data.csv.  Does not need to exist.
     *             4: Temporary path to desired directory to hold the output of running the R Thresholds script.
     *             5: Temporary path to directory to store the weights.json file generated by R analysis.
     *             6: String flag, usually a file extension, that signals that a project to be analyzed is
     *                within the directory the flag was found in. For example, ".sln".
     *             7: Path to directory to place the derived quality model.
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) {

        // Setup
        Properties properties = new Properties();
        // TODO (maybe): Find source control friendly way to deal with MSBuild location property.
        try { properties.load((new FileInputStream("src/test/resources/config/config.properties"))); }
        catch (IOException e) { e.printStackTrace(); }

        // Initialize inputs
        Map<String, String> inputArgs = initialize(args);
        QualityModel qmDescription = new QualityModel(Paths.get(inputArgs.get("qmPath")));
        IToolLOC loc = new LocTool("RoslynatorLOC", TOOLS.toPath(), Paths.get(properties.getProperty("MSBUILD_BIN")));
        ITool roslynator = new Roslynator("Roslynator", TOOLS.toPath(), Paths.get(properties.getProperty("MSBUILD_BIN")));
        HashMap<String, ITool> tools = new HashMap<String, ITool>() {{ put(roslynator.getName(), roslynator); }};
        Path benchmarkRepository = Paths.get(inputArgs.get("benchmarkRepository"));
        Path comparisonMatricesDirectory = Paths.get(inputArgs.get("comparisonMatrices"));
        Path benchmarkData = Paths.get(inputArgs.get("benchmarkData"));
        Path rThresholdsOutput = Paths.get(inputArgs.get("thresholdOut"));
        Path tempWeightsDirectory = Paths.get(inputArgs.get("weightsOut"));
        String projectRootFlag = inputArgs.get("flag");
        Path outputDirectory = Paths.get(inputArgs.get("output"));

        // Run derivation process
        QualityModel derivedQM = qatch.runnable.QualityModelDeriver.deriveModel(
                qmDescription, loc, tools, benchmarkRepository, comparisonMatricesDirectory,
                benchmarkData, rThresholdsOutput, tempWeightsDirectory, projectRootFlag
        );

        // Output to file
        Path jsonOutput = derivedQM.exportToJson(outputDirectory);


        System.out.println("Quality Model derivation finished. You can find the file at " + jsonOutput.toAbsolutePath().toString());


//         constants
//        final boolean RECALIBRATE;
//        final boolean RERUN_TOOLS;
//        final Path BENCH_REPO_PATH;
//        final Path ROOT = Paths.get(System.getProperty("user.dir"));
//        final Path OUTPUT; // = Paths.get(ROOT.toString(), "out");
//        final Path RESOURCES = Paths.get(ROOT.toString(), "src/main/resources");
//        final Path TOOLS = Paths.get(RESOURCES.toString(), "tools");
//        final Path QM_DESCRIPTION_PATH = Paths.get(RESOURCES.toString(), "todo");
//        final String PROJ_ROOT_FLAG = ".csproj";    // identifies individual C# project (module) roots in the repo (at any depth)
//
//
//        System.out.println("\n\n******************************  Model Deriver *******************************");
//
//        // Initialize
//        if (args == null || args.length < 3) {
//            throw new RuntimeException("Incorrect input parameters given. Be sure to include " +
//                "\n\t(0) [true | false] flag for benchmark calibration," +
//                "\n\t(1) [true | false] flag for rerunning of tool analysis," +
//                "\n\t(2) location of benchmark repository," +
//                "\n\t(3) path of directory to place results.");
//        }
//        HashMap<String, String> config = initialize(args);
//
//        RECALIBRATE = Boolean.parseBoolean(config.get("recalibrate"));
//        RERUN_TOOLS = Boolean.parseBoolean(config.get("rerunTools"));
//        BENCH_REPO_PATH = Paths.get(config.get("benchRepoPath"));
//        OUTPUT = Paths.get(config.get("resultsPath"));
//        OUTPUT.toFile().mkdirs();
//
//        System.out.println("* Benchmark Repository: " + BENCH_REPO_PATH.toString());
//        System.out.println("* Output Directory: " + OUTPUT.toString());
//        System.out.println("*");
//        System.out.println("* Starting Analysis...");
//        System.out.println("* Loading Quality Model...");

        // Set the properties and the characteristics
        // TODO: refactor
//        QualityModel qualityModel = new PropertiesAndCharacteristicsLoader(QM_DESCRIPTION_PATH.toAbsolutePath().toString()).importQualityModel();
//        PropertySet properties = qualityModel.getProperties();
//        CharacteristicSet characteristics = qualityModel.getCharacteristics();
//        Tqi tqi = qualityModel.getTqi();
//
//        System.out.println("* Empty Quality Model successfully loaded...!");
//        System.out.println("******************************");
//
//        BenchmarkProjects projects = null;
//        BenchmarkAnalysisExporter exporter = null;
//        BenchmarkAnalyzer benchAnalyzer = new BenchmarkAnalyzer(properties, BENCH_REPO_PATH, OUTPUT);
//
//        // Check if the user wants to execute a benchmark calibration
//        if (RECALIBRATE) {
//
//            // pre-run file system checks
//            File r_dir = new File(RInvoker.R_WORK_DIR.toFile(), "Comparison_Matrices");
//            if (!r_dir.isDirectory() || !Objects.requireNonNull(r_dir.listFiles())[0].isFile()) {
//                throw new IllegalStateException("There must exist the hand-entered .xls comparison matrices in directory " +
//                        r_dir.toString() + ".\nSee ComparisonMatricesCreator class for more information.");
//            }
//
//            /*
//             * Step 1 : Analyze the projects found in the desired Benchmark Repository
//             */
//            if (RERUN_TOOLS) {
//                System.out.println("\n**************** STEP 1 : Benchmark Analyzer *************************");
//                System.out.println("*");
//                System.out.println("* Analyzing the projects of the desired benchmark repository");
//                System.out.println("* Please wait...");
//                System.out.println("*");
//
//                // actualize language-specific analyzers
//                IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer(TOOLS);
//                IAnalyzer findingsAnalyzer = new FxcopAnalyzer(TOOLS);
//
//                //Start the analysis of the benchmark repository
//                benchAnalyzer.analyzeBenchmarkRepo(metricsAnalyzer, findingsAnalyzer, PROJ_ROOT_FLAG);
//
//                System.out.println("* You can find the results at : " + benchAnalyzer.getBENCH_RESULTS_PATH().toString());
//                System.out.println("******************************");
//                System.out.println();
//            }
//            else {
//                System.out.println("\n**************** STEP 1 : Benchmark Analyzer *************************");
//                System.out.println("*");
//                System.out.println("* Skipping tool static analysis. Assuming results already exist.");
//                System.out.println("******************************");
//            }
//
//            /*
//             * Step 2 : Import the results of the analysis and store them into different objects
//             */
//            System.out.println("\n**************** STEP 2 : Benchmark Importer *************************");
//            System.out.println("*");
//            System.out.println("* Importing the results of the analysis...");
//            System.out.println("* Please wait...");
//            System.out.println("*");
//
//            // Create an empty BenchmarkImporter
//            BenchmarkResultImporter benchmarkImporter = new BenchmarkResultImporter();
//
//            // Start importing the project results
//            projects = benchmarkImporter.importResults(benchAnalyzer, new LOCMetricsResultsImporter(), new FxcopResultsImporter());
//
//            // Print some informative messages to the console
//            System.out.println("*");
//            System.out.println("* The results are successfully imported..! ");
//            System.out.println("******************************");
//
//
//            /*
//             * Step 3 : Aggregate the results of each project
//             */
//            System.out.println("\n**************** STEP 3: Aggregation Process *************************");
//            System.out.println("*");
//            System.out.println("* Aggregating the results of each project...");
//            System.out.println("* I.e. Calculating the normalized values of their properties...");
//            System.out.println("* Please wait...");
//            System.out.println("*");
//
//            // Create an empty BenchmarkAggregator and aggregate the metrics of the project
//            BenchmarkAggregator benchAggregator = new BenchmarkAggregator();
//            try {
//                benchAggregator.aggregateProjects(projects, properties, new LOCMetricsAggregator(), new FxcopAggregator());
//            }
//            catch (CloneNotSupportedException e) { e.printStackTrace(); }
//
//            System.out.println("*");
//            System.out.println("* Aggregation process finished..!");
//            System.out.println("******************************");
//
//            /*
//             * Step 4 : Export the benchmark analysis results for the R - Analysis
//             */
//            System.out.println("\n**************** STEP 4: Properties exportation for R analysis *******");
//            System.out.println("*");
//
//            // Create an analysis exporter and export the Properties in a xls form
//            exporter = new BenchmarkAnalysisExporter();
//            exporter.exportToCsv(projects);
//
//            System.out.println("*");
//            System.out.println("* The xls file with the properties is successfully exported \n and placed into R's working directory!");
//
//            /*
//             * Step 5 : Invoke R analysis for the threshold calculation
//             */
//            System.out.println("\n**************** STEP 5: Threshold extraction ************************");
//            System.out.println("*");
//            System.out.println("* Calling R for threshold extraction...");
//            System.out.println("* This will take a while...");
//            System.out.println("*");
//
//            // TODO: find way to have all non-language specific procedure occur in qatch framework module
//            // get r threshold script from framework
//            File rWorkingDir = new File(ROOT.toFile(), "r_working_directory");
//            rWorkingDir.mkdirs();
//            File tempThreshScript = FileUtility.tempFileCopyFromJar(RInvoker.getRScriptResource(RInvoker.Script.THRESHOLD), rWorkingDir.toPath());
//
//            // Create an Empty R Invoker and execute the threshold extraction script
//            RInvoker rInvoker = new RInvoker();
//            rInvoker.executeRScript(RInvoker.R_BIN_PATH, tempThreshScript.toPath(), rWorkingDir.toString());
//
//            System.out.println("*");
//            System.out.println("* R analysis finished..!");
//            System.out.println("* Thresholds exported in a JSON file..!");
//
//            /*
//             * Step 6 : Import the thresholds and assign them to each property of the Quality Model
//             */
//            System.out.println("\n**************** STEP 6: Importing the thresholds ********************");
//            System.out.println("*");
//            System.out.println("* Importing the thresholds from the JSON file into JVM...");
//            System.out.println("* This will take a while...");
//            System.out.println("*");
//
//            // Create an empty Threshold importer and import the thresholds from the json file exported by R
//            // The file is placed into the R working directory (fixed)
//            ThresholdImporter thresholdImp = new ThresholdImporter();
//            thresholdImp.importThresholdsFromJSON(properties);
//
//            System.out.println("* Thresholds successfully imported..!");
//
//            /*
//             * Step 7 : Copy the thresholds to each projects' properties
//             */
//            System.out.println("\n**************** STEP 7: Loading thresholds to Projects  *************");
//            System.out.println("*");
//            System.out.println("* Seting the thresholds to the properties of the existing projects...");
//            System.out.println("* This will take a while...");
//            System.out.println("*");
//
//            // TODO: move this functionality to Qatch framework
//            // TODO: use functional approach, use dictionary mapping instead of by index
//            for(int i = 0; i < projects.size(); i++){
//                PropertySet prop = projects.getProject(i).getProperties();
//                for(int j = 0; j < prop.size(); j++){
//                    prop.get(j).setThresholds(properties.get(j).getThresholds().clone());
//                }
//            }
//
//            System.out.println("*");
//            System.out.println("* Thresholds successfully loaded to the projects' properties..!");
//
//            System.out.println("\n*********************************** Weight Elicitation *************************************");
//            System.out.println("*");
//            System.out.println("*");
//
//            // Call R script for weight elicitation
//            // TODO: find way to have all non-language specific procedure occur in qatch framework module
//            // get r threshold script from framework
//            File tempWeightsScript = FileUtility.tempFileCopyFromJar(RInvoker.getRScriptResource(RInvoker.Script.AHP), rWorkingDir.toPath());
//
//            // execute the weight elicitation script
//            rInvoker.executeRScript(RInvoker.R_BIN_PATH, tempWeightsScript.toPath(), rWorkingDir.toString());
//
//            // Import the weights from the json file
//            WeightsImporter weightImporter = new WeightsImporter();
//            weightImporter.importWeights(tqi, characteristics);
//
//        }
//
//        /*
//         * STEP 8 : Export the properties in an XML (or JSON) File
//         */
//        System.out.println("\n**************** STEP 8: Exporting Results ***********************");
//        System.out.println("*");
//        System.out.println("* Exporting the results to XML, JSON files and JDOM Element object...");
//        System.out.println("* This will take a while...");
//        System.out.println("*");
//
//        QualityModelExporter qmExp = new QualityModelExporter();
//        qmExp.exportQualityModelToXML(qualityModel, Paths.get(OUTPUT.toString(), "qm_output"));
    }

    /**
     * Initialize results directory and handle input parameters.
     * This helper method avoids ordering problems of input params by assigning
     * the values to a key-value hashmap
     *
     * @param inputArgs project and results location as described in main method
     * @return HashMap of config with following key strings:
     *      [qmPath, benchmarkRepository, comparisonMatrices, benchmarkData, thresholdOut, weightsOut, flag, output]
     */
    private static HashMap<String, String> initialize(String[] inputArgs) {

        HashMap<String, String> config = new HashMap<>();

        if (inputArgs.length != 8) {
            throw new RuntimeException("inputArgs should be of length 8");
        }

        if (inputArgs[0].endsWith(".json")) {
            config.put("qmPath", inputArgs[0]);
        }
        else throw new RuntimeException("inputArgs[0] (quality model path) does not end with '.json'");

        if (Paths.get(inputArgs[1]).toFile().isDirectory()) {
            config.put("benchmarkRepository", inputArgs[1]);
        }
        else throw new RuntimeException("inputArgs[1] (benchmark repository output) is not a path to a directory");

        if (Paths.get(inputArgs[2]).toFile().isDirectory()) {
            config.put("comparisonMatrices", inputArgs[2]);
        }
        else throw new RuntimeException("inputArgs[2] (comparison matrices output) is not a path to a directory");

        if (inputArgs[3].endsWith(".csv")) {
            config.put("benchmarkData", inputArgs[3]);
        }
        else throw new RuntimeException("inputArgs[3] (benchmark data output) does not end with '.csv'");

        Paths.get(inputArgs[4]).toFile().mkdirs();
        if (Paths.get(inputArgs[4]).toFile().isDirectory()) {
            config.put("thresholdOut", inputArgs[4]);
        }
        else throw new RuntimeException("inputArgs[4] (threshold output) is not a path to a directory");

        Paths.get(inputArgs[5]).toFile().mkdirs();
        if (Paths.get(inputArgs[5]).toFile().isDirectory()) {
            config.put("weightsOut", inputArgs[5]);
        }
        else throw new RuntimeException("inputArgs[5] (weights output) is not a path to a directory");

        config.put("flag", inputArgs[6]);

        Paths.get(inputArgs[7]).toFile().mkdirs();
        if (Paths.get(inputArgs[7]).toFile().isDirectory()) {
            config.put("output", inputArgs[7]);
        }
        else throw new RuntimeException("inputArgs[7] (output directory) is not a path to a directory");

        return config;
    }

}
