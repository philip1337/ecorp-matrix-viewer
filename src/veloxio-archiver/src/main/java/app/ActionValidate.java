package app;

import veloxio.ArchiveFile;
import veloxio.Provider;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ActionValidate {
    /**
     * Path to the xml file
     */
    private String xmlPath;

    /**
     * Path to the archive (output)
     */
    private String packPath;

    /**
     * Path to the file (that's in the archive)
     */
    private String filePath;

    /**
     * Base path
     */
    private String basePath;

    /**
     * Constructor
     * @param pPackPath to the pack archive
     */
    public ActionValidate(String pFile, String pPackPath) {
        this.basePath = basePath;
        this.filePath = pFile;
        this.packPath = pPackPath;
    }

    /**
     * Run (action entry)
     */
    public void Run() {
        Provider p = new Provider();
        try {
            if (!p.RegisterArchive(packPath))
                System.out.printf("Failed to register archive: %s\n", packPath);
        } catch (FileNotFoundException e) {
            System.out.printf("[Error] Failed to register archive: %s with error: %s \n", packPath, e.getMessage());
            return;
        }

        try {
            ArchiveFile file = p.Get(filePath);
            byte[] buffer = file.Get();

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(buffer);
            }
        } catch (IOException e) {
            System.out.printf("[Error] Failed to get file from archive archive: %s with error: %s \n",
                              packPath, e.getMessage());
        }
    }
}
