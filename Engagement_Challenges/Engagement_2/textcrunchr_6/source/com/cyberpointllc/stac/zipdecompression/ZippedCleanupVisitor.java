package com.cyberpointllc.stac.zipdecompression;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.io.IOException;

public class ZippedCleanupVisitor extends SimpleFileVisitor<Path> {

    //this is the directory unzipper was asked to output to.  make sure we don't delete it!
    private Path root;

    public ZippedCleanupVisitor(String root) {
        this.root = Paths.get(root);
    }

    // delete the file
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        int i = file.toString().lastIndexOf(".");
        String ext = file.toString().substring(i);
        switch(ext) {
            case ".ZIP":
            case ".zip":
                deleteFile(file);
                break;
        }
        return FileVisitResult.CONTINUE;
    }

    private void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch (NoSuchFileException x) {
            System.err.println(x);
        } catch (DirectoryNotEmptyException x) {
            System.err.println(x);
        } catch (IOException x) {
            System.err.println(x);
        }
    }

    // Delete the directory if it's empty (and isn't the root dir)
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        // if dir is empty and isn't the top level dir, delete it
        if (dir.toFile().list().length == 0 && !dir.equals(root)) {
            deleteFile(dir);
        }
        return FileVisitResult.CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }
}
