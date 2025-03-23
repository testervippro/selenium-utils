package com.seleniumutils.videos;

public class VideoRecord {

    public static final String USER_DIR = System.getProperty("user.dir");
    private static final String DOWNLOADED_FILES_FOLDER = "videos";
    private static final Logger log = Logger.getLogger(VideoRecord.class.getName());
    private static ScreenRecorder screenRecorder;
    private static  Path ffmpegPath;
    private static Process process;
    public static String nameVideo = "default_video";  // Ensures nameVideo is never null
    private static String nameVideoAvi = Path.of("videos", nameVideo + ".avi").toString();
    private static String nameVideoMp4 = Path.of("videos", nameVideo + ".mp4").toString();
    public static String os = System.getProperty("os.name").toLowerCase();



    public static void startRecording(String nameVideo) throws Exception {
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

    public static void stopRecording(boolean hasDeleteAndConvet) throws Exception {
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

