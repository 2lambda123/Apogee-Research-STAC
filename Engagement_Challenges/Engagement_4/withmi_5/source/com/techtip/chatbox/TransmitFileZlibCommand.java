package com.techtip.chatbox;

import com.techtip.control.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * Command that lets the user send a file to the person they are chatting with.
 * They can only sent files in the data/<user id>/files directory.
 * The files will be sent to the data/<other user id>/incoming directory.
 */
public class TransmitFileZlibCommand extends Command {
    private static final String COMMAND = "sendfilezlib";
    private static final String USAGE = "Usage: sendfilezlib <file number>";
    private static final String SENDING = "I sent a file.";

    private final DropBy withMi;

    public TransmitFileZlibCommand(DropBy withMi) {
        super(COMMAND,
                "Sends the specified file with ZLIB compression",
                USAGE,
                new AggregateCompleter(
                        new ArgumentCompleter(
                                new StringsCompleter(COMMAND),
                                new TransmitFileCompleter(withMi)
                        )
                )
        );

        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();

        if (argList.size() != 1) {
            executeHome(out);
        } else {
            // get the file we want to send
            int fileToTransmitNumber;

            try {
                fileToTransmitNumber = Integer.parseInt(argList.get(0).trim());
            } catch (NumberFormatException e) {
                out.println("Argument was not a valid number: " + argList.get(0));
                return;
            }

            List<File> files = withMi.fetchFiles();
            if ((fileToTransmitNumber < 0) || (fileToTransmitNumber >= files.size())) {
                executeGuide(out, fileToTransmitNumber);
                return;
            }

            File fileToTransmit = files.get(fileToTransmitNumber);

            try {
                FileTransfer sender = new FileTransfer(withMi);
                sender.transmitZlib(SENDING, fileToTransmit);
                out.println(fileToTransmit.getName() + " sent");
            } catch (Exception e) {
                out.println("Could not send file: " + e.getMessage());
            }
        }
    }

    private void executeGuide(PrintStream out, int fileToTransmitNumber) {
        out.println("Invalid file number: " + fileToTransmitNumber);
        return;
    }

    private void executeHome(PrintStream out) {
        out.println(USAGE);
        out.println("The command 'availablefiles' will show the files you may send along with their file numbers");
    }
}
