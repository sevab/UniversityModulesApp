/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package module_database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author sevabaskin
 */
public class ModuleAppHelper {
    // TODO: split into ModuleUtils & ModuleFileHandler classes?





    static void validateModuleYearQuery(String query) throws InvalidQueryFormatException {
        String pattern = "(1|2|3|M)";
        Pattern regex = Pattern.compile(pattern);
        Matcher match = regex.matcher(query.toUpperCase());
        if (!match.matches())
            throw new InvalidQueryFormatException("Your query '" + query + "' is invalid. Make sure it's one of these: 1,2,3 or M.");
    }










	static void appendLineToFile(File file, String line) {
        // maybe should return a boolean if successful
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(line+"\n");
            bw.close();
        } catch (IOException e) {}
    }


    static void replaceFile(File oldFile, File newFile) {
        if (oldFile.delete()) {
            newFile.renameTo(oldFile);      // Rename new database file to the original name
            oldFile = newFile;    
        }
    }


	// Used by Test Suite to read a particular line in a file
    static String getCsvLine(String fileDirectory, int lineNumber) throws FileNotFoundException, IOException {
        // FIXME: make sure line's not empty
        BufferedReader reader = new BufferedReader(new FileReader(fileDirectory));
        String line;
        int i = 0;
        while ((line = reader.readLine()) != null) {
            if (i == lineNumber) {
                break;
            }
            i++;
        }
        return line;
    }

	// Used to determine the length of the database array that needs to be created
    static int linesInAFile(String fileDirectory) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileDirectory));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }



	// Used by search modules to  dynamically create arrays from strings
    static int[] convertStringToIntArray(String str) {
		String[] strArray = str.split(",");
        System.out.println("str: " + str);
		int[] intArray = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
		return intArray;
    }


    static void modifyLineInAFile(File file, int lineNumber, String action, String substituteLine) {
       File tempFile = new File("temp_" + file.getName());
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(tempFile));
            
            String line;
            int i = -1;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == lineNumber) {
                    if (action == "delete") continue;
                    if (action == "update") line = substituteLine;
                }
                bw.write(line+"\n");
            }
        } catch (Exception e) {
            return;
        } finally {
            try {
                if(br != null)
                   br.close();
            } catch (IOException e) {}

            try {
                if(bw != null)
                   bw.close();
            } catch (IOException e) {}
        }
        ModuleAppHelper.replaceFile(file, tempFile);
    }





   // To be used by the test suite to restore the database back to its original state after modification
    static void restoreDatabaseFileFromBackUp(String backupFilePath, String destinationFilePath) throws IOException {
        File backupFile = new File(backupFilePath);
        File destinationFile = new File(destinationFilePath);
        // TODO: might want to do something else
        if(!destinationFile.exists()) {
            destinationFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(backupFile).getChannel();
            destination = new FileOutputStream(destinationFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    // static void isNotEmpty(String str) {
    //     return (str.equals("")) ? false : true;
    // }
}
