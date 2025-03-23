package com.seleniumutils.videos;



import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.checkerframework.checker.units.qual.s;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;
import com.video.ExtractLibFromJar;

import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;


 enum RECORDTYPE {
    MONTE,
    FFMPEG
} 
public class RecorderManager {

    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String DOWNLOADED_FILES_FOLDER = "videos";
    private static ScreenRecorder screenRecorder;
    private static Process process;
    public static String nameVideo = "default_video";  // Ensures nameVideo is never null
    public static String nameVideoAvi = Path.of("videos", nameVideo + ".avi").toString();
    public static String nameVideoMp4 = Path.of("videos", nameVideo + ".mp4").toString();
    private static String os = System.getProperty("os.name").toLowerCase();
    private static Process ffmpegProcess;
    private static Logger log = Logger.getLogger(RecorderManager.class.getName());
    private static Path ffmpegPath;
    private static final Path VIDEO_DIRECTORY = Path.of("videos");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    // Define the ffmpeg command format for macOS and Windows
    private static  String FFMPEG_COMMAND = "-f %s -framerate 30 -i %s -c:v libx264 -preset veryfast -crf 23 -pix_fmt yuv420p -y \"%s\"";

    
        public static void startVideoRecording(RECORDTYPE recordType,String baseVideoName) throws Exception {
            ExtractLibFromJar.copyToM2Repo();
        
            // Generate timestamp and output file path
            String timestamp = getTimestamp();
            Path outputFile = VIDEO_DIRECTORY.resolve(baseVideoName + "_" + timestamp + ".mp4");
        
            // Ensure the video directory exists
            if (!Files.exists(VIDEO_DIRECTORY)) {
                Files.createDirectories(VIDEO_DIRECTORY);
            }
        
            // Select recording type using Java 17 arrow switch
            switch (recordType) {
                case MONTE -> {
                    log.info("Starting MONTE recording ");
                    VideoRecord._startRecording(baseVideoName);
                    // Implement MONTE recording logic here
                }
                case FFMPEG -> {
                    log.info("Starting Ffmpeg recording ");
                    _startVideoRecording(baseVideoName);
                }
                default -> throw new IllegalArgumentException("Unsupported recording type: " + recordType);
            }
        }
        
