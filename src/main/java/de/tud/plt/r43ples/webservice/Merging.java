package de.tud.plt.r43ples.webservice;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import de.tud.plt.r43ples.existentobjects.RevisionControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("merging")
public class Merging {
	
	private final static Logger logger = LogManager.getLogger(Merging.class);

	/**
	 * get merging HTML start page and input merging information
	 * */
	@GET
	public final Response getMerging() {
		logger.info("Merging - Start page");		
		
		List<String> graphList = new LinkedList<>();
		graphList.addAll(new RevisionControl().getRevisedGraphs().keySet());
		Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("merging_active", true);
		scope.put("graphList", graphList);
		
		StringWriter sw = new StringWriter();
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/merge_start_wip.mustache");
		mustache.execute(sw, scope);		
		return Response.ok().entity(sw.toString()).type(MediaType.TEXT_HTML).build();
	}

}
