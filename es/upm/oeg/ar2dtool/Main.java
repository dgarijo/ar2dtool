package es.upm.oeg.ar2dtool;

import java.util.logging.Level;
import java.util.logging.Logger;

import es.upm.oeg.ar2dtool.exceptions.ConfigFileNotFoundException;
import es.upm.oeg.ar2dtool.exceptions.NullTripleMember;
import es.upm.oeg.ar2dtool.exceptions.RDFInputNotValid;
import es.upm.oeg.ar2dtool.exceptions.RDFNotFound;
import es.upm.oeg.ar2dtool.utils.dot.DOTGenerator;
import es.upm.oeg.ar2dtool.utils.graphml.GraphMLGenerator;

public class Main {

	private static final int ARG_LENGTH = 8;
	public static String syntaxErrorMsg = "Syntax error. Please use the following syntax \"java -jar ar2dtool.jar -i PathToInputRdfFile -o FileToOutputFile -t OutputFileType -c PathToConfFile [-d]\"";
	private static String pathToInputFile = "";
	private static String pathToOuputFile = "";
	private static String outputFileType = ""; 
	private static String pathToConfFile = "";

	private static boolean DEBUG = false;

	// LOGGING
	private static Level logLevel = Level.INFO;
	private static final Logger log = Logger.getLogger("AR2DTOOL");
	
	//GENERATION FLAGS
	private static boolean GENERATE_GV = false;
	private static boolean GENERATE_GML = false;
	private static boolean COMPILE_GV = false;

	public static void main(String[] args) {

		parseArgs(args);

		if ((pathToInputFile.equals("")) || (outputFileType.equals(""))
				|| (pathToOuputFile.equals("")) || (pathToConfFile.equals(""))) {
			System.err.println(syntaxErrorMsg);
			return;
		}
		
		if(DEBUG)
		{
			logLevel =Level.INFO;
		}
		else
		{
			logLevel =Level.OFF;
		}
		
		log.setLevel(logLevel);
		
		log("pathToInputFile:" + pathToInputFile);
		log("pathToOuputFile:" + pathToOuputFile);
		log("outputFileType:" + outputFileType);
		log("pathToConfFile:" + pathToConfFile);
		
		
		
		
		RDF2Diagram r2d = new RDF2Diagram();
		
		try {

			//load config info
			r2d.loadConfigValues(pathToConfFile);

			//print config values 
			log(r2d.getConf().toString());
			
			//load model
			r2d.loadRdf(pathToInputFile);
			
			//apply the filters specified in config file
			r2d.applyFilters();
			log("model:\n" + r2d.printModel());
			
			if(GENERATE_GV)
			{
				log("Generating GV file...");
				
				//get the DOTGenerator with the resultant info
				DOTGenerator dg = r2d.getDOTGenerator();
				
				//apply transformations
				dg.applyTransformations();
				
				//save the DOT source to file
				dg.saveSourceToFile(pathToOuputFile+".dot");
				

				log("Generated! Path="+pathToOuputFile+".dot");
				
				
				if(COMPILE_GV)
				{
					//get source DOT code
					String src = dg.generateDOTSource();
					

					log("Compiling GV, this may take a little while...");
					//compile src code into a graph 
					dg.generateDOTDiagram(src,pathToOuputFile,outputFileType);	
					
					log("Compiled! Path="+pathToOuputFile);
				}	
			}
			
			if(GENERATE_GML)
			{
				//get the GraphMLGenerator with the resultant info
				GraphMLGenerator gg = r2d.getGraphMLGenerator();
				
				//apply transformations
				gg.applyTransformations();
				
				log("GraphML source " + gg.generateGraphMLSource());
				
				//save the GraphML source to file
				gg.saveSourceToFile(pathToOuputFile+".graphml");
			}
			
			
			
			
			
		} catch (ConfigFileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFInputNotValid e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullTripleMember e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

	private static void parseArgs(String[] args) {
		if (args.length < ARG_LENGTH) {
			System.err.println(syntaxErrorMsg);
			return;
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-i")) {
				i++;
				pathToInputFile = args[i];
			} else {

				if (args[i].equals("-o")) {
					i++;
					pathToOuputFile = args[i];
				} else {

					if (args[i].equals("-t")) {
						i++;
						outputFileType = args[i];
					} else {

						if (args[i].equals("-c")) {
							i++;
							pathToConfFile = args[i];
						} else {
							if (args[i].equals("-d")) {
								DEBUG = true;
							} else {
								if(args[i].equals("-gv"))
								{
									GENERATE_GV=true;
								}
								else
								{
									if(args[i].equals("-gml"))
									{
										GENERATE_GML=true;
									}
									else
									{
										if(args[i].equals("-GV"))
										{
											GENERATE_GV=true;
											COMPILE_GV=true;
										}
										else
										{

											System.err.println(syntaxErrorMsg);
											return;	
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static void log(String msg) {
		log.log(logLevel, msg);
	}
}
