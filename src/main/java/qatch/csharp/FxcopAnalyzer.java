package qatch.csharp;

import qatch.analysis.IAnalyzer;
import qatch.model.Property;
import qatch.model.PropertySet;
import qatch.utility.FileUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FxcopAnalyzer implements IAnalyzer {

    static final String TOOL_NAME = "FxCop";

    @Override
    public void analyze(File src, File dest, PropertySet properties) {
        //Create an Iterator in order to iterate through the properties of the desired PropertySet object
        Iterator<Property> iterator = properties.iterator();
        Property p;

        //For each property found in the PropertySet do...
        while(iterator.hasNext()) {

            //Get the current property
            p = iterator.next();

            //Check if it is an FxCop Property
            if (p.getMeasure().getTool().equals(FxcopAnalyzer.TOOL_NAME)) {
                //Analyze the project against this property
                analyzeSubroutine(src, dest, p.getMeasure().getRulesetPath(), p.getName()+".xml");
            }
        }
    }

    /**
     * Analyze a single project against a certain ruleset (property) by calling the FxCop tool
     * through the command line with the appropriate configuration.
     *
     * ATTENTION:
     *  - The appropriate fxcop_build.xml ant file should be placed inside the resources directory.
     *
     * @param src
     *      The path of the folder that contains the sources of the project. The folder must contain
     *      at least one .dll or .exe item.
     * @param dest
     *      The path where the XML files with the results will be placed.
     * @param rulesetPath
     *      The  rules against which the project will be analyzed.
     * @param fileName
     *      The name of the XML file containing scan results.
     */
    private void analyzeSubroutine(File src, File dest, String rulesetPath, String fileName) {
        String sep = File.separator;
        ProcessBuilder pb;
        String destFile = dest + sep + fileName;

        Set<String> projectNames = FileUtility.findFileNamesFromExtension(src.toPath(), ".csproj");
        Set<Path> assemblyDirs = new HashSet<>();
        projectNames.forEach(p -> assemblyDirs.addAll(FileUtility.findAssemblies(src, p, ".exe", ".dll")));
        Set<Path> removeDirs = new HashSet<>();
        if (assemblyDirs.isEmpty()) {
            throw new RuntimeException("[ERROR] No directories containing .exe or .dll file(s) were found in project root "
                    + src + ". Has the project been built?");
        }

        for (Path p : assemblyDirs) {
            for (String directory : p.toString().split("\\\\")) {
                if (directory.trim().equals("obj") || directory.toLowerCase().contains("test")) {
                    removeDirs.add(p);
                }
            }
        }

        assemblyDirs.removeAll(removeDirs);

        StringBuilder sb = new StringBuilder("\"");
        assemblyDirs.forEach(dir -> sb.append("/f:").append(dir.toString()).append(" "));
        sb.append("\"");

        // Attach FxCopExe option flags
        String srcExt = sb.toString();
        String destExt = "/out:" + destFile;
        String rulesetExt = "/r:" + rulesetPath;

        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder(
                "cmd.exe", "/c",
                SingleProjectEvaluation.TOOLS_LOCATION + sep + "FxCop" + sep + "FxCopCmd.exe",
                srcExt,
                destExt,
                rulesetExt,
                "/fo"
            );
        } else {
            throw new RuntimeException("FxCop C# analysis not supported on non-Windows machines. FxCopCmd.exe tool only supported on Windows.");
        }

        pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert p != null;
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Path> findings = new HashSet<>();
        try {
            Files.list(Paths.get(dest.toString()))
                    .filter(f -> f.toString().endsWith(".xml"))
                    .forEach(findings::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        if (findings.isEmpty()) throw new RuntimeException("No findings XML files were generated by FxCop analysis");
    }

}
