/** TODO: currently the use of other methods requires calling loadCSV file; so, either
*         a) throw an error message if loadCSVFile() isn't called first or 
*         b) combine ModuleApp() with loadCSVFile()
*
*   TODO: when doing threading, warn user if he tries to quit the app while a tread hasn't finished writing to a file
*   TODO: decide on uniform variable names (e.g. newModuleTitle vs newTitle)
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
class ModuleApp {
    // make private?:
    // String[][] database;
    // TODO: get rid of elementsInDatabase
    int elementsInDatabase;    
    Pattern csvRegex;
	Pattern moduleYearRegex;
    File databaseFile;
    // Substitute db & Db for db?
    private Module[] db;

    ModuleApp() {
        // FIXME: Do we need this variable considering that databaseArray has preceise length
    	this.elementsInDatabase = 0;

    	// Move to the specific methods if not used in multiple places:
    	this.csvRegex = Pattern.compile("\"(.*?)\"");
		this.moduleYearRegex = Pattern.compile("(?<=^...)(1|2|3|M|m)");
    }

    
    // Accessor methods
    Module[] getDb() { return this.db; }
    

    // TODO: normalize all queries by upcasing; normalize results as well?
    // TODO: Expand database if reached the limit (keep on adding until an exception is thrown?)
    void loadCSVFile(String databaseFileDirectory) throws FileNotFoundException, IOException {
        
        
        this.db = new Module[linesInAFile(databaseFileDirectory)];
        this.databaseFile = new File(databaseFileDirectory);
        BufferedReader reader = new BufferedReader(new FileReader(databaseFile));
        String line;
		while ((line = reader.readLine()) != null) {
			// #OPTIMIZE
            Matcher csvMatcher = csvRegex.matcher(line);
            csvMatcher.find();
            String newCode = csvMatcher.group(1);
            csvMatcher.find();
            String newTitle = csvMatcher.group(1);
            csvMatcher.find();
            String newLeaderName = csvMatcher.group(1);
            csvMatcher.find();
            String newLeaderEmail = csvMatcher.group(1);
            this.db[elementsInDatabase] = new Module(newCode, newTitle, newLeaderName, newLeaderEmail);

			elementsInDatabase++;
		}
    }


    int findModuleRowByCode(String moduleCodeQuery) {
    	int resultRow = -1;
    	for (int i=0; i < this.db.length ; i++) {
    		if (getModule(i).getCode().equals(moduleCodeQuery)) {
    			resultRow = i;
    			break;
    		}
    	}
    	return resultRow;
    }

    // JavaDoc: Describe how strings are used to generte dynamyc-length arrays
    
    int[] findModuleRowsByYear(String moduleYearQuery) {
    	// if (moduleYearQuery.length() == 0) return new int[]{-1};

    	String resultRows = ""; // if nothing's found, assign an empty array.
    	for (int i=0; i < this.db.length ; i++) {
    		Matcher moduleYearMatcher = moduleYearRegex.matcher(getModule(i).getCode());
    		moduleYearMatcher.find();
		   	String candidateResult = moduleYearMatcher.group();
    		if (candidateResult.equals(moduleYearQuery))
    			resultRows = resultRows + i + ",";
    	}
    	return convertStringToIntArray(resultRows);
    }

    int[] findModuleRowsByLeaderName(String moduleLeaderNameQuery) {
    	// if (moduleLeaderNameQuery.length() == 0) return new int[]{-1};

    	String resultRows = ""; // if nothing's found, assign an empty array.
    	Pattern moduleLeaderNameRegex = Pattern.compile(moduleLeaderNameQuery);
    	for (int i=0; i < this.db.length ; i++) {
    		Matcher moduleLeaderNameMatcher = moduleLeaderNameRegex.matcher(getModule(i).getLeaderName());
    		if (moduleLeaderNameMatcher.lookingAt())
    			resultRows = resultRows + i + ",";
    	}
    	return convertStringToIntArray(resultRows);
    }

    int[] findModuleRowsByLeaderEmail(String moduleLeaderEmailQuery) {
    	// if (moduleLeaderEmailQuery.length() == 0) return new int[]{-1};

    	String resultRows = ""; // if nothing's found, assign an empty array.
    	Pattern moduleLeaderEmailRegex = Pattern.compile(moduleLeaderEmailQuery);
    	for (int i=0; i < this.db.length ; i++) {
    		Matcher moduleLeaderEmailMatcher = moduleLeaderEmailRegex.matcher(getModule(i).getLeaderEmail());
    		if (moduleLeaderEmailMatcher.lookingAt())
    			resultRows = resultRows + i + ",";
    	}
    	return convertStringToIntArray(resultRows);
    }




    Module getModule(int moduleRow) { return this.db[moduleRow]; }

    String getCsvLine(String fileDirectory, int lineNumber) throws FileNotFoundException, IOException {
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





    // String[] getModuleInfoAsACsvLine(int moduleRow) {
    //     return new String;
    //     String = "\""+database[moduleRow][0]+"\",\""+database[moduleRow][1]+"\",\""+database[moduleRow][2]+"\",\""+database[moduleRow][3]+"\"";
    // }

    void updateModule(int moduleRow, String newModuleCode, String newModuleTitle, String newModuleLeaderName, String newModuleLeaderEmail) {
        // TODO: Validate first (format + if empty)
        // TODO: update, reload database
        // TODO: extract into to private methods updateDatabaseArray & updateDatabaseCSV, then call both in here after validating values

        String databaseFileName = this.databaseFile.getName();
        String tempDatabaseFileName = "temp_" + databaseFileName;
        // System.out.println("original file name: " + databaseFileName + " temp: " + tempDatabaseFileName);


        // update the database array
        // FIXME: shall we perform this after updating CSV file in case of errors? But errors should be caught.
        // Extract into Validator.isNotEmpty(String str)
        if (!newModuleCode.equals(""))
            getModule(moduleRow).setCode(newModuleCode);
        if (!newModuleTitle.equals(""))
            getModule(moduleRow).setTitle(newModuleTitle);
        if (!newModuleLeaderName.equals(""))
            getModule(moduleRow).setLeaderName(newModuleLeaderName);
        if (!newModuleLeaderEmail.equals(""))
            getModule(moduleRow).setLeaderEmail(newModuleLeaderEmail);

        // update the CSV file
        // TODO: Move to thread
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(this.databaseFile));
            bw = new BufferedWriter(new FileWriter(tempDatabaseFileName));
            String line;
            int i = 0;

            while ((line = br.readLine()) != null) {
                if (i == moduleRow) { // TODO: Extract into a separate method:
                    // System.out.println("before: " + line);
                    line = "\""+newModuleCode+"\",\""+ newModuleTitle +"\",\""+ newModuleLeaderName +"\",\""+newModuleLeaderEmail+"\"";
                    // System.out.println("after: " + line);
                }
                bw.write(line+"\n");
                i++;
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

        // TODO: Extract into a different function. Used in deleteModule() && updateModule()
        // Delete old database file
        if (this.databaseFile.delete()) {
            // Reasign with the new database file saved as a tempFile
            File newFile = new File(tempDatabaseFileName);
            // Rename new database file to the original name
            newFile.renameTo(this.databaseFile);
            this.databaseFile = newFile;    
        }
    }


    void deleteModule(int moduleRow) {
        // delete from the database Array first
        // DELETME
        String[][] newDatabase = new String[this.db.length-1][4];
        Module[] newDB = new Module[this.db.length-1];
        int j = 0;
        for (int i=0; i<newDatabase.length; i++) {
            if (i == moduleRow) j++;
            newDB[i] = this.db[j]; 
            j++;
        }
        this.db = newDB;

       // delete from CSV
        String databaseFileName = this.databaseFile.getName();
        String tempDatabaseFileName = "temp_" + databaseFileName;
       // TODO: Move to thread
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(this.databaseFile));
            bw = new BufferedWriter(new FileWriter(tempDatabaseFileName));
            
            String line;
            int i = -1;
            while ((line = br.readLine()) != null) {
                i++;
                if (i == moduleRow) continue;
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

        // TODO: Extract into a different function. Used in deleteModule() && updateModule()
        // Delete old database file
        if (this.databaseFile.delete()) {
            // Reasign with the new database file saved as a tempFile
            File newFile = new File(tempDatabaseFileName);
            // Rename new database file to the original name
            newFile.renameTo(this.databaseFile);
            this.databaseFile = newFile;    
        }
    }



    void createModule(String newModuleCode, String newModuleTitle, String newModuleLeaderName, String newModuleLeaderEmail) {
        // TODO: Validate (format + nonEmptyness) & Check for duplicates

        // add to databaseArray:
        // DELETEME
        Module[] newDB = new Module[this.db.length+1];
        // Copy old database into the new
        for (int i=0; i<this.db.length; i++) {
            newDB[i] = this.db[i];
        }
        // Add new module to the new database
        newDB[newDB.length-1] = new Module(newModuleCode, newModuleTitle, newModuleLeaderName, newModuleLeaderEmail);
        // reasign new database to the global database
        this.db = newDB;

        // append line to the databaseCSVfile
        // OPTIMIZE: extraction opportunity into appendLine(File file, String line) method
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.databaseFile, true));
            String line = "\""+newModuleCode+"\",\""+ newModuleTitle +"\",\""+newModuleLeaderName+"\",\""+newModuleLeaderEmail+"\"";
            bw.write(line+"\n");
            bw.close();
        } catch (IOException e) {}

    }



    // Helpers
    // private boolean notDuplicate(String moduleCode) {}

    private int[] convertStringToIntArray(String str) {
		String[] strArray = str.split(",");
		int[] intArray = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++)
			intArray[i] = Integer.parseInt(strArray[i]);		
		return intArray;
    }
    private int linesInAFile(String fileDirectory) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileDirectory));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
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
    

}
