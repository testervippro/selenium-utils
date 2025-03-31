package com.seleniumutils.podman;

import com.video.ExtractLibFromJar;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PodmanManager {
  private  static String userProfile = System.getProperty("user.home");
  static String podmanPath = Path.of(userProfile, ".m2", "repository", "selenium-utils", "bin", "podman.exe").toString();

  private static void checkStatusPodman() throws Exception {

    if (!isPodmanInitialized(podmanPath)){
       executeCommand(podmanPath + " machine init");
    };

    if (!isPodmanRunning(podmanPath)){
      executeCommand(podmanPath + " machine start",true);
    }

  }

  public static void main(String[] args) throws Exception {
  }

  private static String executeCommand(String command,boolean hasLog ) throws Exception {
    // Split the command into individual parts
    String[] commandArray = command.split(" ");

    // Create a ProcessBuilder to run the command
    ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

    // Start the process
    Process process = processBuilder.start();

    // Capture and build the output
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (hasLog){
        System.out.println(line);}
        output.append(line).append("\n");
      }
    }

    // Wait for the process to exit and capture the exit code
    int exitCode = process.waitFor();

    return output.toString();
  }

  private  static String executeCommand(String cmd) throws Exception {
    return executeCommand(cmd,false);
  }
  private static boolean isPodmanInitialized(String podmanPath) throws Exception {
    String result = executeCommand(podmanPath + " machine list --format json");
    //System.out.println(result);
    return result != null && !result.isEmpty() && !result.contains("[]");
  }

  private static boolean isPodmanRunning(String podmanPath) throws Exception {
    String result = executeCommand(podmanPath + " machine list --format json");
    //System.out.println(result);
    return result != null && result.contains("\"Running\": true");
  }

  public  static  void stop() throws Exception {
    executeCommand(podmanPath +" stop video selenium" );
    executeCommand(podmanPath +" rm video selenium" );
    executeCommand(podmanPath+ " network rm grid");
    //System.out.println("Done");
  }

  public  static  void  start() throws Exception {
    Path videosDirectory = Path.of("./videos");
    // Check if the directory exists, if not, create it
    if (Files.notExists(videosDirectory)) {
      Files.createDirectory(videosDirectory);
    }

    ExtractLibFromJar.copyToM2Repo();
    checkStatusPodman();
    stop();
    var network = " network create grid";
    var standaloneChrome = " run -d -p 4444:4444 -p 6900:5900 --net grid --name selenium --shm-size=2g selenium/standalone-chrome:4.30.0-20250323";
    var video = " run -d --net grid --name video -v ./videos:/videos selenium/video:ffmpeg-7.1.1.1-20250323";
    //System.out.println("Starting..");;
    executeCommand(podmanPath + network , true);
    executeCommand(podmanPath + standaloneChrome,true);
    executeCommand(podmanPath + video,true);
    // Wait to init driver
    Thread.sleep(5000);
  }
}