        public static void stopVideoRecording(RECORDTYPE recordType) throws Exception{

             // Select recording type using Java 17 arrow switch
             switch (recordType) {
                case MONTE -> {
                    log.info("Stop MONTE recording ");
                    VideoRecord._stopRecording(false);
                
                }
                case FFMPEG -> {
                    log.info("Stop Ffmpeg recording ");
                    if (ffmpegProcess == null) return;
        
                        log.info("Stopping FFmpeg recording...");
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    
                        ffmpegProcess.getOutputStream().write('q');
                        ffmpegProcess.getOutputStream().flush();
            }       
                    else {
                        
                        ffmpegProcess.destroy();
            }
        
                ffmpegProcess.waitFor();
                ffmpegProcess = null;

                }
                default -> throw new IllegalArgumentException("Unsupported recording type: " + recordType);
            }
            
        }
    
        public static  Path getFfmpegPath (){
    
            ffmpegPath = os.contains("mac")
                    ? Path.of(System.getProperty("user.home"), ".m2", "repository", "ffmpeg", "ffmpeg")
                    : Path.of(System.getProperty("user.home"), ".m2", "repository", "ffmpeg", "ffmpeg.exe");
    
             return  ffmpegPath;
    
        }
    
        // Get timestamp for filename in format "yyyyMMdd_HHmmss"
        private static String getTimestamp() {
            return dateFormat.format(new Date());
        }

    
        private static void _startVideoRecording(String baseVideoName) throws IOException, InterruptedException {
            ExtractLibFromJar.copyToM2Repo();
        
            // Generate timestamp and create output file path
            String timestamp = getTimestamp();
            Path outputFile = VIDEO_DIRECTORY.resolve(baseVideoName + "_" + timestamp + ".mp4");
        
            // Ensure the video directory exists
            if (!Files.exists(VIDEO_DIRECTORY)) {
                Files.createDirectories(VIDEO_DIRECTORY);
            }
        
            // Set and validate ffmpeg path
            Path ffmpegPath = getFfmpegPath();
            if (!Files.exists(ffmpegPath)) {
                throw new IOException("FFmpeg executable not found at: " + ffmpegPath);
            }
        
            // Determine OS and set the appropriate input source
            boolean isMac = os.contains("mac");
            String inputSource = isMac ? "avfoundation" : "gdigrab";
            String inputDevice = isMac ? "1" : "desktop";
        
            if (isMac) {
                VideoRecord.setExecutablePermission(ffmpegPath);
            }
        
            // Build the FFmpeg command
            String[] command = {
                ffmpegPath.toString(),
                "-f", inputSource,
                "-framerate", "30",
                "-i", inputDevice,
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "23",
                "-pix_fmt", "yuv420p",
                "-y", outputFile.toString()
            };
        
            // Initialize ProcessBuilder
            ProcessBuilder ffmpegBuilder = isMac
                ? new ProcessBuilder(command)
                : new ProcessBuilder("cmd.exe", "/c", String.join(" ", command));
        
            ffmpegBuilder.redirectErrorStream(true);
            Process ffmpegProcess = ffmpegBuilder.start();
        
            // Log FFmpeg output asynchronously
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info(line);
                    }
                } catch (IOException e) {
                    log.severe("Error reading FFmpeg output: " + e.getMessage());
                }
            }).start();
        
            log.info("Video recording started: " + outputFile);
        }
        
    
        private static void _stopVideoRecording() throws IOException, InterruptedException {
            if (ffmpegProcess == null) return;
        
            System.out.println("Stopping FFmpeg recording...");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                ffmpegProcess.getOutputStream().write('q');
                ffmpegProcess.getOutputStream().flush();
            } else {
                ffmpegProcess.destroy();
            }
        
            ffmpegProcess.waitFor();
            ffmpegProcess = null;
        }


   
       
     // Set up record use Monte    
    static class VideoRecord {

            public static void _startRecording(String nameVideo) throws Exception {
                File file = new File(USER_DIR, DOWNLOADED_FILES_FOLDER);
        
                if (!file.exists()) {
                    file.mkdirs();
                }
        
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle captureSize = new Rectangle(0, 0, screenSize.width, screenSize.height);
                GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration();
        
                SpecializedScreenRecorder recorder = new SpecializedScreenRecorder(
                        gc, captureSize,
                        new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                        new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24,
                                FrameRateKey, Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
                        new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
                        null, file, nameVideo);
        
                screenRecorder = recorder;
                nameVideo = recorder.nameVideo;
                nameVideoAvi = Path.of("videos", nameVideo + ".avi").toString();
                nameVideoMp4 = Path.of("videos", nameVideo + ".mp4").toString();
        
                screenRecorder.start();
        
                // Move file  to local
                ExtractLibFromJar.copyToM2Repo();
            }
        
            public static void _stopRecording(boolean hasDeleteAndConvet) throws Exception {
                if (screenRecorder != null) {
                    screenRecorder.stop();
                }
            
                log.info("Convert video success");
                if(hasDeleteAndConvet){
                    convertAviToMp4(nameVideoAvi,nameVideoMp4);
                    deleteFile(nameVideoAvi);
                }
               
            }
        
            public  static void convertAviToMp4(String inputFileName, String outputFileName) throws IOException, InterruptedException {
                 ffmpegPath = os.contains("mac")
                        ? Path.of(System.getProperty("user.home"), ".m2", "repository", "ffmpeg","ffmpeg")
                        : Path.of(System.getProperty("user.home"), ".m2", "repository", "ffmpeg","ffmpeg.exe");
        
                if (!Files.exists(ffmpegPath)) {
                    throw new IOException("FFmpeg executable not found at: " + ffmpegPath);
                }
        
                if (!os.contains("win")) {
                    setExecutablePermission(ffmpegPath);
                }
        
                ProcessBuilder ffmpegBuilder = new ProcessBuilder(
                        ffmpegPath.toString(),
                        "-i", inputFileName,
                        "-c:v", "libx264",
                        "-preset", "fast",
                        "-crf", "23",
                        "-c:a", "aac",
                        "-b:a", "192k",
                        "-y", outputFileName
                );
        
        
                ffmpegBuilder.redirectErrorStream(true);
                 process = ffmpegBuilder.start();
        
                ExecutorService executorService = Executors.newFixedThreadPool(2);
        
                Process finalProcess = process;
                executorService.submit(() -> readStream(finalProcess.getInputStream()));
                executorService.submit(() -> readStream(finalProcess.getErrorStream()));
        
                int ffmpegExitCode = finalProcess.waitFor();
                executorService.shutdown();
        
                if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
        
                if (ffmpegExitCode == 0) {
                    deleteFile(inputFileName);
                } else {
                    log.severe("Conversion failed with exit code: " + ffmpegExitCode);
                }
            }
        
            private static void readStream(InputStream inputStream) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    while (reader.readLine() != null) {
                        // Can log output if needed
                    }
                } catch (IOException e) {
                    log.severe("Error reading stream: " + e.getMessage());
                }
            }
        
            private static void deleteFile(String inputFileName) {
                Path filePath = Path.of(inputFileName);
                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath);
                        log.info("Deleted original AVI file: " + inputFileName);
                    } catch (IOException e) {
                        log.severe("Error deleting AVI file: " + e.getMessage());
                    }
                }
            }
        
            private static void setExecutablePermission(Path filePath) throws IOException, InterruptedException {
                ProcessBuilder chmodBuilder = new ProcessBuilder("chmod", "+x", filePath.toString());
                Process chmodProcess = chmodBuilder.start();
                int chmodExitCode = chmodProcess.waitFor();
        
                if (chmodExitCode != 0) {
                    throw new IOException("Failed to set execute permission for FFmpeg, exit code: " + chmodExitCode);
                }
            }
        
        }  



        static class  SpecializedScreenRecorder extends ScreenRecorder {

            private String name;
            public  String nameVideo ;
            private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        
            public SpecializedScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat, Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
                    throws IOException, AWTException {
                super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
                this.name = name;
                    nameVideo = name + "-" + dateFormat.format(new Date()) ;
        
            }
        
            @Override
            protected File createMovieFile(Format fileFormat) throws IOException {
                if (!movieFolder.exists()) {
                    movieFolder.mkdirs();
                } else if (!movieFolder.isDirectory()) {
                    throw new IOException("\"" + movieFolder + "\" is not a directory.");
                }
        
                return new File(movieFolder, name + "-" + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
            }
        
        
          }


   
        
}

 




