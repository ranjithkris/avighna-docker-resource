package de.fraunhofer.iem;

import de.fraunhofer.iem.exception.DotToImgException;
import de.fraunhofer.iem.exception.DtsSerializeUtilException;
import de.fraunhofer.iem.exception.DtsZipUtilException;
import de.fraunhofer.iem.exception.UnexpectedError;
import de.fraunhofer.iem.hybridCG.HybridCallGraph;
import de.fraunhofer.iem.hybridCG.ImageType;
import soot.*;
import soot.options.Options;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainRunner {
    private static final String JavaReflectionTestRootPath = "/root/avighna/avighna-output/Reflection-Projects/JavaReflectionTestCases";

    private static final String OUTPUT_ROOT_DIR = "/root/avighna/avighna-output/Reflection-Projects/JavaReflectionTestCases";
    private static final String ROOT_BASE_PACKAGE = "de.fraunhofer.iem";
    private static final String AVIGHNA_CMD_JAR = "/root/avighna/avighna-cmd-interface-1.0.0.jar";
    private static final String AVIGHNA_AGENT_JAR = "/root/avighna/avighna-agent-1.0.0.jar";
    private static void runJava(ProcessBuilder processBuilder) {
        processBuilder.redirectErrorStream(true);
        Process p = null;
        try {
            p = processBuilder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initializeSoot(String appClassPath, String dtsFileName) throws DtsZipUtilException, FileNotFoundException {
        //TODO: Remove this after testing
        G.reset();
        Options.v().set_keep_line_number(true);

        Options.v().setPhaseOption("cg.spark", "on");

        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().set_allow_phantom_refs(true);

        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("cg", "safe-forname:false");

        Options.v().set_soot_classpath(appClassPath);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        //Options.v().setPhaseOption("jb.lns", "enabled:false");

        Options.v().set_output_format(Options.output_format_none);

        List<String> appClasses = new ArrayList<>(FilesUtils.getClassesAsList(appClassPath));

        String dynamicCP = new HybridCallGraph().getDynamicClassesPath(dtsFileName);

        if (new File(dynamicCP).exists()) {
            Options.v().set_soot_classpath(appClassPath + File.pathSeparator + dynamicCP);
            appClasses.addAll(new ArrayList<>());
        }

        System.out.println(appClasses);
        List<SootMethod> entries = new ArrayList<SootMethod>();
        for (String appClass : appClasses) {
            System.out.println(appClass);
            SootClass sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
            sootClass.setApplicationClass();
            entries.addAll(sootClass.getMethods());
        }

        Scene.v().setEntryPoints(entries);
        Scene.v().loadNecessaryClasses();
        PackManager.v().getPack("cg").apply();
    }

    public static void main(String[] args) throws IOException, UnexpectedError, DotToImgException, DtsSerializeUtilException, DtsZipUtilException {
        File rootProjectDir = new File(JavaReflectionTestRootPath);
        String[] modules = rootProjectDir.list();

        for (Object obj : Arrays.stream(modules).sorted().toArray()) {
            String module = (String) obj;

            if (module.startsWith("CSR")
                    || module.startsWith("TR")
                    || module.startsWith("CFNE")
                    || module.startsWith("LRR")) {

                ProcessBuilder processBuilder;

                switch (module) {
                    case "CSR2":
                    case "LRR2":
                    case "LRR1":
                        continue;
                    default:
                        System.out.println("Running for " + module);
                        processBuilder = new ProcessBuilder(
                                "java",
                                "-jar",
                                AVIGHNA_CMD_JAR,
                                "-aj",
                                JavaReflectionTestRootPath +
                                        File.separator + module + File.separator + "target" +
                                        File.separator + module + "-1.0-SNAPSHOT-jar-with-dependencies.jar",
                                "-aaj",
                                AVIGHNA_AGENT_JAR,
                                "-od",
                                OUTPUT_ROOT_DIR +
                                        File.separator + module + File.separator + "avighna-agent-output",
                                "-rap",
                                ROOT_BASE_PACKAGE,
                                "-sdf",
                                "-sif",
                                "-sra");

                        runJava(processBuilder);

                        new File(OUTPUT_ROOT_DIR +
                                File.separator + module + File.separator + "avighna-agent-output"  +
                                File.separator + "hybrid-merger-output").mkdirs();

                        String appClassPath = JavaReflectionTestRootPath +
                                File.separator + module + File.separator +
                                "target" + File.separator + "classes";
                        String dtsFileName = JavaReflectionTestRootPath +
                                File.separator + module + File.separator +
                                "avighna-agent-output" + File.separator + "dynamic_cg.dst";
                        String hybridOutputPath = JavaReflectionTestRootPath +
                                File.separator + module + File.separator +
                                "avighna-agent-output" + File.separator +
                                "hybrid-merger-output" + File.separator;

                        initializeSoot(appClassPath, dtsFileName);

                        new HybridCallGraph().merge(
                                dtsFileName,
                                Scene.v().getCallGraph(),
                                hybridOutputPath,
                                "callgraph",
                                "callgraph",
                                ImageType.SVG
                        );
                        break;
                }
            }
        }
    }
}
