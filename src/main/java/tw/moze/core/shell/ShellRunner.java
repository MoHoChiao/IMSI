package tw.moze.core.shell;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import tw.moze.util.dev.XXX;

public class ShellRunner {
    private String err;
    private String out;
    private int exitCode;
    private String[] cmd;
    private String workingDirectory;
    public static final int PROC_RUNNING = -99919;
    public static final int PROC_KILLED = -2;

    public ShellRunner(String[] cmd) {
    	this.cmd = cmd;
    	this.workingDirectory = ".";
    }

    public ShellRunner workingDirectory(String workingDirectory) {
    	this.workingDirectory = workingDirectory;
    	return this;
    }

    public void exec(OutputHandler hd) throws Exception {
    	BufferedReader stdReader = null;

		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			File wf = new File(workingDirectory);
			pb.directory(wf);
			pb.redirectErrorStream(true);

			Process proc = pb.start();
			stdReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = stdReader.readLine()) != null) {
				hd.onLine(line);
			}
//			proc.waitFor();	這個方法有時疑似會 block 住，不會返回 (至少在 linux openjdk 1.7)
//			exitCode = proc.exitValue(); 如果沒有 waitFor, 直接呼叫 exitCode, 有時會有 process hasn't exited 的錯誤
			exitCode = waitForExit(5000); // 用這種方式，若無法再讀取 process output, 隔幾秒後就離開
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			safeClose(stdReader);
		}
    }

    /**
     * 執行 process，process 結束立即返回
     * 因有時情況怪異，process output stream 已關閉，但程式並未結束，
     * 此時這個 function 超過 millis 指定的時間後，就會強制停止這個 process
     * @param hd
     * @param millis
     * @throws Exception
     */
    public void exec(OutputHandler hd, final long millis) throws Exception {
    	BufferedReader stdReader = null;

		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			File wf = new File(workingDirectory);
			pb.directory(wf);
			pb.redirectErrorStream(true);

			proc = pb.start();
			stdReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = stdReader.readLine()) != null) {
				hd.onLine(line);
			}

			long count = millis/200;
			while (true) {
				line = stdReader.readLine();
				if (line != null) {
					hd.onLine(line);
					count = millis/200;	// 每次 standout 有輸出，就重設可嚐試次數
					continue;
				}

				exitCode = checkExit(PROC_RUNNING);
				if (exitCode != PROC_RUNNING)
					break;
				else if(count-- <= 0) {
					proc.destroy();
					exitCode = PROC_KILLED;
					XXX.out("XXXXX Process [" + getCmdString(cmd) + "] execution exceeded time limit, force destroyed!");
					break;
				}
				Thread.sleep(200);
			}

//			proc.waitFor();	這個方法有時疑似會 block 住，不會返回
//			exitCode = proc.exitValue(); 如果沒有 waitFor, 直接呼叫 exitCode, 有時會有 process hasn't exited 的錯誤
			proc = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			safeClose(stdReader);
		}
    }

    // 當 stdReader.readLine() == null 之後幾秒，強制停止 process 且返回
    private int waitForExit(long millis) {
    	if (proc == null)
    		return -1;

    	long count = millis;
		try {
	    	do {
	    		// JDK 8 有 Process.isAlive(), 此專案在 jdk 環境，用這種笨方法
		    	try {
					return proc.exitValue();
				}
				catch(IllegalThreadStateException e) {
					count -= 200;
				}
				Thread.sleep(200);
	    	} while(count > 0);
	    	proc.destroy();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
    	return -2;
    }

    private int checkExit(int stillRunningValue) {
    	if (proc == null)
    		return 0;
    	try {
			return proc.exitValue();
		}
		catch(IllegalThreadStateException e) {
			return stillRunningValue;
		}
    }

    private volatile Process proc;


	public void exe2Console() throws Exception {
		InputStream stdReader = null;
		InputStream errReader = null;

		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			File wf = new File(workingDirectory);
			pb.directory(wf);

			Process proc = pb.start();
			stdReader = new BufferedInputStream(proc.getInputStream());
			errReader = new BufferedInputStream(proc.getErrorStream());

			Pipe.pipe(stdReader, System.out);
			Pipe.pipe(errReader, System.err);

			proc.waitFor();
			exitCode = proc.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			safeClose(stdReader);
			safeClose(errReader);
		}
	}


	public String exe2String() throws Exception {
		BufferedReader stdReader = null;
		BufferedReader errReader = null;

		BufferedWriter stdWriter = null;
		BufferedWriter errWriter = null;

//		System.out.println("cmd = " + getCmdString(command) );
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			File wf = new File(workingDirectory);
			pb.directory(wf);

			Process proc = pb.start();
			stdReader = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF8"));
			errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream(), "UTF8"));

			// read stdout from the command
			StringWriter sw = new StringWriter();
			stdWriter = new BufferedWriter(sw);

			StringWriter ew = new StringWriter();
			errWriter = new BufferedWriter(ew);

			Pipe.pipe(stdReader, stdWriter);
			Pipe.pipe(errReader, errWriter);

			stdWriter.flush();
			errWriter.flush();

			proc.waitFor();
			err = ew.toString();
			out = sw.getBuffer().toString();
			exitCode = proc.exitValue();
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			safeClose(stdReader);
			safeClose(errReader);
			safeClose(stdWriter);
			safeClose(errWriter);
		}
	}

    public int getExitCode() {
    	return exitCode;
    }

    public int exe2File(String file) {
        BufferedReader stdInput = null;
        BufferedReader stdError = null;
        BufferedWriter fileWriter = null;

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            File wf = new File(workingDirectory);
            pb.directory(wf);

            Process proc = pb.start();
            stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF8"));
            stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream(), "UTF8"));

            // read stdout from the command
            fileWriter = new BufferedWriter(new FileWriter(file));
            Pipe.pipe(stdInput, fileWriter);
            fileWriter.flush();
            // read any errors from the attempted command
            StringBuffer sb = new StringBuffer();
            String newLine = System.getProperty("line.separator");
            String s;

            while ((s = stdError.readLine()) != null) {
                sb.append(s);
                sb.append(newLine);
            }

            err = sb.toString();
            exitCode = proc.exitValue();
            return exitCode;
        } catch (IOException e) {
            throw new RuntimeException("Problem occurred:", e);
        } finally {
            safeClose(stdInput);
            safeClose(stdError);
            safeClose(fileWriter);
        }
    }

    public boolean hasErrOut() {
    	return err.length() > 0;
    }

    public String getError() {
        return err;
    }

    public String getOutput() {
    	return out;
    }

    public String getCmdString() {
    	return getCmdString(cmd);
    }

    private void safeClose(Closeable res) {
        try {
            if(res != null)
                res.close();
        } catch(Exception ex) {
            ; // do nothing
        }
    }

    private static String getCmdString(String[] cmd) {
    	StringBuilder sb = new StringBuilder();
    	for (String s: cmd) {
    		sb.append(s).append(" ");
    	}
    	return sb.toString();
    }

    public static interface OutputHandler {
    	void onLine(String line);
    }
}
