package uk.co.compendiumdev.serverstart;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ChallengerAutoRuntime implements AutoCloseable {

    private static ChallengerAutoRuntime currentRuntime;
    private static ChallengerAutoConfig currentConfig;
    private static boolean configurationOverridden;

    private final ChallengerAutoConfig config;
    private final String baseUrl;
    private final Process localProcess;
    private final File logFile;
    private boolean closed;

    private ChallengerAutoRuntime(
            final ChallengerAutoConfig config,
            final String baseUrl,
            final Process localProcess,
            final File logFile) {
        this.config = config;
        this.baseUrl = baseUrl;
        this.localProcess = localProcess;
        this.logFile = logFile;
        this.closed = false;
    }

    public static synchronized ChallengerAutoRuntime current() {
        if (currentRuntime != null && configurationOverridden) {
            return currentRuntime;
        }

        ChallengerAutoConfig config = ChallengerAutoConfig.current();
        if (currentRuntime == null
                || currentConfig == null
                || !currentConfig.toString().equals(config.toString())) {
            closeCurrentRuntime();
            currentRuntime = start(config);
            currentConfig = config;
        }
        return currentRuntime;
    }

    public static synchronized ChallengerAutoConfig currentConfig() {
        if (currentConfig == null) {
            currentConfig = ChallengerAutoConfig.current();
        }
        return currentConfig;
    }

    public static synchronized void useConfiguration(final ChallengerAutoConfig config) {
        closeCurrentRuntime();
        currentRuntime = start(config);
        currentConfig = config;
        configurationOverridden = true;
    }

    public static synchronized void reset() {
        closeCurrentRuntime();
        currentConfig = null;
        configurationOverridden = false;
    }

    public static ChallengerAutoRuntime start(final ChallengerAutoConfig config) {
        if (config.startsOwnedLocalProcess()) {
            return startOwnedLocal(config);
        }

        ChallengerAutoRuntime runtime =
                new ChallengerAutoRuntime(config, config.getBaseUrl(), null, null);
        runtime.waitUntilReady();
        return runtime;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ChallengerAutoConfig getConfig() {
        return config;
    }

    public boolean ownsLocalProcess() {
        return localProcess != null;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;

        if (localProcess == null) {
            return;
        }

        requestShutdown();
        try {
            if (!localProcess.waitFor(5, TimeUnit.SECONDS)) {
                localProcess.destroy();
            }
            if (!localProcess.waitFor(5, TimeUnit.SECONDS)) {
                localProcess.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            localProcess.destroyForcibly();
        }
    }

    private static ChallengerAutoRuntime startOwnedLocal(final ChallengerAutoConfig config) {
        int port = config.isAutoPort() ? findFreePort() : config.fixedPort();
        if (!config.isAutoPort() && portIsOpen("localhost", port, 250)) {
            throw new IllegalStateException(
                    "Configured Challenger local port is already in use: "
                            + port
                            + ". Use -D"
                            + ChallengerAutoConfig.PROPERTY_LOCAL_PORT
                            + "=auto "
                            + "or target=existing to attach to it.");
        }

        deleteSinglePlayerDataIfNeeded(config);

        File targetDirectory = new File(System.getProperty("user.dir"), "target");
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create " + targetDirectory.getAbsolutePath());
        }
        File logFile = new File(targetDirectory, "challenger-auto-local-" + port + ".log");

        Process process = startProcess(config, port, logFile);
        String baseUrl = "http://localhost:" + port;
        ChallengerAutoRuntime runtime =
                new ChallengerAutoRuntime(config, baseUrl, process, logFile);
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::close));
        runtime.waitUntilReady();
        return runtime;
    }

    private static Process startProcess(
            final ChallengerAutoConfig config, final int port, final File logFile) {
        List<String> command = new ArrayList<>();
        command.add(javaBinary());
        command.add("-cp");
        command.add(testClasspath());
        command.add("uk.co.compendiumdev.challenge.ChallengeMain");
        command.addAll(config.challengeMainArgs(port));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(System.getProperty("user.dir")));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not start Challenger local process. Command: " + command, e);
        }
    }

    private void waitUntilReady() {
        long deadline = System.currentTimeMillis() + 30_000;
        String heartbeat = baseUrl + "/heartbeat";
        while (System.currentTimeMillis() < deadline) {
            if (localProcess != null && !localProcess.isAlive()) {
                throw new IllegalStateException(
                        "Challenger local process exited before readiness. Log: "
                                + logFile.getAbsolutePath());
            }
            if (httpStatus(heartbeat) == 204) {
                return;
            }
            sleep(250);
        }
        String message = "Challenger target did not become ready at " + heartbeat;
        if (logFile != null) {
            message += ". Log: " + logFile.getAbsolutePath();
        }
        throw new IllegalStateException(message);
    }

    private void requestShutdown() {
        httpStatus(baseUrl + "/shutdown");
    }

    private static int httpStatus(final String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestMethod("GET");
            return connection.getResponseCode();
        } catch (IOException e) {
            return -1;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void deleteSinglePlayerDataIfNeeded(final ChallengerAutoConfig config) {
        if (config.getLocalPlayerMode() != ChallengerAutoConfig.PlayerMode.SINGLE) {
            return;
        }
        File folder = new File(System.getProperty("user.dir"), "challengersessions");
        File dataFile = new File(folder, "rest-api-challenges-single-player.data.txt");
        if (dataFile.exists() && !dataFile.delete()) {
            throw new IllegalStateException(
                    "Could not delete single-player data file " + dataFile.getAbsolutePath());
        }
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Could not find a free local port", e);
        }
    }

    private static boolean portIsOpen(final String host, final int port, final int timeoutMillis) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMillis);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String javaBinary() {
        String executable = isWindows() ? "java.exe" : "java";
        Path java = Paths.get(System.getProperty("java.home"), "bin", executable);
        return java.toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }

    private static String testClasspath() {
        String surefireClasspath = System.getProperty("surefire.test.class.path");
        if (surefireClasspath != null && !surefireClasspath.trim().isEmpty()) {
            return surefireClasspath;
        }
        return System.getProperty("java.class.path");
    }

    private static void closeCurrentRuntime() {
        if (currentRuntime != null) {
            currentRuntime.close();
            currentRuntime = null;
        }
    }

    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for Challenger readiness", e);
        }
    }
}
