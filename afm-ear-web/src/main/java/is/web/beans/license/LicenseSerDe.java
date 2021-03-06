package is.web.beans.license;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LicenseSerDe {
	
	@Inject
	private Logger logger;

	public String serialize(License license) throws IOException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonOutput = gson.toJson(license);
		//logger.info("=> serialized output: "+jsonOutput);
		return jsonOutput;
	}

	public License deserialize(String content) throws IOException
	{
		//logger.info("=> dserializining content: "+content);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		License dataHolder = gson.fromJson(content, License.class);
		
		return dataHolder;
	}
}

