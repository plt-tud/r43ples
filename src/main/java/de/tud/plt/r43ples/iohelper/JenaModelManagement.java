package de.tud.plt.r43ples.iohelper;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import de.tud.plt.r43ples.management.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map.Entry;

public class JenaModelManagement {
	
	/**
	 * Read turtle file to jena model.
	 * 
	 * @param path the file path to the turtle file
	 * @return the jena model
	 */
	public static Model readTurtleFileToJenaModel(String path) {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = ClassLoader.getSystemResourceAsStream(path);
		model.read(is, null, "TURTLE");
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	
	
	/**
	 * Read RDF string to jena model.
	 * 
	 * @param triples the triples in RDF serialization
	 * @param format Jena format of triples
	 * @return the model
	 */
	public static Model readStringToJenaModel(String triples, String format) {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = new ByteArrayInputStream(triples.getBytes(StandardCharsets.UTF_8));
		model.read(is, null, format);
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<Entry<String, String>> it = Config.user_defined_prefixes.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> ns = it.next();
			model.setNsPrefix(ns.getKey(), ns.getValue());
		}
		
		return model;
	}
	
	
	/**
	 * Read N-Triple string to jena model.
	 * 
	 * @param triples the triples in N-Triple serialization
	 * @return the model
	 */
	public static Model readNTripleStringToJenaModel(String triples) {
        return readStringToJenaModel(triples, "N-TRIPLE");
    }

    /**
     * Read Turtle string to jena model.
     *
     * @param triples the triples in turtle serialization
     * @return the model
     */
    public static Model readTurtleStringToJenaModel(String triples) {
        return readStringToJenaModel(triples, "TURTLE");
    }


	/**
	 * Converts a jena model to the specified serialization. 
	 * 
	 * @param model the jena model
	 * @return the string which contains the N-Triples
	 */
	public static String convertJenaModelToString(Model model, String format) {
			
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		model.write(os, format);
		
		try {
			return new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
        }
    }

    /**
     * Converts a jena model to N-Triple serialization.
     *
     * @param model the jena model
	 * @return the string which contains the N-Triples
	 */
    public static String convertJenaModelToNTriple(Model model) {
        return convertJenaModelToString(model, "N-TRIPLES");
    }

    /**
     * Converts a jena model to Turtle serialization.
     *
     * @param model the jena model
     * @return the string which contains the Turtle serialization
     */
    public static String convertJenaModelToTurtle(Model model) {
        return convertJenaModelToString(model, "TURTLE");
    }

	/**
	 * Converts Turtle serialization to N-Triple serialization.
	 *
	 * @param triples the triples in turtle serialization
	 * @return the string which contains the N-Triples
	 */
	public static String convertTurtleToNTriple(String triples) {
		return convertJenaModelToNTriple(readTurtleStringToJenaModel(triples));
	}

}
