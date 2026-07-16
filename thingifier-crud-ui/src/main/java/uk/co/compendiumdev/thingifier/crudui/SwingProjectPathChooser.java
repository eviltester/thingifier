package uk.co.compendiumdev.thingifier.crudui;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

final class SwingProjectPathChooser implements ProjectPathChooser {

    @Override
    public ProjectPathSelection choose(final ProjectActionRequest request) {
        if (GraphicsEnvironment.isHeadless()) {
            return ProjectPathSelection.unavailable(
                    "Native project browsing is not available in headless mode.");
        }
        try {
            return chooseOnEventThread(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ProjectPathSelection.unavailable("Native project browsing was interrupted.");
        } catch (InvocationTargetException | RuntimeException e) {
            return ProjectPathSelection.unavailable(
                    "Native project browsing is unavailable: " + rootMessage(e));
        }
    }

    private ProjectPathSelection chooseOnEventThread(final ProjectActionRequest request)
            throws InterruptedException, InvocationTargetException {
        if (EventQueue.isDispatchThread()) {
            return showChooser(request);
        }
        AtomicReference<ProjectPathSelection> selection = new AtomicReference<>();
        EventQueue.invokeAndWait(() -> selection.set(showChooser(request)));
        return selection.get();
    }

    private ProjectPathSelection showChooser(final ProjectActionRequest request) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(request.isSave() ? "Save Project" : "Load Project");
        chooser.setFileSelectionMode(
                request.isSave()
                        ? JFileChooser.DIRECTORIES_ONLY
                        : JFileChooser.FILES_AND_DIRECTORIES);
        if (!request.path().isEmpty()) {
            chooser.setSelectedFile(new File(request.path()));
        }

        JFrame owner = foregroundOwner();
        try {
            int result =
                    request.isSave()
                            ? chooser.showSaveDialog(owner)
                            : chooser.showOpenDialog(owner);
            if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
                return ProjectPathSelection.cancelled();
            }
            return ProjectPathSelection.selected(
                    chooser.getSelectedFile().toPath().toAbsolutePath().normalize().toString());
        } finally {
            owner.dispose();
        }
    }

    private JFrame foregroundOwner() {
        JFrame owner = new JFrame("Thingifier Project Browser");
        owner.setType(Window.Type.UTILITY);
        owner.setUndecorated(true);
        owner.setAlwaysOnTop(true);
        owner.setSize(1, 1);
        owner.setLocationRelativeTo(null);
        owner.setVisible(true);
        owner.toFront();
        owner.requestFocus();
        return owner;
    }

    private String rootMessage(final Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
