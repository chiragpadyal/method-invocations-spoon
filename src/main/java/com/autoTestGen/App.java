package com.autoTestGen;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import spoon.MavenLauncher;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.QueueProcessingManager;
import java.util.Map;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *         String methodName = "org.jfree.chart.AreaChartTest.testSetSeriesToolTipGenerator";
        // String methodName = "org.jfree.chart.JFreeChart.createBufferedImage";
 * Find which test method affects which method in the source code 
 *  By using Spoon, we can find the method call hierarchy of a method in the source code.
 * and so by know what is being called in test methods, we can know which method in the source code is being affected by the test method.
 */
public class App 
{
        public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("m", "method", true, "Method name");
        options.addOption("t", "type", true, "Source type: TEST or APP");
        options.addOption("p", "path", true, "maven project path");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command-line options: " + e.getMessage());
            return;
        }

        String methodName = cmd.getOptionValue("m");
        String sourceTypeStr = cmd.getOptionValue("t");
        String path = cmd.getOptionValue("p");
        
        if (methodName == null || sourceTypeStr == null || path == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("App", options);
            return;
        }
        
        PrintStream printStream = System.out;
        App app = new App();

        // check if folder exists
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Path does not exist");
            return;
        }
        
        if (sourceTypeStr.equals("TEST")) {
            app.callHierarchyTest(methodName, printStream , path);
        } else if (sourceTypeStr.equals("APP")) {
            app.callHierarchySource(methodName, printStream, path);
        } else {
            System.err.println("Invalid source type. Please specify TEST or APP.");
            return;
        }
    }

    private void callHierarchyTest(String methodName, PrintStream printStream , String path) throws Exception {
        callHierarchy(methodName, printStream, MavenLauncher.SOURCE_TYPE.TEST_SOURCE, path);
    }

    private void callHierarchySource(String methodName, PrintStream printStream , String path) throws Exception {
        callHierarchy(methodName, printStream, MavenLauncher.SOURCE_TYPE.APP_SOURCE, path);
    }

    private void callHierarchy(String methodName, PrintStream printStream, MavenLauncher.SOURCE_TYPE sourceType, String path) throws Exception {
        ArrayNode aggregatedHierarchy = new ObjectMapper().createArrayNode();
        MavenLauncher launcher = new MavenLauncher(path, sourceType);
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        QueueProcessingManager queueProcessingManager = new QueueProcessingManager(launcher.getFactory());
        Map<CtTypeReference<?>, Set<CtTypeReference<?>>> classHierarchy = new ClassHierarchyProcessor().executeSpoon(queueProcessingManager);
        Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList = new MethodExecutionProcessor().executeSpoon(queueProcessingManager);
        List<MethodCallHierarchyBuilder> methodCallHierarchyBuilders = MethodCallHierarchyBuilder.forMethodName(methodName, callList, classHierarchy);
        
        if (methodCallHierarchyBuilders.isEmpty()) {
            printStream.println("No method containing `" + methodName + "` found.");
        }
        
        // if (methodCallHierarchyBuilders.size() > 1) {
        //     printStream.println("Found " + methodCallHierarchyBuilders.size() + " matching methods...");
        //     printStream.println();
        // }

        for (MethodCallHierarchyBuilder each : methodCallHierarchyBuilders) {
            ArrayNode hierarchyForBuilder = each.printCallHierarchy(printStream);
            aggregatedHierarchy.addAll(hierarchyForBuilder);
        }




        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("call_hierarchy.json"), aggregatedHierarchy);
            printStream.println(aggregatedHierarchy.toPrettyString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
