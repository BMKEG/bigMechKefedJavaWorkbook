package _08_semanticWeb;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.SinkTripleOutput;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Script permitting a large RDF file to be partially read to discern
 * the types of classes defined and the predicates used to describe 
 * data.
 * 
 * @author Gully
 */
public class S08_ExamineTypesAndPredicates {

	private static Model M = ModelFactory.createDefaultModel();

	public static class Options {

		@Option(name = "-inFile", usage = "Input =file", required = true, metaVar = "INPUT")
		public File inFile;

		@Option(name = "-outFile", usage = "Output file", required = true, metaVar = "OUTPUT")
		public File outFile;

	}

	private static Logger logger = Logger.getLogger(S08_ExamineTypesAndPredicates.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);

		try {

			parser.parseArgument(args);

			if (!options.outFile.exists())
				options.outFile.delete();

			FileOutputStream out = new FileOutputStream(options.outFile);

			// This is the heart of N-triples printing ... output is heavily buffered
		    // so the FilterSinkRDF called flush at the end of parsing.
		    Sink<Triple> output = new SinkTripleOutput( out, null, 
		    		SyntaxLabels.createNodeToLabel());
		    
		    
		    StreamRDF filtered = new FilterCountRDF(output, 
		    		M.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 
		    		);
		    
		    // Call the parsing process. 
		    RDFDataMgr.parse(filtered, options.inFile.getPath()) ;
			
			out.close();

		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.print("Arguments: ");
			parser.printSingleLineUsage(System.err);
			System.err.println("\n\n Options: \n");
			parser.printUsage(System.err);
			System.exit(-1);

		} catch (Exception e2) {

			e2.printStackTrace();

		}

	}

	static class FilterCountRDF extends StreamRDFBase {
		
		// Where to send the filtered triples.
		private final Sink<Triple> dest;

		// Where to send the filtered triples.
		private Set<Node> objectNodes = new HashSet<Node>();
		private Set<Node> predicates = new HashSet<Node>();
		private int newCount = 0;
		
		FilterCountRDF(Sink<Triple> dest, Property... properties) {
			this.dest = dest;
		}

		@Override
		public void triple(Triple triple) {
			if (triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				if( this.objectNodes.contains(triple.getObject()) ) {
					newCount++;
				} else {
					this.objectNodes.add(triple.getObject());
					newCount = 0;
				}
			}
			if( this.predicates.contains(triple.getPredicate()) ) {
				newCount++;
			} else {
				this.predicates.add(triple.getPredicate());
				newCount = 0;
			}

			if( newCount > 100000 ) {
				
				System.out.println( "TYPES:" );
				for(Node n : this.objectNodes) {
					System.out.println( "	" + n.getURI() );
				}
				System.out.println( "PREDICATES:" );
				for(Node n : this.predicates) {
					System.out.println( "	" + n.getURI() );
				}
				System.exit(0);
			}
				
		}
		
		@Override
		public void finish() {
			// Output may be buffered.
			dest.flush();
		}
	}

}
