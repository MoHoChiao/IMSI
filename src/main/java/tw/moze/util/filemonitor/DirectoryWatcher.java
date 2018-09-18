package tw.moze.util.filemonitor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import tw.moze.util.dev.XXX;

/**
 */
public class DirectoryWatcher implements Closeable {
	private String path;
	private WatchEventHandler createhd = dummyHandler;
	private WatchEventHandler deletehd = dummyHandler;
	private WatchEventHandler modifyhd = dummyHandler;
	private volatile boolean stop = false;

	public DirectoryWatcher(String path) {
		this.path = path;
	}

	public void setCreatehd(WatchEventHandler createhd) {
		this.createhd = createhd;
	}

	public void setDeletehd(WatchEventHandler deletehd) {
		this.deletehd = deletehd;
	}

	public void setModifyhd(WatchEventHandler modifyhd) {
		this.modifyhd = modifyhd;
	}

	public void start() {
		WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(path);
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            System.out.println("Watch Service registered for dir: " + dir.toString());

            while (!stop) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    XXX.out(kind.name() + ": " + fileName);

                    if (kind == OVERFLOW) {
                        continue;
                    } else if (kind == ENTRY_CREATE) {
                    	createhd.onEvent(event);
                    } else if (kind == ENTRY_DELETE) {
                    	deletehd.onEvent(event);
                    } else if (kind == ENTRY_MODIFY) {
                    	modifyhd.onEvent(event);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                	XXX.out("Got invalid reset!!!");
                    break;
                }
            }

        } catch (IOException ex) {
            System.err.println(ex);
            ex.printStackTrace();
        } finally {
        	try {
				watcher.close();
			} catch (IOException e) {
				; // do nothing
			}
		}
    }

	@Override
	public void close() {
		this.stop = true;
	}

	public static interface WatchEventHandler {
		void onEvent(WatchEvent<?> event);
	}

	private static WatchEventHandler dummyHandler = new WatchEventHandler() {
		@Override
		public void onEvent(WatchEvent<?> event) {
			// do noting
		}
	};
}