

import static com.sun.jna.Platform.isWindows;
import static com.thoaikx.grid.SeleniumGrid.PodmanManager.waitForServerAvailability;

import com.video.ExtractLibFromJar;
import io.restassured.RestAssured;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public  class SeleniumGrid {

  static {
    try {
      ExtractLibFromJar.copyToM2Repo();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public enum BrowerControlType  {
    LOCAL,
    REMOTE
  }

  private static final Logger logger = Logger.getLogger(SeleniumGrid.class.getName());

  public  static  int PORT = 4444;;
  public  static  String GRID_URL = "http://localhost:4444/wd/hub";

  private static String USER_PROFILE = System.getProperty("user.home");

  private  static  String CURRENT_DIR = System.getProperty("user.dir");

  private static Path SOURCE_SELENIUM_SERVER = Path.of(USER_PROFILE, ".m2", "repository",
      "selenium-utils", "selenium-server-4.29.0.jar");

  public static String PODMAN_PATH = Path.of(USER_PROFILE, ".m2", "repository", "selenium-utils",
      "bin", "podman.exe").toString();

  public static String PODMAN_COMPOSE_PATH = Path.of(USER_PROFILE, ".m2", "repository", "selenium-utils",
      "bin", "podman-compose.exe").toString();



  public static void main(String[] args) throws Exception {
    // Run in case want reset all . or meet error
    PodmanManager.removeResourcePodMan();
    PodmanManager.forceRemoveVM();
  }


  public static void start(BrowerControlType browerControlType,String nameFileYml) throws Exception {
    if (browerControlType == BrowerControlType.LOCAL){
      startSeleniumServer();
      waitForServerAvailability(4444);
    }
    else {
      if (!isWindows())
      {
        throw new Exception(" Mac ,linux should use docker");
      };

      start(nameFileYml);

    }
  }

  public  static void stop(String nameFileYml) throws Exception {

    Path composeFile = Path.of(CURRENT_DIR, nameFileYml);

    // Stop containers
    System.out.println("\nStopping containers...");

    // Use stop instand down( down will stop and remove)
    PodmanManager.executePodmanCompose(composeFile, "stop", true);

    //PodmanManager.removeResourcePodMan();
  }

  public static void start(String nameFileYml) throws Exception {

    Path composeFile = Path.of(CURRENT_DIR, nameFileYml);

    if (isWindows()) {
      Path videosDirectory = Path.of("./videos");

      if (Files.notExists(videosDirectory)) {
        Files.createDirectory(videosDirectory);
      }

   //  PodmanManager.removeResourcePodMan();

      PodmanManager.checkStatusPodman();

      // Start container
      logger.info("Starting containers...");


      PodmanManager.executePodmanCompose(composeFile, "up -d --no-recreate", true);

      PodmanManager.waitForServerAvailability(4444);
    } else {
      throw new Exception("Mac and Linux should use Docker instead of Podman.");
    }
  }

  // Utils class to start stop podman
  static class PodmanManager {

    private static void checkStatusPodman() throws Exception {
      if (!isPodmanInitialized(PODMAN_PATH)) {
        executeCommand(PODMAN_PATH + " machine init");
      }

      if (!isPodmanRunning(PODMAN_PATH)) {
        executeCommand(PODMAN_PATH + " machine start", true);
      }
    }


    public static String executePowerShellCommand(String command, boolean logOutput) throws Exception {
      if (command == null || command.trim().isEmpty()) {
        throw new IllegalArgumentException("Command cannot be empty");
      }

      // PowerShell execution command
      List<String> powershellCommand = new ArrayList<>();
      powershellCommand.add("powershell.exe");
      powershellCommand.add("-Command");
      powershellCommand.add(command);

      System.out.println("Executing: " + String.join(" ", powershellCommand));

      ProcessBuilder pb = new ProcessBuilder(powershellCommand);
      // Combine stdout and stderr
      pb.redirectErrorStream(true);
      Process process = pb.start();

      // Read output
      StringBuilder output = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (logOutput) {
            System.out.println(line);
          }
          output.append(line).append("\n");
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new Exception("Command failed (exit=" + exitCode + "):\n" + output);
      }

      return output.toString();
    }
    public static void executePodmanCompose(Path composeFile, String action, boolean logOutput) throws Exception {

      String command = String.format(
          "& '%s' -f '%s' %s",
          PODMAN_COMPOSE_PATH,
          composeFile.toString().replace("\\", "\\\\"),
          action
      );

      executePowerShellCommand(command, logOutput);
    }

    private static String executeCommand(String command, boolean isLog) throws Exception {

      String[] commandArray = command.split(" ");

      // Create a ProcessBuilder to run the command
      ProcessBuilder processBuilder = new ProcessBuilder(commandArray);

      // Start the process
      Process process = processBuilder.start();

      // Capture and build the output
      StringBuilder output = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (isLog) {
            System.out.println(line);
          }
          output.append(line).append("\n");
        }
      }

      // Wait for the process to exit and capture the exit code
      int exitCode = process.waitFor();

      return output.toString();
    }

    private static String executeCommand(String cmd) throws Exception {
      return executeCommand(cmd, false);
    }

    private static boolean isPodmanInitialized(String podmanPath) throws Exception {
      String result = executeCommand(podmanPath + " machine list --format json");
      return result != null && !result.isEmpty() && !result.contains("[]");
    }

    private static void forceRemoveVM() throws Exception {
      executeCommand(PODMAN_PATH + " machine rm -f");
    }

    private static boolean isPodmanRunning(String podmanPath) throws Exception {
      String result = executeCommand(podmanPath + " machine list --format json");
      return result != null && result.contains("\"Running\": true");
    }

    public static void removeResourcePodMan() throws Exception {
      executeCommand(PODMAN_PATH + " stop -a -f", true);
      executeCommand(PODMAN_PATH + " rm -a -f", true);
      executeCommand(PODMAN_PATH + " pod rm -a -f", true);
      executeCommand(PODMAN_PATH + " network prune -f", true);
    }

    public static void waitForServerAvailability(int port) {
        String url = "http://localhost:" + port + "/wd/hub/status";
        logger.info("Checking availability: " + url);

        boolean isAvailable = IntStream.range(0, 10).anyMatch(i -> {
            if (RestAssured.get(url).getStatusCode() == 200) {
              return true;
            }
          try {
            TimeUnit.SECONDS.sleep(10);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
          }

          return false;
        });

        logger.info(isAvailable ? "Service is available!" : "Service not available after 10 attempts.");
      }
    }




    public static void startSeleniumServer() throws Exception {

      Path lib = Path.of("lib");
      if (!Files.exists(lib)) {
        Files.createDirectory(lib);
      }

      Path targetPath = lib.resolve("selenium-server-4.29.0.jar");
      Files.copy(SOURCE_SELENIUM_SERVER, targetPath, StandardCopyOption.REPLACE_EXISTING);

      // Command to be run in the same CMD window with docker-compose-v3-video.yml custom title using text block for better readability
      String cmdCommand = """
          title SeleniumServer && java -jar "%s" standalone -p %d
          """.formatted(targetPath.toAbsolutePath(), PORT);

      // Full command to run in the current CMD window (no new CMD)
      String fullCmd = """
          cmd /c "%s"
          """.formatted(cmdCommand);

      // Execute the command
      Runtime.getRuntime().exec(fullCmd);

      logger.info(
          "Selenium Grid started with the title 'SeleniumServer' at: http://localhost:" + PORT
              + "/ui/");

      waitForServerAvailability(4444);
    }

    public static void stopSeleniumServer(int port) throws IOException {

      String findPidCmd = """
            for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%d') do taskkill /PID %%a /F
            """.formatted(port);

      // Full command to run in the current CMD window (no new CMD)
      String fullCmd = """
            cmd /c "%s"
            """.formatted(findPidCmd);

      // Execute the command
      Runtime.getRuntime().exec(fullCmd);

      logger.info("Stopping Selenium Grid running on port " + port + "...");

    }

  }


